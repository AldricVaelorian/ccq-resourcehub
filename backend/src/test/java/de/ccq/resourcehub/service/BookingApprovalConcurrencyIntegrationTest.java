package de.ccq.resourcehub.service;

import static org.assertj.core.api.Assertions.assertThat;

import de.ccq.resourcehub.entity.Booking;
import de.ccq.resourcehub.entity.BookingStatus;
import de.ccq.resourcehub.repository.BookingRepository;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers
class BookingApprovalConcurrencyIntegrationTest {

    @Container
    private static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine");

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .load()
                .migrate();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private BookingApprovalService sut;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long managerId;
    private Long firstBookingId;
    private Long secondBookingId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("truncate table bookings, resources, users restart identity cascade");
        jdbcTemplate.execute("""
                create or replace function delay_booking_approval() returns trigger as $$
                begin
                    perform pg_sleep(0.5);
                    return new;
                end;
                $$ language plpgsql
                """);
        jdbcTemplate.execute("""
                create or replace trigger delay_booking_approval
                before update of status on bookings
                for each row when (new.status = 'APPROVED')
                execute function delay_booking_approval()
                """);
        managerId = insertUser("manager", "manager@example.com", "MANAGER");
        var requesterId = insertUser("requester", "requester@example.com", "USER");
        var resourceId = jdbcTemplate.queryForObject(
                "insert into resources (name) values ('Concurrent approval room') returning id", Long.class);
        firstBookingId = insertBooking(resourceId, requesterId, "2026-08-12", "2026-08-14");
        secondBookingId = insertBooking(resourceId, requesterId, "2026-08-13", "2026-08-15");
    }

    @Test
    void approveBooking_allowsOnlyOneApprovalWhenOverlappingRequestsRunConcurrently() throws Exception {
        // arrange
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> approveAfterSignal(firstBookingId, start));
            var second = executor.submit(() -> approveAfterSignal(secondBookingId, start));

            // act
            start.countDown();
            var outcomes = java.util.List.of(first.get(), second.get());

            // assert
            assertThat(outcomes).containsExactlyInAnyOrder("APPROVED", "CONFLICT");
        }
        assertThat(bookingRepository.findAll())
                .extracting(Booking::getStatus)
                .containsExactlyInAnyOrder(BookingStatus.APPROVED, BookingStatus.PENDING);
    }

    @Test
    void approveBooking_approvesBothRequestsWhenDifferentResourcesRunConcurrently() throws Exception {
        // arrange
        var otherResourceId = jdbcTemplate.queryForObject(
                "insert into resources (name) values ('Other approval room') returning id", Long.class);
        var requesterId = jdbcTemplate.queryForObject(
                "select user_id from bookings where id = ?", Long.class, firstBookingId);
        var otherBookingId = insertBooking(otherResourceId, requesterId, "2026-08-12", "2026-08-14");
        var start = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> approveAfterSignal(firstBookingId, start));
            var second = executor.submit(() -> approveAfterSignal(otherBookingId, start));

            // act
            start.countDown();
            var outcomes = java.util.List.of(first.get(), second.get());

            // assert
            assertThat(outcomes).containsExactly("APPROVED", "APPROVED");
        }
        assertThat(bookingRepository.findAllById(java.util.List.of(firstBookingId, otherBookingId)))
                .extracting(Booking::getStatus)
                .containsOnly(BookingStatus.APPROVED);
    }

    private String approveAfterSignal(Long bookingId, CountDownLatch start) {
        try {
            start.await();
            sut.approveBooking(bookingId, managerId);
            return "APPROVED";
        } catch (de.ccq.resourcehub.exception.BookingApprovalException exception) {
            return "CONFLICT";
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

    private Long insertUser(String username, String email, String role) {
        return jdbcTemplate.queryForObject(
                "insert into users (username, email, role, is_active) values (?, ?, ?, true) returning id",
                Long.class, username, email, role);
    }

    private Long insertBooking(Long resourceId, Long requesterId, String startDate, String endDate) {
        return jdbcTemplate.queryForObject("""
                insert into bookings (resource_id, user_id, start_date, end_date, created_at, status)
                values (?, ?, ?::date, ?::date, ?::timestamptz, 'PENDING') returning id
                """, Long.class, resourceId, requesterId, startDate, endDate, "2026-08-11T10:00:00Z");
    }
}

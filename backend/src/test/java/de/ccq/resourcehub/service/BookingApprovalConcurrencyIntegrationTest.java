package de.ccq.resourcehub.service;

import static org.assertj.core.api.Assertions.assertThat;

import de.ccq.resourcehub.entity.Booking;
import de.ccq.resourcehub.entity.BookingStatus;
import de.ccq.resourcehub.entity.Resource;
import de.ccq.resourcehub.entity.User;
import de.ccq.resourcehub.repository.BookingRepository;
import de.ccq.resourcehub.repository.ResourceRepository;
import de.ccq.resourcehub.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private BookingApprovalService sut;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private UserRepository userRepository;

    private Long managerId;
    private Long firstBookingId;
    private Long secondBookingId;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        resourceRepository.deleteAll();
        userRepository.deleteAll();

        var manager = user("manager", "manager@example.com", "MANAGER");
        managerId = userRepository.save(manager).getId();
        var requester = userRepository.save(user("requester", "requester@example.com", "USER"));

        var resource = new Resource();
        resource.setName("Concurrent approval room");
        resource = resourceRepository.save(resource);

        firstBookingId = bookingRepository.save(booking(resource, requester, LocalDate.of(2026, 8, 12),
                LocalDate.of(2026, 8, 14))).getId();
        secondBookingId = bookingRepository.save(booking(resource, requester, LocalDate.of(2026, 8, 13),
                LocalDate.of(2026, 8, 15))).getId();
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

    private User user(String username, String email, String role) {
        var user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setRole(role);
        user.setActive(true);
        return user;
    }

    private Booking booking(Resource resource, User requester, LocalDate startDate, LocalDate endDate) {
        var booking = new Booking();
        booking.setResource(resource);
        booking.setUser(requester);
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        booking.setCreatedAt(Instant.parse("2026-08-11T10:00:00Z"));
        booking.setStatus(BookingStatus.PENDING);
        return booking;
    }
}

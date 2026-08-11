package de.ccq.resourcehub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.ccq.resourcehub.dto.AdminBookingOverviewResponse;
import de.ccq.resourcehub.entity.Booking;
import de.ccq.resourcehub.entity.BookingStatus;
import de.ccq.resourcehub.entity.Resource;
import de.ccq.resourcehub.entity.User;
import de.ccq.resourcehub.exception.AdminBookingOverviewException;
import de.ccq.resourcehub.repository.BookingRepository;
import de.ccq.resourcehub.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminBookingOverviewService")
class AdminBookingOverviewServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    private AdminBookingOverviewService sut;

    @BeforeEach
    void setUp() {
        sut = new AdminBookingOverviewService(bookingRepository, userRepository);
    }

    @Nested
    @DisplayName("findAllBookings")
    class findAllBookings {

        @Test
        @DisplayName("returns all bookings when admin exists and is active with ADMIN role")
        void returnsAllBookingsWhenAdminIsValid() {
            // Given
            Long adminId = 1L;
            User admin = new User();
            admin.setId(adminId);
            admin.setActive(true);
            admin.setRole("ADMIN");

            Booking booking1 = createBooking(1L, 10L, 20L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));
            booking1.setCreatedAt(Instant.parse("2026-01-01T10:00:00Z"));
            
            Booking booking2 = createBooking(2L, 11L, 21L, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 15));
            booking2.setCreatedAt(Instant.parse("2026-01-10T10:00:00Z"));

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
            when(bookingRepository.findAllByOrderByCreatedAtDescIdDesc())
                    .thenReturn(List.of(booking2, booking1));

            // When
            List<AdminBookingOverviewResponse> result = sut.findAllBookings(adminId);

            // Then
            assertThat(result).hasSize(2);
            
            // Check first booking (newest)
            AdminBookingOverviewResponse first = result.get(0);
            assertThat(first.id()).isEqualTo(2L);
            assertThat(first.resourceId()).isEqualTo(11L);
            assertThat(first.userId()).isEqualTo(21L);
            assertThat(first.startDate()).isEqualTo(LocalDate.of(2026, 1, 10));
            assertThat(first.endDate()).isEqualTo(LocalDate.of(2026, 1, 15));
            assertThat(first.createdAt()).isEqualTo(Instant.parse("2026-01-10T10:00:00Z"));

            // Check second booking
            AdminBookingOverviewResponse second = result.get(1);
            assertThat(second.id()).isEqualTo(1L);
            assertThat(second.resourceId()).isEqualTo(10L);
            assertThat(second.userId()).isEqualTo(20L);
            assertThat(second.startDate()).isEqualTo(LocalDate.of(2026, 1, 1));
            assertThat(second.endDate()).isEqualTo(LocalDate.of(2026, 1, 5));
            assertThat(second.createdAt()).isEqualTo(Instant.parse("2026-01-01T10:00:00Z"));

            verify(userRepository).findById(adminId);
            verify(bookingRepository).findAllByOrderByCreatedAtDescIdDesc();
        }

        @Test
        @DisplayName("returns empty list when admin exists but no bookings exist")
        void returnsEmptyListWhenNoBookings() {
            // Given
            Long adminId = 1L;
            User admin = new User();
            admin.setId(adminId);
            admin.setActive(true);
            admin.setRole("ADMIN");

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
            when(bookingRepository.findAllByOrderByCreatedAtDescIdDesc()).thenReturn(List.of());

            // When
            List<AdminBookingOverviewResponse> result = sut.findAllBookings(adminId);

            // Then
            assertThat(result).isEmpty();

            verify(userRepository).findById(adminId);
            verify(bookingRepository).findAllByOrderByCreatedAtDescIdDesc();
        }

        @Test
        @DisplayName("throws NOT_FOUND exception when admin does not exist")
        void throwsNotFoundWhenAdminDoesNotExist() {
            // Given
            Long adminId = 999L;
            when(userRepository.findById(adminId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> sut.findAllBookings(adminId))
                    .isInstanceOf(AdminBookingOverviewException.class)
                    .hasMessage("Administrator not found with ID: 999")
                    .extracting(ex -> ((AdminBookingOverviewException) ex).getReason())
                    .isEqualTo(AdminBookingOverviewException.Reason.NOT_FOUND);

            verifyNoInteractions(bookingRepository);
        }

        @Test
        @DisplayName("throws FORBIDDEN exception when admin is inactive")
        void throwsForbiddenWhenAdminIsInactive() {
            // Given
            Long adminId = 1L;
            User admin = new User();
            admin.setId(adminId);
            admin.setActive(false);
            admin.setRole("ADMIN");

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

            // When/Then
            assertThatThrownBy(() -> sut.findAllBookings(adminId))
                    .isInstanceOf(AdminBookingOverviewException.class)
                    .hasMessage("User 1 may not view all bookings")
                    .extracting(ex -> ((AdminBookingOverviewException) ex).getReason())
                    .isEqualTo(AdminBookingOverviewException.Reason.FORBIDDEN);

            verifyNoInteractions(bookingRepository);
        }

        @Test
        @DisplayName("throws FORBIDDEN exception when admin does not have ADMIN role")
        void throwsForbiddenWhenAdminIsNotAdminRole() {
            // Given
            Long adminId = 1L;
            User admin = new User();
            admin.setId(adminId);
            admin.setActive(true);
            admin.setRole("USER");

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

            // When/Then
            assertThatThrownBy(() -> sut.findAllBookings(adminId))
                    .isInstanceOf(AdminBookingOverviewException.class)
                    .hasMessage("User 1 may not view all bookings")
                    .extracting(ex -> ((AdminBookingOverviewException) ex).getReason())
                    .isEqualTo(AdminBookingOverviewException.Reason.FORBIDDEN);

            verifyNoInteractions(bookingRepository);
        }

        @Test
        @DisplayName("returns correct mapping with null timestamps when lifecycle state does not use them")
        void returnsNullTimestampsForPendingBooking() {
            // Given
            Long adminId = 1L;
            User admin = new User();
            admin.setId(adminId);
            admin.setActive(true);
            admin.setRole("ADMIN");

            Booking booking = createBooking(1L, 10L, 20L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));
            booking.setCreatedAt(Instant.parse("2026-01-01T10:00:00Z"));
            // approvedAt, rejectedAt, rejectionReason remain null

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
            when(bookingRepository.findAllByOrderByCreatedAtDescIdDesc()).thenReturn(List.of(booking));

            // When
            List<AdminBookingOverviewResponse> result = sut.findAllBookings(adminId);

            // Then
            assertThat(result).hasSize(1);
            AdminBookingOverviewResponse response = result.get(0);
            assertThat(response.approvedAt()).isNull();
            assertThat(response.rejectedAt()).isNull();
            assertThat(response.rejectionReason()).isNull();
        }

        @Test
        @DisplayName("returns correct mapping with timestamps when booking is approved")
        void returnsTimestampsForApprovedBooking() {
            // Given
            Long adminId = 1L;
            User admin = new User();
            admin.setId(adminId);
            admin.setActive(true);
            admin.setRole("ADMIN");

            Booking booking = createBooking(1L, 10L, 20L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));
            booking.setStatus(BookingStatus.APPROVED);
            booking.setCreatedAt(Instant.parse("2026-01-01T10:00:00Z"));
            booking.setApprovedAt(Instant.parse("2026-01-02T10:00:00Z"));

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
            when(bookingRepository.findAllByOrderByCreatedAtDescIdDesc()).thenReturn(List.of(booking));

            // When
            List<AdminBookingOverviewResponse> result = sut.findAllBookings(adminId);

            // Then
            assertThat(result).hasSize(1);
            AdminBookingOverviewResponse response = result.get(0);
            assertThat(response.approvedAt()).isEqualTo(Instant.parse("2026-01-02T10:00:00Z"));
            assertThat(response.rejectedAt()).isNull();
            assertThat(response.rejectionReason()).isNull();
        }

        @Test
        @DisplayName("returns correct mapping with timestamps when booking is rejected")
        void returnsTimestampsForRejectedBooking() {
            // Given
            Long adminId = 1L;
            User admin = new User();
            admin.setId(adminId);
            admin.setActive(true);
            admin.setRole("ADMIN");

            Booking booking = createBooking(1L, 10L, 20L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));
            booking.setStatus(BookingStatus.REJECTED);
            booking.setCreatedAt(Instant.parse("2026-01-01T10:00:00Z"));
            booking.setApprovedAt(Instant.parse("2026-01-02T10:00:00Z"));
            booking.setRejectedAt(Instant.parse("2026-01-03T10:00:00Z"));
            booking.setRejectionReason("Resource unavailable");

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
            when(bookingRepository.findAllByOrderByCreatedAtDescIdDesc()).thenReturn(List.of(booking));

            // When
            List<AdminBookingOverviewResponse> result = sut.findAllBookings(adminId);

            // Then
            assertThat(result).hasSize(1);
            AdminBookingOverviewResponse response = result.get(0);
            assertThat(response.approvedAt()).isEqualTo(Instant.parse("2026-01-02T10:00:00Z"));
            assertThat(response.rejectedAt()).isEqualTo(Instant.parse("2026-01-03T10:00:00Z"));
            assertThat(response.rejectionReason()).isEqualTo("Resource unavailable");
        }

        @Test
        @DisplayName("orders bookings by createdAt descending, then by id descending for tie-break")
        void ordersBookingsByCreatedAtDescThenIdDesc() {
            // Given
            Long adminId = 1L;
            User admin = new User();
            admin.setId(adminId);
            admin.setActive(true);
            admin.setRole("ADMIN");

            // Create bookings with same createdAt timestamp but different IDs
            Booking booking1 = createBooking(1L, 10L, 20L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));
            booking1.setCreatedAt(Instant.parse("2026-01-01T10:00:00Z"));

            Booking booking2 = createBooking(2L, 11L, 21L, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 15));
            booking2.setCreatedAt(Instant.parse("2026-01-10T10:00:00Z"));

            Booking booking3 = createBooking(3L, 12L, 22L, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 15));
            booking3.setCreatedAt(Instant.parse("2026-01-10T10:00:00Z")); // Same as booking2

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
            // Repository should return in order: booking3 (id=3), booking2 (id=2), booking1 (id=1)
            when(bookingRepository.findAllByOrderByCreatedAtDescIdDesc()).thenReturn(List.of(booking3, booking2, booking1));

            // When
            List<AdminBookingOverviewResponse> result = sut.findAllBookings(adminId);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).id()).isEqualTo(3L); // Newest by ID among same createdAt
            assertThat(result.get(1).id()).isEqualTo(2L);
            assertThat(result.get(2).id()).isEqualTo(1L);
        }
    }

    private Booking createBooking(Long id, Long resourceId, Long userId, LocalDate startDate, LocalDate endDate) {
        Resource resource = new Resource();
        resource.setId(resourceId);

        User user = new User();
        user.setId(userId);

        Booking booking = new Booking();
        booking.setId(id);
        booking.setResource(resource);
        booking.setUser(user);
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        booking.setStatus(BookingStatus.PENDING);
        return booking;
    }
}
package de.ccq.resourcehub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.ccq.resourcehub.dto.BookingExportResponse;
import de.ccq.resourcehub.entity.Booking;
import de.ccq.resourcehub.entity.BookingStatus;
import de.ccq.resourcehub.entity.Resource;
import de.ccq.resourcehub.entity.User;
import de.ccq.resourcehub.exception.BookingExportException;
import de.ccq.resourcehub.repository.BookingRepository;
import de.ccq.resourcehub.repository.UserRepository;
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
@DisplayName("BookingExportService")
class BookingExportServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    private BookingExportService sut;

    @BeforeEach
    void setUp() {
        sut = new BookingExportService(bookingRepository, userRepository);
    }

    @Nested
    @DisplayName("exportAllBookings")
    class exportAllBookings {

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
            booking1.setCreatedAt(java.time.Instant.parse("2026-01-01T10:00:00Z"));

            Booking booking2 = createBooking(2L, 11L, 21L, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 15));
            booking2.setCreatedAt(java.time.Instant.parse("2026-01-10T10:00:00Z"));

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
            when(bookingRepository.findAllByOrderByCreatedAtDescIdDesc())
                    .thenReturn(List.of(booking2, booking1));

            // When
            List<BookingExportResponse> result = sut.exportAllBookings(adminId);

            // Then
            assertThat(result).hasSize(2);

            // Check first booking (newest)
            BookingExportResponse first = result.get(0);
            assertThat(first.id()).isEqualTo(2L);
            assertThat(first.resourceId()).isEqualTo(11L);
            assertThat(first.resourceName()).isEqualTo("Resource 11");
            assertThat(first.userId()).isEqualTo(21L);
            assertThat(first.username()).isEqualTo("user21");
            assertThat(first.startDate()).isEqualTo(LocalDate.of(2026, 1, 10));
            assertThat(first.endDate()).isEqualTo(LocalDate.of(2026, 1, 15));
            assertThat(first.status()).isEqualTo(BookingStatus.PENDING);
            assertThat(first.createdAt()).isEqualTo(java.time.Instant.parse("2026-01-10T10:00:00Z"));
            assertThat(first.approvedAt()).isNull();
            assertThat(first.rejectedAt()).isNull();
            assertThat(first.rejectionReason()).isNull();

            // Check second booking
            BookingExportResponse second = result.get(1);
            assertThat(second.id()).isEqualTo(1L);
            assertThat(second.resourceId()).isEqualTo(10L);
            assertThat(second.resourceName()).isEqualTo("Resource 10");
            assertThat(second.userId()).isEqualTo(20L);
            assertThat(second.username()).isEqualTo("user20");
            assertThat(second.startDate()).isEqualTo(LocalDate.of(2026, 1, 1));
            assertThat(second.endDate()).isEqualTo(LocalDate.of(2026, 1, 5));
            assertThat(second.status()).isEqualTo(BookingStatus.PENDING);
            assertThat(second.createdAt()).isEqualTo(java.time.Instant.parse("2026-01-01T10:00:00Z"));

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
            List<BookingExportResponse> result = sut.exportAllBookings(adminId);

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
            assertThatThrownBy(() -> sut.exportAllBookings(adminId))
                    .isInstanceOf(BookingExportException.class)
                    .hasMessage("Admin with ID 999 not found");

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
            assertThatThrownBy(() -> sut.exportAllBookings(adminId))
                    .isInstanceOf(BookingExportException.class)
                    .hasMessage("Admin with ID 1 is not authorized to export bookings");

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
            assertThatThrownBy(() -> sut.exportAllBookings(adminId))
                    .isInstanceOf(BookingExportException.class)
                    .hasMessage("Admin with ID 1 is not authorized to export bookings");

            verifyNoInteractions(bookingRepository);
        }

        @Test
        @DisplayName("uses display name when available for username")
        void usesDisplayNameForUsername() {
            // Given
            Long adminId = 1L;
            User admin = new User();
            admin.setId(adminId);
            admin.setActive(true);
            admin.setRole("ADMIN");

            User user = new User();
            user.setId(20L);
            user.setUsername("user20");
            user.setDisplayName("John Doe");

            Resource resource = new Resource();
            resource.setId(10L);
            resource.setName("Resource A");

            Booking booking = createBooking(1L, 10L, 20L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));
            booking.setCreatedAt(java.time.Instant.parse("2026-01-01T10:00:00Z"));
            booking.setUser(user);
            booking.setResource(resource);

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
            when(bookingRepository.findAllByOrderByCreatedAtDescIdDesc()).thenReturn(List.of(booking));

            // When
            List<BookingExportResponse> result = sut.exportAllBookings(adminId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).username()).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("uses username when display name is null for username")
        void usesUsernameWhenDisplayNameIsNull() {
            // Given
            Long adminId = 1L;
            User admin = new User();
            admin.setId(adminId);
            admin.setActive(true);
            admin.setRole("ADMIN");

            User user = new User();
            user.setId(20L);
            user.setUsername("user20");
            user.setDisplayName(null);

            Resource resource = new Resource();
            resource.setId(10L);
            resource.setName("Resource A");

            Booking booking = createBooking(1L, 10L, 20L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));
            booking.setCreatedAt(java.time.Instant.parse("2026-01-01T10:00:00Z"));
            booking.setUser(user);
            booking.setResource(resource);

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
            when(bookingRepository.findAllByOrderByCreatedAtDescIdDesc()).thenReturn(List.of(booking));

            // When
            List<BookingExportResponse> result = sut.exportAllBookings(adminId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).username()).isEqualTo("user20");
        }
    }

    @Nested
    @DisplayName("exportBookingsByStatus")
    class exportBookingsByStatus {

        @Test
        @DisplayName("returns filtered bookings by status when admin is valid")
        void returnsFilteredBookingsByStatus() {
            // Given
            Long adminId = 1L;
            User admin = new User();
            admin.setId(adminId);
            admin.setActive(true);
            admin.setRole("ADMIN");

            Booking pendingBooking = createBooking(1L, 10L, 20L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));
            pendingBooking.setCreatedAt(java.time.Instant.parse("2026-01-01T10:00:00Z"));
            pendingBooking.setStatus(BookingStatus.PENDING);

            Booking approvedBooking = createBooking(2L, 11L, 21L, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 15));
            approvedBooking.setCreatedAt(java.time.Instant.parse("2026-01-10T10:00:00Z"));
            approvedBooking.setStatus(BookingStatus.APPROVED);

            Booking rejectedBooking = createBooking(3L, 12L, 22L, LocalDate.of(2026, 1, 20), LocalDate.of(2026, 1, 25));
            rejectedBooking.setCreatedAt(java.time.Instant.parse("2026-01-20T10:00:00Z"));
            rejectedBooking.setStatus(BookingStatus.REJECTED);

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
            when(bookingRepository.findAllByOrderByCreatedAtDescIdDesc())
                    .thenReturn(List.of(pendingBooking, approvedBooking, rejectedBooking));

            // When
            List<BookingExportResponse> result = sut.exportBookingsByStatus(adminId, "approved");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(2L);
            assertThat(result.get(0).status()).isEqualTo(BookingStatus.APPROVED);

            verify(userRepository).findById(adminId);
            verify(bookingRepository).findAllByOrderByCreatedAtDescIdDesc();
        }

        @Test
        @DisplayName("returns filtered bookings by status (case insensitive)")
        void returnsFilteredBookingsByStatusCaseInsensitive() {
            // Given
            Long adminId = 1L;
            User admin = new User();
            admin.setId(adminId);
            admin.setActive(true);
            admin.setRole("ADMIN");

            Booking approvedBooking = createBooking(1L, 10L, 20L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));
            approvedBooking.setCreatedAt(java.time.Instant.parse("2026-01-01T10:00:00Z"));
            approvedBooking.setStatus(BookingStatus.APPROVED);

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
            when(bookingRepository.findAllByOrderByCreatedAtDescIdDesc()).thenReturn(List.of(approvedBooking));

            // When
            List<BookingExportResponse> result = sut.exportBookingsByStatus(adminId, "APPROVED");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).status()).isEqualTo(BookingStatus.APPROVED);
        }

        @Test
        @DisplayName("throws INVALID_STATUS exception when status is null")
        void throwsInvalidStatusWhenStatusIsNull() {
            // Given
            Long adminId = 1L;
            User admin = new User();
            admin.setId(adminId);
            admin.setActive(true);
            admin.setRole("ADMIN");

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

            // When/Then
            assertThatThrownBy(() -> sut.exportBookingsByStatus(adminId, null))
                    .isInstanceOf(BookingExportException.class)
                    .hasMessage("Invalid status: null");

            verifyNoInteractions(bookingRepository);
        }

        @Test
        @DisplayName("throws INVALID_STATUS exception when status is empty")
        void throwsInvalidStatusWhenStatusIsEmpty() {
            // Given
            Long adminId = 1L;
            User admin = new User();
            admin.setId(adminId);
            admin.setActive(true);
            admin.setRole("ADMIN");

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

            // When/Then
            assertThatThrownBy(() -> sut.exportBookingsByStatus(adminId, ""))
                    .isInstanceOf(BookingExportException.class)
                    .hasMessage("Invalid status: ");

            verifyNoInteractions(bookingRepository);
        }

        @Test
        @DisplayName("throws INVALID_STATUS exception when status is whitespace")
        void throwsInvalidStatusWhenStatusIsWhitespace() {
            // Given
            Long adminId = 1L;
            User admin = new User();
            admin.setId(adminId);
            admin.setActive(true);
            admin.setRole("ADMIN");

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

            // When/Then
            assertThatThrownBy(() -> sut.exportBookingsByStatus(adminId, "   "))
                    .isInstanceOf(BookingExportException.class)
                    .hasMessage("Invalid status:    ");

            verifyNoInteractions(bookingRepository);
        }

        @Test
        @DisplayName("returns empty list when no bookings match the status")
        void returnsEmptyListWhenNoBookingsMatchStatus() {
            // Given
            Long adminId = 1L;
            User admin = new User();
            admin.setId(adminId);
            admin.setActive(true);
            admin.setRole("ADMIN");

            Booking pendingBooking = createBooking(1L, 10L, 20L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));
            pendingBooking.setCreatedAt(java.time.Instant.parse("2026-01-01T10:00:00Z"));
            pendingBooking.setStatus(BookingStatus.PENDING);

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
            when(bookingRepository.findAllByOrderByCreatedAtDescIdDesc()).thenReturn(List.of(pendingBooking));

            // When
            List<BookingExportResponse> result = sut.exportBookingsByStatus(adminId, "APPROVED");

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("exportBookingsByDateRange")
    class exportBookingsByDateRange {

        @Test
        @DisplayName("returns bookings within date range when admin is valid")
        void returnsBookingsWithinDateRange() {
            // Given
            Long adminId = 1L;
            User admin = new User();
            admin.setId(adminId);
            admin.setActive(true);
            admin.setRole("ADMIN");

            Booking booking1 = createBooking(1L, 10L, 20L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));
            booking1.setCreatedAt(java.time.Instant.parse("2026-01-01T10:00:00Z"));

            Booking booking2 = createBooking(2L, 11L, 21L, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 15));
            booking2.setCreatedAt(java.time.Instant.parse("2026-01-10T10:00:00Z"));

            Booking booking3 = createBooking(3L, 12L, 22L, LocalDate.of(2026, 1, 20), LocalDate.of(2026, 1, 25));
            booking3.setCreatedAt(java.time.Instant.parse("2026-01-20T10:00:00Z"));

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
            when(bookingRepository.findAllByOrderByCreatedAtDescIdDesc())
                    .thenReturn(List.of(booking3, booking2, booking1));  // ordered by createdAt DESC

            // When: query for bookings between 2026-01-05 and 2026-01-15
            List<BookingExportResponse> result = sut.exportBookingsByDateRange(
                    adminId,
                    LocalDate.of(2026, 1, 5),
                    LocalDate.of(2026, 1, 15));

            // Then: should return booking2 (2026-01-10 to 2026-01-15) and booking1 (2026-01-01 to 2026-01-05)
            // booking2 has createdAt 2026-01-10, booking1 has createdAt 2026-01-01
            // Expected order by createdAt DESC: booking2 (newer), then booking1
            assertThat(result).hasSize(2);
            assertThat(result.get(0).id()).isEqualTo(2L);  // booking2 is newer
            assertThat(result.get(1).id()).isEqualTo(1L);  // booking1 is older

            verify(userRepository).findById(adminId);
            verify(bookingRepository).findAllByOrderByCreatedAtDescIdDesc();
        }

        @Test
        @DisplayName("throws INVALID_DATE_RANGE exception when startDate is after endDate")
        void throwsInvalidDateRangeWhenStartDateAfterEndDate() {
            // Given
            Long adminId = 1L;
            User admin = new User();
            admin.setId(adminId);
            admin.setActive(true);
            admin.setRole("ADMIN");

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

            // When/Then
            assertThatThrownBy(() -> sut.exportBookingsByDateRange(
                    adminId,
                    LocalDate.of(2026, 1, 15),
                    LocalDate.of(2026, 1, 5)))
                    .isInstanceOf(BookingExportException.class)
                    .hasMessage("Invalid date range: start=2026-01-15, end=2026-01-05");

            verifyNoInteractions(bookingRepository);
        }

        @Test
        @DisplayName("throws INVALID_DATE_RANGE exception when startDate is null")
        void throwsInvalidDateRangeWhenStartDateIsNull() {
            // Given
            Long adminId = 1L;
            User admin = new User();
            admin.setId(adminId);
            admin.setActive(true);
            admin.setRole("ADMIN");

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

            // When/Then
            assertThatThrownBy(() -> sut.exportBookingsByDateRange(
                    adminId,
                    null,
                    LocalDate.of(2026, 1, 15)))
                    .isInstanceOf(BookingExportException.class)
                    .hasMessage("Invalid date range: start=null, end=2026-01-15");

            verifyNoInteractions(bookingRepository);
        }

        @Test
        @DisplayName("throws INVALID_DATE_RANGE exception when endDate is null")
        void throwsInvalidDateRangeWhenEndDateIsNull() {
            // Given
            Long adminId = 1L;
            User admin = new User();
            admin.setId(adminId);
            admin.setActive(true);
            admin.setRole("ADMIN");

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

            // When/Then
            assertThatThrownBy(() -> sut.exportBookingsByDateRange(
                    adminId,
                    LocalDate.of(2026, 1, 5),
                    null))
                    .isInstanceOf(BookingExportException.class)
                    .hasMessage("Invalid date range: start=2026-01-05, end=null");

            verifyNoInteractions(bookingRepository);
        }

        @Test
        @DisplayName("returns empty list when no bookings match the date range")
        void returnsEmptyListWhenNoBookingsMatchDateRange() {
            // Given
            Long adminId = 1L;
            User admin = new User();
            admin.setId(adminId);
            admin.setActive(true);
            admin.setRole("ADMIN");

            Booking booking1 = createBooking(1L, 10L, 20L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));
            booking1.setCreatedAt(java.time.Instant.parse("2026-01-01T10:00:00Z"));

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
            when(bookingRepository.findAllByOrderByCreatedAtDescIdDesc()).thenReturn(List.of(booking1));

            // When: query for bookings in February
            List<BookingExportResponse> result = sut.exportBookingsByDateRange(
                    adminId,
                    LocalDate.of(2026, 2, 1),
                    LocalDate.of(2026, 2, 15));

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("includes bookings that start before range but end within range")
        void includesBookingsStartingBeforeRange() {
            // Given
            Long adminId = 1L;
            User admin = new User();
            admin.setId(adminId);
            admin.setActive(true);
            admin.setRole("ADMIN");

            // Booking: 2026-01-10 to 2026-01-20
            Booking booking = createBooking(1L, 10L, 20L, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 20));
            booking.setCreatedAt(java.time.Instant.parse("2026-01-10T10:00:00Z"));

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
            when(bookingRepository.findAllByOrderByCreatedAtDescIdDesc()).thenReturn(List.of(booking));

            // When: query for bookings in 2026-01-15 to 2026-01-18
            List<BookingExportResponse> result = sut.exportBookingsByDateRange(
                    adminId,
                    LocalDate.of(2026, 1, 15),
                    LocalDate.of(2026, 1, 18));

            // Then: booking should be included (overlaps)
            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(1L);
        }

        @Test
        @DisplayName("includes bookings that start within range but end after range")
        void includesBookingsEndingAfterRange() {
            // Given
            Long adminId = 1L;
            User admin = new User();
            admin.setId(adminId);
            admin.setActive(true);
            admin.setRole("ADMIN");

            // Booking: 2026-01-10 to 2026-01-20
            Booking booking = createBooking(1L, 10L, 20L, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 20));
            booking.setCreatedAt(java.time.Instant.parse("2026-01-10T10:00:00Z"));

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
            when(bookingRepository.findAllByOrderByCreatedAtDescIdDesc()).thenReturn(List.of(booking));

            // When: query for bookings in 2026-01-15 to 2026-01-18
            List<BookingExportResponse> result = sut.exportBookingsByDateRange(
                    adminId,
                    LocalDate.of(2026, 1, 15),
                    LocalDate.of(2026, 1, 18));

            // Then: booking should be included (overlaps)
            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(1L);
        }

        @Test
        @DisplayName("excludes bookings that end before range starts")
        void excludesBookingsEndingBeforeRange() {
            // Given
            Long adminId = 1L;
            User admin = new User();
            admin.setId(adminId);
            admin.setActive(true);
            admin.setRole("ADMIN");

            // Booking: 2026-01-01 to 2026-01-05
            Booking booking = createBooking(1L, 10L, 20L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));
            booking.setCreatedAt(java.time.Instant.parse("2026-01-01T10:00:00Z"));

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
            when(bookingRepository.findAllByOrderByCreatedAtDescIdDesc()).thenReturn(List.of(booking));

            // When: query for bookings in 2026-01-10 to 2026-01-15
            List<BookingExportResponse> result = sut.exportBookingsByDateRange(
                    adminId,
                    LocalDate.of(2026, 1, 10),
                    LocalDate.of(2026, 1, 15));

            // Then: booking should be excluded (entirely before range)
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("excludes bookings that start after range ends")
        void excludesBookingsStartingAfterRange() {
            // Given
            Long adminId = 1L;
            User admin = new User();
            admin.setId(adminId);
            admin.setActive(true);
            admin.setRole("ADMIN");

            // Booking: 2026-01-20 to 2026-01-25
            Booking booking = createBooking(1L, 10L, 20L, LocalDate.of(2026, 1, 20), LocalDate.of(2026, 1, 25));
            booking.setCreatedAt(java.time.Instant.parse("2026-01-20T10:00:00Z"));

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
            when(bookingRepository.findAllByOrderByCreatedAtDescIdDesc()).thenReturn(List.of(booking));

            // When: query for bookings in 2026-01-10 to 2026-01-15
            List<BookingExportResponse> result = sut.exportBookingsByDateRange(
                    adminId,
                    LocalDate.of(2026, 1, 10),
                    LocalDate.of(2026, 1, 15));

            // Then: booking should be excluded (entirely after range)
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("includes bookings fully contained within range")
        void includesBookingsFullyContainedInRange() {
            // Given
            Long adminId = 1L;
            User admin = new User();
            admin.setId(adminId);
            admin.setActive(true);
            admin.setRole("ADMIN");

            // Booking: 2026-01-12 to 2026-01-18
            Booking booking = createBooking(1L, 10L, 20L, LocalDate.of(2026, 1, 12), LocalDate.of(2026, 1, 18));
            booking.setCreatedAt(java.time.Instant.parse("2026-01-12T10:00:00Z"));

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
            when(bookingRepository.findAllByOrderByCreatedAtDescIdDesc()).thenReturn(List.of(booking));

            // When: query for bookings in 2026-01-10 to 2026-01-20
            List<BookingExportResponse> result = sut.exportBookingsByDateRange(
                    adminId,
                    LocalDate.of(2026, 1, 10),
                    LocalDate.of(2026, 1, 20));

            // Then: booking should be included (fully within range)
            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(1L);
        }
    }

    private Booking createBooking(Long id, Long resourceId, Long userId, LocalDate startDate, LocalDate endDate) {
        Resource resource = new Resource();
        resource.setId(resourceId);
        resource.setName("Resource " + resourceId);

        User user = new User();
        user.setId(userId);
        user.setUsername("user" + userId);

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
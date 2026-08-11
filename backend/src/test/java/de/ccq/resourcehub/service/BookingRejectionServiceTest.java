package de.ccq.resourcehub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.ccq.resourcehub.entity.Booking;
import de.ccq.resourcehub.entity.BookingStatus;
import de.ccq.resourcehub.entity.Resource;
import de.ccq.resourcehub.entity.User;
import de.ccq.resourcehub.exception.BookingRejectionException;
import de.ccq.resourcehub.repository.BookingRepository;
import de.ccq.resourcehub.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingRejectionServiceTest {

    private static final Instant REJECTION_TIME = Instant.parse("2026-08-11T12:30:00Z");

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    private BookingRejectionService sut;

    @BeforeEach
    void setUp() {
        var clock = Clock.fixed(REJECTION_TIME, ZoneOffset.UTC);
        sut = new BookingRejectionService(bookingRepository, userRepository, clock);
    }

    @Test
    void rejectBooking_returnsRejectedBookingAndTrimsReasonWhenRequestIsEligible() {
        // arrange
        var booking = booking(7L, BookingStatus.PENDING);
        when(bookingRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(11L)).thenReturn(Optional.of(manager(11L, true, "MANAGER")));
        when(bookingRepository.save(booking)).thenReturn(booking);

        // act
        var result = sut.rejectBooking(7L, 11L, "  Resource unavailable  ");

        // assert
        assertThat(result.id()).isEqualTo(7L);
        assertThat(result.resourceId()).isEqualTo(3L);
        assertThat(result.userId()).isEqualTo(5L);
        assertThat(result.startDate()).isEqualTo(LocalDate.parse("2026-09-01"));
        assertThat(result.endDate()).isEqualTo(LocalDate.parse("2026-09-03"));
        assertThat(result.status()).isEqualTo(BookingStatus.REJECTED);
        assertThat(result.rejectedAt()).isEqualTo(REJECTION_TIME);
        assertThat(result.rejectionReason()).isEqualTo("Resource unavailable");
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.REJECTED);
        assertThat(booking.getRejectedAt()).isEqualTo(REJECTION_TIME);
        assertThat(booking.getRejectionReason()).isEqualTo("Resource unavailable");
        verify(bookingRepository).save(booking);
    }

    @Test
    void rejectBooking_throwsNotFoundBeforeManagerLookupWhenBookingDoesNotExist() {
        // arrange
        when(bookingRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> sut.rejectBooking(99L, 11L, "Unavailable"))
                .isInstanceOf(BookingRejectionException.class)
                .hasMessage("Booking not found with ID: 99")
                .extracting(exception -> ((BookingRejectionException) exception).getReason())
                .isEqualTo(BookingRejectionException.Reason.NOT_FOUND);
        verifyNoInteractions(userRepository);
        verify(bookingRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectBooking_throwsNotFoundWhenManagerDoesNotExist() {
        // arrange
        var booking = booking(7L, BookingStatus.PENDING);
        when(bookingRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(88L)).thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> sut.rejectBooking(7L, 88L, "Unavailable"))
                .isInstanceOf(BookingRejectionException.class)
                .hasMessage("Manager not found with ID: 88");
        verify(bookingRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectBooking_throwsForbiddenWhenManagerIsInactive() {
        assertManagerForbidden(false, "MANAGER");
    }

    @Test
    void rejectBooking_throwsForbiddenWhenUserDoesNotHaveManagerRole() {
        assertManagerForbidden(true, "USER");
    }

    @ParameterizedTest
    @EnumSource(value = BookingStatus.class, names = "PENDING", mode = EnumSource.Mode.EXCLUDE)
    void rejectBooking_throwsConflictWithoutChangingBookingWhenBookingIsNotPending(BookingStatus status) {
        // arrange
        var booking = booking(7L, status);
        when(bookingRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(11L)).thenReturn(Optional.of(manager(11L, true, "MANAGER")));

        // act & assert
        assertThatThrownBy(() -> sut.rejectBooking(7L, 11L, "Unavailable"))
                .isInstanceOf(BookingRejectionException.class)
                .hasMessage("Only pending bookings can be rejected; status is " + status)
                .extracting(exception -> ((BookingRejectionException) exception).getReason())
                .isEqualTo(BookingRejectionException.Reason.CONFLICT);
        assertThat(booking.getStatus()).isEqualTo(status);
        assertThat(booking.getRejectedAt()).isNull();
        assertThat(booking.getRejectionReason()).isNull();
        verify(bookingRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private void assertManagerForbidden(boolean active, String role) {
        // arrange
        var booking = booking(7L, BookingStatus.PENDING);
        when(bookingRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(11L)).thenReturn(Optional.of(manager(11L, active, role)));

        // act & assert
        assertThatThrownBy(() -> sut.rejectBooking(7L, 11L, "Unavailable"))
                .isInstanceOf(BookingRejectionException.class)
                .hasMessage("User 11 may not reject bookings")
                .extracting(exception -> ((BookingRejectionException) exception).getReason())
                .isEqualTo(BookingRejectionException.Reason.FORBIDDEN);
        verify(bookingRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private Booking booking(Long id, BookingStatus status) {
        var resource = new Resource();
        resource.setId(3L);
        var user = new User();
        user.setId(5L);
        var booking = new Booking();
        booking.setId(id);
        booking.setResource(resource);
        booking.setUser(user);
        booking.setStartDate(LocalDate.parse("2026-09-01"));
        booking.setEndDate(LocalDate.parse("2026-09-03"));
        booking.setStatus(status);
        return booking;
    }

    private User manager(Long id, boolean active, String role) {
        var manager = new User();
        manager.setId(id);
        manager.setActive(active);
        manager.setRole(role);
        return manager;
    }
}

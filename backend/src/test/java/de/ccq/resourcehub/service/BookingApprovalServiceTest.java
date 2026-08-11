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
import de.ccq.resourcehub.exception.BookingApprovalException;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingApprovalServiceTest {

    private static final Instant APPROVAL_TIME = Instant.parse("2026-08-11T10:15:30Z");

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    private BookingApprovalService sut;

    @BeforeEach
    void setUp() {
        var clock = Clock.fixed(APPROVAL_TIME, ZoneOffset.UTC);
        sut = new BookingApprovalService(bookingRepository, userRepository, clock);
    }

    @Test
    void approveBooking_returnsApprovedBookingAndTimestampWhenRequestIsEligible() {
        // arrange
        var booking = booking(7L, BookingStatus.PENDING);
        var manager = manager(11L, true, "MANAGER");
        when(bookingRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(11L)).thenReturn(Optional.of(manager));
        when(bookingRepository.existsApprovedOverlap(
                        3L, LocalDate.parse("2026-09-01"), LocalDate.parse("2026-09-03"), 7L))
                .thenReturn(false);
        when(bookingRepository.save(booking)).thenReturn(booking);

        // act
        var result = sut.approveBooking(7L, 11L);

        // assert
        assertThat(result.id()).isEqualTo(7L);
        assertThat(result.resourceId()).isEqualTo(3L);
        assertThat(result.userId()).isEqualTo(5L);
        assertThat(result.startDate()).isEqualTo(LocalDate.parse("2026-09-01"));
        assertThat(result.endDate()).isEqualTo(LocalDate.parse("2026-09-03"));
        assertThat(result.status()).isEqualTo(BookingStatus.APPROVED);
        assertThat(result.approvedAt()).isEqualTo(APPROVAL_TIME);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(booking.getApprovedAt()).isEqualTo(APPROVAL_TIME);
        verify(bookingRepository).save(booking);
    }

    @Test
    void approveBooking_throwsNotFoundBeforeManagerLookupWhenBookingDoesNotExist() {
        // arrange
        when(bookingRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> sut.approveBooking(99L, 11L))
                .isInstanceOf(BookingApprovalException.class)
                .hasMessage("Booking not found with ID: 99")
                .extracting(exception -> ((BookingApprovalException) exception).getReason())
                .isEqualTo(BookingApprovalException.Reason.NOT_FOUND);
        verifyNoInteractions(userRepository);
        verify(bookingRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void approveBooking_throwsNotFoundWhenManagerDoesNotExist() {
        // arrange
        var booking = booking(7L, BookingStatus.PENDING);
        when(bookingRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(88L)).thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> sut.approveBooking(7L, 88L))
                .isInstanceOf(BookingApprovalException.class)
                .hasMessage("Manager not found with ID: 88");
        verify(bookingRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void approveBooking_throwsForbiddenWhenManagerIsInactive() {
        assertManagerForbidden(false, "MANAGER");
    }

    @Test
    void approveBooking_throwsForbiddenWhenUserDoesNotHaveManagerRole() {
        assertManagerForbidden(true, "USER");
    }

    @Test
    void approveBooking_throwsConflictBeforeOverlapCheckWhenBookingIsNotPending() {
        // arrange
        var booking = booking(7L, BookingStatus.APPROVED);
        when(bookingRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(11L)).thenReturn(Optional.of(manager(11L, true, "MANAGER")));

        // act & assert
        assertThatThrownBy(() -> sut.approveBooking(7L, 11L))
                .isInstanceOf(BookingApprovalException.class)
                .hasMessage("Only pending bookings can be approved; status is APPROVED");
        verify(bookingRepository, never()).existsApprovedOverlap(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyLong());
        verify(bookingRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void approveBooking_throwsConflictWithoutChangingBookingWhenApprovedDatesOverlap() {
        // arrange
        var booking = booking(7L, BookingStatus.PENDING);
        when(bookingRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(11L)).thenReturn(Optional.of(manager(11L, true, "MANAGER")));
        when(bookingRepository.existsApprovedOverlap(
                        3L, LocalDate.parse("2026-09-01"), LocalDate.parse("2026-09-03"), 7L))
                .thenReturn(true);

        // act & assert
        assertThatThrownBy(() -> sut.approveBooking(7L, 11L))
                .isInstanceOf(BookingApprovalException.class)
                .hasMessage("Booking overlaps an already approved booking");
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(booking.getApprovedAt()).isNull();
        verify(bookingRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private void assertManagerForbidden(boolean active, String role) {
        // arrange
        var booking = booking(7L, BookingStatus.PENDING);
        when(bookingRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(booking));
        when(userRepository.findById(11L)).thenReturn(Optional.of(manager(11L, active, role)));

        // act & assert
        assertThatThrownBy(() -> sut.approveBooking(7L, 11L))
                .isInstanceOf(BookingApprovalException.class)
                .hasMessage("User 11 may not approve bookings")
                .extracting(exception -> ((BookingApprovalException) exception).getReason())
                .isEqualTo(BookingApprovalException.Reason.FORBIDDEN);
        verify(bookingRepository, never()).existsApprovedOverlap(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyLong());
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

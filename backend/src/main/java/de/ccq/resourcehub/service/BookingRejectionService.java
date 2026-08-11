package de.ccq.resourcehub.service;

import de.ccq.resourcehub.dto.BookingRejectionResponse;
import de.ccq.resourcehub.entity.Booking;
import de.ccq.resourcehub.entity.BookingStatus;
import de.ccq.resourcehub.entity.User;
import de.ccq.resourcehub.exception.BookingRejectionException;
import de.ccq.resourcehub.repository.BookingRepository;
import de.ccq.resourcehub.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingRejectionService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public BookingRejectionService(BookingRepository bookingRepository, UserRepository userRepository, Clock clock) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Transactional
    public BookingRejectionResponse rejectBooking(Long bookingId, Long managerId, String reason) {
        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> BookingRejectionException.bookingNotFound(bookingId));
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> BookingRejectionException.managerNotFound(managerId));

        if (!Boolean.TRUE.equals(manager.getActive()) || !"MANAGER".equals(manager.getRole())) {
            throw BookingRejectionException.forbidden(managerId);
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw BookingRejectionException.invalidStatus(booking.getStatus());
        }

        booking.setStatus(BookingStatus.REJECTED);
        booking.setRejectedAt(Instant.now(clock));
        booking.setRejectionReason(reason.trim());
        return toResponse(bookingRepository.save(booking));
    }

    private BookingRejectionResponse toResponse(Booking booking) {
        return new BookingRejectionResponse(
                booking.getId(),
                booking.getResource().getId(),
                booking.getUser().getId(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getStatus(),
                booking.getRejectedAt(),
                booking.getRejectionReason());
    }
}

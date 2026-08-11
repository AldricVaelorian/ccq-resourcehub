package de.ccq.resourcehub.service;

import de.ccq.resourcehub.dto.BookingResponse;
import de.ccq.resourcehub.entity.Booking;
import de.ccq.resourcehub.entity.BookingStatus;
import de.ccq.resourcehub.entity.User;
import de.ccq.resourcehub.exception.BookingApprovalException;
import de.ccq.resourcehub.repository.BookingRepository;
import de.ccq.resourcehub.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingApprovalService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public BookingApprovalService(BookingRepository bookingRepository, UserRepository userRepository, Clock clock) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Transactional
    public BookingResponse approveBooking(Long bookingId, Long managerId) {
        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> BookingApprovalException.notFound(bookingId));
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> BookingApprovalException.managerNotFound(managerId));

        if (!Boolean.TRUE.equals(manager.getActive()) || !"MANAGER".equals(manager.getRole())) {
            throw BookingApprovalException.forbidden(managerId);
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw BookingApprovalException.invalidStatus(booking.getStatus());
        }
        if (bookingRepository.existsApprovedOverlap(
                booking.getResource().getId(), booking.getStartDate(), booking.getEndDate(), booking.getId())) {
            throw BookingApprovalException.overlap();
        }

        booking.setStatus(BookingStatus.APPROVED);
        booking.setApprovedAt(Instant.now(clock));
        return toResponse(bookingRepository.save(booking));
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getResource().getId(),
                booking.getUser().getId(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getStatus(),
                booking.getApprovedAt());
    }
}

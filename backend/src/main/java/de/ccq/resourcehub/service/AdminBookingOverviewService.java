package de.ccq.resourcehub.service;

import de.ccq.resourcehub.dto.AdminBookingOverviewResponse;
import de.ccq.resourcehub.entity.Booking;
import de.ccq.resourcehub.entity.User;
import de.ccq.resourcehub.exception.AdminBookingOverviewException;
import de.ccq.resourcehub.repository.BookingRepository;
import de.ccq.resourcehub.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminBookingOverviewService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public AdminBookingOverviewService(BookingRepository bookingRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminBookingOverviewResponse> findAllBookings(Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> AdminBookingOverviewException.adminNotFound(adminId));
        if (!Boolean.TRUE.equals(admin.getActive()) || !"ADMIN".equals(admin.getRole())) {
            throw AdminBookingOverviewException.forbidden(adminId);
        }

        return bookingRepository.findAllByOrderByCreatedAtDescIdDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    private AdminBookingOverviewResponse toResponse(Booking booking) {
        return new AdminBookingOverviewResponse(
                booking.getId(),
                booking.getResource().getId(),
                booking.getUser().getId(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getStatus(),
                booking.getCreatedAt(),
                booking.getApprovedAt(),
                booking.getRejectedAt(),
                booking.getRejectionReason());
    }
}

package de.ccq.resourcehub.service;

import de.ccq.resourcehub.dto.BookingExportResponse;
import de.ccq.resourcehub.entity.Booking;
import de.ccq.resourcehub.entity.User;
import de.ccq.resourcehub.exception.BookingExportException;
import de.ccq.resourcehub.repository.BookingRepository;
import de.ccq.resourcehub.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingExportService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public BookingExportService(BookingRepository bookingRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<BookingExportResponse> exportAllBookings(Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> BookingExportException.adminNotFound(adminId));
        validateAdminAccess(admin);
        
        return bookingRepository.findAllByOrderByCreatedAtDescIdDesc().stream()
                .map(this::toExportResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingExportResponse> exportBookingsByStatus(Long adminId, String status) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> BookingExportException.adminNotFound(adminId));
        validateAdminAccess(admin);
        
        if (status == null || status.trim().isEmpty()) {
            throw BookingExportException.invalidStatus(status);
        }

        return bookingRepository.findAllByOrderByCreatedAtDescIdDesc().stream()
                .filter(booking -> booking.getStatus().name().equalsIgnoreCase(status))
                .map(this::toExportResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingExportResponse> exportBookingsByDateRange(Long adminId, LocalDate startDate, LocalDate endDate) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> BookingExportException.adminNotFound(adminId));
        validateAdminAccess(admin);
        
        if (startDate == null || endDate == null) {
            throw BookingExportException.invalidDateRange(startDate, endDate);
        }
        
        if (startDate.isAfter(endDate)) {
            throw BookingExportException.invalidDateRange(startDate, endDate);
        }

        return bookingRepository.findAllByOrderByCreatedAtDescIdDesc().stream()
                .filter(booking -> 
                    !booking.getStartDate().isAfter(endDate) && 
                    !booking.getEndDate().isBefore(startDate))
                .map(this::toExportResponse)
                .toList();
    }

    private void validateAdminAccess(User admin) {
        if (!Boolean.TRUE.equals(admin.getActive()) || !"ADMIN".equals(admin.getRole())) {
            throw BookingExportException.forbidden(admin.getId());
        }
    }

    private BookingExportResponse toExportResponse(Booking booking) {
        return new BookingExportResponse(
                booking.getId(),
                booking.getResource().getId(),
                booking.getResource().getName(),
                booking.getUser().getId(),
                booking.getUser().getDisplayName() != null ? booking.getUser().getDisplayName() : booking.getUser().getUsername(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getStatus(),
                booking.getCreatedAt(),
                booking.getApprovedAt(),
                booking.getRejectedAt(),
                booking.getRejectionReason());
    }
}
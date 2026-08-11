package de.ccq.resourcehub.repository;

import de.ccq.resourcehub.entity.Booking;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.id = :id")
    Optional<Booking> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select (count(b) > 0) from Booking b
            where b.resource.id = :resourceId
              and b.status = de.ccq.resourcehub.entity.BookingStatus.APPROVED
              and b.id <> :excludedBookingId
              and b.startDate <= :endDate
              and b.endDate >= :startDate
            """)
    boolean existsApprovedOverlap(
            @Param("resourceId") Long resourceId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludedBookingId") Long excludedBookingId);
}

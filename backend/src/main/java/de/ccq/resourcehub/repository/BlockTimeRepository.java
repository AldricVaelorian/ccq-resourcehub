package de.ccq.resourcehub.repository;

import de.ccq.resourcehub.entity.BlockTime;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for BlockTime entity operations.
 */
@Repository
public interface BlockTimeRepository extends JpaRepository<BlockTime, Long> {

    /**
     * Find all block times for a specific resource.
     *
     * @param resourceId the resource ID
     * @return list of block times for the resource
     */
    List<BlockTime> findByResourceIdOrderByStartDateAsc(Long resourceId);

    /**
     * Find all block times that start on or before the given end date and end on or after the given start date.
     *
     * @param resourceId the resource ID
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return list of overlapping block times
     */
    @Query("SELECT bt FROM BlockTime bt WHERE bt.resourceId = :resourceId " +
           "AND bt.startDate <= :endDate AND bt.endDate >= :startDate")
    List<BlockTime> findActiveBlockTimesInRange(@Param("resourceId") Long resourceId,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);
}
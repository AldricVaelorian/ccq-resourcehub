package de.ccq.resourcehub.repository;

import de.ccq.resourcehub.entity.AvailabilityRule;
import java.time.DayOfWeek;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AvailabilityRuleRepository extends JpaRepository<AvailabilityRule, Long> {

    List<AvailabilityRule> findByDayOfWeek(DayOfWeek dayOfWeek);

    List<AvailabilityRule> findByActiveTrue();

    List<AvailabilityRule> findByDayOfWeekAndActiveTrue(DayOfWeek dayOfWeek);
}

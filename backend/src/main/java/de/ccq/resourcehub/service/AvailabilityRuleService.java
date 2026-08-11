package de.ccq.resourcehub.service;

import de.ccq.resourcehub.dto.AvailabilityRuleRequest;
import de.ccq.resourcehub.dto.AvailabilityRuleResponse;
import de.ccq.resourcehub.entity.AvailabilityRule;
import de.ccq.resourcehub.repository.AvailabilityRuleRepository;
import java.time.DayOfWeek;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AvailabilityRuleService {

    private final AvailabilityRuleRepository repository;

    public AvailabilityRuleService(AvailabilityRuleRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<AvailabilityRuleResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AvailabilityRuleResponse findById(Long id) {
        return toResponse(findEntity(id));
    }

    public AvailabilityRuleResponse create(AvailabilityRuleRequest request) {
        validateTimeWindow(request);
        AvailabilityRule rule = new AvailabilityRule(
                request.name().trim(), request.dayOfWeek(), request.startTime(), request.endTime());
        return toResponse(repository.save(rule));
    }

    public AvailabilityRuleResponse update(Long id, AvailabilityRuleRequest request) {
        validateTimeWindow(request);
        AvailabilityRule rule = findEntity(id);
        rule.setName(request.name().trim());
        rule.setDayOfWeek(request.dayOfWeek());
        rule.setStartTime(request.startTime());
        rule.setEndTime(request.endTime());
        return toResponse(repository.save(rule));
    }

    public void delete(Long id) {
        repository.delete(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<AvailabilityRuleResponse> findByDayOfWeek(DayOfWeek dayOfWeek) {
        return repository.findByDayOfWeek(dayOfWeek).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AvailabilityRuleResponse> findActiveRules() {
        return repository.findByActiveTrue().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AvailabilityRuleResponse> findActiveRulesForDay(DayOfWeek dayOfWeek) {
        return repository.findByDayOfWeekAndActiveTrue(dayOfWeek).stream().map(this::toResponse).toList();
    }

    private AvailabilityRule findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("AvailabilityRule not found with id: " + id));
    }

    private void validateTimeWindow(AvailabilityRuleRequest request) {
        if (request.startTime() != null && request.endTime() != null
                && !request.endTime().isAfter(request.startTime())) {
            throw new InvalidTimeWindowException("End time must be after start time");
        }
    }

    private AvailabilityRuleResponse toResponse(AvailabilityRule rule) {
        return new AvailabilityRuleResponse(
                rule.getId(),
                rule.getName(),
                rule.getDayOfWeek(),
                rule.getStartTime(),
                rule.getEndTime(),
                rule.isActive());
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }

    public static class InvalidTimeWindowException extends RuntimeException {
        public InvalidTimeWindowException(String message) {
            super(message);
        }
    }
}

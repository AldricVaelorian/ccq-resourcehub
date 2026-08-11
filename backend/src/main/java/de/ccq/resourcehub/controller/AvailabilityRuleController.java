package de.ccq.resourcehub.controller;

import de.ccq.resourcehub.dto.AvailabilityRuleRequest;
import de.ccq.resourcehub.dto.AvailabilityRuleResponse;
import de.ccq.resourcehub.service.AvailabilityRuleService;
import jakarta.validation.Valid;
import java.time.DayOfWeek;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/availability-rules")
public class AvailabilityRuleController {

    private final AvailabilityRuleService service;

    public AvailabilityRuleController(AvailabilityRuleService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<AvailabilityRuleResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AvailabilityRuleResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<AvailabilityRuleResponse> create(@Valid @RequestBody AvailabilityRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AvailabilityRuleResponse> update(
            @PathVariable Long id, @Valid @RequestBody AvailabilityRuleRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/day/{dayOfWeek}")
    public ResponseEntity<List<AvailabilityRuleResponse>> findByDayOfWeek(
            @PathVariable DayOfWeek dayOfWeek) {
        return ResponseEntity.ok(service.findByDayOfWeek(dayOfWeek));
    }

    @GetMapping("/active")
    public ResponseEntity<List<AvailabilityRuleResponse>> findActiveRules() {
        return ResponseEntity.ok(service.findActiveRules());
    }

    @GetMapping("/active/day/{dayOfWeek}")
    public ResponseEntity<List<AvailabilityRuleResponse>> findActiveRulesForDay(
            @PathVariable DayOfWeek dayOfWeek) {
        return ResponseEntity.ok(service.findActiveRulesForDay(dayOfWeek));
    }
}

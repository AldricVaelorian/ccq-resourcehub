package de.ccq.resourcehub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.ccq.resourcehub.dto.AvailabilityRuleRequest;
import de.ccq.resourcehub.entity.AvailabilityRule;
import de.ccq.resourcehub.repository.AvailabilityRuleRepository;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AvailabilityRuleServiceTest {

    @Mock
    private AvailabilityRuleRepository repository;

    private AvailabilityRuleService sut;

    @BeforeEach
    void setUp() {
        sut = new AvailabilityRuleService(repository);
    }

    @Test
    void create_returnsActiveResponseAndTrimsNameWhenTimeWindowIsValid() {
        // arrange
        var request = request("  Monday hours  ", DayOfWeek.MONDAY, "08:00", "17:00");
        when(repository.save(any(AvailabilityRule.class))).thenAnswer(invocation -> {
            var rule = invocation.getArgument(0, AvailabilityRule.class);
            rule.setId(41L);
            return rule;
        });

        // act
        var result = sut.create(request);

        // assert
        assertThat(result.id()).isEqualTo(41L);
        assertThat(result.name()).isEqualTo("Monday hours");
        assertThat(result.dayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(result.startTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(result.endTime()).isEqualTo(LocalTime.of(17, 0));
        assertThat(result.active()).isTrue();
    }

    @Test
    void create_throwsInvalidTimeWindowWhenTimesAreEqual() {
        // arrange
        var request = request("Monday hours", DayOfWeek.MONDAY, "08:00", "08:00");

        // act & assert
        assertThatThrownBy(() -> sut.create(request))
                .isInstanceOf(AvailabilityRuleService.InvalidTimeWindowException.class)
                .hasMessage("End time must be after start time");
        verifyNoInteractions(repository);
    }

    @Test
    void create_throwsInvalidTimeWindowWhenEndIsBeforeStart() {
        // arrange
        var request = request("Monday hours", DayOfWeek.MONDAY, "17:00", "08:00");

        // act & assert
        assertThatThrownBy(() -> sut.create(request))
                .isInstanceOf(AvailabilityRuleService.InvalidTimeWindowException.class)
                .hasMessage("End time must be after start time");
        verifyNoInteractions(repository);
    }

    @Test
    void update_preservesInactiveStateWhenChangingWritableFields() {
        // arrange
        var existing = rule(7L, "Old", DayOfWeek.TUESDAY, "09:00", "12:00", false);
        var request = request("  Updated  ", DayOfWeek.WEDNESDAY, "10:00", "18:00");
        when(repository.findById(7L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        // act
        var result = sut.update(7L, request);

        // assert
        assertThat(result.name()).isEqualTo("Updated");
        assertThat(result.dayOfWeek()).isEqualTo(DayOfWeek.WEDNESDAY);
        assertThat(result.startTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(result.endTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(result.active()).isFalse();
        verify(repository).save(existing);
    }

    @Test
    void findById_throwsNotFoundWhenRuleDoesNotExist() {
        // arrange
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> sut.findById(99L))
                .isInstanceOf(AvailabilityRuleService.NotFoundException.class)
                .hasMessage("AvailabilityRule not found with id: 99");
    }

    @Test
    void delete_deletesResolvedRuleWhenItExists() {
        // arrange
        var existing = rule(7L, "Monday", DayOfWeek.MONDAY, "08:00", "17:00", true);
        when(repository.findById(7L)).thenReturn(Optional.of(existing));

        // act
        sut.delete(7L);

        // assert
        verify(repository).delete(existing);
    }

    @Test
    void findActiveRulesForDay_mapsOnlyRepositoryResults() {
        // arrange
        var monday = rule(1L, "Monday", DayOfWeek.MONDAY, "08:00", "17:00", true);
        when(repository.findByDayOfWeekAndActiveTrue(DayOfWeek.MONDAY)).thenReturn(List.of(monday));

        // act
        var result = sut.findActiveRulesForDay(DayOfWeek.MONDAY);

        // assert
        assertThat(result).singleElement().satisfies(response -> {
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.name()).isEqualTo("Monday");
            assertThat(response.active()).isTrue();
        });
    }

    private AvailabilityRuleRequest request(String name, DayOfWeek day, String start, String end) {
        return new AvailabilityRuleRequest(name, day, LocalTime.parse(start), LocalTime.parse(end));
    }

    private AvailabilityRule rule(
            Long id, String name, DayOfWeek day, String start, String end, boolean active) {
        var rule = new AvailabilityRule(name, day, LocalTime.parse(start), LocalTime.parse(end));
        rule.setId(id);
        rule.setActive(active);
        return rule;
    }
}

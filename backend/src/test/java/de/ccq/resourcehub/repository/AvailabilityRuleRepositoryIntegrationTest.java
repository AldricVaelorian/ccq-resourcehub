package de.ccq.resourcehub.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.ccq.resourcehub.entity.AvailabilityRule;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@DataJpaTest
@ActiveProfiles("test")
@Testcontainers
class AvailabilityRuleRepositoryIntegrationTest {

    @Container
    private static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine");

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .load()
                .migrate();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private AvailabilityRuleRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void save_persistsDayTimesAndActiveDefault() {
        // arrange
        var rule = rule("Monday", DayOfWeek.MONDAY, "08:00", "17:00", true);

        // act
        var id = repository.saveAndFlush(rule).getId();
        entityManager.clear();

        // assert
        assertThat(repository.findById(id)).get().satisfies(persisted -> {
            assertThat(persisted.getName()).isEqualTo("Monday");
            assertThat(persisted.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
            assertThat(persisted.getStartTime()).isEqualTo(LocalTime.of(8, 0));
            assertThat(persisted.getEndTime()).isEqualTo(LocalTime.of(17, 0));
            assertThat(persisted.isActive()).isTrue();
        });
    }

    @Test
    void findByDayOfWeek_returnsMatchingRulesAndExcludesOtherDays() {
        // arrange
        repository.saveAll(List.of(
                rule("Monday", DayOfWeek.MONDAY, "08:00", "12:00", true),
                rule("Monday afternoon", DayOfWeek.MONDAY, "13:00", "17:00", false),
                rule("Tuesday", DayOfWeek.TUESDAY, "08:00", "17:00", true)));
        repository.flush();
        entityManager.clear();

        // act
        var result = repository.findByDayOfWeek(DayOfWeek.MONDAY);

        // assert
        assertThat(result).extracting(AvailabilityRule::getName)
                .containsExactlyInAnyOrder("Monday", "Monday afternoon");
    }

    @Test
    void findByActiveTrue_returnsOnlyActiveRules() {
        // arrange
        repository.saveAll(List.of(
                rule("Active", DayOfWeek.MONDAY, "08:00", "12:00", true),
                rule("Inactive", DayOfWeek.TUESDAY, "08:00", "12:00", false)));
        repository.flush();
        entityManager.clear();

        // act
        var result = repository.findByActiveTrue();

        // assert
        assertThat(result).extracting(AvailabilityRule::getName).containsExactly("Active");
    }

    @Test
    void findByDayOfWeekAndActiveTrue_filtersByBothProperties() {
        // arrange
        repository.saveAll(List.of(
                rule("Active Monday", DayOfWeek.MONDAY, "08:00", "12:00", true),
                rule("Inactive Monday", DayOfWeek.MONDAY, "13:00", "17:00", false),
                rule("Active Tuesday", DayOfWeek.TUESDAY, "08:00", "12:00", true)));
        repository.flush();
        entityManager.clear();

        // act
        var result = repository.findByDayOfWeekAndActiveTrue(DayOfWeek.MONDAY);

        // assert
        assertThat(result).extracting(AvailabilityRule::getName).containsExactly("Active Monday");
    }

    @Test
    void save_throwsPersistenceExceptionWhenTimesAreEqual() {
        // arrange
        var invalid = rule("Invalid", DayOfWeek.MONDAY, "08:00", "08:00", true);

        // act & assert
        assertThatThrownBy(() -> repository.saveAndFlush(invalid))
                .isInstanceOf(PersistenceException.class);
    }

    private AvailabilityRule rule(
            String name, DayOfWeek day, String start, String end, boolean active) {
        var rule = new AvailabilityRule(name, day, LocalTime.parse(start), LocalTime.parse(end));
        rule.setActive(active);
        return rule;
    }
}

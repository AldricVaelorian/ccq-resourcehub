package de.ccq.resourcehub.repository;

import static org.assertj.core.api.Assertions.assertThat;

import de.ccq.resourcehub.entity.BlockTime;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.test.context.ImportAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Integration tests for BlockTimeRepository using Testcontainers for a real PostgreSQL database.
 * Tests JPA behavior, database constraints, and custom queries.
 */
@DataJpaTest
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@ActiveProfiles("test")
@Testcontainers
@DisplayName("BlockTimeRepository Integration Test")
class BlockTimeRepositoryIntegrationTest {

    @Container
    private static final PostgreSQLContainer postgresqlContainer =
            new PostgreSQLContainer("postgres:18-alpine");

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
    }

    @Autowired
    private BlockTimeRepository repository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        // Clear persistence context to avoid first-level-cache issues
        entityManager.clear();
    }

    @Nested
    @DisplayName("findByResourceIdOrderByStartDateAsc")
    class FindByResourceIdOrderByStartDateAsc {

        @BeforeEach
        void setUpData() {
            entityManager.clear();
        }

        @Test
        @DisplayName("returns block times ordered by start date for a resource")
        void returnsBlockTimesOrderedByStartDate() {
            // Given
            Long resourceId = 1L;
            BlockTime blockTime1 = createBlockTime(resourceId, "Block 1",
                    LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 15));
            BlockTime blockTime2 = createBlockTime(resourceId, "Block 2",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));
            BlockTime blockTime3 = createBlockTime(resourceId, "Block 3",
                    LocalDate.of(2026, 1, 20), LocalDate.of(2026, 1, 25));

            repository.saveAll(List.of(blockTime1, blockTime2, blockTime3));
            entityManager.flush();
            entityManager.clear();

            // When
            List<BlockTime> result = repository.findByResourceIdOrderByStartDateAsc(resourceId);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getTitle()).isEqualTo("Block 2"); // Starts Jan 1
            assertThat(result.get(1).getTitle()).isEqualTo("Block 1"); // Starts Jan 10
            assertThat(result.get(2).getTitle()).isEqualTo("Block 3"); // Starts Jan 20
        }

        @Test
        @DisplayName("returns empty list when no block times exist for resource")
        void returnsEmptyListWhenNoBlockTimes() {
            // Given
            Long resourceId = 999L;

            // When
            List<BlockTime> result = repository.findByResourceIdOrderByStartDateAsc(resourceId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns block times only for specified resource")
        void returnsOnlyForSpecifiedResource() {
            // Given
            Long resource1Id = 1L;
            Long resource2Id = 2L;
            BlockTime blockTime1 = createBlockTime(resource1Id, "Block on Resource 1",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));
            BlockTime blockTime2 = createBlockTime(resource2Id, "Block on Resource 2",
                    LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 15));

            repository.saveAll(List.of(blockTime1, blockTime2));
            entityManager.flush();
            entityManager.clear();

            // When
            List<BlockTime> result1 = repository.findByResourceIdOrderByStartDateAsc(resource1Id);
            List<BlockTime> result2 = repository.findByResourceIdOrderByStartDateAsc(resource2Id);

            // Then
            assertThat(result1).hasSize(1);
            assertThat(result1.get(0).getTitle()).isEqualTo("Block on Resource 1");

            assertThat(result2).hasSize(1);
            assertThat(result2.get(0).getTitle()).isEqualTo("Block on Resource 2");
        }
    }

    @Nested
    @DisplayName("findActiveBlockTimesInRange")
    class FindActiveBlockTimesInRange {

        @BeforeEach
        void setUpData() {
            entityManager.clear();
        }

        @Test
        @DisplayName("finds block times that overlap with the given range")
        void findsOverlappingBlockTimes() {
            // Given
            Long resourceId = 1L;
            // Query range: Jan 5 to Jan 10
            LocalDate startDate = LocalDate.of(2026, 1, 5);
            LocalDate endDate = LocalDate.of(2026, 1, 10);

            // Block time: Jan 1 to Jan 15 (completely contains query range)
            BlockTime blockTime = createBlockTime(resourceId, "Overlapping Block",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15));

            repository.save(blockTime);
            entityManager.flush();
            entityManager.clear();

            // When
            List<BlockTime> result = repository.findActiveBlockTimesInRange(resourceId, startDate, endDate);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Overlapping Block");
        }

        @Test
        @DisplayName("finds block times that exactly match the range boundaries")
        void findsExactBoundaryMatches() {
            // Given
            Long resourceId = 1L;
            // Query range: Jan 1 to Jan 5
            LocalDate startDate = LocalDate.of(2026, 1, 1);
            LocalDate endDate = LocalDate.of(2026, 1, 5);

            // Block time exactly matches query range
            BlockTime blockTime = createBlockTime(resourceId, "Exact Match",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));

            repository.save(blockTime);
            entityManager.flush();
            entityManager.clear();

            // When
            List<BlockTime> result = repository.findActiveBlockTimesInRange(resourceId, startDate, endDate);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Exact Match");
        }

        @Test
        @DisplayName("finds block times that contain the query range")
        void findsBlockTimesContainingRange() {
            // Given
            Long resourceId = 1L;
            // Query range: Jan 5 to Jan 10
            LocalDate startDate = LocalDate.of(2026, 1, 5);
            LocalDate endDate = LocalDate.of(2026, 1, 10);

            // Block time: Jan 1 to Jan 20 (completely contains query range)
            BlockTime blockTime = createBlockTime(resourceId, "Container Block",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 20));

            repository.save(blockTime);
            entityManager.flush();
            entityManager.clear();

            // When
            List<BlockTime> result = repository.findActiveBlockTimesInRange(resourceId, startDate, endDate);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Container Block");
        }

        @Test
        @DisplayName("finds block times contained within the query range")
        void findsBlockTimesContainedInRange() {
            // Given
            Long resourceId = 1L;
            // Query range: Jan 1 to Jan 20
            LocalDate startDate = LocalDate.of(2026, 1, 1);
            LocalDate endDate = LocalDate.of(2026, 1, 20);

            // Block time: Jan 5 to Jan 10 (contained within query range)
            BlockTime blockTime = createBlockTime(resourceId, "Contained Block",
                    LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 10));

            repository.save(blockTime);
            entityManager.flush();
            entityManager.clear();

            // When
            List<BlockTime> result = repository.findActiveBlockTimesInRange(resourceId, startDate, endDate);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Contained Block");
        }

        @Test
        @DisplayName("finds block times that start exactly at end date (adjacent)")
        void findsBlockTimesStartingAtEndDate() {
            // Given
            Long resourceId = 1L;
            // Query range: Jan 10 to Jan 10 (single day)
            LocalDate startDate = LocalDate.of(2026, 1, 10);
            LocalDate endDate = LocalDate.of(2026, 1, 10);

            // Block time: Jan 1 to Jan 10 (ends exactly at query start date)
            // This should be considered overlapping based on inclusive range check
            BlockTime blockTime = createBlockTime(resourceId, "Adjacent Block",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10));

            repository.save(blockTime);
            entityManager.flush();
            entityManager.clear();

            // When
            List<BlockTime> result = repository.findActiveBlockTimesInRange(resourceId, startDate, endDate);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Adjacent Block");
        }

        @Test
        @DisplayName("returns empty list when no overlapping block times exist")
        void returnsEmptyListWhenNoOverlap() {
            // Given
            Long resourceId = 1L;
            // Query range: Jan 20 to Jan 25
            LocalDate startDate = LocalDate.of(2026, 1, 20);
            LocalDate endDate = LocalDate.of(2026, 1, 25);

            // Block time: Jan 1 to Jan 5 (no overlap)
            BlockTime blockTime = createBlockTime(resourceId, "Non-overlapping Block",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));

            repository.save(blockTime);
            entityManager.flush();
            entityManager.clear();

            // When
            List<BlockTime> result = repository.findActiveBlockTimesInRange(resourceId, startDate, endDate);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty list when checking different resource")
        void returnsEmptyListForDifferentResource() {
            // Given
            Long resourceId1 = 1L;
            Long resourceId2 = 2L;
            // Query range: Jan 5 to Jan 10
            LocalDate startDate = LocalDate.of(2026, 1, 5);
            LocalDate endDate = LocalDate.of(2026, 1, 10);

            // Block time on different resource
            BlockTime blockTime = createBlockTime(resourceId1, "Different Resource Block",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15));

            repository.save(blockTime);
            entityManager.flush();
            entityManager.clear();

            // When
            List<BlockTime> result = repository.findActiveBlockTimesInRange(resourceId2, startDate, endDate);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("finds multiple overlapping block times")
        void findsMultipleOverlappingBlockTimes() {
            // Given
            Long resourceId = 1L;
            // Query range: Jan 5 to Jan 15
            LocalDate startDate = LocalDate.of(2026, 1, 5);
            LocalDate endDate = LocalDate.of(2026, 1, 15);

            BlockTime blockTime1 = createBlockTime(resourceId, "Overlap 1",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10));
            BlockTime blockTime2 = createBlockTime(resourceId, "Overlap 2",
                    LocalDate.of(2026, 1, 12), LocalDate.of(2026, 1, 20));
            BlockTime blockTime3 = createBlockTime(resourceId, "No Overlap",
                    LocalDate.of(2026, 1, 20), LocalDate.of(2026, 1, 25));

            repository.saveAll(List.of(blockTime1, blockTime2, blockTime3));
            entityManager.flush();
            entityManager.clear();

            // When
            List<BlockTime> result = repository.findActiveBlockTimesInRange(resourceId, startDate, endDate);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.stream().map(BlockTime::getTitle).toList())
                    .containsExactlyInAnyOrder("Overlap 1", "Overlap 2");
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @BeforeEach
        void setUpData() {
            entityManager.clear();
        }

        @Test
        @DisplayName("returns block time when it exists")
        void returnsBlockTimeWhenExists() {
            // Given
            BlockTime blockTime = createBlockTime(1L, "Test Block",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));
            repository.save(blockTime);
            entityManager.flush();
            entityManager.clear();

            Long blockTimeId = blockTime.getId();

            // When
            Optional<BlockTime> result = repository.findById(blockTimeId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getTitle()).isEqualTo("Test Block");
        }

        @Test
        @DisplayName("returns empty optional when block time does not exist")
        void returnsEmptyOptionalWhenNotFound() {
            // Given
            Long blockTimeId = 999L;

            // When
            Optional<BlockTime> result = repository.findById(blockTimeId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("creates new block time with auto-generated ID")
        void createsNewBlockTime() {
            // Given
            BlockTime blockTime = createBlockTime(1L, "New Block",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));

            // When
            BlockTime saved = repository.save(blockTime);
            entityManager.flush();
            entityManager.clear();

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getTitle()).isEqualTo("New Block");
        }

        @Test
        @DisplayName("updates existing block time")
        void updatesExistingBlockTime() {
            // Given
            BlockTime blockTime = createBlockTime(1L, "Original Title",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));
            repository.save(blockTime);
            entityManager.flush();
            entityManager.clear();

            BlockTime updated = repository.findById(blockTime.getId()).orElseThrow();
            updated.setTitle("Updated Title");
            entityManager.flush();
            entityManager.clear();

            // When
            BlockTime result = repository.findById(updated.getId()).orElseThrow();

            // Then
            assertThat(result.getTitle()).isEqualTo("Updated Title");
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @BeforeEach
        void setUpData() {
            entityManager.clear();
        }

        @Test
        @DisplayName("deletes block time by ID")
        void deletesBlockTime() {
            // Given
            BlockTime blockTime = createBlockTime(1L, "To Delete",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));
            repository.save(blockTime);
            entityManager.flush();
            entityManager.clear();

            Long blockTimeId = blockTime.getId();

            // When
            repository.deleteById(blockTimeId);
            entityManager.flush();
            entityManager.clear();

            // Then
            Optional<BlockTime> result = repository.findById(blockTimeId);
            assertThat(result).isEmpty();
        }
    }

    private BlockTime createBlockTime(Long resourceId, String title, LocalDate startDate, LocalDate endDate) {
        BlockTime blockTime = new BlockTime();
        blockTime.setResourceId(resourceId);
        blockTime.setTitle(title);
        blockTime.setDescription("Test description");
        blockTime.setStartDate(startDate);
        blockTime.setEndDate(endDate);
        blockTime.setBlocked(true);
        return blockTime;
    }
}

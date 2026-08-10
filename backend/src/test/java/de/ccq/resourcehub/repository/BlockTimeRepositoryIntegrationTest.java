package de.ccq.resourcehub.repository;

import static org.assertj.core.api.Assertions.assertThat;

import de.ccq.resourcehub.entity.BlockTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration test for BlockTimeRepository using Testcontainers for PostgreSQL.
 * Tests repository queries with a real database.
 */
@Testcontainers
@SpringJUnitConfig(BlockTimeRepositoryIntegrationTest.TestConfig.class)
@DisplayName("BlockTimeRepository Integration Test (Testcontainers)")
class BlockTimeRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
    }

    @Configuration
    static class TestConfig {
        @Bean
        public BlockTimeRepository blockTimeRepository() {
            return new BlockTimeRepository();
        }
    }

    @BeforeEach
    void setUp() {
        // Database is auto-created by Testcontainers
    }

    @AfterEach
    void tearDown() {
        // Database is auto-cleaned by Testcontainers
    }

    @Nested
    @DisplayName("findBlockTimesByResourceId")
    class FindBlockTimesByResourceId {

        @Test
        @DisplayName("returns all block times for a resource")
        void returnsAllBlockTimesForResource() {
            // Given
            BlockTimeRepository repository = new BlockTimeRepository();

            BlockTime blockTime1 = new BlockTime();
            blockTime1.setResourceId(1L);
            blockTime1.setTitle("Block 1");
            blockTime1.setStartDate(LocalDate.of(2026, 1, 1));
            blockTime1.setEndDate(LocalDate.of(2026, 1, 5));
            blockTime1.setBlocked(true);

            BlockTime blockTime2 = new BlockTime();
            blockTime2.setResourceId(1L);
            blockTime2.setTitle("Block 2");
            blockTime2.setStartDate(LocalDate.of(2026, 2, 1));
            blockTime2.setEndDate(LocalDate.of(2026, 2, 10));
            blockTime2.setBlocked(true);

            repository.save(blockTime1);
            repository.save(blockTime2);

            // When
            List<BlockTime> result = repository.findBlockTimesByResourceId(1L);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(BlockTime::getTitle).containsExactlyInAnyOrder("Block 1", "Block 2");
        }

        @Test
        @DisplayName("returns empty list when no block times exist for resource")
        void returnsEmptyListWhenNoBlockTimes() {
            // Given
            BlockTimeRepository repository = new BlockTimeRepository();

            // When
            List<BlockTime> result = repository.findBlockTimesByResourceId(999L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns block times sorted by start date")
        void returnsBlockTimesSortedByStartDate() {
            // Given
            BlockTimeRepository repository = new BlockTimeRepository();

            BlockTime earlier = new BlockTime();
            earlier.setResourceId(1L);
            earlier.setTitle("Earlier Block");
            earlier.setStartDate(LocalDate.of(2026, 1, 1));
            earlier.setEndDate(LocalDate.of(2026, 1, 5));
            earlier.setBlocked(true);

            BlockTime later = new BlockTime();
            later.setResourceId(1L);
            later.setTitle("Later Block");
            later.setStartDate(LocalDate.of(2026, 2, 1));
            later.setEndDate(LocalDate.of(2026, 2, 10));
            later.setBlocked(true);

            repository.save(earlier);
            repository.save(later);

            // When
            List<BlockTime> result = repository.findBlockTimesByResourceId(1L);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getTitle()).isEqualTo("Earlier Block");
            assertThat(result.get(1).getTitle()).isEqualTo("Later Block");
        }
    }

    @Nested
    @DisplayName("hasOverlappingBlockTimes")
    class HasOverlappingBlockTimes {

        @Test
        @DisplayName("returns true when there is a complete overlap")
        void returnsTrueForCompleteOverlap() {
            // Given - block time: Jan 1-5
            // Query: Jan 2-3 (completely inside)
            BlockTimeRepository repository = new BlockTimeRepository();

            BlockTime blockTime = new BlockTime();
            blockTime.setResourceId(1L);
            blockTime.setTitle("Test Block");
            blockTime.setStartDate(LocalDate.of(2026, 1, 1));
            blockTime.setEndDate(LocalDate.of(2026, 1, 5));
            blockTime.setBlocked(true);
            repository.save(blockTime);

            LocalDate queryStart = LocalDate.of(2026, 1, 2);
            LocalDate queryEnd = LocalDate.of(2026, 1, 3);

            // When
            boolean result = repository.hasOverlappingBlockTimes(1L, queryStart, queryEnd);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("returns true when there is partial overlap at start")
        void returnsTrueForPartialOverlapAtStart() {
            // Given - block time: Jan 1-5
            // Query: Jan 3-7 (partial overlap)
            BlockTimeRepository repository = new BlockTimeRepository();

            BlockTime blockTime = new BlockTime();
            blockTime.setResourceId(1L);
            blockTime.setTitle("Test Block");
            blockTime.setStartDate(LocalDate.of(2026, 1, 1));
            blockTime.setEndDate(LocalDate.of(2026, 1, 5));
            blockTime.setBlocked(true);
            repository.save(blockTime);

            LocalDate queryStart = LocalDate.of(2026, 1, 3);
            LocalDate queryEnd = LocalDate.of(2026, 1, 7);

            // When
            boolean result = repository.hasOverlappingBlockTimes(1L, queryStart, queryEnd);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("returns true when there is partial overlap at end")
        void returnsTrueForPartialOverlapAtEnd() {
            // Given - block time: Jan 1-5
            // Query: Jan 3-3 (partial overlap)
            BlockTimeRepository repository = new BlockTimeRepository();

            BlockTime blockTime = new BlockTime();
            blockTime.setResourceId(1L);
            blockTime.setTitle("Test Block");
            blockTime.setStartDate(LocalDate.of(2026, 1, 1));
            blockTime.setEndDate(LocalDate.of(2026, 1, 5));
            blockTime.setBlocked(true);
            repository.save(blockTime);

            LocalDate queryStart = LocalDate.of(2026, 1, 3);
            LocalDate queryEnd = LocalDate.of(2026, 1, 3);

            // When
            boolean result = repository.hasOverlappingBlockTimes(1L, queryStart, queryEnd);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("returns false when no overlap - query ends before block starts")
        void returnsFalseWhenQueryEndsBeforeBlockStarts() {
            // Given - block time: Jan 1-5
            // Query: Dec 20-28 (ends before block starts)
            BlockTimeRepository repository = new BlockTimeRepository();

            BlockTime blockTime = new BlockTime();
            blockTime.setResourceId(1L);
            blockTime.setTitle("Test Block");
            blockTime.setStartDate(LocalDate.of(2026, 1, 1));
            blockTime.setEndDate(LocalDate.of(2026, 1, 5));
            blockTime.setBlocked(true);
            repository.save(blockTime);

            LocalDate queryStart = LocalDate.of(2025, 12, 20);
            LocalDate queryEnd = LocalDate.of(2025, 12, 28);

            // When
            boolean result = repository.hasOverlappingBlockTimes(1L, queryStart, queryEnd);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when no overlap - query starts after block ends")
        void returnsFalseWhenQueryStartsAfterBlockEnds() {
            // Given - block time: Jan 1-5
            // Query: Jan 6-10 (starts after block ends)
            BlockTimeRepository repository = new BlockTimeRepository();

            BlockTime blockTime = new BlockTime();
            blockTime.setResourceId(1L);
            blockTime.setTitle("Test Block");
            blockTime.setStartDate(LocalDate.of(2026, 1, 1));
            blockTime.setEndDate(LocalDate.of(2026, 1, 5));
            blockTime.setBlocked(true);
            repository.save(blockTime);

            LocalDate queryStart = LocalDate.of(2026, 1, 6);
            LocalDate queryEnd = LocalDate.of(2026, 1, 10);

            // When
            boolean result = repository.hasOverlappingBlockTimes(1L, queryStart, queryEnd);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false for different resource")
        void returnsFalseForDifferentResource() {
            // Given - block time for resource 1
            // Query for resource 2 (different resource)
            BlockTimeRepository repository = new BlockTimeRepository();

            BlockTime blockTime = new BlockTime();
            blockTime.setResourceId(1L);
            blockTime.setTitle("Test Block");
            blockTime.setStartDate(LocalDate.of(2026, 1, 1));
            blockTime.setEndDate(LocalDate.of(2026, 1, 5));
            blockTime.setBlocked(true);
            repository.save(blockTime);

            LocalDate queryStart = LocalDate.of(2026, 1, 2);
            LocalDate queryEnd = LocalDate.of(2026, 1, 3);

            // When
            boolean result = repository.hasOverlappingBlockTimes(2L, queryStart, queryEnd);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when block time exactly matches query (no gap)")
        void returnsTrueWhenBlockTimeMatchesQueryExactly() {
            // Given - block time: Jan 1-5
            // Query: Jan 1-5 (exact match)
            BlockTimeRepository repository = new BlockTimeRepository();

            BlockTime blockTime = new BlockTime();
            blockTime.setResourceId(1L);
            blockTime.setTitle("Test Block");
            blockTime.setStartDate(LocalDate.of(2026, 1, 1));
            blockTime.setEndDate(LocalDate.of(2026, 1, 5));
            blockTime.setBlocked(true);
            repository.save(blockTime);

            LocalDate queryStart = LocalDate.of(2026, 1, 1);
            LocalDate queryEnd = LocalDate.of(2026, 1, 5);

            // When
            boolean result = repository.hasOverlappingBlockTimes(1L, queryStart, queryEnd);

            // Then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("returns Optional with block time when exists")
        void returnsOptionalWithBlockTimeWhenExists() {
            // Given
            BlockTimeRepository repository = new BlockTimeRepository();

            BlockTime blockTime = new BlockTime();
            blockTime.setResourceId(1L);
            blockTime.setTitle("Test Block");
            blockTime.setStartDate(LocalDate.of(2026, 1, 1));
            blockTime.setEndDate(LocalDate.of(2026, 1, 5));
            blockTime.setBlocked(true);
            BlockTime saved = repository.save(blockTime);

            // When
            Optional<BlockTime> result = repository.findById(saved.getId());

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getTitle()).isEqualTo("Test Block");
        }

        @Test
        @DisplayName("returns empty Optional when block time does not exist")
        void returnsEmptyOptionalWhenDoesNotExist() {
            // Given
            BlockTimeRepository repository = new BlockTimeRepository();

            // When
            Optional<BlockTime> result = repository.findById(999L);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("persists new block time and returns saved entity")
        void persistsNewBlockTime() {
            // Given
            BlockTimeRepository repository = new BlockTimeRepository();

            BlockTime newBlockTime = new BlockTime();
            newBlockTime.setResourceId(2L);
            newBlockTime.setTitle("New Block");
            newBlockTime.setStartDate(LocalDate.of(2026, 3, 1));
            newBlockTime.setEndDate(LocalDate.of(2026, 3, 10));
            newBlockTime.setBlocked(true);

            // When
            BlockTime saved = repository.save(newBlockTime);

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getTitle()).isEqualTo("New Block");
            assertThat(saved.getResourceId()).isEqualTo(2L);

            // Verify in database
            BlockTime fetched = repository.findById(saved.getId()).orElse(null);
            assertThat(fetched).isNotNull();
            assertThat(fetched.getTitle()).isEqualTo("New Block");
        }

        @Test
        @DisplayName("updates existing block time")
        void updatesExistingBlockTime() {
            // Given
            BlockTimeRepository repository = new BlockTimeRepository();

            BlockTime blockTime = new BlockTime();
            blockTime.setResourceId(1L);
            blockTime.setTitle("Original Title");
            blockTime.setStartDate(LocalDate.of(2026, 1, 1));
            blockTime.setEndDate(LocalDate.of(2026, 1, 5));
            blockTime.setBlocked(true);
            BlockTime saved = repository.save(blockTime);

            // Update
            saved.setTitle("Updated Title");
            saved.setDescription("Updated description");

            // When
            BlockTime updated = repository.save(saved);

            // Then
            assertThat(updated.getTitle()).isEqualTo("Updated Title");
            assertThat(updated.getDescription()).isEqualTo("Updated description");

            // Verify in database
            BlockTime fetched = repository.findById(saved.getId()).orElse(null);
            assertThat(fetched).isNotNull();
            assertThat(fetched.getTitle()).isEqualTo("Updated Title");
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("removes block time from database")
        void removesBlockTime() {
            // Given
            BlockTimeRepository repository = new BlockTimeRepository();

            BlockTime blockTime = new BlockTime();
            blockTime.setResourceId(1L);
            blockTime.setTitle("Test Block");
            blockTime.setStartDate(LocalDate.of(2026, 1, 1));
            blockTime.setEndDate(LocalDate.of(2026, 1, 5));
            blockTime.setBlocked(true);
            BlockTime saved = repository.save(blockTime);
            assertThat(repository.findById(saved.getId())).isPresent();

            // When
            repository.delete(saved);

            // Then
            assertThat(repository.findById(saved.getId())).isEmpty();
        }
    }
}
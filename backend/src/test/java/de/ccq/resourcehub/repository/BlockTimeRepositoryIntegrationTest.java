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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

/**
 * Integration test for BlockTimeRepository using TestEntityManager.
 * Tests repository queries and database operations without full Spring context.
 */
@DataJpaTest
@DisplayName("BlockTimeRepository Integration Test")
class BlockTimeRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BlockTimeRepository repository;

    private BlockTime testBlockTime;

    @BeforeEach
    void setUp() {
        testBlockTime = new BlockTime();
        testBlockTime.setResourceId(1L);
        testBlockTime.setTitle("Test Block");
        testBlockTime.setDescription("Test description");
        testBlockTime.setStartDate(LocalDate.of(2026, 1, 1));
        testBlockTime.setEndDate(LocalDate.of(2026, 1, 5));
        testBlockTime.setBlocked(true);
        testBlockTime = entityManager.persistAndFlush(testBlockTime);
    }

    @AfterEach
    void tearDown() {
        entityManager.clear();
    }

    @Nested
    @DisplayName("findBlockTimesByResourceId")
    class FindBlockTimesByResourceId {

        @Test
        @DisplayName("returns all block times for a resource")
        void returnsAllBlockTimesForResource() {
            // Given
            entityManager.persistAndFlush(new BlockTime() {{
                setResourceId(1L);
                setTitle("Another Block");
                setStartDate(LocalDate.of(2026, 2, 1));
                setEndDate(LocalDate.of(2026, 2, 10));
                setBlocked(true);
            }});

            // When
            List<BlockTime> result = repository.findBlockTimesByResourceId(1L);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(BlockTime::getTitle).containsExactlyInAnyOrder("Test Block", "Another Block");
        }

        @Test
        @DisplayName("returns empty list when no block times exist for resource")
        void returnsEmptyListWhenNoBlockTimes() {
            // When
            List<BlockTime> result = repository.findBlockTimesByResourceId(999L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns block times sorted by start date")
        void returnsBlockTimesSortedByStartDate() {
            // Given
            BlockTime earlier = new BlockTime() {{
                setResourceId(1L);
                setTitle("Earlier Block");
                setStartDate(LocalDate.of(2026, 1, 1));
                setEndDate(LocalDate.of(2026, 1, 5));
                setBlocked(true);
            }};
            BlockTime later = new BlockTime() {{
                setResourceId(1L);
                setTitle("Later Block");
                setStartDate(LocalDate.of(2026, 2, 1));
                setEndDate(LocalDate.of(2026, 2, 10));
                setBlocked(true);
            }};

            entityManager.persist(earlier);
            entityManager.persist(later);
            entityManager.flush();

            // When
            List<BlockTime> result = repository.findBlockTimesByResourceId(1L);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getTitle()).isEqualTo("Test Block");
            assertThat(result.get(1).getTitle()).isEqualTo("Earlier Block");
            assertThat(result.get(2).getTitle()).isEqualTo("Later Block");
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
            Long id = testBlockTime.getId();

            // When
            Optional<BlockTime> result = repository.findById(id);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getTitle()).isEqualTo("Test Block");
        }

        @Test
        @DisplayName("returns empty Optional when block time does not exist")
        void returnsEmptyOptionalWhenDoesNotExist() {
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
            BlockTime newBlockTime = new BlockTime();
            newBlockTime.setResourceId(2L);
            newBlockTime.setTitle("New Block");
            newBlockTime.setStartDate(LocalDate.of(2026, 3, 1));
            newBlockTime.setEndDate(LocalDate.of(2026, 3, 10));
            newBlockTime.setBlocked(true);

            // When
            BlockTime saved = repository.save(newBlockTime);
            entityManager.clear();

            BlockTime fetched = repository.findById(saved.getId()).orElse(null);

            // Then
            assertThat(fetched).isNotNull();
            assertThat(fetched.getTitle()).isEqualTo("New Block");
            assertThat(fetched.getResourceId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("updates existing block time")
        void updatesExistingBlockTime() {
            // Given
            testBlockTime.setTitle("Updated Title");
            testBlockTime.setDescription("Updated description");

            // When
            BlockTime updated = repository.save(testBlockTime);
            entityManager.clear();

            BlockTime fetched = repository.findById(testBlockTime.getId()).orElse(null);

            // Then
            assertThat(fetched).isNotNull();
            assertThat(fetched.getTitle()).isEqualTo("Updated Title");
            assertThat(fetched.getDescription()).isEqualTo("Updated description");
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("removes block time from database")
        void removesBlockTime() {
            // Given
            Long id = testBlockTime.getId();
            assertThat(repository.findById(id)).isPresent();

            // When
            repository.delete(testBlockTime);

            // Then
            assertThat(repository.findById(id)).isEmpty();
        }
    }
}
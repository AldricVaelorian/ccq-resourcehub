package de.ccq.resourcehub.repository;

import static org.assertj.core.api.Assertions.assertThat;

import de.ccq.resourcehub.entity.BlockTime;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@DisplayName("BlockTimeRepository Integration")
class BlockTimeRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private BlockTimeRepository blockTimeRepository;

    @Nested
    @DisplayName("findByResourceIdOrderByStartDateAsc")
    class FindByResourceIdOrderByStartDateAsc {

        @Test
        @DisplayName("returns block times ordered by start date")
        void returnsBlockTimesOrderedByStartDate() {
            // Given
            Long resourceId = 1L;
            BlockTime blockTime3 = new BlockTime();
            blockTime3.setResourceId(resourceId);
            blockTime3.setTitle("Block 3");
            blockTime3.setDescription("Description");
            blockTime3.setStartDate(LocalDate.of(2026, 1, 15));
            blockTime3.setEndDate(LocalDate.of(2026, 1, 20));
            blockTime3.setBlocked(true);

            BlockTime blockTime1 = new BlockTime();
            blockTime1.setResourceId(resourceId);
            blockTime1.setTitle("Block 1");
            blockTime1.setDescription("Description");
            blockTime1.setStartDate(LocalDate.of(2026, 1, 1));
            blockTime1.setEndDate(LocalDate.of(2026, 1, 5));
            blockTime1.setBlocked(true);

            BlockTime blockTime2 = new BlockTime();
            blockTime2.setResourceId(resourceId);
            blockTime2.setTitle("Block 2");
            blockTime2.setDescription("Description");
            blockTime2.setStartDate(LocalDate.of(2026, 1, 10));
            blockTime2.setEndDate(LocalDate.of(2026, 1, 15));
            blockTime2.setBlocked(true);

            testEntityManager.persistAndFlush(blockTime3);
            testEntityManager.persistAndFlush(blockTime1);
            testEntityManager.persistAndFlush(blockTime2);
            testEntityManager.clear();

            // When
            List<BlockTime> result = blockTimeRepository.findByResourceIdOrderByStartDateAsc(resourceId);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getTitle()).isEqualTo("Block 1");
            assertThat(result.get(1).getTitle()).isEqualTo("Block 2");
            assertThat(result.get(2).getTitle()).isEqualTo("Block 3");
        }

        @Test
        @DisplayName("returns empty list when no block times exist for resource")
        void returnsEmptyListWhenNoBlockTimes() {
            // Given
            Long resourceId = 999L;

            // When
            List<BlockTime> result = blockTimeRepository.findByResourceIdOrderByStartDateAsc(resourceId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns block times only for specified resource")
        void returnsBlockTimesOnlyForSpecifiedResource() {
            // Given
            Long resourceId1 = 1L;
            Long resourceId2 = 2L;

            BlockTime blockTime1 = new BlockTime();
            blockTime1.setResourceId(resourceId1);
            blockTime1.setTitle("Block for resource 1");
            blockTime1.setDescription("Description");
            blockTime1.setStartDate(LocalDate.of(2026, 1, 1));
            blockTime1.setEndDate(LocalDate.of(2026, 1, 5));
            blockTime1.setBlocked(true);

            BlockTime blockTime2 = new BlockTime();
            blockTime2.setResourceId(resourceId2);
            blockTime2.setTitle("Block for resource 2");
            blockTime2.setDescription("Description");
            blockTime2.setStartDate(LocalDate.of(2026, 1, 1));
            blockTime2.setEndDate(LocalDate.of(2026, 1, 5));
            blockTime2.setBlocked(true);

            testEntityManager.persistAndFlush(blockTime1);
            testEntityManager.persistAndFlush(blockTime2);
            testEntityManager.clear();

            // When
            List<BlockTime> result1 = blockTimeRepository.findByResourceIdOrderByStartDateAsc(resourceId1);
            List<BlockTime> result2 = blockTimeRepository.findByResourceIdOrderByStartDateAsc(resourceId2);

            // Then
            assertThat(result1).hasSize(1);
            assertThat(result1.get(0).getTitle()).isEqualTo("Block for resource 1");

            assertThat(result2).hasSize(1);
            assertThat(result2.get(0).getTitle()).isEqualTo("Block for resource 2");
        }
    }

    @Nested
    @DisplayName("findActiveBlockTimesInRange")
    class FindActiveBlockTimesInRange {

        @Test
        @DisplayName("finds block times that overlap with the given range")
        void findsOverlappingBlockTimes() {
            // Given
            Long resourceId = 1L;
            LocalDate startDate = LocalDate.of(2026, 1, 5);
            LocalDate endDate = LocalDate.of(2026, 1, 10);

            BlockTime overlappingBlockTime = new BlockTime();
            overlappingBlockTime.setResourceId(resourceId);
            overlappingBlockTime.setTitle("Overlapping Block");
            overlappingBlockTime.setDescription("Description");
            overlappingBlockTime.setStartDate(LocalDate.of(2026, 1, 1));
            overlappingBlockTime.setEndDate(LocalDate.of(2026, 1, 15));
            overlappingBlockTime.setBlocked(true);

            BlockTime nonOverlappingBlockTime = new BlockTime();
            nonOverlappingBlockTime.setResourceId(resourceId);
            nonOverlappingBlockTime.setTitle("Non Overlapping Block");
            nonOverlappingBlockTime.setDescription("Description");
            nonOverlappingBlockTime.setStartDate(LocalDate.of(2026, 2, 1));
            nonOverlappingBlockTime.setEndDate(LocalDate.of(2026, 2, 15));
            nonOverlappingBlockTime.setBlocked(true);

            testEntityManager.persistAndFlush(overlappingBlockTime);
            testEntityManager.persistAndFlush(nonOverlappingBlockTime);
            testEntityManager.clear();

            // When
            List<BlockTime> result = blockTimeRepository.findActiveBlockTimesInRange(resourceId, startDate, endDate);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Overlapping Block");
        }

        @Test
        @DisplayName("finds block times that exactly match the range boundaries")
        void findsBlockTimesMatchingBoundaries() {
            // Given
            Long resourceId = 1L;
            LocalDate startDate = LocalDate.of(2026, 1, 1);
            LocalDate endDate = LocalDate.of(2026, 1, 5);

            BlockTime exactMatchBlockTime = new BlockTime();
            exactMatchBlockTime.setResourceId(resourceId);
            exactMatchBlockTime.setTitle("Exact Match");
            exactMatchBlockTime.setDescription("Description");
            exactMatchBlockTime.setStartDate(startDate);
            exactMatchBlockTime.setEndDate(endDate);
            exactMatchBlockTime.setBlocked(true);

            testEntityManager.persistAndFlush(exactMatchBlockTime);
            testEntityManager.clear();

            // When
            List<BlockTime> result = blockTimeRepository.findActiveBlockTimesInRange(resourceId, startDate, endDate);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Exact Match");
        }

        @Test
        @DisplayName("finds block times that contain the range")
        void findsBlockTimesContainingRange() {
            // Given
            Long resourceId = 1L;
            LocalDate startDate = LocalDate.of(2026, 1, 10);
            LocalDate endDate = LocalDate.of(2026, 1, 15);

            BlockTime containingBlockTime = new BlockTime();
            containingBlockTime.setResourceId(resourceId);
            containingBlockTime.setTitle("Containing Block");
            containingBlockTime.setDescription("Description");
            containingBlockTime.setStartDate(LocalDate.of(2026, 1, 1));
            containingBlockTime.setEndDate(LocalDate.of(2026, 1, 30));
            containingBlockTime.setBlocked(true);

            testEntityManager.persistAndFlush(containingBlockTime);
            testEntityManager.clear();

            // When
            List<BlockTime> result = blockTimeRepository.findActiveBlockTimesInRange(resourceId, startDate, endDate);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Containing Block");
        }

        @Test
        @DisplayName("finds block times contained within the range")
        void findsBlockTimesContainedInRange() {
            // Given
            Long resourceId = 1L;
            LocalDate startDate = LocalDate.of(2026, 1, 1);
            LocalDate endDate = LocalDate.of(2026, 1, 30);

            BlockTime containedBlockTime = new BlockTime();
            containedBlockTime.setResourceId(resourceId);
            containedBlockTime.setTitle("Contained Block");
            containedBlockTime.setDescription("Description");
            containedBlockTime.setStartDate(LocalDate.of(2026, 1, 10));
            containedBlockTime.setEndDate(LocalDate.of(2026, 1, 20));
            containedBlockTime.setBlocked(true);

            testEntityManager.persistAndFlush(containedBlockTime);
            testEntityManager.clear();

            // When
            List<BlockTime> result = blockTimeRepository.findActiveBlockTimesInRange(resourceId, startDate, endDate);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Contained Block");
        }

        @Test
        @DisplayName("finds block times that start exactly at end date")
        void findsBlockTimesStartingAtEndDate() {
            // Given
            Long resourceId = 1L;
            LocalDate startDate = LocalDate.of(2026, 1, 10);
            LocalDate endDate = LocalDate.of(2026, 1, 10);

            BlockTime adjacentBlockTime = new BlockTime();
            adjacentBlockTime.setResourceId(resourceId);
            adjacentBlockTime.setTitle("Adjacent Block");
            adjacentBlockTime.setDescription("Description");
            adjacentBlockTime.setStartDate(LocalDate.of(2026, 1, 1));
            adjacentBlockTime.setEndDate(LocalDate.of(2026, 1, 10));
            adjacentBlockTime.setBlocked(true);

            testEntityManager.persistAndFlush(adjacentBlockTime);
            testEntityManager.clear();

            // When
            List<BlockTime> result = blockTimeRepository.findActiveBlockTimesInRange(resourceId, startDate, endDate);

            // Then
            // Adjacent dates should be considered overlapping
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Adjacent Block");
        }

        @Test
        @DisplayName("returns empty list when no overlapping block times exist")
        void returnsEmptyListWhenNoOverlappingBlockTimes() {
            // Given
            Long resourceId = 1L;
            LocalDate startDate = LocalDate.of(2026, 2, 1);
            LocalDate endDate = LocalDate.of(2026, 2, 15);

            BlockTime nonOverlappingBlockTime = new BlockTime();
            nonOverlappingBlockTime.setResourceId(resourceId);
            nonOverlappingBlockTime.setTitle("Non Overlapping");
            nonOverlappingBlockTime.setDescription("Description");
            nonOverlappingBlockTime.setStartDate(LocalDate.of(2026, 1, 1));
            nonOverlappingBlockTime.setEndDate(LocalDate.of(2026, 1, 15));
            nonOverlappingBlockTime.setBlocked(true);

            testEntityManager.persistAndFlush(nonOverlappingBlockTime);
            testEntityManager.clear();

            // When
            List<BlockTime> result = blockTimeRepository.findActiveBlockTimesInRange(resourceId, startDate, endDate);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty list when checking different resource")
        void returnsEmptyListForDifferentResource() {
            // Given
            Long resourceId1 = 1L;
            Long resourceId2 = 2L;
            LocalDate startDate = LocalDate.of(2026, 1, 5);
            LocalDate endDate = LocalDate.of(2026, 1, 10);

            BlockTime blockTimeForResource1 = new BlockTime();
            blockTimeForResource1.setResourceId(resourceId1);
            blockTimeForResource1.setTitle("Block for resource 1");
            blockTimeForResource1.setDescription("Description");
            blockTimeForResource1.setStartDate(LocalDate.of(2026, 1, 1));
            blockTimeForResource1.setEndDate(LocalDate.of(2026, 1, 15));
            blockTimeForResource1.setBlocked(true);

            testEntityManager.persistAndFlush(blockTimeForResource1);
            testEntityManager.clear();

            // When
            List<BlockTime> result = blockTimeRepository.findActiveBlockTimesInRange(resourceId2, startDate, endDate);

            // Then
            assertThat(result).isEmpty();
        }
    }
}
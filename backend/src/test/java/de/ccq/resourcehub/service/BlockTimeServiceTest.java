package de.ccq.resourcehub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import de.ccq.resourcehub.entity.BlockTime;
import de.ccq.resourcehub.repository.BlockTimeRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("BlockTimeService")
class BlockTimeServiceTest {

    @Mock
    private BlockTimeRepository blockTimeRepository;

    private BlockTimeService sut;

    @BeforeEach
    void setUp() {
        sut = new BlockTimeService(blockTimeRepository);
    }

    @Nested
    @DisplayName("getAllBlockTimesByResourceId")
    class GetAllBlockTimesByResourceId {

        @Test
        @DisplayName("returns all block times for a resource ordered by start date")
        void returnsAllBlockTimesForResource() {
            // Given
            Long resourceId = 1L;
            BlockTime blockTime1 = createBlockTime(1L, resourceId, "Block 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));
            BlockTime blockTime2 = createBlockTime(2L, resourceId, "Block 2", LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 15));

            when(blockTimeRepository.findByResourceIdOrderByStartDateAsc(resourceId))
                    .thenReturn(List.of(blockTime1, blockTime2));

            // When
            List<BlockTime> result = sut.getAllBlockTimesByResourceId(resourceId);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(1).getId()).isEqualTo(2L);

            verify(blockTimeRepository).findByResourceIdOrderByStartDateAsc(resourceId);
            verifyNoMoreInteractions(blockTimeRepository);
        }

        @Test
        @DisplayName("returns empty list when no block times exist for resource")
        void returnsEmptyListWhenNoBlockTimes() {
            // Given
            Long resourceId = 999L;
            when(blockTimeRepository.findByResourceIdOrderByStartDateAsc(resourceId)).thenReturn(List.of());

            // When
            List<BlockTime> result = sut.getAllBlockTimesByResourceId(resourceId);

            // Then
            assertThat(result).isEmpty();

            verify(blockTimeRepository).findByResourceIdOrderByStartDateAsc(resourceId);
            verifyNoMoreInteractions(blockTimeRepository);
        }
    }

    @Nested
    @DisplayName("getBlockTimeById")
    class GetBlockTimeById {

        @Test
        @DisplayName("returns block time when it exists")
        void returnsBlockTimeWhenExists() {
            // Given
            Long blockTimeId = 1L;
            BlockTime blockTime = createBlockTime(blockTimeId, 1L, "Test Block", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));

            when(blockTimeRepository.findById(blockTimeId)).thenReturn(Optional.of(blockTime));

            // When
            Optional<BlockTime> result = sut.getBlockTimeById(blockTimeId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(blockTimeId);

            verify(blockTimeRepository).findById(blockTimeId);
            verifyNoMoreInteractions(blockTimeRepository);
        }

        @Test
        @DisplayName("returns empty optional when block time does not exist")
        void returnsEmptyOptionalWhenNotFound() {
            // Given
            Long blockTimeId = 999L;
            when(blockTimeRepository.findById(blockTimeId)).thenReturn(Optional.empty());

            // When
            Optional<BlockTime> result = sut.getBlockTimeById(blockTimeId);

            // Then
            assertThat(result).isEmpty();

            verify(blockTimeRepository).findById(blockTimeId);
            verifyNoMoreInteractions(blockTimeRepository);
        }
    }

    @Nested
    @DisplayName("createBlockTime")
    class CreateBlockTime {

        @Test
        @DisplayName("creates and returns block time when valid")
        void createsBlockTimeWhenValid() {
            // Given
            BlockTime blockTime = createBlockTime(null, 1L, "New Block", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));
            BlockTime savedBlockTime = createBlockTime(1L, 1L, "New Block",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));

            when(blockTimeRepository.save(blockTime)).thenReturn(savedBlockTime);

            // When
            BlockTime result = sut.createBlockTime(blockTime);

            // Then
            assertThat(result).isSameAs(savedBlockTime);
            assertThat(result.getId()).isEqualTo(1L);

            verify(blockTimeRepository).save(blockTime);
            verifyNoMoreInteractions(blockTimeRepository);
        }

        @Test
        @DisplayName("throws IllegalArgumentException when start date is null")
        void throwsExceptionWhenStartDateIsNull() {
            // Given
            BlockTime blockTime = new BlockTime();
            blockTime.setResourceId(1L);
            blockTime.setTitle("Test");
            blockTime.setEndDate(LocalDate.of(2026, 1, 5));
            blockTime.setBlocked(true);

            // When/Then
            assertThatThrownBy(() -> sut.createBlockTime(blockTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Start date is required");

            verifyNoInteractions(blockTimeRepository);
        }

        @Test
        @DisplayName("throws IllegalArgumentException when end date is null")
        void throwsExceptionWhenEndDateIsNull() {
            // Given
            BlockTime blockTime = new BlockTime();
            blockTime.setResourceId(1L);
            blockTime.setTitle("Test");
            blockTime.setStartDate(LocalDate.of(2026, 1, 1));
            blockTime.setBlocked(true);

            // When/Then
            assertThatThrownBy(() -> sut.createBlockTime(blockTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("End date is required");

            verifyNoInteractions(blockTimeRepository);
        }

        @Test
        @DisplayName("throws IllegalArgumentException when start date is after end date")
        void throwsExceptionWhenStartDateAfterEndDate() {
            // Given
            BlockTime blockTime = new BlockTime();
            blockTime.setResourceId(1L);
            blockTime.setTitle("Test");
            blockTime.setStartDate(LocalDate.of(2026, 1, 10));
            blockTime.setEndDate(LocalDate.of(2026, 1, 5));
            blockTime.setBlocked(true);

            // When/Then
            assertThatThrownBy(() -> sut.createBlockTime(blockTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Start date must be before or equal to end date");

            verifyNoInteractions(blockTimeRepository);
        }

        @Test
        @DisplayName("throws IllegalArgumentException when title is null")
        void throwsExceptionWhenTitleIsNull() {
            // Given
            BlockTime blockTime = new BlockTime();
            blockTime.setResourceId(1L);
            blockTime.setStartDate(LocalDate.of(2026, 1, 1));
            blockTime.setEndDate(LocalDate.of(2026, 1, 5));
            blockTime.setBlocked(true);

            // When/Then
            assertThatThrownBy(() -> sut.createBlockTime(blockTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Title is required");

            verifyNoInteractions(blockTimeRepository);
        }

        @Test
        @DisplayName("throws IllegalArgumentException when title is empty")
        void throwsExceptionWhenTitleIsEmpty() {
            // Given
            BlockTime blockTime = new BlockTime();
            blockTime.setResourceId(1L);
            blockTime.setStartDate(LocalDate.of(2026, 1, 1));
            blockTime.setEndDate(LocalDate.of(2026, 1, 5));
            blockTime.setBlocked(true);
            blockTime.setTitle("");

            // When/Then
            assertThatThrownBy(() -> sut.createBlockTime(blockTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Title is required");

            verifyNoInteractions(blockTimeRepository);
        }

        @Test
        @DisplayName("throws IllegalArgumentException when title is whitespace only")
        void throwsExceptionWhenTitleIsWhitespace() {
            // Given
            BlockTime blockTime = new BlockTime();
            blockTime.setResourceId(1L);
            blockTime.setStartDate(LocalDate.of(2026, 1, 1));
            blockTime.setEndDate(LocalDate.of(2026, 1, 5));
            blockTime.setBlocked(true);
            blockTime.setTitle("   ");

            // When/Then
            assertThatThrownBy(() -> sut.createBlockTime(blockTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Title is required");

            verifyNoInteractions(blockTimeRepository);
        }
    }

    @Nested
    @DisplayName("updateBlockTime")
    class UpdateBlockTime {

        @Test
        @DisplayName("updates and returns block time when valid")
        void updatesBlockTimeWhenValid() {
            // Given
            Long blockTimeId = 1L;
            BlockTime updatedBlockTime = createBlockTime(blockTimeId, 1L, "New Title", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 10));

            when(blockTimeRepository.save(Mockito.any(BlockTime.class))).thenReturn(updatedBlockTime);

            // When
            BlockTime result = sut.updateBlockTime(blockTimeId, updatedBlockTime);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("New Title");

            verify(blockTimeRepository).save(Mockito.any(BlockTime.class));
            verifyNoMoreInteractions(blockTimeRepository);
        }

        @Test
        @DisplayName("throws IllegalArgumentException when start date is after end date")
        void throwsExceptionWhenStartDateAfterEndDate() {
            // Given
            Long blockTimeId = 1L;
            BlockTime blockTime = new BlockTime();
            blockTime.setId(blockTimeId);
            blockTime.setResourceId(1L);
            blockTime.setStartDate(LocalDate.of(2026, 1, 10));
            blockTime.setEndDate(LocalDate.of(2026, 1, 5));
            blockTime.setBlocked(true);

            // When/Then
            assertThatThrownBy(() -> sut.updateBlockTime(blockTimeId, blockTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Start date must be before or equal to end date");

            verifyNoInteractions(blockTimeRepository);
        }
    }

    @Nested
    @DisplayName("deleteBlockTime")
    class DeleteBlockTime {

        @Test
        @DisplayName("deletes block time by ID")
        void deletesBlockTime() {
            // Given
            Long blockTimeId = 1L;

            // When
            sut.deleteBlockTime(blockTimeId);

            // Then
            verify(blockTimeRepository).deleteById(blockTimeId);
            verifyNoMoreInteractions(blockTimeRepository);
        }
    }

    @Nested
    @DisplayName("hasOverlappingBlockTimes")
    class HasOverlappingBlockTimes {

        @Test
        @DisplayName("returns true when overlapping block times exist")
        void returnsTrueWhenOverlappingBlockTimesExist() {
            // Given
            Long resourceId = 1L;
            LocalDate startDate = LocalDate.of(2026, 1, 5);
            LocalDate endDate = LocalDate.of(2026, 1, 10);
            BlockTime overlappingBlockTime = createBlockTime(1L, resourceId, "Overlapping Block",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15));

            when(blockTimeRepository.findActiveBlockTimesInRange(resourceId, startDate, endDate))
                    .thenReturn(List.of(overlappingBlockTime));

            // When
            boolean result = sut.hasOverlappingBlockTimes(resourceId, startDate, endDate);

            // Then
            assertThat(result).isTrue();

            verify(blockTimeRepository).findActiveBlockTimesInRange(resourceId, startDate, endDate);
            verifyNoMoreInteractions(blockTimeRepository);
        }

        @Test
        @DisplayName("returns false when no overlapping block times exist")
        void returnsFalseWhenNoOverlappingBlockTimes() {
            // Given
            Long resourceId = 1L;
            LocalDate startDate = LocalDate.of(2026, 1, 20);
            LocalDate endDate = LocalDate.of(2026, 1, 25);

            when(blockTimeRepository.findActiveBlockTimesInRange(resourceId, startDate, endDate))
                    .thenReturn(List.of());

            // When
            boolean result = sut.hasOverlappingBlockTimes(resourceId, startDate, endDate);

            // Then
            assertThat(result).isFalse();

            verify(blockTimeRepository).findActiveBlockTimesInRange(resourceId, startDate, endDate);
            verifyNoMoreInteractions(blockTimeRepository);
        }

        @Test
        @DisplayName("correctly detects adjacent non-overlapping block times")
        void correctlyDetectsAdjacentNonOverlappingBlockTimes() {
            // Given
            Long resourceId = 1L;
            // Check for block starting exactly where previous one ends
            LocalDate startDate = LocalDate.of(2026, 1, 10);
            LocalDate endDate = LocalDate.of(2026, 1, 10);
            // Existing block ends on start date
            BlockTime existingBlockTime = createBlockTime(1L, resourceId, "Existing Block",
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10));

            when(blockTimeRepository.findActiveBlockTimesInRange(resourceId, startDate, endDate))
                    .thenReturn(List.of(existingBlockTime));

            // When
            boolean result = sut.hasOverlappingBlockTimes(resourceId, startDate, endDate);

            // Then
            // Adjacent dates (end date = start date) should be considered overlapping
            assertThat(result).isTrue();

            verify(blockTimeRepository).findActiveBlockTimesInRange(resourceId, startDate, endDate);
        }

        @Test
        @DisplayName("returns false when different resource has block times")
        void returnsFalseWhenDifferentResource() {
            // Given
            Long resourceId = 1L;
            LocalDate startDate = LocalDate.of(2026, 1, 5);
            LocalDate endDate = LocalDate.of(2026, 1, 10);

            when(blockTimeRepository.findActiveBlockTimesInRange(resourceId, startDate, endDate))
                    .thenReturn(List.of());

            // When
            boolean result = sut.hasOverlappingBlockTimes(resourceId, startDate, endDate);

            // Then
            assertThat(result).isFalse();

            verify(blockTimeRepository).findActiveBlockTimesInRange(resourceId, startDate, endDate);
        }
    }

    private BlockTime createBlockTime(Long id, Long resourceId, String title, LocalDate startDate, LocalDate endDate) {
        BlockTime blockTime = new BlockTime();
        blockTime.setId(id);
        blockTime.setResourceId(resourceId);
        blockTime.setTitle(title);
        blockTime.setDescription("Test description");
        blockTime.setStartDate(startDate);
        blockTime.setEndDate(endDate);
        blockTime.setBlocked(true);
        return blockTime;
    }
}

package de.ccq.resourcehub.service;

import de.ccq.resourcehub.entity.BlockTime;
import de.ccq.resourcehub.repository.BlockTimeRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for BlockTime entity operations.
 */
@Service
@Transactional
public class BlockTimeService {

    private final BlockTimeRepository blockTimeRepository;

    public BlockTimeService(BlockTimeRepository blockTimeRepository) {
        this.blockTimeRepository = blockTimeRepository;
    }

    /**
     * Get all block times for a specific resource.
     *
     * @param resourceId the resource ID
     * @return list of block times for the resource
     */
    @Transactional(readOnly = true)
    public List<BlockTime> getAllBlockTimesByResourceId(Long resourceId) {
        return blockTimeRepository.findByResourceIdOrderByStartDateAsc(resourceId);
    }

    /**
     * Get a specific block time by ID.
     *
     * @param id the block time ID
     * @return optional block time
     */
    @Transactional(readOnly = true)
    public Optional<BlockTime> getBlockTimeById(Long id) {
        return blockTimeRepository.findById(id);
    }

    /**
     * Create a new block time.
     *
     * @param blockTime the block time to create
     * @return the created block time
     */
    public BlockTime createBlockTime(BlockTime blockTime) {
        validateBlockTime(blockTime);
        return blockTimeRepository.save(blockTime);
    }

    /**
     * Update an existing block time.
     *
     * @param id the block time ID
     * @param blockTime the updated block time
     * @return the updated block time
     */
    public BlockTime updateBlockTime(Long id, BlockTime blockTime) {
        validateBlockTime(blockTime);
        blockTime.setId(id);
        return blockTimeRepository.save(blockTime);
    }

    /**
     * Delete a block time by ID.
     *
     * @param id the block time ID
     */
    public void deleteBlockTime(Long id) {
        blockTimeRepository.deleteById(id);
    }

    /**
     * Check if there are any active block times that overlap with a given date range.
     *
     * @param resourceId the resource ID
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return true if there are overlapping block times, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean hasOverlappingBlockTimes(Long resourceId, LocalDate startDate, LocalDate endDate) {
        List<BlockTime> overlappingBlockTimes = blockTimeRepository.findActiveBlockTimesInRange(resourceId, startDate, endDate);
        return !overlappingBlockTimes.isEmpty();
    }

    /**
     * Validate a block time.
     *
     * @param blockTime the block time to validate
     */
    private void validateBlockTime(BlockTime blockTime) {
        if (blockTime.getStartDate() == null) {
            throw new IllegalArgumentException("Start date is required");
        }
        if (blockTime.getEndDate() == null) {
            throw new IllegalArgumentException("End date is required");
        }
        if (blockTime.getStartDate().isAfter(blockTime.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        if (blockTime.getTitle() == null || blockTime.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
    }
}
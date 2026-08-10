package de.ccq.resourcehub.controller;

import de.ccq.resourcehub.entity.BlockTime;
import de.ccq.resourcehub.service.BlockTimeService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for BlockTime entity operations.
 */
@RestController
@RequestMapping("/api/block-times")
public class BlockTimeController {

    private final BlockTimeService blockTimeService;

    public BlockTimeController(BlockTimeService blockTimeService) {
        this.blockTimeService = blockTimeService;
    }

    /**
     * Get all block times for a specific resource.
     *
     * @param resourceId the resource ID
     * @return list of block times for the resource
     */
    @GetMapping("/resource/{resourceId}")
    public ResponseEntity<List<BlockTime>> getAllBlockTimesByResourceId(@PathVariable Long resourceId) {
        List<BlockTime> blockTimes = blockTimeService.getAllBlockTimesByResourceId(resourceId);
        return ResponseEntity.ok(blockTimes);
    }

    /**
     * Get a specific block time by ID.
     *
     * @param id the block time ID
     * @return the block time or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<BlockTime> getBlockTimeById(@PathVariable Long id) {
        Optional<BlockTime> blockTime = blockTimeService.getBlockTimeById(id);
        return blockTime.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create a new block time.
     *
     * @param blockTime the block time to create
     * @return the created block time
     */
    @PostMapping
    public ResponseEntity<BlockTime> createBlockTime(@Valid @RequestBody BlockTime blockTime) {
        BlockTime createdBlockTime = blockTimeService.createBlockTime(blockTime);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBlockTime);
    }

    /**
     * Update an existing block time.
     *
     * @param id the block time ID
     * @param blockTime the updated block time
     * @return the updated block time or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<BlockTime> updateBlockTime(@PathVariable Long id, @Valid @RequestBody BlockTime blockTime) {
        try {
            BlockTime updatedBlockTime = blockTimeService.updateBlockTime(id, blockTime);
            return ResponseEntity.ok(updatedBlockTime);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete a block time by ID.
     *
     * @param id the block time ID
     * @return 204 if deleted, 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlockTime(@PathVariable Long id) {
        try {
            blockTimeService.deleteBlockTime(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Check if there are any active block times that overlap with a given date range.
     *
     * @param resourceId the resource ID
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return true if there are overlapping block times, false otherwise
     */
    @GetMapping("/check-overlap")
    public ResponseEntity<Boolean> hasOverlappingBlockTimes(
            @RequestParam Long resourceId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        boolean hasOverlap = blockTimeService.hasOverlappingBlockTimes(resourceId, startDate, endDate);
        return ResponseEntity.ok(hasOverlap);
    }
}
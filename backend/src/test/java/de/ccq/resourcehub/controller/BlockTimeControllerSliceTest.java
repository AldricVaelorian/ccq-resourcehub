package de.ccq.resourcehub.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ccq.resourcehub.entity.BlockTime;
import de.ccq.resourcehub.service.BlockTimeService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(BlockTimeController.class)
@DisplayName("BlockTimeController Slice Test")
class BlockTimeControllerSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BlockTimeService blockTimeService;

    private BlockTime sampleBlockTime;

    @BeforeEach
    void setUp() {
        sampleBlockTime = new BlockTime();
        sampleBlockTime.setId(1L);
        sampleBlockTime.setResourceId(1L);
        sampleBlockTime.setTitle("Test Block");
        sampleBlockTime.setDescription("Test description");
        sampleBlockTime.setStartDate(LocalDate.of(2026, 1, 1));
        sampleBlockTime.setEndDate(LocalDate.of(2026, 1, 5));
        sampleBlockTime.setBlocked(true);
    }

    @Nested
    @DisplayName("GET /api/block-times/resource/{resourceId}")
    class GetAllBlockTimesByResourceId {

        @Test
        @DisplayName("returns 200 OK with list of block times")
        void returnsBlockTimesForResource() throws Exception {
            // Given
            Long resourceId = 1L;
            BlockTime blockTime1 = createBlockTime(1L, resourceId, "Block 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));
            BlockTime blockTime2 = createBlockTime(2L, resourceId, "Block 2", LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 15));

            Mockito.when(blockTimeService.getAllBlockTimesByResourceId(resourceId))
                    .thenReturn(List.of(blockTime1, blockTime2));

            // When/Then
            mockMvc.perform(get("/api/block-times/resource/{resourceId}", resourceId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Verify
            Mockito.verify(blockTimeService).getAllBlockTimesByResourceId(resourceId);
        }

        @Test
        @DisplayName("returns 200 OK with empty list when no block times exist")
        void returnsEmptyListWhenNoBlockTimes() throws Exception {
            // Given
            Long resourceId = 999L;
            Mockito.when(blockTimeService.getAllBlockTimesByResourceId(resourceId)).thenReturn(List.of());

            // When/Then
            mockMvc.perform(get("/api/block-times/resource/{resourceId}", resourceId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Verify
            Mockito.verify(blockTimeService).getAllBlockTimesByResourceId(resourceId);
        }
    }

    @Nested
    @DisplayName("GET /api/block-times/{id}")
    class GetBlockTimeById {

        @Test
        @DisplayName("returns 200 OK with block time when it exists")
        void returnsBlockTimeWhenExists() throws Exception {
            // Given
            Long blockTimeId = 1L;
            Mockito.when(blockTimeService.getBlockTimeById(blockTimeId))
                    .thenReturn(Optional.of(sampleBlockTime));

            // When/Then
            mockMvc.perform(get("/api/block-times/{id}", blockTimeId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Verify
            Mockito.verify(blockTimeService).getBlockTimeById(blockTimeId);
        }

        @Test
        @DisplayName("returns 404 Not Found when block time does not exist")
        void returnsNotFoundWhenBlockTimeDoesNotExist() throws Exception {
            // Given
            Long blockTimeId = 999L;
            Mockito.when(blockTimeService.getBlockTimeById(blockTimeId))
                    .thenReturn(Optional.empty());

            // When/Then
            mockMvc.perform(get("/api/block-times/{id}", blockTimeId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            // Verify
            Mockito.verify(blockTimeService).getBlockTimeById(blockTimeId);
        }
    }

    @Nested
    @DisplayName("POST /api/block-times")
    class CreateBlockTime {

        @Test
        @DisplayName("returns 201 Created when block time is valid")
        void returnsCreatedWhenBlockTimeIsValid() throws Exception {
            // Given
            String requestBody = objectMapper.writeValueAsString(sampleBlockTime);
            Mockito.when(blockTimeService.createBlockTime(Mockito.any(BlockTime.class)))
                    .thenReturn(sampleBlockTime);

            // When/Then
            MvcResult result = mockMvc.perform(post("/api/block-times")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andReturn();

            // Verify
            Mockito.verify(blockTimeService).createBlockTime(Mockito.any(BlockTime.class));
        }

        @Test
        @DisplayName("returns 400 Bad Request when validation fails")
        void returnsBadRequestWhenValidationFails() throws Exception {
            // Given - Create block time with missing required fields
            BlockTime invalidBlockTime = new BlockTime();
            invalidBlockTime.setResourceId(1L);
            // Title is missing
            invalidBlockTime.setStartDate(LocalDate.of(2026, 1, 1));
            invalidBlockTime.setEndDate(LocalDate.of(2026, 1, 5));

            String requestBody = objectMapper.writeValueAsString(invalidBlockTime);

            // When/Then
            mockMvc.perform(post("/api/block-times")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .accept(MediaType.APPLICATION_JSON))
                    // Spring Validation should catch this before reaching the service
                    .andExpect(status().isBadRequest());

            // Verify service was not called due to validation
            Mockito.verifyNoInteractions(blockTimeService);
        }
    }

    @Nested
    @DisplayName("PUT /api/block-times/{id}")
    class UpdateBlockTime {

        @Test
        @DisplayName("returns 200 OK when block time is updated successfully")
        void returnsOkWhenBlockTimeIsUpdated() throws Exception {
            // Given
            Long blockTimeId = 1L;
            sampleBlockTime.setTitle("Updated Title");
            String requestBody = objectMapper.writeValueAsString(sampleBlockTime);

            Mockito.when(blockTimeService.updateBlockTime(Mockito.eq(blockTimeId), Mockito.any(BlockTime.class)))
                    .thenReturn(sampleBlockTime);

            // When/Then
            mockMvc.perform(put("/api/block-times/{id}", blockTimeId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Verify
            Mockito.verify(blockTimeService).updateBlockTime(Mockito.eq(blockTimeId), Mockito.any(BlockTime.class));
        }

        @Test
        @DisplayName("returns 400 Bad Request when validation fails")
        void returnsBadRequestWhenValidationFails() throws Exception {
            // Given - Create block time with invalid date range
            BlockTime invalidBlockTime = new BlockTime();
            invalidBlockTime.setId(1L);
            invalidBlockTime.setResourceId(1L);
            invalidBlockTime.setTitle("Invalid Block");
            invalidBlockTime.setStartDate(LocalDate.of(2026, 1, 10));
            invalidBlockTime.setEndDate(LocalDate.of(2026, 1, 5)); // End before start

            String requestBody = objectMapper.writeValueAsString(invalidBlockTime);

            // When/Then
            mockMvc.perform(put("/api/block-times/{id}", 1L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            // Verify service was called but threw exception
            Mockito.verify(blockTimeService).updateBlockTime(Mockito.eq(1L), Mockito.any(BlockTime.class));
        }

        @Test
        @DisplayName("returns 400 Bad Request when block time does not exist")
        void returnsBadRequestWhenBlockTimeDoesNotExist() throws Exception {
            // Given
            Long blockTimeId = 999L;
            Mockito.when(blockTimeService.updateBlockTime(Mockito.eq(blockTimeId), Mockito.any(BlockTime.class)))
                    .thenThrow(IllegalArgumentException.class);

            String requestBody = objectMapper.writeValueAsString(sampleBlockTime);

            // When/Then
            mockMvc.perform(put("/api/block-times/{id}", blockTimeId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            // Verify
            Mockito.verify(blockTimeService).updateBlockTime(Mockito.eq(blockTimeId), Mockito.any(BlockTime.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/block-times/{id}")
    class DeleteBlockTime {

        @Test
        @DisplayName("returns 204 No Content when block time is deleted")
        void returnsNoContentWhenBlockTimeIsDeleted() throws Exception {
            // Given
            Long blockTimeId = 1L;

            // When/Then
            mockMvc.perform(delete("/api/block-times/{id}", blockTimeId))
                    .andExpect(status().isNoContent());

            // Verify
            Mockito.verify(blockTimeService).deleteBlockTime(blockTimeId);
        }

        @Test
        @DisplayName("returns 400 Bad Request when block time does not exist")
        void returnsBadRequestWhenBlockTimeDoesNotExist() throws Exception {
            // Given
            Long blockTimeId = 999L;
            Mockito.doThrow(IllegalArgumentException.class).when(blockTimeService).deleteBlockTime(blockTimeId);

            // When/Then
            mockMvc.perform(delete("/api/block-times/{id}", blockTimeId))
                    .andExpect(status().isBadRequest());

            // Verify
            Mockito.verify(blockTimeService).deleteBlockTime(blockTimeId);
        }
    }

    @Nested
    @DisplayName("GET /api/block-times/check-overlap")
    class HasOverlappingBlockTimes {

        @Test
        @DisplayName("returns 200 OK with true when overlapping block times exist")
        void returnsTrueWhenOverlappingBlockTimesExist() throws Exception {
            // Given
            Long resourceId = 1L;
            LocalDate startDate = LocalDate.of(2026, 1, 5);
            LocalDate endDate = LocalDate.of(2026, 1, 10);

            Mockito.when(blockTimeService.hasOverlappingBlockTimes(resourceId, startDate, endDate))
                    .thenReturn(true);

            // When/Then
            mockMvc.perform(get("/api/block-times/check-overlap")
                    .param("resourceId", resourceId.toString())
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(result -> {
                        try {
                            Boolean response = objectMapper.readValue(result.getResponse().getContentAsString(), Boolean.class);
                            assertThat(response).isTrue();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });

            // Verify
            Mockito.verify(blockTimeService).hasOverlappingBlockTimes(resourceId, startDate, endDate);
        }

        @Test
        @DisplayName("returns 200 OK with false when no overlapping block times exist")
        void returnsFalseWhenNoOverlappingBlockTimes() throws Exception {
            // Given
            Long resourceId = 1L;
            LocalDate startDate = LocalDate.of(2026, 1, 20);
            LocalDate endDate = LocalDate.of(2026, 1, 25);

            Mockito.when(blockTimeService.hasOverlappingBlockTimes(resourceId, startDate, endDate))
                    .thenReturn(false);

            // When/Then
            mockMvc.perform(get("/api/block-times/check-overlap")
                    .param("resourceId", resourceId.toString())
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(result -> {
                        try {
                            Boolean response = objectMapper.readValue(result.getResponse().getContentAsString(), Boolean.class);
                            assertThat(response).isFalse();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });

            // Verify
            Mockito.verify(blockTimeService).hasOverlappingBlockTimes(resourceId, startDate, endDate);
        }

        @Test
        @DisplayName("returns 400 Bad Request when required parameters are missing")
        void returnsBadRequestWhenParametersAreMissing() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/block-times/check-overlap")
                    .param("resourceId", "1")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
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
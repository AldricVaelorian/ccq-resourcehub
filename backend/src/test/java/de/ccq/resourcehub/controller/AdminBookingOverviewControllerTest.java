package de.ccq.resourcehub.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.ccq.resourcehub.dto.AdminBookingOverviewResponse;
import de.ccq.resourcehub.entity.BookingStatus;
import de.ccq.resourcehub.exception.AdminBookingOverviewException;
import de.ccq.resourcehub.service.AdminBookingOverviewService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@DisplayName("AdminBookingOverviewController")
class AdminBookingOverviewControllerTest {

    private AdminBookingOverviewService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = org.mockito.Mockito.mock(AdminBookingOverviewService.class);
        var objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        var validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminBookingOverviewController(service))
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Nested
    @DisplayName("GET /api/admin/bookings")
    class findAllBookings {

        @Test
        @DisplayName("returns 200 OK with all bookings when adminId is valid")
        void returnsBookingsWhenAdminIdIsValid() throws Exception {
            // Given
            Long adminId = 1L;
            AdminBookingOverviewResponse response1 = new AdminBookingOverviewResponse(
                    1L,
                    10L,
                    20L,
                    LocalDate.parse("2026-01-01"),
                    LocalDate.parse("2026-01-05"),
                    BookingStatus.PENDING,
                    Instant.parse("2026-01-01T10:00:00Z"),
                    null,
                    null,
                    null);

            AdminBookingOverviewResponse response2 = new AdminBookingOverviewResponse(
                    2L,
                    11L,
                    21L,
                    LocalDate.parse("2026-01-10"),
                    LocalDate.parse("2026-01-15"),
                    BookingStatus.APPROVED,
                    Instant.parse("2026-01-10T10:00:00Z"),
                    Instant.parse("2026-01-11T10:00:00Z"),
                    null,
                    null);

            when(service.findAllBookings(adminId)).thenReturn(List.of(response1, response2));

            // When/Then
            mockMvc.perform(get("/api/admin/bookings?adminId={adminId}", adminId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(2))
                    .andExpect(jsonPath("$[0].resourceId").value(11))
                    .andExpect(jsonPath("$[0].userId").value(21))
                    .andExpect(jsonPath("$[0].startDate").value("2026-01-10"))
                    .andExpect(jsonPath("$[0].endDate").value("2026-01-15"))
                    .andExpect(jsonPath("$[0].status").value("APPROVED"))
                    .andExpect(jsonPath("$[0].approvedAt").value("2026-01-11T10:00:00Z"))
                    .andExpect(jsonPath("$[1].id").value(1))
                    .andExpect(jsonPath("$[1].resourceId").value(10))
                    .andExpect(jsonPath("$[1].userId").value(20))
                    .andExpect(jsonPath("$[1].startDate").value("2026-01-01"))
                    .andExpect(jsonPath("$[1].endDate").value("2026-01-05"))
                    .andExpect(jsonPath("$[1].status").value("PENDING"))
                    .andExpect(jsonPath("$[1].approvedAt").doesNotExist())
                    .andExpect(jsonPath("$[1].rejectedAt").doesNotExist());
        }

        @Test
        @DisplayName("returns 200 OK with empty list when no bookings exist")
        void returnsEmptyListWhenNoBookings() throws Exception {
            // Given
            Long adminId = 1L;
            when(service.findAllBookings(adminId)).thenReturn(List.of());

            // When/Then
            mockMvc.perform(get("/api/admin/bookings?adminId={adminId}", adminId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("returns 400 Bad Request when adminId is missing")
        void returnsBadRequestWhenAdminIdIsMissing() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/admin/bookings")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("adminId:")));
            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("returns 400 Bad Request when adminId is not positive")
        void returnsBadRequestWhenAdminIdIsNotPositive() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/admin/bookings?adminId=0")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("adminId:")));
            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("returns 400 Bad Request when adminId is negative")
        void returnsBadRequestWhenAdminIdIsNegative() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/admin/bookings?adminId=-1")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("adminId:")));
            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("returns 404 Not Found with controlled error when administrator does not exist")
        void returnsNotFoundWhenAdminDoesNotExist() throws Exception {
            assertDomainError(
                    AdminBookingOverviewException.adminNotFound(99L),
                    99L,
                    404,
                    "ADMIN_BOOKING_OVERVIEW_NOT_FOUND",
                    "Administrator not found with ID: 99");
        }

        @Test
        @DisplayName("returns 403 Forbidden with controlled error when user cannot view all bookings")
        void returnsForbiddenWhenAdminCannotViewBookings() throws Exception {
            assertDomainError(
                    AdminBookingOverviewException.forbidden(1L),
                    1L,
                    403,
                    "ADMIN_BOOKING_OVERVIEW_FORBIDDEN",
                    "User 1 may not view all bookings");
        }

        private void assertDomainError(
                AdminBookingOverviewException exception,
                long adminId,
                int status,
                String code,
                String message) throws Exception {
            // Given
            when(service.findAllBookings(adminId)).thenThrow(exception);

            // When/Then
            mockMvc.perform(get("/api/admin/bookings?adminId={adminId}", adminId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(status))
                    .andExpect(jsonPath("$.code").value(code))
                    .andExpect(jsonPath("$.message").value(message));
        }
    }
}
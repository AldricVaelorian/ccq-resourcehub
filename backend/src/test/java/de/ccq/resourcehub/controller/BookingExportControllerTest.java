package de.ccq.resourcehub.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.ccq.resourcehub.dto.BookingExportResponse;
import de.ccq.resourcehub.entity.BookingStatus;
import de.ccq.resourcehub.exception.BookingExportException;
import de.ccq.resourcehub.service.BookingExportService;
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

@DisplayName("BookingExportController")
class BookingExportControllerTest {

    private BookingExportService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = org.mockito.Mockito.mock(BookingExportService.class);
        var objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        var validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new BookingExportController(service))
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Nested
    @DisplayName("GET /api/admin/bookings/export")
    class exportAllBookings {

        @Test
        @DisplayName("returns 200 OK with all bookings when adminId is valid")
        void returnsBookingsWhenAdminIdIsValid() throws Exception {
            // Given
            Long adminId = 1L;
            BookingExportResponse response1 = new BookingExportResponse(
                    1L,
                    10L,
                    "Resource A",
                    20L,
                    "user20",
                    LocalDate.parse("2026-01-01"),
                    LocalDate.parse("2026-01-05"),
                    BookingStatus.PENDING,
                    null,
                    null,
                    null,
                    null);

            BookingExportResponse response2 = new BookingExportResponse(
                    2L,
                    11L,
                    "Resource B",
                    21L,
                    "user21",
                    LocalDate.parse("2026-01-10"),
                    LocalDate.parse("2026-01-15"),
                    BookingStatus.APPROVED,
                    null,
                    null,
                    null,
                    null);

            when(service.exportAllBookings(adminId)).thenReturn(List.of(response2, response1));

            // When/Then
            mockMvc.perform(get("/api/admin/bookings/export?adminId={adminId}", adminId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(2))
                    .andExpect(jsonPath("$[0].resourceId").value(11))
                    .andExpect(jsonPath("$[0].resourceName").value("Resource B"))
                    .andExpect(jsonPath("$[0].userId").value(21))
                    .andExpect(jsonPath("$[0].username").value("user21"))
                    .andExpect(jsonPath("$[0].startDate").value("2026-01-10"))
                    .andExpect(jsonPath("$[0].endDate").value("2026-01-15"))
                    .andExpect(jsonPath("$[0].status").value("APPROVED"))
                    .andExpect(jsonPath("$[1].id").value(1))
                    .andExpect(jsonPath("$[1].resourceId").value(10))
                    .andExpect(jsonPath("$[1].resourceName").value("Resource A"))
                    .andExpect(jsonPath("$[1].userId").value(20))
                    .andExpect(jsonPath("$[1].username").value("user20"))
                    .andExpect(jsonPath("$[1].startDate").value("2026-01-01"))
                    .andExpect(jsonPath("$[1].endDate").value("2026-01-05"))
                    .andExpect(jsonPath("$[1].status").value("PENDING"))
                    .andExpect(jsonPath("$[1].approvedAt").doesNotExist())
                    .andExpect(jsonPath("$[1].rejectedAt").doesNotExist())
                    .andExpect(jsonPath("$[1].rejectionReason").doesNotExist());
        }

        @Test
        @DisplayName("returns 200 OK with empty list when no bookings exist")
        void returnsEmptyListWhenNoBookings() throws Exception {
            // Given
            Long adminId = 1L;
            when(service.exportAllBookings(adminId)).thenReturn(List.of());

            // When/Then
            mockMvc.perform(get("/api/admin/bookings/export?adminId={adminId}", adminId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("returns 400 Bad Request when adminId is missing")
        void returnsBadRequestWhenAdminIdIsMissing() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/admin/bookings/export")
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
            mockMvc.perform(get("/api/admin/bookings/export?adminId=0")
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
            mockMvc.perform(get("/api/admin/bookings/export?adminId=-1")
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
                    BookingExportException.adminNotFound(99L),
                    99L,
                    404,
                    "BOOKING_EXPORT_NOT_FOUND",
                    "Admin with ID 99 not found");
        }

        @Test
        @DisplayName("returns 403 Forbidden with controlled error when user cannot export bookings")
        void returnsForbiddenWhenAdminCannotExportBookings() throws Exception {
            assertDomainError(
                    BookingExportException.forbidden(1L),
                    1L,
                    403,
                    "BOOKING_EXPORT_FORBIDDEN",
                    "Admin with ID 1 is not authorized to export bookings");
        }

        private void assertDomainError(
                BookingExportException exception,
                long adminId,
                int status,
                String code,
                String message) throws Exception {
            // Given
            when(service.exportAllBookings(adminId)).thenThrow(exception);

            // When/Then
            mockMvc.perform(get("/api/admin/bookings/export?adminId={adminId}", adminId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(status))
                    .andExpect(jsonPath("$.code").value(code))
                    .andExpect(jsonPath("$.message").value(message));
        }
    }

    @Nested
    @DisplayName("GET /api/admin/bookings/export/status")
    class exportBookingsByStatus {

        @Test
        @DisplayName("returns 200 OK with filtered bookings by status when adminId is valid")
        void returnsBookingsByStatusWhenAdminIdIsValid() throws Exception {
            // Given
            Long adminId = 1L;
            BookingExportResponse response = new BookingExportResponse(
                    1L,
                    10L,
                    "Resource A",
                    20L,
                    "user20",
                    LocalDate.parse("2026-01-01"),
                    LocalDate.parse("2026-01-05"),
                    BookingStatus.APPROVED,
                    null,
                    null,
                    null,
                    null);

            when(service.exportBookingsByStatus(adminId, "APPROVED")).thenReturn(List.of(response));

            // When/Then
            mockMvc.perform(get("/api/admin/bookings/export/status?adminId={adminId}&status={status}", adminId, "APPROVED")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].status").value("APPROVED"));
        }

        @Test
        @DisplayName("returns 200 OK with empty list when no bookings match the status")
        void returnsEmptyListWhenNoBookingsMatchStatus() throws Exception {
            // Given
            Long adminId = 1L;
            when(service.exportBookingsByStatus(adminId, "APPROVED")).thenReturn(List.of());

            // When/Then
            mockMvc.perform(get("/api/admin/bookings/export/status?adminId={adminId}&status={status}", adminId, "APPROVED")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("returns 400 Bad Request when adminId is missing")
        void returnsBadRequestWhenAdminIdIsMissing() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/admin/bookings/export/status?status=APPROVED")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("adminId:")));
            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("returns 400 Bad Request when status is missing")
        void returnsBadRequestWhenStatusIsMissing() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/admin/bookings/export/status?adminId=1")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("status:")));
            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("returns 400 Bad Request when status is null")
        void returnsBadRequestWhenStatusIsNull() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/admin/bookings/export/status?adminId=1&status=null")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("status:")));
            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("returns 400 Bad Request when status is empty")
        void returnsBadRequestWhenStatusIsEmpty() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/admin/bookings/export/status?adminId=1&status=")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("status:")));
            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("returns 400 Bad Request with controlled error when status is invalid")
        void returnsBadRequestWhenStatusIsInvalid() throws Exception {
            assertDomainError(
                    BookingExportException.invalidStatus("INVALID"),
                    1L,
                    "INVALID",
                    400,
                    "BOOKING_EXPORT_INVALID_REQUEST",
                    "Invalid status: INVALID");
        }

        @Test
        @DisplayName("returns 404 Not Found with controlled error when administrator does not exist")
        void returnsNotFoundWhenAdminDoesNotExist() throws Exception {
            assertDomainError(
                    BookingExportException.adminNotFound(99L),
                    99L,
                    "APPROVED",
                    404,
                    "BOOKING_EXPORT_NOT_FOUND",
                    "Admin with ID 99 not found");
        }

        @Test
        @DisplayName("returns 403 Forbidden with controlled error when user cannot export bookings")
        void returnsForbiddenWhenAdminCannotExportBookings() throws Exception {
            assertDomainError(
                    BookingExportException.forbidden(1L),
                    1L,
                    "APPROVED",
                    403,
                    "BOOKING_EXPORT_FORBIDDEN",
                    "Admin with ID 1 is not authorized to export bookings");
        }

        private void assertDomainError(
                BookingExportException exception,
                long adminId,
                String status,
                int statusHttp,
                String code,
                String message) throws Exception {
            // Given
            when(service.exportBookingsByStatus(adminId, status)).thenThrow(exception);

            // When/Then
            mockMvc.perform(get("/api/admin/bookings/export/status?adminId={adminId}&status={status}", adminId, status)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(statusHttp))
                    .andExpect(jsonPath("$.code").value(code))
                    .andExpect(jsonPath("$.message").value(message));
        }
    }

    @Nested
    @DisplayName("GET /api/admin/bookings/export/daterange")
    class exportBookingsByDateRange {

        @Test
        @DisplayName("returns 200 OK with filtered bookings by date range when adminId is valid")
        void returnsBookingsByDateRangeWhenAdminIdIsValid() throws Exception {
            // Given
            Long adminId = 1L;
            BookingExportResponse response = new BookingExportResponse(
                    1L,
                    10L,
                    "Resource A",
                    20L,
                    "user20",
                    LocalDate.parse("2026-01-10"),
                    LocalDate.parse("2026-01-15"),
                    BookingStatus.APPROVED,
                    null,
                    null,
                    null,
                    null);

            when(service.exportBookingsByDateRange(adminId, LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-31")))
                    .thenReturn(List.of(response));

            // When/Then
            mockMvc.perform(get("/api/admin/bookings/export/daterange?adminId={adminId}&startDate={startDate}&endDate={endDate}",
                            adminId, "2026-01-01", "2026-01-31")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].startDate").value("2026-01-10"))
                    .andExpect(jsonPath("$[0].endDate").value("2026-01-15"));
        }

        @Test
        @DisplayName("returns 200 OK with empty list when no bookings match the date range")
        void returnsEmptyListWhenNoBookingsMatchDateRange() throws Exception {
            // Given
            Long adminId = 1L;
            when(service.exportBookingsByDateRange(adminId, LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-31")))
                    .thenReturn(List.of());

            // When/Then
            mockMvc.perform(get("/api/admin/bookings/export/daterange?adminId={adminId}&startDate={startDate}&endDate={endDate}",
                            adminId, "2026-01-01", "2026-01-31")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("returns 400 Bad Request when adminId is missing")
        void returnsBadRequestWhenAdminIdIsMissing() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/admin/bookings/export/daterange?startDate=2026-01-01&endDate=2026-01-31")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("adminId:")));
            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("returns 400 Bad Request when startDate is missing")
        void returnsBadRequestWhenStartDateIsMissing() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/admin/bookings/export/daterange?adminId=1&endDate=2026-01-31")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("startDate:")));
            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("returns 400 Bad Request when endDate is missing")
        void returnsBadRequestWhenEndDateIsMissing() throws Exception {
            // When/Then
            mockMvc.perform(get("/api/admin/bookings/export/daterange?adminId=1&startDate=2026-01-01")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("endDate:")));
            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("returns 400 Bad Request with controlled error when date range is invalid")
        void returnsBadRequestWhenDateRangeIsInvalid() throws Exception {
            assertDomainError(
                    BookingExportException.invalidDateRange(LocalDate.parse("2026-01-15"), LocalDate.parse("2026-01-01")),
                    1L,
                    "2026-01-15",
                    "2026-01-01",
                    400,
                    "BOOKING_EXPORT_INVALID_REQUEST",
                    "Invalid date range: start=2026-01-15, end=2026-01-01");
        }

        @Test
        @DisplayName("returns 404 Not Found with controlled error when administrator does not exist")
        void returnsNotFoundWhenAdminDoesNotExist() throws Exception {
            assertDomainError(
                    BookingExportException.adminNotFound(99L),
                    99L,
                    "2026-01-01",
                    "2026-01-31",
                    404,
                    "BOOKING_EXPORT_NOT_FOUND",
                    "Admin with ID 99 not found");
        }

        @Test
        @DisplayName("returns 403 Forbidden with controlled error when user cannot export bookings")
        void returnsForbiddenWhenAdminCannotExportBookings() throws Exception {
            assertDomainError(
                    BookingExportException.forbidden(1L),
                    1L,
                    "2026-01-01",
                    "2026-01-31",
                    403,
                    "BOOKING_EXPORT_FORBIDDEN",
                    "Admin with ID 1 is not authorized to export bookings");
        }

        private void assertDomainError(
                BookingExportException exception,
                long adminId,
                String startDate,
                String endDate,
                int statusHttp,
                String code,
                String message) throws Exception {
            // Given
            when(service.exportBookingsByDateRange(adminId, LocalDate.parse(startDate), LocalDate.parse(endDate)))
                    .thenThrow(exception);

            // When/Then
            mockMvc.perform(get("/api/admin/bookings/export/daterange?adminId={adminId}&startDate={startDate}&endDate={endDate}",
                            adminId, startDate, endDate)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(statusHttp))
                    .andExpect(jsonPath("$.code").value(code))
                    .andExpect(jsonPath("$.message").value(message));
        }
    }
}
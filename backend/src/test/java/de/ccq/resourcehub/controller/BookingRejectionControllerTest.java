package de.ccq.resourcehub.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.ccq.resourcehub.dto.BookingRejectionResponse;
import de.ccq.resourcehub.entity.BookingStatus;
import de.ccq.resourcehub.exception.BookingRejectionException;
import de.ccq.resourcehub.service.BookingRejectionService;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class BookingRejectionControllerTest {

    private BookingRejectionService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = org.mockito.Mockito.mock(BookingRejectionService.class);
        var objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        var validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new BookingRejectionController(service))
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void rejectBooking_returns200WithRejectedBookingWhenRequestIsValid() throws Exception {
        // arrange
        var response = new BookingRejectionResponse(
                7L,
                3L,
                5L,
                LocalDate.parse("2026-09-01"),
                LocalDate.parse("2026-09-03"),
                BookingStatus.REJECTED,
                Instant.parse("2026-08-11T12:30:00Z"),
                "Resource unavailable");
        when(service.rejectBooking(7L, 11L, "Resource unavailable")).thenReturn(response);

        // act & assert
        mockMvc.perform(put("/api/bookings/{bookingId}/reject", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"managerId\":11,\"reason\":\"Resource unavailable\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.resourceId").value(3))
                .andExpect(jsonPath("$.userId").value(5))
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.rejectedAt").value("2026-08-11T12:30:00Z"))
                .andExpect(jsonPath("$.rejectionReason").value("Resource unavailable"));
    }

    @Test
    void rejectBooking_returns400WhenManagerIdIsMissing() throws Exception {
        assertValidationError("{\"reason\":\"Unavailable\"}", "managerId:");
    }

    @Test
    void rejectBooking_returns400WhenManagerIdIsNotPositive() throws Exception {
        assertValidationError("{\"managerId\":0,\"reason\":\"Unavailable\"}", "managerId:");
    }

    @Test
    void rejectBooking_returns400WhenReasonIsBlank() throws Exception {
        assertValidationError("{\"managerId\":11,\"reason\":\"   \"}", "reason:");
    }

    @Test
    void rejectBooking_returns400WhenReasonExceedsMaximumLength() throws Exception {
        assertValidationError(
                "{\"managerId\":11,\"reason\":\"" + "x".repeat(1001) + "\"}",
                "reason:");
    }

    @Test
    void rejectBooking_returns404WithControlledErrorWhenBookingDoesNotExist() throws Exception {
        assertDomainError(
                BookingRejectionException.bookingNotFound(99L),
                99L,
                404,
                "BOOKING_REJECTION_NOT_FOUND",
                "Booking not found with ID: 99");
    }

    @Test
    void rejectBooking_returns403WithControlledErrorWhenUserCannotReject() throws Exception {
        assertDomainError(
                BookingRejectionException.forbidden(11L),
                7L,
                403,
                "BOOKING_REJECTION_FORBIDDEN",
                "User 11 may not reject bookings");
    }

    @Test
    void rejectBooking_returns409WithControlledErrorWhenBookingIsNotPending() throws Exception {
        assertDomainError(
                BookingRejectionException.invalidStatus(BookingStatus.APPROVED),
                7L,
                409,
                "BOOKING_REJECTION_CONFLICT",
                "Only pending bookings can be rejected; status is APPROVED");
    }

    private void assertValidationError(String requestBody, String messagePrefix) throws Exception {
        // act & assert
        mockMvc.perform(put("/api/bookings/{bookingId}/reject", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith(messagePrefix)));
        verifyNoInteractions(service);
    }

    private void assertDomainError(
            BookingRejectionException exception,
            long bookingId,
            int status,
            String code,
            String message) throws Exception {
        // arrange
        when(service.rejectBooking(bookingId, 11L, "Unavailable")).thenThrow(exception);

        // act & assert
        mockMvc.perform(put("/api/bookings/{bookingId}/reject", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"managerId\":11,\"reason\":\"Unavailable\"}"))
                .andExpect(status().is(status))
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.message").value(message));
    }
}

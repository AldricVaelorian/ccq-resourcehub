package de.ccq.resourcehub.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.ccq.resourcehub.dto.BookingResponse;
import de.ccq.resourcehub.entity.BookingStatus;
import de.ccq.resourcehub.exception.BookingApprovalException;
import de.ccq.resourcehub.service.BookingApprovalService;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class BookingApprovalControllerTest {

    private BookingApprovalService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = org.mockito.Mockito.mock(BookingApprovalService.class);
        var objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        var validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new BookingApprovalController(service))
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void approveBooking_returns200WithApprovedBookingWhenRequestIsValid() throws Exception {
        // arrange
        var response = new BookingResponse(
                7L,
                3L,
                5L,
                LocalDate.parse("2026-09-01"),
                LocalDate.parse("2026-09-03"),
                BookingStatus.APPROVED,
                Instant.parse("2026-08-11T10:15:30Z"));
        when(service.approveBooking(7L, 11L)).thenReturn(response);

        // act & assert
        mockMvc.perform(put("/api/bookings/{bookingId}/approve", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"managerId\":11}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.resourceId").value(3))
                .andExpect(jsonPath("$.userId").value(5))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.approvedAt").value("2026-08-11T10:15:30Z"));
    }

    @Test
    void approveBooking_returns400WhenManagerIdIsMissing() throws Exception {
        // act & assert
        mockMvc.perform(put("/api/bookings/{bookingId}/approve", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("managerId:")));
        verifyNoInteractions(service);
    }

    @Test
    void approveBooking_returns400WhenManagerIdIsNotPositive() throws Exception {
        // act & assert
        mockMvc.perform(put("/api/bookings/{bookingId}/approve", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"managerId\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("managerId:")));
        verifyNoInteractions(service);
    }

    @Test
    void approveBooking_returns404WithControlledErrorWhenBookingDoesNotExist() throws Exception {
        assertDomainError(
                BookingApprovalException.notFound(99L),
                99L,
                11L,
                404,
                "BOOKING_APPROVAL_NOT_FOUND",
                "Booking not found with ID: 99");
    }

    @Test
    void approveBooking_returns403WithControlledErrorWhenUserCannotApprove() throws Exception {
        assertDomainError(
                BookingApprovalException.forbidden(11L),
                7L,
                11L,
                403,
                "BOOKING_APPROVAL_FORBIDDEN",
                "User 11 may not approve bookings");
    }

    @Test
    void approveBooking_returns409WithControlledErrorWhenBookingOverlaps() throws Exception {
        assertDomainError(
                BookingApprovalException.overlap(),
                7L,
                11L,
                409,
                "BOOKING_APPROVAL_CONFLICT",
                "Booking overlaps an already approved booking");
    }

    private void assertDomainError(
            BookingApprovalException exception,
            long bookingId,
            long managerId,
            int status,
            String code,
            String message) throws Exception {
        // arrange
        when(service.approveBooking(bookingId, managerId)).thenThrow(exception);

        // act & assert
        mockMvc.perform(put("/api/bookings/{bookingId}/approve", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"managerId\":" + managerId + "}"))
                .andExpect(status().is(status))
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.message").value(message));
    }
}

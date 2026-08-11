package de.ccq.resourcehub.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.ccq.resourcehub.dto.AvailabilityRuleRequest;
import de.ccq.resourcehub.dto.AvailabilityRuleResponse;
import de.ccq.resourcehub.service.AvailabilityRuleService;
import java.time.DayOfWeek;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class AvailabilityRuleControllerTest {

    private AvailabilityRuleService service;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        service = org.mockito.Mockito.mock(AvailabilityRuleService.class);
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        var validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new AvailabilityRuleController(service))
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void create_returns201WithResponseDtoWhenRequestIsValid() throws Exception {
        // arrange
        var request = request("Monday hours", DayOfWeek.MONDAY, "08:00", "17:00");
        var response = response(5L, "Monday hours", DayOfWeek.MONDAY, "08:00", "17:00", true);
        when(service.create(any(AvailabilityRuleRequest.class))).thenReturn(response);

        // act & assert
        mockMvc.perform(post("/api/availability-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Monday hours"))
                .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$.startTime[0]").value(8))
                .andExpect(jsonPath("$.startTime[1]").value(0))
                .andExpect(jsonPath("$.endTime[0]").value(17))
                .andExpect(jsonPath("$.endTime[1]").value(0))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void create_returns400WithValidationErrorWhenNameIsBlank() throws Exception {
        // arrange
        var request = request(" ", DayOfWeek.MONDAY, "08:00", "17:00");

        // act & assert
        mockMvc.perform(post("/api/availability-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("name:")));
        verifyNoInteractions(service);
    }

    @Test
    void create_returns400WithDomainErrorWhenTimeWindowIsInvalid() throws Exception {
        // arrange
        var request = request("Monday hours", DayOfWeek.MONDAY, "17:00", "08:00");
        when(service.create(any(AvailabilityRuleRequest.class)))
                .thenThrow(new AvailabilityRuleService.InvalidTimeWindowException(
                        "End time must be after start time"));

        // act & assert
        mockMvc.perform(post("/api/availability-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_TIME_WINDOW"))
                .andExpect(jsonPath("$.message").value("End time must be after start time"));
    }

    @Test
    void findById_returns404WithControlledErrorWhenRuleDoesNotExist() throws Exception {
        // arrange
        when(service.findById(99L))
                .thenThrow(new AvailabilityRuleService.NotFoundException(
                        "AvailabilityRule not found with id: 99"));

        // act & assert
        mockMvc.perform(get("/api/availability-rules/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("AVAILABILITY_RULE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("AvailabilityRule not found with id: 99"));
    }

    @Test
    void update_returns200WithUpdatedResponseWhenRequestIsValid() throws Exception {
        // arrange
        var request = request("Evening", DayOfWeek.FRIDAY, "18:00", "22:00");
        var response = response(8L, "Evening", DayOfWeek.FRIDAY, "18:00", "22:00", false);
        when(service.update(eq(8L), any(AvailabilityRuleRequest.class))).thenReturn(response);

        // act & assert
        mockMvc.perform(put("/api/availability-rules/{id}", 8L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void findByDayOfWeek_returns400WhenPathValueIsInvalid() throws Exception {
        // arrange & act & assert
        mockMvc.perform(get("/api/availability-rules/day/not-a-day"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    private AvailabilityRuleRequest request(String name, DayOfWeek day, String start, String end) {
        return new AvailabilityRuleRequest(name, day, LocalTime.parse(start), LocalTime.parse(end));
    }

    private AvailabilityRuleResponse response(
            Long id, String name, DayOfWeek day, String start, String end, boolean active) {
        return new AvailabilityRuleResponse(
                id, name, day, LocalTime.parse(start), LocalTime.parse(end), active);
    }
}

package com.esteirahabitacional.shared.adapter.in.web.error;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.esteirahabitacional.shared.adapter.in.web.CorrelationIdFilter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ApiExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-03T12:00:00Z"), ZoneOffset.UTC);
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new ApiExceptionHandler(clock))
                .addFilters(new CorrelationIdFilter())
                .build();
    }

    @Test
    void shouldReturnProblemDetailsForInvalidInput() throws Exception {
        mockMvc.perform(post("/test")
                        .header(CorrelationIdFilter.HEADER_NAME, "test-trace-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().string(CorrelationIdFilter.HEADER_NAME, "test-trace-id"))
                .andExpect(jsonPath("$.type").value(
                        "https://api.esteirahabitacional.com/problems/validation-error"))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.traceId").value("test-trace-id"))
                .andExpect(jsonPath("$.timestamp").value("2026-08-03T12:00:00Z"))
                .andExpect(jsonPath("$.violations[0].field").value("name"));
    }

    @Test
    void shouldHideUnexpectedErrorDetails() throws Exception {
        mockMvc.perform(get("/test/failure"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Não foi possível concluir a solicitação."))
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.hasKey("stackTrace"))));
    }

    @RestController
    static class TestController {

        @PostMapping("/test")
        TestRequest validate(@Valid @RequestBody TestRequest request) {
            return request;
        }

        @GetMapping("/test/failure")
        void fail() {
            throw new IllegalStateException("sensitive internal detail");
        }
    }

    record TestRequest(@NotBlank String name) {}
}


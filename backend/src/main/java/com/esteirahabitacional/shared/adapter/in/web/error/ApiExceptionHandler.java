package com.esteirahabitacional.shared.adapter.in.web.error;

import com.esteirahabitacional.shared.adapter.in.web.CorrelationIdFilter;
import com.esteirahabitacional.shared.ApplicationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private static final String PROBLEM_BASE =
            "https://api.esteirahabitacional.com/problems/";
    private final Clock clock;

    public ApiExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<Violation> violations = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new Violation(
                        error.getField(),
                        error.getCode() == null ? "INVALID" : error.getCode(),
                        error.getDefaultMessage() == null ? "Valor inválido." : error.getDefaultMessage()))
                .toList();

        ProblemDetail problem = createProblem(
                HttpStatus.BAD_REQUEST,
                "validation-error",
                "Entrada inválida",
                "Um ou mais campos possuem valores inválidos.",
                request);
        problem.setProperty("violations", violations);
        return problem;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail handleConstraintViolation(
            ConstraintViolationException exception, HttpServletRequest request) {
        List<Violation> violations = exception.getConstraintViolations().stream()
                .map(violation -> new Violation(
                        violation.getPropertyPath().toString(),
                        "INVALID",
                        violation.getMessage()))
                .toList();

        ProblemDetail problem = createProblem(
                HttpStatus.BAD_REQUEST,
                "constraint-violation",
                "Entrada inválida",
                "Uma ou mais restrições foram violadas.",
                request);
        problem.setProperty("violations", violations);
        return problem;
    }

    @ExceptionHandler(ApplicationException.class)
    ProblemDetail handleApplication(ApplicationException exception, HttpServletRequest request) {
        return createProblem(
                HttpStatus.valueOf(exception.status()),
                exception.code(),
                exception.title(),
                exception.getMessage(),
                request);
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception exception, HttpServletRequest request) {
        LOGGER.error("Unexpected request failure", exception);
        return createProblem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "internal-error",
                "Erro interno",
                "Não foi possível concluir a solicitação.",
                request);
    }

    private ProblemDetail createProblem(
            HttpStatus status,
            String code,
            String title,
            String detail,
            HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create(PROBLEM_BASE + code));
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", code.toUpperCase().replace('-', '_'));
        problem.setProperty("traceId", currentTraceId());
        problem.setProperty("timestamp", Instant.now(clock));
        return problem;
    }

    private String currentTraceId() {
        String traceId = MDC.get(CorrelationIdFilter.MDC_KEY);
        return traceId == null ? "unavailable" : traceId;
    }

    record Violation(String field, String code, String message) {}
}

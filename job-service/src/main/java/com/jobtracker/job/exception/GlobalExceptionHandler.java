package com.jobtracker.job.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Turns exceptions into clean HTTP responses for the whole service.
 * Uses RFC-7807 ProblemDetail (Spring's standard error body).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(JobNotFoundException.class)
    public ProblemDetail handleNotFound(JobNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /** Bean-validation failures (@Valid) -> 400 with a field -> message map. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Validation failed");
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        problem.setProperty("errors", errors);
        return problem;
    }
}

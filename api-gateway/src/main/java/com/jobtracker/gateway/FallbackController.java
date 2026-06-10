package com.jobtracker.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Where the circuit breaker sends traffic when job-service is unavailable.
 * The route's `fallbackUri: forward:/fallback/jobs` forwards here, and we
 * return a clean 503 so clients fail fast with a useful message instead of
 * hanging on a dead/slow backend.
 */
@RestController
public class FallbackController {

    @RequestMapping("/fallback/jobs")
    public ResponseEntity<Map<String, Object>> jobsFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "job-service unavailable",
                        "message", "The job service is temporarily unreachable. "
                                + "This fallback was returned by the API gateway's circuit breaker "
                                + "so your request fails fast instead of hanging.",
                        "status", 503
                ));
    }
}

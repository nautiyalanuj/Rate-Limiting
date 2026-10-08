//Copyright ANuj Nautiyal
package com.example.demo.api;

import com.example.demo.limiting.RateLimitExceeded;
import com.example.demo.resilience.DependencyUnavailable;
import com.example.demo.weather.WeatherNotConfigured;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ProblemDetail;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Convert expected failures into Problem Details with stable application codes. */
@RestControllerAdvice
public class ApiExceptionHandler {
    /**
     * @param error denied request
     * @return HTTP 429 with a retry hint
     */
    @ExceptionHandler(RateLimitExceeded.class)
    public ResponseEntity<ProblemDetail> rateLimited(RateLimitExceeded error) {
        return ResponseEntity.status(429).header("Retry-After", Long.toString(error.retrySeconds()))
                .body(problem(429, "RATE_LIMITED", error.getMessage()));
    }

    /**
     * @param error breaker denial
     * @return HTTP 503 without executing the dependency
     */
    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ProblemDetail> circuitOpen(CallNotPermittedException error) {
        return ResponseEntity.status(503).body(problem(503, "CIRCUIT_OPEN", "Dependency is recovering; retry later"));
    }

    /**
     * @param error provider or database failure
     * @return a safe HTTP 503
     */
    @ExceptionHandler({DependencyUnavailable.class, DataAccessException.class})
    public ResponseEntity<ProblemDetail> unavailable(RuntimeException error) {
        return ResponseEntity.status(503).body(problem(503, "DEPENDENCY_UNAVAILABLE", "Dependency temporarily unavailable"));
    }

    /**
     * @param error missing key
     * @return HTTP 503 with configuration guidance
     */
    @ExceptionHandler(WeatherNotConfigured.class)
    public ResponseEntity<ProblemDetail> notConfigured(WeatherNotConfigured error) {
        return ResponseEntity.status(503).body(problem(503, "WEATHER_NOT_CONFIGURED", error.getMessage()));
    }

    /**
     * @param error invalid input
     * @return HTTP 400 without internal exception details
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> invalidRequest(IllegalArgumentException error) {
        return ResponseEntity.badRequest().body(problem(400, "INVALID_REQUEST", "Check the supplied request parameters"));
    }

    private ProblemDetail problem(int status, String code, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.valueOf(status), detail);
        problem.setTitle(code);
        problem.setProperty("code", code);
        return problem;
    }
}

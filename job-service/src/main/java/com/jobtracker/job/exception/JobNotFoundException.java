package com.jobtracker.job.exception;

/**
 * Thrown when a job id doesn't exist. Translated to HTTP 404 by the
 * GlobalExceptionHandler.
 */
public class JobNotFoundException extends RuntimeException {
    public JobNotFoundException(Long id) {
        super("Job not found with id " + id);
    }
}

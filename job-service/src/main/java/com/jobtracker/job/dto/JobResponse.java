package com.jobtracker.job.dto;

import com.jobtracker.job.model.Job;
import com.jobtracker.job.model.JobStatus;

import java.time.LocalDate;

/**
 * Returned to clients. Keeping a separate response DTO (instead of
 * leaking the JPA entity) means the API contract is decoupled from the
 * database schema.
 */
public record JobResponse(
        Long id,
        String company,
        JobStatus status,
        LocalDate dateApplied,
        String hrContact
) {
    public static JobResponse from(Job job) {
        return new JobResponse(
                job.getId(),
                job.getCompany(),
                job.getStatus(),
                job.getDateApplied(),
                job.getHrContact()
        );
    }
}

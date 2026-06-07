package com.jobtracker.job.dto;

import com.jobtracker.job.model.JobStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Incoming payload for creating/updating a job. A record keeps it immutable
 * and concise; bean-validation annotations are enforced by @Valid in the
 * controller before we ever touch the database.
 */
public record JobRequest(
        @NotBlank(message = "company is required")
        String company,

        @NotNull(message = "status is required")
        JobStatus status,

        @NotNull(message = "dateApplied is required")
        LocalDate dateApplied,

        String hrContact
) {}

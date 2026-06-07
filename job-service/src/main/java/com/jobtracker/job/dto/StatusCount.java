package com.jobtracker.job.dto;

import com.jobtracker.job.model.JobStatus;

/**
 * One slice of the pie chart: how many applications are in a given status.
 * Used both as a JPQL constructor-expression target and as JSON output.
 */
public record StatusCount(
        JobStatus status,
        long count
) {}

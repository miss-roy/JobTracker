package com.jobtracker.job.model;

/**
 * The lifecycle stages a job application can be in.
 * These map 1:1 to the tabs in the frontend.
 */
public enum JobStatus {
    APPLIED,
    INTERVIEWING,
    OFFERED,
    REJECTED,
    GHOSTED
}

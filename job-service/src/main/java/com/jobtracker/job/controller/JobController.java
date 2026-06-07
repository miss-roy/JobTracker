package com.jobtracker.job.controller;

import com.jobtracker.job.dto.JobRequest;
import com.jobtracker.job.dto.JobResponse;
import com.jobtracker.job.dto.StatusCount;
import com.jobtracker.job.model.JobStatus;
import com.jobtracker.job.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API for job applications. All paths are under /api so the gateway's
 * Path=/api/** predicate routes them here.
 */
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService service;

    /** GET /api/jobs          -> all jobs
     *  GET /api/jobs?status=APPLIED -> only that status (drives the tab views) */
    @GetMapping
    public List<JobResponse> getJobs(@RequestParam(required = false) JobStatus status) {
        return service.findAll(status);
    }

    /** GET /api/jobs/stats -> counts per status for the pie chart */
    @GetMapping("/stats")
    public List<StatusCount> getStats() {
        return service.stats();
    }

    @GetMapping("/{id}")
    public JobResponse getJob(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobResponse createJob(@Valid @RequestBody JobRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public JobResponse updateJob(@PathVariable Long id, @Valid @RequestBody JobRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

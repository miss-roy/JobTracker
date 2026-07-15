package com.jobtracker.job.controller;

import com.jobtracker.job.dto.JobRequest;
import com.jobtracker.job.dto.JobResponse;
import com.jobtracker.job.dto.StatusCount;
import com.jobtracker.job.model.JobStatus;
import com.jobtracker.job.model.User;
import com.jobtracker.job.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
 * REST API for job applications. Every endpoint operates only on the
 * authenticated user's data — the current user is injected via
 * @AuthenticationPrincipal (set by the JWT filter).
 */
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService service;

    @GetMapping
    public List<JobResponse> getJobs(@AuthenticationPrincipal User user,
                                     @RequestParam(required = false) JobStatus status) {
        return service.findAll(user.getId(), status);
    }

    @GetMapping("/stats")
    public List<StatusCount> getStats(@AuthenticationPrincipal User user) {
        return service.stats(user.getId());
    }

    @GetMapping("/{id}")
    public JobResponse getJob(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return service.findById(user.getId(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobResponse createJob(@AuthenticationPrincipal User user,
                                 @Valid @RequestBody JobRequest request) {
        return service.create(user.getId(), request);
    }

    @PutMapping("/{id}")
    public JobResponse updateJob(@AuthenticationPrincipal User user,
                                 @PathVariable Long id,
                                 @Valid @RequestBody JobRequest request) {
        return service.update(user.getId(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@AuthenticationPrincipal User user, @PathVariable Long id) {
        service.delete(user.getId(), id);
        return ResponseEntity.noContent().build();
    }
}

package com.jobtracker.job.service;

import com.jobtracker.job.dto.JobRequest;
import com.jobtracker.job.dto.JobResponse;
import com.jobtracker.job.dto.StatusCount;
import com.jobtracker.job.exception.JobNotFoundException;
import com.jobtracker.job.model.Job;
import com.jobtracker.job.model.JobStatus;
import com.jobtracker.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Business logic lives here, keeping controllers thin and the repository dumb.
 * Every method takes the current user's id so all data access is scoped to the
 * owner — a user can never see or touch another user's jobs.
 */
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository repository;

    @Transactional(readOnly = true)
    public List<JobResponse> findAll(Long userId, JobStatus status) {
        List<Job> jobs = (status == null)
                ? repository.findByUserIdOrderByDateAppliedDesc(userId)
                : repository.findByUserIdAndStatusOrderByDateAppliedDesc(userId, status);
        return jobs.stream().map(JobResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public JobResponse findById(Long userId, Long id) {
        return repository.findByIdAndUserId(id, userId)
                .map(JobResponse::from)
                .orElseThrow(() -> new JobNotFoundException(id));
    }

    @Transactional
    public JobResponse create(Long userId, JobRequest request) {
        Job job = Job.builder()
                .company(request.company())
                .status(request.status())
                .dateApplied(request.dateApplied())
                .hrContact(request.hrContact())
                .userId(userId)
                .build();
        return JobResponse.from(repository.save(job));
    }

    @Transactional
    public JobResponse update(Long userId, Long id, JobRequest request) {
        Job job = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new JobNotFoundException(id));
        job.setCompany(request.company());
        job.setStatus(request.status());
        job.setDateApplied(request.dateApplied());
        job.setHrContact(request.hrContact());
        return JobResponse.from(repository.save(job));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        if (!repository.existsByIdAndUserId(id, userId)) {
            throw new JobNotFoundException(id);
        }
        repository.deleteById(id);
    }

    /**
     * Counts per status for this user's pie chart. Starts from a full map so
     * every status appears (with 0) even when it has no applications yet.
     */
    @Transactional(readOnly = true)
    public List<StatusCount> stats(Long userId) {
        Map<JobStatus, Long> counts = new EnumMap<>(JobStatus.class);
        for (JobStatus status : JobStatus.values()) {
            counts.put(status, 0L);
        }
        for (StatusCount row : repository.countGroupedByStatus(userId)) {
            counts.put(row.status(), row.count());
        }
        return counts.entrySet().stream()
                .map(e -> new StatusCount(e.getKey(), e.getValue()))
                .toList();
    }
}

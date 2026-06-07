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
 */
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository repository;

    @Transactional(readOnly = true)
    public List<JobResponse> findAll(JobStatus status) {
        List<Job> jobs = (status == null)
                ? repository.findAllByOrderByDateAppliedDesc()
                : repository.findByStatusOrderByDateAppliedDesc(status);
        return jobs.stream().map(JobResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public JobResponse findById(Long id) {
        return repository.findById(id)
                .map(JobResponse::from)
                .orElseThrow(() -> new JobNotFoundException(id));
    }

    @Transactional
    public JobResponse create(JobRequest request) {
        Job job = Job.builder()
                .company(request.company())
                .status(request.status())
                .dateApplied(request.dateApplied())
                .hrContact(request.hrContact())
                .build();
        return JobResponse.from(repository.save(job));
    }

    @Transactional
    public JobResponse update(Long id, JobRequest request) {
        Job job = repository.findById(id)
                .orElseThrow(() -> new JobNotFoundException(id));
        job.setCompany(request.company());
        job.setStatus(request.status());
        job.setDateApplied(request.dateApplied());
        job.setHrContact(request.hrContact());
        return JobResponse.from(repository.save(job));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new JobNotFoundException(id);
        }
        repository.deleteById(id);
    }

    /**
     * Counts per status for the pie chart. We start from a full map so every
     * status appears (with 0) even when it has no applications yet.
     */
    @Transactional(readOnly = true)
    public List<StatusCount> stats() {
        Map<JobStatus, Long> counts = new EnumMap<>(JobStatus.class);
        for (JobStatus status : JobStatus.values()) {
            counts.put(status, 0L);
        }
        for (StatusCount row : repository.countGroupedByStatus()) {
            counts.put(row.status(), row.count());
        }
        return counts.entrySet().stream()
                .map(e -> new StatusCount(e.getKey(), e.getValue()))
                .toList();
    }
}

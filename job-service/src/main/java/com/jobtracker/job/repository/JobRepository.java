package com.jobtracker.job.repository;

import com.jobtracker.job.dto.StatusCount;
import com.jobtracker.job.model.Job;
import com.jobtracker.job.model.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Spring Data JPA provides all the CRUD for free just by extending JpaRepository.
 * Only the extra queries are declared here.
 */
public interface JobRepository extends JpaRepository<Job, Long> {

    // Derived query: Spring generates "where status = ?" from the method name.
    List<Job> findByStatusOrderByDateAppliedDesc(JobStatus status);

    List<Job> findAllByOrderByDateAppliedDesc();

    // Aggregate the counts in the database (one query) instead of in Java.
    // Hibernate 6 maps the result rows straight into the StatusCount record.
    @Query("""
            SELECT new com.jobtracker.job.dto.StatusCount(j.status, COUNT(j))
            FROM Job j
            GROUP BY j.status
            """)
    List<StatusCount> countGroupedByStatus();
}

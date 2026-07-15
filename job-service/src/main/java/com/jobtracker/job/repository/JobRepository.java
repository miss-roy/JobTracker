package com.jobtracker.job.repository;

import com.jobtracker.job.dto.StatusCount;
import com.jobtracker.job.model.Job;
import com.jobtracker.job.model.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA provides all the CRUD for free just by extending JpaRepository.
 * Every query here is scoped by userId so a user only ever touches their own jobs.
 */
public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByUserIdOrderByDateAppliedDesc(Long userId);

    List<Job> findByUserIdAndStatusOrderByDateAppliedDesc(Long userId, JobStatus status);

    // Ownership-checked lookup: only returns the job if it belongs to the user.
    Optional<Job> findByIdAndUserId(Long id, Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);

    // Counts per status for one user (drives that user's pie chart).
    @Query("""
            SELECT new com.jobtracker.job.dto.StatusCount(j.status, COUNT(j))
            FROM Job j
            WHERE j.userId = :userId
            GROUP BY j.status
            """)
    List<StatusCount> countGroupedByStatus(@Param("userId") Long userId);

    // Used by the data seeder to adopt pre-auth (ownerless) jobs.
    List<Job> findByUserIdIsNull();

    long countByUserId(Long userId);
}

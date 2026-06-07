package com.jobtracker.job.config;

import com.jobtracker.job.model.Job;
import com.jobtracker.job.model.JobStatus;
import com.jobtracker.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

/**
 * Seeds a few sample applications, but only when the table is empty AND
 * app.seed-sample-data is true (the default). With a persistent Postgres
 * database this effectively runs once; disable it (APP_SEED_SAMPLE_DATA=false)
 * once you have real data you don't want demo rows mixed into.
 */
@Configuration
@ConditionalOnProperty(name = "app.seed-sample-data", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final JobRepository repository;

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        repository.saveAll(List.of(
                Job.builder().company("Acme Corp").status(JobStatus.APPLIED)
                        .dateApplied(LocalDate.now().minusDays(2)).hrContact("hr@acme.com").build(),
                Job.builder().company("Globex").status(JobStatus.INTERVIEWING)
                        .dateApplied(LocalDate.now().minusDays(7)).hrContact("recruit@globex.io").build(),
                Job.builder().company("Initech").status(JobStatus.OFFERED)
                        .dateApplied(LocalDate.now().minusDays(14)).hrContact("peter@initech.com").build(),
                Job.builder().company("Umbrella").status(JobStatus.REJECTED)
                        .dateApplied(LocalDate.now().minusDays(21)).hrContact("careers@umbrella.co").build(),
                Job.builder().company("Hooli").status(JobStatus.GHOSTED)
                        .dateApplied(LocalDate.now().minusDays(30)).hrContact("gavin@hooli.com").build(),
                Job.builder().company("Stark Industries").status(JobStatus.APPLIED)
                        .dateApplied(LocalDate.now().minusDays(1)).hrContact("pepper@stark.com").build()
        ));
    }
}

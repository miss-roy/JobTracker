package com.jobtracker.job.config;

import com.jobtracker.job.model.Job;
import com.jobtracker.job.model.JobStatus;
import com.jobtracker.job.model.User;
import com.jobtracker.job.repository.JobRepository;
import com.jobtracker.job.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Seeds a "demo" account (demo / demo1234) so recruiters can log in and see a
 * populated board without registering. Runs only when app.seed-sample-data is
 * true (the default) and is idempotent.
 *
 * It also adopts any pre-auth jobs (rows that existed before ownership was
 * added) into the demo account, so upgrading a live database keeps its data.
 */
@Configuration
@ConditionalOnProperty(name = "app.seed-sample-data", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final JobRepository jobs;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        User demo = users.findByUsername("demo").orElseGet(() ->
                users.save(User.builder()
                        .username("demo")
                        .password(passwordEncoder.encode("demo1234"))
                        .build()));

        // Adopt jobs created before ownership existed.
        List<Job> orphans = jobs.findByUserIdIsNull();
        if (!orphans.isEmpty()) {
            orphans.forEach(j -> j.setUserId(demo.getId()));
            jobs.saveAll(orphans);
        }

        // Give the demo account a sample board if it has none.
        if (jobs.countByUserId(demo.getId()) == 0) {
            jobs.saveAll(List.of(
                    Job.builder().company("Acme Corp").status(JobStatus.APPLIED)
                            .dateApplied(LocalDate.now().minusDays(2)).hrContact("hr@acme.com").userId(demo.getId()).build(),
                    Job.builder().company("Globex").status(JobStatus.INTERVIEWING)
                            .dateApplied(LocalDate.now().minusDays(7)).hrContact("recruit@globex.io").userId(demo.getId()).build(),
                    Job.builder().company("Initech").status(JobStatus.OFFERED)
                            .dateApplied(LocalDate.now().minusDays(14)).hrContact("peter@initech.com").userId(demo.getId()).build(),
                    Job.builder().company("Umbrella").status(JobStatus.REJECTED)
                            .dateApplied(LocalDate.now().minusDays(21)).hrContact("careers@umbrella.co").userId(demo.getId()).build(),
                    Job.builder().company("Hooli").status(JobStatus.GHOSTED)
                            .dateApplied(LocalDate.now().minusDays(30)).hrContact("gavin@hooli.com").userId(demo.getId()).build(),
                    Job.builder().company("Stark Industries").status(JobStatus.APPLIED)
                            .dateApplied(LocalDate.now().minusDays(1)).hrContact("pepper@stark.com").userId(demo.getId()).build()
            ));
        }
    }
}

package com.jobtracker.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The one service that actually owns the domain: job applications.
 * Layered as controller -> service -> repository -> H2.
 */
@SpringBootApplication
public class JobServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(JobServiceApplication.class, args);
    }
}

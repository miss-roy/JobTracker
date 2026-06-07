package com.jobtracker.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Single front door for all clients. The browser only ever talks to :8080;
 * the gateway forwards /api/** to job-service (resolved through Eureka),
 * so backend services can scale or move without the frontend knowing.
 */
@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}

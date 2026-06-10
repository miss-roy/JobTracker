package com.jobtracker.job.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Stamps every response with an "X-Served-By" header naming the job-service
 * instance that handled the request, making the gateway's client-side load
 * balancing visible across replicas.
 */
@Component
public class InstanceHeaderFilter extends OncePerRequestFilter {

    private final String instanceId = resolveInstanceId();

    private static String resolveInstanceId() {
        String env = System.getenv("HOSTNAME"); // Docker sets this to the container id
        if (env != null && !env.isBlank()) {
            return env;
        }
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        response.setHeader("X-Served-By", instanceId);
        chain.doFilter(request, response);
    }
}

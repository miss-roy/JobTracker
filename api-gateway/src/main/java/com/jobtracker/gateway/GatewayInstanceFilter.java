package com.jobtracker.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetAddress;

/**
 * Stamps every response with an "X-Gateway" header identifying which gateway
 * replica handled it. Purely for demonstrating that the edge load balancer
 * spreads traffic across multiple gateway instances.
 */
@Component
public class GatewayInstanceFilter implements GlobalFilter, Ordered {

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
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Add the header just before the response is committed, so it isn't
        // overwritten while the gateway proxies the downstream response.
        exchange.getResponse().beforeCommit(() -> {
            exchange.getResponse().getHeaders().add("X-Gateway", instanceId);
            return Mono.empty();
        });
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}

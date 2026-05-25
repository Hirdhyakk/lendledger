package com.lendledger.gateway.filter;

import com.lendledger.common.Constants;
import com.lendledger.common.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class JwtAuthGatewayFilter implements GlobalFilter, Ordered {
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/auth/login", "/api/auth/register", "/api/auth/refresh"
    );

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthGatewayFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (path.startsWith("/actuator") || PUBLIC_PATHS.contains(path)) {
            return chain.filter(exchange);
        }
        if (!path.startsWith("/api/")) {
            return chain.filter(exchange);
        }
        List<String> authHeaders = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (authHeaders == null || authHeaders.isEmpty() || !authHeaders.get(0).startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        try {
            String token = authHeaders.get(0).substring(7);
            Claims claims = jwtTokenProvider.parseToken(token);
            if (!jwtTokenProvider.isAccessToken(claims)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            ServerWebExchange mutated = exchange.mutate()
                    .request(r -> r.header(Constants.HEADER_USER_ID, claims.getSubject())
                            .header(Constants.HEADER_ROLE, claims.get("role", String.class)))
                    .build();
            return chain.filter(mutated);
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}

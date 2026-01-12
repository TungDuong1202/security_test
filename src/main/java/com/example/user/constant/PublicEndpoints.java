package com.example.user.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpMethod;

import java.util.List;

/**
 * Danh sách các endpoint KHÔNG cần JWT
 * (Public APIs)
 */
public final class PublicEndpoints {
    private PublicEndpoints() {}

    @Getter
    @AllArgsConstructor
    public static class Endpoint {
        private String pattern;
        private HttpMethod method; // Null = All methods

        public Endpoint(String pattern) {
            this.pattern = pattern;
            this.method = null;
        }
    }

    public static final List<Endpoint> PUBLIC_API = List.of(
            new Endpoint("/api/auth/register", HttpMethod.POST),
            new Endpoint("/api/auth/login", HttpMethod.POST),
            new Endpoint("/api/transactions/**"),
            new Endpoint("/v3/api-docs/**"),
            new Endpoint("/swagger-ui/**"),
            new Endpoint("/swagger-ui.html"),
            new Endpoint("/swagger-resources/**"),
            new Endpoint("/webjars/**")
    );
}


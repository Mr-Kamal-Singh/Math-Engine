package com.engine.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    // The header name we will look for
    private static final String API_KEY_HEADER = "X-API-KEY";

    private final String VALID_API_KEY = System.getenv("API_KEY") != null ? System.getenv("API_KEY") : "local-test-key";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Always let CORS preflight requests through safely
        if ("OPTIONS".equals(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Check the header for the API Key
        String requestApiKey = request.getHeader(API_KEY_HEADER);

        // 3. The Bouncer Logic
        if (VALID_API_KEY.equals(requestApiKey)) {
            // Key is good! Let them into the Controller.
            filterChain.doFilter(request, response);
        } else {
            // Bad or missing key! Kick them out immediately.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("401 Unauthorized: Invalid or missing X-API-KEY");
        }
    }
}

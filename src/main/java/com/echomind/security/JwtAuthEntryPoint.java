package com.echomind.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Handles what happens when an UNAUTHENTICATED request hits a protected endpoint.
 *
 * Without this, Spring Security's default behavior:
 * - For browser requests: redirects to /login (we don't have a login page — we're a REST API)
 * - For API requests: returns a generic 403 with no useful body
 *
 * With this, unauthenticated requests get a clean JSON 401:
 * {
 *   "timestamp": "...",
 *   "status": 401,
 *   "error": "Unauthorized",
 *   "message": "Authentication required to access this resource"
 * }
 *
 * 401 vs 403:
 * - 401 Unauthorized = "I don't know who you are" (no/invalid credentials)
 * - 403 Forbidden = "I know who you are, but you're not allowed" (insufficient role)
 *
 * This class handles 401. For 403, Spring Security's AccessDeniedHandler kicks in
 * (we handle it in GlobalExceptionHandler).
 */
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String json = String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Authentication required to access this resource",
                request.getServletPath()
        );

        response.getWriter().write(json);
    }
}

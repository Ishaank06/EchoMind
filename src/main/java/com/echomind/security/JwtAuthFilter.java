package com.echomind.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter — the gatekeeper for every HTTP request.
 *
 * WHERE IT SITS IN THE PIPELINE:
 *
 *   HTTP Request
 *       │
 *       ▼
 *   [Spring Security Filter Chain]
 *       │
 *       ├── CorsFilter
 *       ├── CsrfFilter (disabled for stateless APIs)
 *       ├── ══════════════════════════════
 *       ├── ║  JwtAuthFilter (THIS CLASS) ║  ← We insert here
 *       ├── ══════════════════════════════
 *       ├── UsernamePasswordAuthenticationFilter (default Spring login, we skip)
 *       ├── ExceptionTranslationFilter
 *       └── AuthorizationFilter
 *       │
 *       ▼
 *   Controller
 *
 * WHY OncePerRequestFilter?
 * A regular Filter can execute multiple times per request (e.g., when the request
 * is internally forwarded/dispatched). OncePerRequestFilter guarantees our JWT
 * validation logic runs exactly once, regardless of internal dispatches.
 *
 * WHAT THIS FILTER DOES (for every request):
 * 1. Check if Authorization header exists and starts with "Bearer "
 * 2. If no header → skip (let SecurityConfig decide if the endpoint is public)
 * 3. If header exists → extract JWT → validate → load user → set SecurityContext
 * 4. Call filterChain.doFilter() to continue to the next filter
 *
 * THE SecurityContext IS EVERYTHING:
 * After this filter sets SecurityContextHolder.getContext().setAuthentication(...),
 * Spring Security considers the request "authenticated". All downstream components
 * (@PreAuthorize, controller methods, SecurityContext lookups) can see who the user is.
 * Without this, the request is anonymous and protected endpoints return 401.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // Step 1: Extract the Authorization header
        final String authHeader = request.getHeader("Authorization");

        // Step 2: If no Bearer token, skip JWT processing entirely.
        // The request continues through the filter chain.
        // If the endpoint is public (/auth/**, /actuator/health), it proceeds normally.
        // If it's protected, Spring Security will return 401 via JwtAuthEntryPoint.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3: Extract the token (everything after "Bearer ")
        final String jwt = authHeader.substring(7);

        // Step 4: Extract email from token claims
        final String email = jwtService.extractEmail(jwt);

        // Step 5: Only proceed if we got an email AND no authentication exists yet.
        // The second check prevents re-processing if another filter already authenticated.
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Step 6: Load full user details from the database.
            // Why not just trust the JWT claims? Because:
            // - The user might have been deleted/disabled after the JWT was issued
            // - The role might have changed
            // - DB is the source of truth, JWT is a short-lived credential
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // Step 7: Validate the token (signature + expiration)
            if (jwtService.validateToken(jwt)) {

                // Step 8: Create an Authentication object.
                // UsernamePasswordAuthenticationToken with 3 args = "authenticated"
                // (the 2-arg constructor means "not yet authenticated")
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,          // principal (the "who")
                                null,                 // credentials (null — JWT already verified)
                                userDetails.getAuthorities()  // roles/permissions
                        );

                // Attach request details (IP address, session ID) for audit logging
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Step 9: SET THE SECURITY CONTEXT.
                // This is the critical line. After this, Spring Security considers
                // this request authenticated. @PreAuthorize checks, SecurityContext
                // lookups, and controller access all work.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Step 10: Continue the filter chain regardless of outcome.
        // If we set the context → request proceeds as authenticated.
        // If we didn't → request proceeds as anonymous → 401 if protected.
        filterChain.doFilter(request, response);
    }
}

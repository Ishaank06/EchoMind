package com.echomind.config;

import com.echomind.security.JwtAuthEntryPoint;
import com.echomind.security.JwtAuthFilter;
import com.echomind.security.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Central security configuration for EchoMind.
 *
 * This replaces the old WebSecurityConfigurerAdapter (deprecated since Spring Security 5.7).
 * Modern Spring Security uses SecurityFilterChain beans — composable and testable.
 *
 * @EnableWebSecurity: activates Spring Security's web integration.
 * Without this, the SecurityFilterChain bean would be registered but
 * not wired into the servlet filter chain.
 *
 * @EnableMethodSecurity: enables @PreAuthorize, @PostAuthorize, @Secured
 * on individual controller methods. Without this, those annotations are ignored.
 * We use @PreAuthorize("hasRole('ADMIN')") for role-based method security.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    /**
     * Optional: only present when OAuth2 client properties are configured
     * (i.e., when running with the "oauth" profile and GITHUB_CLIENT_ID set).
     *
     * @Autowired(required = false) means: inject this if it exists, otherwise null.
     * Without this, Spring would crash at startup trying to find a
     * ClientRegistrationRepository bean when no OAuth2 registrations are configured.
     */
    @Autowired(required = false)
    private ClientRegistrationRepository clientRegistrationRepository;

    /**
     * The SecurityFilterChain defines HOW requests are secured.
     *
     * Think of it as a series of rules applied in order:
     * 1. Is CSRF needed? (No — we're stateless)
     * 2. Which endpoints are public?
     * 3. How are sessions managed? (Stateless — no server-side sessions)
     * 4. What happens when auth fails? (JwtAuthEntryPoint → JSON 401)
     * 5. Where does our JWT filter sit? (Before UsernamePasswordAuthenticationFilter)
     * 6. How does OAuth2 login work? (GitHub → our success handler, if configured)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ── CSRF ──────────────────────────────────────────
                // CSRF protection prevents cross-site request forgery by requiring
                // a token in forms. But we're a STATELESS REST API:
                // - No cookies (JWT is in Authorization header)
                // - No server-side sessions
                // - No HTML forms
                // CSRF doesn't apply to stateless APIs and would break our clients.
                .csrf(csrf -> csrf.disable())

                // ── Authorization Rules ────────────────────────────
                // Order matters: more specific rules first, catch-all last.
                .authorizeHttpRequests(auth -> auth
                        // Auth endpoints: MUST be public (you can't require auth to log in!)
                        .requestMatchers("/auth/**").permitAll()

                        // Actuator health: public for monitoring/load balancers
                        .requestMatchers("/actuator/health").permitAll()

                        // OAuth2 endpoints: Spring Security's internal OAuth flow
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                        // Everything else: must be authenticated
                        .anyRequest().authenticated()
                )

                // ── Session Management ─────────────────────────────
                // STATELESS: Spring Security won't create or use HTTP sessions.
                // Every request is independently authenticated via JWT.
                // This is essential for horizontal scaling — no sticky sessions needed.
                // Without this, Spring creates sessions and your JWT filter would
                // fight with session-based authentication.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ── Exception Handling ─────────────────────────────
                // When an unauthenticated request hits a protected endpoint,
                // Spring calls our JwtAuthEntryPoint (clean JSON 401).
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(jwtAuthEntryPoint)
                );

        // ── OAuth2 Login (conditional) ─────────────────────
        // Only enable OAuth2 if client registrations exist (i.e., the "oauth"
        // profile is active with GITHUB_CLIENT_ID set).
        // Without this check, .oauth2Login() would crash when no
        // ClientRegistrationRepository bean is available.
        if (clientRegistrationRepository != null) {
            http.oauth2Login(oauth2 ->
                    oauth2.successHandler(oAuth2LoginSuccessHandler)
            );
        }

        // ── JWT Filter Registration ────────────────────────
        // Insert our JwtAuthFilter BEFORE Spring's default
        // UsernamePasswordAuthenticationFilter.
        // This ensures JWT is checked before Spring tries form-based auth.
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * AuthenticationManager bean — needed by AuthService for programmatic auth.
     *
     * Spring Boot auto-configures this internally, but we expose it as a bean
     * so it can be injected into services that need to trigger authentication
     * programmatically (e.g., for future "change password" or "re-authenticate" flows).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

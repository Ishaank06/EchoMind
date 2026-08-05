package com.echomind.controller;

import com.echomind.dto.AuthResponse;
import com.echomind.dto.LoginRequest;
import com.echomind.dto.SignupRequest;
import com.echomind.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 *
 * Base path: /auth (NOT /api/auth)
 * Why? Convention: /api/** is for authenticated, resource-based endpoints.
 * /auth/** is for authentication itself — it MUST be public, so separating
 * it from /api/** makes SecurityConfig rules cleaner:
 *   .requestMatchers("/auth/**").permitAll()
 *   .anyRequest().authenticated()
 *
 * This controller follows the same pattern as UserController:
 * - No business logic (delegates to AuthService)
 * - No try/catch (GlobalExceptionHandler handles errors)
 * - Just HTTP in → service call → HTTP out
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /auth/signup — Register a new user
     *
     * @Valid triggers Bean Validation on SignupRequest:
     * - @NotBlank on name, email, password
     * - @Email on email
     * - @Size(min=8) on password
     *
     * Returns 201 Created with JWT — user is immediately logged in.
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /auth/login — Authenticate and receive JWT
     *
     * Returns 200 OK with JWT on success.
     * Returns 401 Unauthorized on invalid credentials
     * (handled by GlobalExceptionHandler catching BadCredentialsException).
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /auth/oauth2/success — OAuth2 callback endpoint
     *
     * After GitHub OAuth2 login, OAuth2LoginSuccessHandler redirects here
     * with the JWT as a query parameter. This endpoint simply returns it
     * as JSON so the client can extract and store it.
     *
     * In production with a React frontend, the success handler would redirect
     * to the frontend URL (e.g., http://localhost:3000/oauth/callback?token=...)
     * and the frontend would extract the token from the URL.
     */
    @GetMapping("/oauth2/success")
    public ResponseEntity<AuthResponse> oauthSuccess(@RequestParam String token) {
        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .build());
    }
}

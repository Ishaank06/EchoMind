package com.echomind.service;

import com.echomind.dto.AuthResponse;
import com.echomind.dto.LoginRequest;
import com.echomind.dto.SignupRequest;
import com.echomind.entity.Role;
import com.echomind.entity.User;
import com.echomind.exception.DuplicateResourceException;
import com.echomind.repository.UserRepository;
import com.echomind.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for authentication operations: signup and login.
 *
 * Why a separate AuthService instead of adding to UserService?
 * - Single Responsibility: UserService manages user CRUD, AuthService manages auth
 * - Different dependencies: AuthService needs PasswordEncoder + JwtService,
 *   UserService doesn't (and shouldn't)
 * - Different transaction boundaries: signup needs hashing inside the transaction,
 *   login doesn't need a write transaction at all
 * - Later: AuthService may handle refresh tokens, password reset, OAuth linking —
 *   all auth-specific concerns that would bloat UserService
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Registers a new user.
     *
     * Flow:
     * 1. Check duplicate email (reuses existing DuplicateResourceException)
     * 2. Hash password with BCrypt (NEVER store plain text)
     * 3. Build User entity with ROLE_USER default
     * 4. Save to database
     * 5. Generate JWT so the user is immediately logged in after signup
     *    (no need for a separate login call — better UX)
     *
     * Why hash BEFORE saving?
     * If you save first and hash later, there's a window where the plain-text
     * password exists in the database. Even if it's milliseconds, it's a risk
     * (what if the app crashes between save and hash?). Hash first, save the hash.
     */
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        // Check for duplicate email
        userRepository.findByEmail(request.getEmail())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("User", "email", request.getEmail());
                });

        // Build user with hashed password
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .build();

        User savedUser = userRepository.save(user);

        // Generate JWT for immediate login
        String token = jwtService.generateToken(savedUser);

        return AuthResponse.builder()
                .token(token)
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .build();
    }

    /**
     * Authenticates an existing user.
     *
     * Flow:
     * 1. Find user by email (or throw)
     * 2. Verify password with BCrypt matches()
     * 3. Generate and return JWT
     *
     * Why we throw the SAME error for "email not found" and "wrong password":
     * If we said "email not found" for missing emails and "wrong password" for
     * wrong passwords, an attacker could enumerate valid emails by trying random
     * addresses and checking which error they get.
     *
     * Production convention: always say "Invalid email or password" for both cases.
     * This is called a "generic credential error" — it reveals nothing about which
     * part of the credentials was wrong.
     *
     * Why PasswordEncoder.matches() instead of encoding the input and comparing?
     * BCrypt generates a RANDOM salt for each hash. So:
     *   encode("hello123") → "$2a$10$abc..."
     *   encode("hello123") → "$2a$10$xyz..."  (different salt → different hash!)
     * matches() extracts the salt from the stored hash and re-hashes the candidate
     * with that same salt, then compares. This is the only correct way to verify.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // Find user — same error message whether email or password is wrong
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // Verify password — check the user has a password (OAuth users don't)
        if (user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // Generate JWT
        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}

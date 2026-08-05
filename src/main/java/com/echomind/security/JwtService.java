package com.echomind.security;

import com.echomind.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Service responsible for JWT creation, validation, and parsing.
 *
 * Where this fits in the request lifecycle:
 *
 * LOGIN:  AuthService → JwtService.generateToken() → token sent to client
 * FUTURE: JwtAuthFilter → JwtService.validateToken() + extractEmail() → SecurityContext
 *
 * JWT Structure (three Base64-encoded parts separated by dots):
 *   eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJpc2hhYW5AZXhhbXBsZS5jb20iLCJpZCI6Ii4uLiJ9.signature
 *   ├── Header ──────────┤├── Payload (Claims) ────────────────────────────────┤├── Signature ┤
 *
 * Header:  {"alg":"HS256"}                    — signing algorithm
 * Payload: {"sub":"email", "id":"uuid",       — claims (data we embed)
 *           "role":"ROLE_USER", "iat":...,     — issued-at timestamp
 *           "exp":...}                         — expiration timestamp
 * Signature: HMAC-SHA256(header + payload, secret_key) — tamper detection
 *
 * SECURITY: The payload is NOT encrypted — anyone can Base64-decode it.
 * The signature only proves the token wasn't tampered with.
 * NEVER put passwords, SSNs, or secrets in JWT claims.
 */
@Service
public class JwtService {

    /**
     * @Value reads from application.properties, which uses ${JWT_SECRET:default}
     * syntax to pull from environment variables first, falling back to a dev default.
     *
     * In production, JWT_SECRET must be:
     * - At least 256 bits (32+ characters) for HMAC-SHA256
     * - Randomly generated (not "mysecret" or "password123")
     * - Never committed to source control
     * - Rotated periodically
     */
    @Value("${app.jwt.secret}")
    private String secretKey;

    /**
     * Token expiration in milliseconds.
     * Default: 86400000 = 24 hours.
     * In production, shorter is better (15-60 minutes) with refresh tokens.
     * For development, 24 hours avoids constant re-login.
     */
    @Value("${app.jwt.expiration}")
    private long expirationMs;

    /**
     * Generates a JWT for a given user.
     *
     * Claims included:
     * - sub (subject): email — the standard JWT claim for "who is this token for"
     * - id: user UUID — useful for API calls that need the user's ID without a DB lookup
     * - role: authorization level — so the filter can set authorities without a DB query
     *
     * Claims NOT included:
     * - password (never!)
     * - name (not needed for auth decisions; fetch from DB when needed)
     */
    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("id", user.getId().toString())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts the email (subject claim) from a token.
     * Used by JwtAuthFilter to identify which user the request belongs to.
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Validates a token by attempting to parse it.
     * JJWT throws specific exceptions for each failure mode:
     * - ExpiredJwtException: token's exp claim is in the past
     * - MalformedJwtException: token isn't valid JWT format
     * - SecurityException: signature doesn't match (tampered!)
     * - IllegalArgumentException: token is null/empty
     *
     * We catch all and return false. The JwtAuthFilter will handle
     * the "what to do when invalid" logic (let the request proceed
     * without authentication, so Spring Security returns 401).
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Parses the token and returns all claims.
     *
     * verifyWith(key) ensures the signature matches.
     * If someone modifies the payload (e.g., changes role to ROLE_ADMIN),
     * the signature won't match and parsing throws SecurityException.
     * This is the core tamper-detection mechanism of JWT.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Converts the String secret into a cryptographic key.
     * Keys.hmacShaKeyFor() ensures the key is the right length for HS256.
     * If the secret is too short, it throws an error at startup — fail fast.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
}

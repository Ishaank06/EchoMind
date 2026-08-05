package com.echomind.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Maps to the "users" table in PostgreSQL.
 *
 * Why "users" and not "user"?
 * "user" is a reserved keyword in PostgreSQL (and most SQL databases).
 * If you used @Table(name = "user"), every query would need quoting:
 * SELECT * FROM "user" — fragile and error-prone.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * UUID vs auto-increment Long:
     * - UUIDs are globally unique — safe for distributed systems, merging databases,
     *   and exposing in URLs (no sequential guessing).
     * - Auto-increment is simpler and faster for joins/indexing, but leaks info
     *   (user count, creation order) and breaks in multi-database scenarios.
     * - For EchoMind, UUID is the right choice since we'll expose IDs in APIs.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * @Column(nullable = false) is a DB-level constraint.
     * This is DIFFERENT from @NotBlank (which is a validation-layer constraint).
     * Both matter:
     * - @NotBlank catches bad input at the API boundary (before hitting the DB)
     * - nullable = false is a safety net at the database level
     * We'll add @NotBlank on the DTO, not here — entities shouldn't do input validation.
     */
    @Column(nullable = false)
    private String name;

    /**
     * unique = true creates a UNIQUE constraint in PostgreSQL.
     * Hibernate's ddl-auto=update will add this constraint automatically.
     * This prevents duplicate emails at the database level — even if your
     * application code has a bug, the DB is the last line of defense.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Password hash (BCrypt).
     *
     * NULLABLE because OAuth2 users (GitHub login) don't have a password.
     * They authenticate via GitHub — we never see or store their password.
     * If this were non-null, every GitHub user would need a dummy password,
     * which is a security smell.
     *
     * NEVER store plain-text passwords. This field holds the BCrypt hash:
     *   "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
     * BCrypt is a one-way function — you cannot reverse it to get the password.
     * You can only CHECK if a candidate password matches the hash.
     */
    @Column(name = "password")
    private String password;

    /**
     * @Enumerated(EnumType.STRING) stores the enum as a string in the DB:
     *   "ROLE_USER" instead of 0, "ROLE_ADMIN" instead of 1
     *
     * Why STRING and not ORDINAL?
     * - ORDINAL stores the enum's position (0, 1, 2...)
     * - If you reorder the enum or insert a new value in the middle,
     *   all existing rows break silently (0 now maps to a different role)
     * - STRING is human-readable in the DB and immune to reordering
     * - The only downside: slightly more storage (~10 bytes vs 4 bytes)
     *   This is never worth optimizing.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.ROLE_USER;

    /**
     * OAuth2 provider name (e.g., "github", "google").
     * Null for users who signed up with email/password.
     *
     * Why track this?
     * 1. A user who signed up via GitHub cannot "login" with a password
     *    (they don't have one). The login flow needs to know this.
     * 2. Later, when EchoMind adds Google OAuth, this distinguishes providers.
     * 3. Prevents conflicts: if ishaan@gmail.com signs up with password,
     *    then later tries GitHub OAuth with the same email, we need to
     *    know the account already exists via a different provider.
     */
    @Column(name = "provider")
    private String provider;

    /**
     * The user's ID on the OAuth provider (e.g., GitHub's numeric user ID).
     * Combined with 'provider', this uniquely identifies an OAuth account.
     */
    @Column(name = "provider_id")
    private String providerId;
}

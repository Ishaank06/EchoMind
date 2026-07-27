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
}

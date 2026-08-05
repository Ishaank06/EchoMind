package com.echomind.entity;

/**
 * User roles for authorization.
 *
 * Why ROLE_USER and not just USER?
 * Spring Security's hasRole("USER") internally checks for the authority "ROLE_USER".
 * This is a Spring convention dating back to early Spring Security versions.
 *
 * If you store "USER" in the database but Spring checks for "ROLE_USER",
 * authorization silently fails. By storing the full "ROLE_" prefix, the
 * enum value matches exactly what Spring Security expects. No surprises.
 *
 * Alternative: use hasAuthority("USER") which checks the exact string.
 * But mixing hasRole() and hasAuthority() in a codebase creates confusion.
 * Pick one convention and stick with it — we use the ROLE_ prefix.
 *
 * Why an enum instead of a String?
 * - Compile-time safety: typos like "ROLE_UESR" are caught by the compiler
 * - Finite set: you can't accidentally create "ROLE_SUPERADMIN" without updating this enum
 * - IDE autocomplete: discoverable without reading docs
 * - Later: when EchoMind adds ROLE_ADMIN for managing connectors, you add it here once
 */
public enum Role {
    ROLE_USER,
    ROLE_ADMIN
}

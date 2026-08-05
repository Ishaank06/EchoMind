package com.echomind.dto;

import lombok.*;

/**
 * DTO for authentication responses (signup and login).
 *
 * Returns the JWT token + basic user info so the client can:
 * 1. Store the token (localStorage, cookie, or in-memory)
 * 2. Display the user's email/role in the UI
 * 3. Make role-based UI decisions (show admin panel or not)
 *
 * What's NOT included:
 * - password (obviously)
 * - user ID (available in the JWT claims if the client decodes it)
 * - full user profile (that's what GET /api/users/{id} is for)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String email;
    private String role;
}

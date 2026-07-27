package com.echomind.dto;

import lombok.*;

import java.util.UUID;

/**
 * DTO for outgoing user responses.
 *
 * Why a separate response DTO?
 * Right now, User only has id, name, email — the response looks identical.
 * But later, User will gain fields like:
 * - passwordHash (auth phase — NEVER expose this)
 * - internalFlags, audit timestamps (not relevant to API consumers)
 *
 * The response DTO acts as a whitelist: only fields explicitly included
 * here are serialized to JSON. This is "secure by default" — new entity
 * fields don't leak into the API unless you consciously add them here.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID id;
    private String name;
    private String email;
}

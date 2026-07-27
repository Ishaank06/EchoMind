package com.echomind.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO for incoming user creation requests.
 *
 * Why a DTO instead of accepting the User entity directly?
 * 1. SECURITY: The entity has an 'id' field. If you accept User as input,
 *    a client could send {"id": "some-uuid", "name": "...", "email": "..."}
 *    and potentially overwrite an existing user (mass assignment vulnerability).
 *
 * 2. DECOUPLING: If you add fields to User later (createdAt, roles, etc.),
 *    you don't want those to become part of the create API automatically.
 *    The DTO defines the API contract independently from the DB schema.
 *
 * 3. VALIDATION: Validation annotations belong here, not on the entity.
 *    The entity represents database structure; the DTO represents API input rules.
 *    Same data, different responsibilities.
 *
 * Later phases will add more DTOs:
 * - LoginRequestDto (auth phase)
 * - ConnectorRequestDto (GitHub connector phase)
 * - SearchQueryDto (search phase)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;
}

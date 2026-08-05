package com.echomind.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO for user registration (signup).
 *
 * Why a separate DTO from CreateUserRequest?
 * CreateUserRequest (Phase 2) has name + email — it's for admin user creation.
 * SignupRequest adds password with validation — it's for self-registration.
 * Different use cases, different validation rules, different DTOs.
 *
 * The @Size(min=8) on password is a UX-level check.
 * BCrypt handles any length, but short passwords are trivially brute-forced.
 * NIST SP 800-63B recommends minimum 8 characters for memorized secrets.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}

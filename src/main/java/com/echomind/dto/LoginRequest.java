package com.echomind.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO for login requests.
 *
 * Only email + password. No name needed — we're identifying an existing user.
 * No @Size on password here — if the user types a 3-character password,
 * let it fail at the "wrong password" step, not at validation.
 * Giving different error messages for "password too short" vs "wrong password"
 * leaks information about your password policy to attackers.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}

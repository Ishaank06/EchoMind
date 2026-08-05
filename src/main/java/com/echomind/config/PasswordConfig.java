package com.echomind.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Password encoder configuration.
 *
 * Why a separate class instead of putting this @Bean in SecurityConfig?
 * CIRCULAR DEPENDENCY prevention:
 *   SecurityConfig → needs AuthenticationManager
 *   AuthenticationManager → needs UserDetailsService
 *   UserDetailsService → needs UserRepository (fine)
 *   AuthService → needs PasswordEncoder
 *   If PasswordEncoder is defined in SecurityConfig, and AuthService is injected
 *   into SecurityConfig (for AuthenticationManager), Spring can't resolve the cycle.
 *
 * Separating PasswordEncoder into its own @Configuration breaks the cycle cleanly.
 * This is a common pattern in production Spring Security setups.
 *
 * Why BCrypt specifically?
 * BCrypt is designed for password hashing. Unlike SHA-256 or MD5:
 * 1. It's INTENTIONALLY SLOW (configurable cost factor, default 10 = 2^10 rounds)
 *    This makes brute-force attacks expensive. SHA-256 can hash billions/second;
 *    BCrypt does ~1000/second at cost 10. That's the point.
 * 2. It generates a RANDOM SALT per password automatically.
 *    Two users with the password "hello123" get different hashes.
 *    Without salting, identical passwords produce identical hashes,
 *    making rainbow table attacks trivial.
 * 3. The hash includes the algorithm, cost, and salt:
 *    $2a$10$N9qo8uLOickgx2ZMRZoMye...
 *    ^^^^ ^^ ^^^^^^^^^^^^^^^^^^^^^^
 *    algo cost  salt (22 chars)     hash (31 chars)
 *
 * Why inject PasswordEncoder instead of doing `new BCryptPasswordEncoder()` directly?
 * - Testability: in tests, you can swap in a no-op encoder for speed
 * - Flexibility: switching to Argon2 later means changing ONE bean, not every usage
 * - Spring convention: anything that might change should be a bean
 */
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

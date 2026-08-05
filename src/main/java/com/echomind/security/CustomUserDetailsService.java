package com.echomind.security;

import com.echomind.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Bridges our User entity to Spring Security's UserDetails interface.
 *
 * Why does Spring Security need this?
 * Spring Security doesn't know about our User entity. It has its own
 * concept of a user: the UserDetails interface, which requires:
 * - getUsername() → identifier (we use email)
 * - getPassword() → password hash (for BCrypt matching)
 * - getAuthorities() → roles/permissions (for authorization)
 * - isAccountNonExpired(), isAccountNonLocked(), etc. → account status flags
 *
 * Why not make our User entity implement UserDetails?
 * You CAN do that — many tutorials do. But it couples your domain entity
 * to Spring Security. If you ever:
 * - Switch from Spring Security to another framework
 * - Use the User entity in a context where Security isn't available
 * - Need different UserDetails for different auth mechanisms
 * ...the coupling causes pain.
 *
 * Instead, this service ADAPTS our entity to UserDetails when needed.
 * Separation of concerns: entity = database structure, UserDetails = security contract.
 *
 * Who calls this?
 * 1. AuthenticationManager (during login) — to load the user and verify password
 * 2. JwtAuthFilter (during every authenticated request) — to load the user
 *    from the JWT's email claim and set the SecurityContext
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Spring Security calls this with the "username" (for us, the email).
     *
     * @throws UsernameNotFoundException if no user with this email exists.
     *         Spring Security catches this and translates it to a 401.
     *         The message is intentionally vague ("User not found") —
     *         in production, you don't want to confirm whether an email
     *         is registered (information leakage).
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        com.echomind.entity.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new User(
                user.getEmail(),
                user.getPassword() != null ? user.getPassword() : "",
                List.of(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }
}

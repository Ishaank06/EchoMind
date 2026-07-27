package com.echomind.service;

import com.echomind.dto.CreateUserRequest;
import com.echomind.dto.UserResponse;
import com.echomind.entity.User;
import com.echomind.exception.DuplicateResourceException;
import com.echomind.exception.ResourceNotFoundException;
import com.echomind.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service layer for User operations.
 *
 * Why this layer exists even though it looks like a "passthrough" right now:
 *
 * 1. TRANSACTION BOUNDARIES: @Transactional here means the entire method
 *    runs in a single database transaction. If you have multiple repository
 *    calls (e.g., check for duplicate + save), they're atomic.
 *
 * 2. BUSINESS LOGIC HOME: Duplicate email checks, data transformations,
 *    and orchestration between repositories belong here — not in controllers
 *    (which handle HTTP) or repositories (which handle SQL).
 *
 * 3. FUTURE-PROOFING for EchoMind:
 *    - Auth phase: this service will hash passwords, validate credentials
 *    - GitHub connector: will orchestrate between UserRepository and GitHubClient
 *    - AI layer: will coordinate between the DB and AI service calls
 *    - If controllers called repositories directly, adding these concerns
 *      later would mean rewriting controllers.
 *
 * @RequiredArgsConstructor (Lombok) generates a constructor for all 'final' fields.
 * Since Spring does constructor injection by default (no @Autowired needed when
 * there's only one constructor), this is the most idiomatic way to inject
 * dependencies in modern Spring.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Creates a new user after checking for duplicate email.
     *
     * @Transactional: wraps findByEmail + save in one transaction.
     * Without this, there's a race condition: two requests with the same
     * email could both pass the duplicate check before either saves.
     * (The DB unique constraint is the ultimate safety net, but this
     * gives us a cleaner error message.)
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        // Check for duplicate email BEFORE attempting save
        userRepository.findByEmail(request.getEmail())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("User", "email", request.getEmail());
                });

        // Map DTO → Entity (manual mapping for now; MapStruct is overkill for 3 fields)
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .build();

        User savedUser = userRepository.save(user);

        // Map Entity → Response DTO
        return mapToResponse(savedUser);
    }

    /**
     * @Transactional(readOnly = true): optimization hint.
     * - Hibernate skips dirty checking (no need to detect changes)
     * - Some JDBC drivers optimize read-only connections
     * - Documents intent: this method doesn't modify data
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        return mapToResponse(user);
    }

    /**
     * Maps User entity to UserResponse DTO.
     *
     * This is intentionally a private method here rather than a separate
     * "mapper" class. For a single entity with 3 fields, a mapper class
     * is over-engineering. When EchoMind grows to 5+ entities with complex
     * mappings, we'll introduce MapStruct (a compile-time mapper generator).
     */
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}

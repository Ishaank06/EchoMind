package com.echomind.controller;

import com.echomind.dto.CreateUserRequest;
import com.echomind.dto.UserResponse;
import com.echomind.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for User operations.
 *
 * @RestController = @Controller + @ResponseBody
 * - @Controller: marks this as a Spring MVC controller (component-scanned)
 * - @ResponseBody: all return values are serialized to JSON (via Jackson)
 *   instead of being resolved as view names
 *
 * @RequestMapping("/api/users"): base path for all endpoints in this controller.
 * Using "/api/" prefix is a convention that:
 * - Separates API routes from potential static content routes
 * - Makes it easy to apply security rules (e.g., "/api/**" requires auth)
 * - Helps reverse proxies and API gateways route traffic
 *
 * Notice what this controller does NOT do:
 * - No direct repository calls (delegates to UserService)
 * - No business logic (no duplicate checking, no entity mapping)
 * - No try/catch blocks (GlobalExceptionHandler handles errors)
 * The controller's ONLY job: receive HTTP → call service → return HTTP
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * POST /api/users — Create a new user
     *
     * @Valid triggers Bean Validation on the CreateUserRequest.
     * If validation fails, Spring throws MethodArgumentNotValidException
     * BEFORE this method body even executes. Our GlobalExceptionHandler
     * catches it and returns a 400 with per-field error details.
     *
     * @RequestBody tells Spring to deserialize the JSON request body
     * into a CreateUserRequest object using Jackson.
     *
     * Returns 201 Created (not 200 OK) because REST convention says
     * "201" means "a new resource was successfully created."
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/users — List all users
     *
     * Returns 200 OK with an array of users.
     * No pagination yet — fine for development, but for production you'd
     * want Pageable: GET /api/users?page=0&size=20&sort=name,asc
     * Spring Data JPA supports this natively via Pageable parameter.
     * We'll add this when the dataset grows.
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * GET /api/users/{id} — Get a single user by ID
     *
     * @PathVariable extracts {id} from the URL path.
     * Spring automatically converts the String UUID from the URL
     * into a java.util.UUID object. If the format is invalid,
     * Spring throws MethodArgumentTypeMismatchException (which our
     * catch-all handler turns into a 500 — we could add a specific
     * handler for this later to return 400).
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * GET /api/users/admin/all — Admin-only endpoint
     *
     * @PreAuthorize("hasRole('ADMIN')"): Spring Security checks if the
     * authenticated user's authorities contain "ROLE_ADMIN".
     * Note: hasRole('ADMIN') automatically prepends "ROLE_" — so it
     * checks for "ROLE_ADMIN" in the authorities list.
     *
     * If the user has ROLE_USER → 403 Forbidden
     * If the user is not authenticated → 401 Unauthorized
     * If the user has ROLE_ADMIN → 200 OK
     *
     * This demonstrates method-level security. You can also do path-level
     * security in SecurityConfig, but @PreAuthorize is more granular and
     * keeps the authorization rule next to the code it protects.
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsersAdmin() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}

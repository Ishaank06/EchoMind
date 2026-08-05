package com.echomind.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized exception handler for the entire application.
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 * This means:
 * 1. Spring intercepts exceptions thrown by ANY controller
 * 2. Routes them to the matching @ExceptionHandler method here
 * 3. The return value is serialized to JSON automatically (@ResponseBody)
 *
 * Without this, Spring Boot's default error handling returns:
 * - Stack traces in development (security risk if deployed)
 * - A generic whitelabel error page (not useful for API clients)
 *
 * With this, every error returns a consistent JSON structure:
 * {
 *   "timestamp": "...",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "User not found with id: abc-123"
 * }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles ResourceNotFoundException → 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Handles DuplicateResourceException → 409 Conflict
     *
     * Why 409 and not 400?
     * - 400 Bad Request = the request format is wrong (malformed JSON, missing fields)
     * - 409 Conflict = the request is valid but conflicts with existing state
     * A duplicate email is a state conflict, not a format error.
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateResource(DuplicateResourceException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Handles validation failures from @Valid on request DTOs → 400 Bad Request
     *
     * When @Valid fails, Spring throws MethodArgumentNotValidException.
     * This handler extracts each field error and returns them as a map:
     * {
     *   "timestamp": "...",
     *   "status": 400,
     *   "error": "Bad Request",
     *   "message": "Validation failed",
     *   "errors": {
     *     "name": "Name is required",
     *     "email": "Email must be a valid email address"
     *   }
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        body.put("message", "Validation failed");
        body.put("errors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Handles BadCredentialsException → 401 Unauthorized
     *
     * Thrown by AuthService when email/password don't match.
     * Uses a generic message to prevent email enumeration.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    /**
     * Handles AccessDeniedException → 403 Forbidden
     *
     * Thrown when an authenticated user tries to access a resource
     * they don't have the right role for (e.g., USER accessing an ADMIN endpoint).
     *
     * 401 = "Who are you?" (not authenticated)
     * 403 = "I know who you are, but you can't do this" (not authorized)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "Access denied: insufficient permissions");
    }

    /**
     * Catch-all for any unhandled exception → 500 Internal Server Error
     *
     * This prevents stack traces from ever reaching the client.
     * The actual exception is logged server-side (Spring does this automatically),
     * but the client only sees a generic message.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}

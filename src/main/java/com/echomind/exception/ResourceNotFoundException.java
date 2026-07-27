package com.echomind.exception;

/**
 * Thrown when a requested resource doesn't exist.
 *
 * Why a custom exception instead of just returning null?
 * 1. Null returns push null-checking responsibility onto every caller.
 *    Exceptions enforce handling — the caller MUST deal with it.
 * 2. The @ControllerAdvice handler translates this into a clean 404 JSON
 *    response, so controllers stay focused on the happy path.
 * 3. The exception carries the resource name and field, making error
 *    messages specific: "User not found with id: abc-123"
 *
 * Extends RuntimeException (unchecked) rather than Exception (checked) because:
 * - Checked exceptions force try/catch at every call site — verbose and clutters
 *   the service layer.
 * - Spring's @Transactional only rolls back on unchecked exceptions by default.
 * - This is the standard Spring convention for business exceptions.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}

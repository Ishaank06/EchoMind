package com.echomind.exception;

/**
 * Thrown when a uniqueness constraint would be violated.
 * Example: trying to create a user with an email that already exists.
 *
 * Why catch this at the service layer instead of letting the DB throw it?
 * The database WILL throw a DataIntegrityViolationException if you try to
 * insert a duplicate email. But that exception:
 * - Contains raw SQL error messages (leaks implementation details)
 * - Returns a 500 status code (misleading — it's a client error, not server)
 * - Is hard to map to a clean user-facing message
 *
 * By checking in the service and throwing this, we control the narrative:
 * clean 409 Conflict with a helpful message.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}

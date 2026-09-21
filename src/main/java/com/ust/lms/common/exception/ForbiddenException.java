package com.ust.lms.common.exception;

/**
 * Exception thrown when a user is not authorized to perform an operation.
 */
public class ForbiddenException extends RuntimeException {

    /**
     * Creates a new ForbiddenException with the specified message.
     *
     * @param message error message describing the forbidden operation
     */
    public ForbiddenException(String message) {
        super(message);
    }
}
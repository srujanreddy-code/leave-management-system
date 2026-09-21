package com.ust.lms.common.exception;

/**
 * Exception thrown when a requested resource cannot be found.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Creates a new ResourceNotFoundException with the specified message.
     *
     * @param message error message describing the missing resource
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
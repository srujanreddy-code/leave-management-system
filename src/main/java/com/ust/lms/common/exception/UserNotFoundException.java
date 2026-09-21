package com.ust.lms.common.exception;

/**
 * Exception thrown when the requested user cannot be found.
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Creates a new UserNotFoundException with the specified message.
     *
     * @param message error message describing the missing user
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}
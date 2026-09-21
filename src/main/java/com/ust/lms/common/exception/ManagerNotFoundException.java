package com.ust.lms.common.exception;

/**
 * Exception thrown when a manager cannot be found.
 */
public class ManagerNotFoundException extends RuntimeException {

    /**
     * Creates a new ManagerNotFoundException with the specified message.
     *
     * @param message error message describing the missing manager
     */
    public ManagerNotFoundException(String message) {
        super(message);
    }
}
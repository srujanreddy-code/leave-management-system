package com.ust.lms.common.exception;

/**
 * Exception thrown when the requested employee cannot be found.
 */
public class EmployeeNotFoundException extends RuntimeException {

    /**
     * Creates a new EmployeeNotFoundException with the specified message.
     *
     * @param message error message describing the missing employee
     */
    public EmployeeNotFoundException(String message) {
        super(message);
    }
}
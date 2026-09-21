package com.ust.lms.common.exception;

/**
 * Exception thrown when an employee record already exists.
 */
public class EmployeeAlreadyExistsException extends RuntimeException {

    /**
     * Creates a new EmployeeAlreadyExistsException with the specified message.
     *
     * @param message error message describing the existing employee
     */
    public EmployeeAlreadyExistsException(String message) {
        super(message);
    }
}
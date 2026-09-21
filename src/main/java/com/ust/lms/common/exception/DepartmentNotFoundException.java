package com.ust.lms.common.exception;

/**
 * Exception thrown when the requested department cannot be found.
 */
public class DepartmentNotFoundException extends RuntimeException {

    /**
     * Creates a new DepartmentNotFoundException with the specified message.
     *
     * @param message error message describing the missing department
     */
    public DepartmentNotFoundException(String message) {
        super(message);
    }
}
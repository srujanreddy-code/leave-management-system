package com.ust.lms.common.exception;

/**
 * Exception thrown when a department with the same details already exists.
 */
public class DuplicateDepartmentException extends RuntimeException {

    /**
     * Creates a new DuplicateDepartmentException with the specified message.
     *
     * @param message error message describing the duplicate department
     */
    public DuplicateDepartmentException(String message) {
        super(message);
    }
}
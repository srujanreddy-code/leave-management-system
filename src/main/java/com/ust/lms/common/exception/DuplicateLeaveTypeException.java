package com.ust.lms.common.exception;

/**
 * Exception thrown when a leave type with the same details already exists.
 */
public class DuplicateLeaveTypeException extends RuntimeException {

    /**
     * Creates a new DuplicateLeaveTypeException with the specified message.
     *
     * @param message error message describing the duplicate leave type
     */
    public DuplicateLeaveTypeException(String message) {
        super(message);
    }
}
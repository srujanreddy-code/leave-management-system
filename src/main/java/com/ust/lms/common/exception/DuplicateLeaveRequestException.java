package com.ust.lms.common.exception;

/**
 * Exception thrown when a duplicate leave request is detected.
 */
public class DuplicateLeaveRequestException extends RuntimeException {

    /**
     * Creates a new DuplicateLeaveRequestException with the specified message.
     *
     * @param message error message describing the duplicate leave request
     */
    public DuplicateLeaveRequestException(String message) {
        super(message);
    }
}
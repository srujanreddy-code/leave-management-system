package com.ust.lms.common.exception;

/**
 * Exception thrown when an invalid leave status transition is attempted.
 */
public class InvalidLeaveStatusTransitionException extends RuntimeException {

    /**
     * Creates a new InvalidLeaveStatusTransitionException with the specified message.
     *
     * @param message error message describing the invalid leave status transition
     */
    public InvalidLeaveStatusTransitionException(String message) {
        super(message);
    }
}
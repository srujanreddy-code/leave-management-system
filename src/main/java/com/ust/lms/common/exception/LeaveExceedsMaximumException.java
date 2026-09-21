package com.ust.lms.common.exception;

/**
 * Exception thrown when a leave request exceeds the maximum allowed leave days.
 */
public class LeaveExceedsMaximumException extends RuntimeException {

    /**
     * Creates a new LeaveExceedsMaximumException with the specified message.
     *
     * @param message error message describing the exceeded maximum leave days
     */
    public LeaveExceedsMaximumException(String message) {
        super(message);
    }
}
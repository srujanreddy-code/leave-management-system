package com.ust.lms.common.exception;

/**
 * Exception thrown when a leave request has already been approved.
 */
public class LeaveAlreadyApprovedException extends RuntimeException {

    /**
     * Creates a new LeaveAlreadyApprovedException with the specified message.
     *
     * @param message error message describing the already approved leave request
     */
    public LeaveAlreadyApprovedException(String message) {
        super(message);
    }
}
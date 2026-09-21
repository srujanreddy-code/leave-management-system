package com.ust.lms.common.exception;

/**
 * Exception thrown when a leave request has already been cancelled.
 */
public class LeaveAlreadyCancelledException extends RuntimeException {

    /**
     * Creates a new LeaveAlreadyCancelledException with the specified message.
     *
     * @param message error message describing the already cancelled leave request
     */
    public LeaveAlreadyCancelledException(String message) {
        super(message);
    }
}
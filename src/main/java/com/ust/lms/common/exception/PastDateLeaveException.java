package com.ust.lms.common.exception;

/**
 * Exception thrown when a leave request is made for a date in the past.
 */
public class PastDateLeaveException extends RuntimeException {

    /**
     * Creates a new PastDateLeaveException with the specified message.
     *
     * @param message error message describing the past date leave request
     */
    public PastDateLeaveException(String message) {
        super(message);
    }
}
package com.ust.lms.common.exception;

/**
 * Exception thrown when the start and end dates of a leave request are invalid.
 */
public class InvalidLeaveDatesException extends RuntimeException {

    /**
     * Creates a new InvalidLeaveDatesException with the specified message.
     *
     * @param message error message describing the invalid leave dates
     */
    public InvalidLeaveDatesException(String message) {
        super(message);
    }
}
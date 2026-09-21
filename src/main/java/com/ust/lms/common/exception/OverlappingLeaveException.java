package com.ust.lms.common.exception;

/**
 * Exception thrown when a leave request overlaps with an existing leave request.
 */
public class OverlappingLeaveException extends RuntimeException {

    /**
     * Creates a new OverlappingLeaveException with the specified message.
     *
     * @param message error message describing the overlapping leave request
     */
    public OverlappingLeaveException(String message) {
        super(message);
    }
}
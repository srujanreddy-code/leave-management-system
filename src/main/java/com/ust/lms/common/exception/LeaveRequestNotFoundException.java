package com.ust.lms.common.exception;

/**
 * Exception thrown when the requested leave request cannot be found.
 */
public class LeaveRequestNotFoundException extends RuntimeException {

    /**
     * Creates a new LeaveRequestNotFoundException with the specified message.
     *
     * @param message error message describing the missing leave request
     */
    public LeaveRequestNotFoundException(String message) {
        super(message);
    }
}
package com.ust.lms.common.exception;

/**
 * Exception thrown when the requested leave type cannot be found.
 */
public class LeaveTypeNotFoundException extends RuntimeException {

    /**
     * Creates a new LeaveTypeNotFoundException with the specified message.
     *
     * @param message error message describing the missing leave type
     */
    public LeaveTypeNotFoundException(String message) {
        super(message);
    }
}
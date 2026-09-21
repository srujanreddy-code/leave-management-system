package com.ust.lms.common.exception;

/**
 * Exception thrown when the requested employee leave balance cannot be found.
 */
public class LeaveBalanceNotFoundException extends RuntimeException {

    /**
     * Creates a new LeaveBalanceNotFoundException with the specified message.
     *
     * @param message error message describing the missing leave balance
     */
    public LeaveBalanceNotFoundException(String message) {
        super(message);
    }
}
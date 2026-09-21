package com.ust.lms.common.exception;

/**
 * Exception thrown when an employee does not have sufficient leave balance
 * for the requested leave.
 */
public class InsufficientLeaveBalanceException extends RuntimeException {

    /**
     * Creates a new InsufficientLeaveBalanceException with the specified message.
     *
     * @param message error message describing the insufficient leave balance
     */
    public InsufficientLeaveBalanceException(String message) {
        super(message);
    }
}
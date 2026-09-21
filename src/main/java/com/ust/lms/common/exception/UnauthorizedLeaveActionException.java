package com.ust.lms.common.exception;

/**
 * Exception thrown when a user is not authorized to perform a leave-related action.
 */
public class UnauthorizedLeaveActionException extends RuntimeException {

    /**
     * Creates a new UnauthorizedLeaveActionException with the specified message.
     *
     * @param message error message describing the unauthorized leave action
     */
    public UnauthorizedLeaveActionException(String message) {
        super(message);
    }
}
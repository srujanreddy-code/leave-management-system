package com.ust.lms.common.exception;

/**
 * Exception thrown when a request contains invalid or unacceptable data.
 */
public class BadRequestException extends RuntimeException {

    /**
     * Creates a new BadRequestException with the specified message.
     *
     * @param message error message describing the bad request
     */
    public BadRequestException(String message) {
        super(message);
    }
}
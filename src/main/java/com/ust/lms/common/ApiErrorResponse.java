package com.ust.lms.common;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents the standard structure for API responses containing
 * success status, message, and response data.
 */
@Getter
@Setter
public class ApiErrorResponse {
    private boolean success;
    private String message;
    private Object data;

    /**
     * Creates a new ApiErrorResponse with the specified response details.
     *
     * @param success indicates whether the operation was successful
     * @param message message describing the response
     * @param data additional response data
     */
    public ApiErrorResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
}
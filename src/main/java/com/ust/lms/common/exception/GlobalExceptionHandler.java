package com.ust.lms.common.exception;

import com.ust.lms.common.ApiErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * Handles framework and technical exceptions and converts them into
 * standardized API error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles access denied exceptions.
     *
     * @param ex access denied exception
     * @return API error response with HTTP 403 status
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiErrorResponse(false, "You do not have permission to perform this action", null));
    }

    /**
     * Handles authentication failures caused by invalid credentials.
     *
     * @param ex bad credentials exception
     * @return API error response with HTTP 401 status
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorResponse(false, "Invalid email or password", null));
    }

    /**
     * Handles bean validation failures.
     *
     * @param ex method argument validation exception
     * @return API error response with HTTP 400 status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(false, message, null));
    }

    /**
     * Handles invalid or unreadable request bodies.
     *
     * @param ex HTTP message not readable exception
     * @return API error response with HTTP 400 status
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(
                        false,
                        "Invalid request data or JSON format",
                        null
                ));
    }

    /**
     * Handles invalid parameter values.
     *
     * @param ex method argument type mismatch exception
     * @return API error response with HTTP 400 status
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(false,
                        "Invalid value for parameter: " + ex.getName(), null));
    }

    /**
     * Handles missing required request parameters.
     *
     * @param ex missing servlet request parameter exception
     * @return API error response with HTTP 400 status
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingRequestParameter(
            MissingServletRequestParameterException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(false,
                        "Required parameter is missing: " + ex.getParameterName(), null));
    }

    /**
     * Handles unsupported HTTP methods.
     *
     * @param ex HTTP request method not supported exception
     * @return API error response with HTTP 405 status
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ApiErrorResponse(false,
                        "HTTP method not supported for this endpoint", null));
    }

    /**
     * Handles unsupported request content types.
     *
     * @param ex HTTP media type not supported exception
     * @return API error response with HTTP 415 status
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(new ApiErrorResponse(false,
                        "Content type is not supported", null));
    }

    /**
     * Handles requests to endpoints that do not exist.
     *
     * @param ex no resource found exception
     * @return API error response with HTTP 404 status
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(false,
                        "The requested endpoint was not found", null));
    }

    /**
     * Handles missing path variables.
     *
     * @param ex missing path variable exception
     * @return API error response with HTTP 400 status
     */
    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingPathVariable(
            MissingPathVariableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(false,
                        "Required path variable is missing: " + ex.getVariableName(), null));
    }

    /**
     * Handles method-level validation failures.
     *
     * @param ex handler method validation exception
     * @return API error response with HTTP 400 status
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodValidation(
            HandlerMethodValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(false,
                        "Invalid request parameters", null));
    }

    /**
     * Handles database integrity constraint violations.
     *
     * @param ex data integrity violation exception
     * @return API error response with HTTP 409 status
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse(false,
                        "The request conflicts with existing data", null));
    }

    /**
     * Handles cases where the authenticated user cannot be found.
     *
     * @param ex username not found exception
     * @return API error response with HTTP 401 status
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUsernameNotFound(
            UsernameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorResponse(false,
                        "Invalid email or password", null));
    }

    /**
     * Handles unexpected exceptions not covered by specific handlers.
     *
     * @param ex unexpected exception
     * @return API error response with HTTP 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(
                        false,
                        "An unexpected error occurred",
                        null
                ));
    }
}
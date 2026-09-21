package com.ust.lms.common.exception;

import com.ust.lms.common.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handles application-specific exceptions and converts them into
 * standardized API error responses.
 */
@RestControllerAdvice
public class CustomExceptionHandler {

    /**
     * Handles employee not found exceptions.
     *
     * @param ex employee not found exception
     * @return API error response with HTTP 404 status
     */
    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEmployeeNotFound(
            EmployeeNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(false, ex.getMessage(), null));
    }

    /**
     * Handles leave request not found exceptions.
     *
     * @param ex leave request not found exception
     * @return API error response with HTTP 404 status
     */
    @ExceptionHandler(LeaveRequestNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleLeaveRequestNotFound(
            LeaveRequestNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(false, ex.getMessage(), null));
    }

    /**
     * Handles leave balance not found exceptions.
     *
     * @param ex leave balance not found exception
     * @return API error response with HTTP 404 status
     */
    @ExceptionHandler(LeaveBalanceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleLeaveBalanceNotFound(
            LeaveBalanceNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(false, ex.getMessage(), null));
    }

    /**
     * Handles leave type not found exceptions.
     *
     * @param ex leave type not found exception
     * @return API error response with HTTP 404 status
     */
    @ExceptionHandler(LeaveTypeNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleLeaveTypeNotFound(
            LeaveTypeNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(false, ex.getMessage(), null));
    }

    /**
     * Handles manager not found exceptions.
     *
     * @param ex manager not found exception
     * @return API error response with HTTP 404 status
     */
    @ExceptionHandler(ManagerNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleManagerNotFound(
            ManagerNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(false, ex.getMessage(), null));
    }

    /**
     * Handles generic resource not found exceptions.
     *
     * @param ex resource not found exception
     * @return API error response with HTTP 404 status
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(false, ex.getMessage(), null));
    }

    /**
     * Handles bad request exceptions.
     *
     * @param ex bad request exception
     * @return API error response with HTTP 400 status
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            BadRequestException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(false, ex.getMessage(), null));
    }

    /**
     * Handles forbidden operation exceptions.
     *
     * @param ex forbidden exception
     * @return API error response with HTTP 403 status
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(
            ForbiddenException ex) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiErrorResponse(false, ex.getMessage(), null));
    }

    /**
     * Handles user not found exceptions.
     *
     * @param ex user not found exception
     * @return API error response with HTTP 404 status
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFound(
            UserNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(false, ex.getMessage(), null));
    }

    /**
     * Handles department not found exceptions.
     *
     * @param ex department not found exception
     * @return API error response with HTTP 404 status
     */
    @ExceptionHandler(DepartmentNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleDepartmentNotFound(
            DepartmentNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(false, ex.getMessage(), null));
    }

    /**
     * Handles invalid leave date exceptions.
     *
     * @param ex invalid leave dates exception
     * @return API error response with HTTP 400 status
     */
    @ExceptionHandler(InvalidLeaveDatesException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidLeaveDates(InvalidLeaveDatesException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(false, ex.getMessage(), null));
    }

    /**
     * Handles leave requests made for past dates.
     *
     * @param ex past date leave exception
     * @return API error response with HTTP 400 status
     */
    @ExceptionHandler(PastDateLeaveException.class)
    public ResponseEntity<ApiErrorResponse> handlePastDateLeave(PastDateLeaveException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(false, ex.getMessage(), null));
    }

    /**
     * Handles insufficient leave balance exceptions.
     *
     * @param ex insufficient leave balance exception
     * @return API error response with HTTP 422 status
     */
    @ExceptionHandler(InsufficientLeaveBalanceException.class)
    public ResponseEntity<ApiErrorResponse> handleInsufficientLeaveBalance(
            InsufficientLeaveBalanceException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiErrorResponse(false, ex.getMessage(), null));
    }

    /**
     * Handles overlapping leave exceptions.
     *
     * @param ex overlapping leave exception
     * @return API error response with HTTP 409 status
     */
    @ExceptionHandler(OverlappingLeaveException.class)
    public ResponseEntity<ApiErrorResponse> handleOverlappingLeave(OverlappingLeaveException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse(false, ex.getMessage(), null));
    }

    /**
     * Handles duplicate leave request exceptions.
     *
     * @param ex duplicate leave request exception
     * @return API error response with HTTP 409 status
     */
    @ExceptionHandler(DuplicateLeaveRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateLeaveRequest(
            DuplicateLeaveRequestException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse(false, ex.getMessage(), null));
    }

    /**
     * Handles unauthorized leave action exceptions.
     *
     * @param ex unauthorized leave action exception
     * @return API error response with HTTP 403 status
     */
    @ExceptionHandler(UnauthorizedLeaveActionException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorizedLeaveAction(
            UnauthorizedLeaveActionException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiErrorResponse(false, ex.getMessage(), null));
    }

    /**
     * Handles invalid leave status transition exceptions.
     *
     * @param ex invalid leave status transition exception
     * @return API error response with HTTP 409 status
     */
    @ExceptionHandler(InvalidLeaveStatusTransitionException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidLeaveStatusTransition(
            InvalidLeaveStatusTransitionException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse(false, ex.getMessage(), null));
    }

    /**
     * Handles leave already approved exceptions.
     *
     * @param ex leave already approved exception
     * @return API error response with HTTP 409 status
     */
    @ExceptionHandler(LeaveAlreadyApprovedException.class)
    public ResponseEntity<ApiErrorResponse> handleLeaveAlreadyApproved(
            LeaveAlreadyApprovedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse(false, ex.getMessage(), null));
    }

    /**
     * Handles leave already cancelled exceptions.
     *
     * @param ex leave already cancelled exception
     * @return API error response with HTTP 409 status
     */
    @ExceptionHandler(LeaveAlreadyCancelledException.class)
    public ResponseEntity<ApiErrorResponse> handleLeaveAlreadyCancelled(
            LeaveAlreadyCancelledException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse(false, ex.getMessage(), null));
    }

    /**
     * Handles employee already exists exceptions.
     *
     * @param ex employee already exists exception
     * @return API error response with HTTP 409 status
     */
    @ExceptionHandler(EmployeeAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleEmployeeAlreadyExists(
            EmployeeAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse(false, ex.getMessage(), null));
    }

    /**
     * Handles duplicate department exceptions.
     *
     * @param ex duplicate department exception
     * @return API error response with HTTP 409 status
     */
    @ExceptionHandler(DuplicateDepartmentException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateDepartment(
            DuplicateDepartmentException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse(false, ex.getMessage(), null));
    }

    /**
     * Handles duplicate leave type exceptions.
     *
     * @param ex duplicate leave type exception
     * @return API error response with HTTP 409 status
     */
    @ExceptionHandler(DuplicateLeaveTypeException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateLeaveType(
            DuplicateLeaveTypeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse(false, ex.getMessage(), null));
    }

    /**
     * Handles leave requests that exceed the maximum allowed days.
     *
     * @param ex leave exceeds maximum exception
     * @return API error response with HTTP 422 status
     */
    @ExceptionHandler(LeaveExceedsMaximumException.class)
    public ResponseEntity<ApiErrorResponse> handleLeaveExceedsMaximum(
            LeaveExceedsMaximumException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiErrorResponse(false, ex.getMessage(), null));
    }
}

package com.surest.exception;

import com.surest.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private ErrorResponse buildError(HttpStatus status, String message, String path) {
        return new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, path);
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMemberNotFound(MemberNotFoundException ex, HttpServletRequest request) {
        return new ResponseEntity<>(buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MemberAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleMemberExists(MemberAlreadyExistsException ex, HttpServletRequest request) {
        return new ResponseEntity<>(buildError(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleJwtError(JwtAuthenticationException ex, HttpServletRequest request) {
        return new ResponseEntity<>(buildError(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return new ResponseEntity<>(buildError(HttpStatus.BAD_REQUEST, errors, request.getRequestURI()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        return new ResponseEntity<>(buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<ErrorResponse> handleEntityExists(EntityExistsException ex, HttpServletRequest request) {
        return new ResponseEntity<>(buildError(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return new ResponseEntity<>(buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

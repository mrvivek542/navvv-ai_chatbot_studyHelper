package com.studymind.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

/**
 * GlobalExceptionHandler catches unhandled exceptions across all controllers
 * and returns user-friendly error responses without exposing internal stack traces.
 *
 * OOP / Viva Concepts:
 * - Aspect-Oriented Programming (AOP) via @ControllerAdvice.
 * - Defensive Programming: Prevents leaking server internal errors or DB structures.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(Map.of(
                "success", false,
                "error", "File upload failed: File exceeds the maximum allowed limit of 5 MB."
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException exc) {
        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", exc.getMessage()
        ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalState(IllegalStateException exc) {
        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", exc.getMessage()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception exc) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "A temporary issue occurred while processing your study request. Please retry."
        ));
    }
}

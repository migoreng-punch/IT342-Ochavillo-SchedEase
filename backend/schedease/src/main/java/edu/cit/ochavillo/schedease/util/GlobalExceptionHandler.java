package edu.cit.ochavillo.schedease.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // This ONE method dynamically catches EVERY custom business error you ever throw!
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiErrorResponse> handleAppException(AppException ex) {

        // Dynamically grab the code and message you passed in the service layer
        ApiErrorResponse response = new ApiErrorResponse(
                ex.getCode(),
                ex.getMessage()
        );

        return ResponseEntity.badRequest().body(response);
    }

    // Keep your catch-all for 500 server crashes here...
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {

        // 1. Log the full stack trace to your backend console/files for debugging.
        // Using logger.error() ensures it gets flagged in your monitoring tools.
        logger.error("🚨 CRITICAL UNHANDLED EXCEPTION: ", ex);

        // 2. Create a safe, generic response for the frontend.
        // Notice we do NOT pass ex.getMessage() here!
        ApiErrorResponse response = new ApiErrorResponse(
                "SYS-500",
                "An unexpected internal server error occurred. Our team has been notified."
        );

        // 3. Return the 500 status code with the structured JSON
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
package com.sribalajiads.media_app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

// This annotation makes this class a global handler for exceptions across all controllers.
@ControllerAdvice
public class GlobalExceptionHandler {

    // This annotation specifies that this method will handle any ResourceNotFoundException.
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {

        // Create a clean, structured map for our JSON response body.
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        // Return the structured response with a 404 NOT FOUND status.
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }
}
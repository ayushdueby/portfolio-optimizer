package com.example.portfolio.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        log.error("Request failed", e);
        String message = e.getMessage() != null ? e.getMessage() : "Internal server error";
        if (e.getCause() != null && e.getCause().getMessage() != null) {
            message = e.getCause().getMessage();
        }
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", message));
    }

    @ExceptionHandler({ MultipartException.class, MaxUploadSizeExceededException.class })
    public ResponseEntity<Map<String, String>> handleUploadError(Exception e) {
        log.warn("Upload error: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Upload failed: " + e.getMessage()));
    }
}

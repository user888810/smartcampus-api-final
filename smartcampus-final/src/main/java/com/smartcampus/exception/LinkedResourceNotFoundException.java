package com.smartcampus.exception;

/**
 * Thrown when a POST request references a resource (e.g. roomId) that does
 * not exist. Mapped to HTTP 422 Unprocessable Entity by its ExceptionMapper.
 */
public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}

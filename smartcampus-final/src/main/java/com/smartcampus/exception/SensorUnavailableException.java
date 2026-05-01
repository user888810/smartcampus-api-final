package com.smartcampus.exception;

/**
 * Thrown when a reading is POSTed to a Sensor whose status is "MAINTENANCE".
 * Mapped to HTTP 403 Forbidden by SensorUnavailableExceptionMapper.
 */
public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String message) {
        super(message);
    }
}

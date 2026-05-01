package com.smartcampus.model;

/**
 * Standard error response payload.
 *
 * As taught in Lecture Week 08 (Slide 10), clients require predictability.
 * Every error response across the entire API uses this single, consistent
 * structure so client applications can parse errors without special-casing
 * each endpoint.
 *
 * Fields:
 *   status  – the HTTP status code (e.g. 404, 409)
 *   code    – a short machine-readable error code (e.g. "NOT_FOUND")
 *   message – a human-readable explanation of what went wrong
 */
public class ErrorMessage {

    private int    status;
    private String code;
    private String message;

    public ErrorMessage() {}

    public ErrorMessage(int status, String code, String message) {
        this.status  = status;
        this.code    = code;
        this.message = message;
    }

    public int    getStatus()              { return status;  }
    public void   setStatus(int status)    { this.status = status; }

    public String getCode()                { return code;    }
    public void   setCode(String code)     { this.code = code; }

    public String getMessage()             { return message; }
    public void   setMessage(String msg)   { this.message = msg; }
}

package com.smartcampus.exception;

import com.smartcampus.model.ErrorMessage;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps SensorUnavailableException → HTTP 403 Forbidden.
 *
 * Demonstrates the ExceptionMapper pattern from Tutorial Week 09:
 * the resource class throws a plain Java exception; this @Provider
 * intercepts it and returns a structured JSON response.
 */
@Provider
public class SensorUnavailableExceptionMapper
        implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException exception) {
        ErrorMessage errorMessage = new ErrorMessage(
                403,
                "FORBIDDEN",
                exception.getMessage()
        );
        return Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorMessage)
                .build();
    }
}

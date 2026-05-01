package com.smartcampus.exception;

import com.smartcampus.model.ErrorMessage;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Preserves intentional JAX-RS WebApplicationException responses.
 *
 * This prevents the catch-all Throwable mapper from converting deliberate
 * HTTP errors, such as 404 Not Found, into generic 500 responses.
 */
@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(WebApplicationException exception) {
        Response original = exception.getResponse();
        int status = original != null ? original.getStatus() : 500;

        Object entity = original != null ? original.getEntity() : null;
        if (entity != null) {
            return Response.status(status)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(entity)
                    .build();
        }

        Response.Status statusType = Response.Status.fromStatusCode(status);
        String code = statusType != null
                ? statusType.name()
                : "HTTP_ERROR";
        String message = exception.getMessage() != null
                ? exception.getMessage()
                : "The request could not be completed.";

        ErrorMessage errorMessage = new ErrorMessage(status, code, message);
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorMessage)
                .build();
    }
}

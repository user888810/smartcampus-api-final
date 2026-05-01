package com.smartcampus.exception;

import com.smartcampus.model.ErrorMessage;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps LinkedResourceNotFoundException → HTTP 422 Unprocessable Entity.
 *
 * 422 is preferred over 404 because the URL itself is valid — the problem
 * is inside the JSON payload (a referenced roomId that does not exist).
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper
        implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        ErrorMessage errorMessage = new ErrorMessage(
                422,
                "UNPROCESSABLE_ENTITY",
                exception.getMessage()
        );
        return Response.status(422)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorMessage)
                .build();
    }
}

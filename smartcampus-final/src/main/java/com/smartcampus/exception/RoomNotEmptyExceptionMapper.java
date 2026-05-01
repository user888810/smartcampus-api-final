package com.smartcampus.exception;

import com.smartcampus.model.ErrorMessage;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps RoomNotEmptyException → HTTP 409 Conflict.
 *
 * @Provider registers this globally with the JAX-RS runtime so that whenever
 * RoomNotEmptyException is thrown anywhere in the application, this mapper
 * intercepts it and returns a clean JSON error response — exactly as
 * demonstrated in Tutorial Week 09 and Lecture Week 08.
 */
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        ErrorMessage errorMessage = new ErrorMessage(
                409,
                "CONFLICT",
                exception.getMessage()
        );
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorMessage)
                .build();
    }
}

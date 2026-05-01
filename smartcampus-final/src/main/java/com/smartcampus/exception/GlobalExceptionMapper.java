package com.smartcampus.exception;

import com.smartcampus.model.ErrorMessage;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global safety-net ExceptionMapper (500 Internal Server Error).
 *
 * Catches any Throwable not handled by a more specific mapper.
 * Logs the full stack trace server-side but returns only a safe,
 * generic message to the client — preventing information leakage.
 *
 * This is the "Catch-All" approach recommended in Lecture Week 08
 * to ensure no raw stack traces are ever exposed to the client.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // Log full details internally — never send to the client
        LOGGER.log(Level.SEVERE, "Unhandled exception: " + exception.getMessage(), exception);

        ErrorMessage errorMessage = new ErrorMessage(
                500,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please contact support."
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorMessage)
                .build();
    }
}

package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Demonstration-only endpoint for the Part 5 global 500 ExceptionMapper.
 *
 * Calling GET /api/v1/debug/crash deliberately throws an unexpected runtime
 * error. The GlobalExceptionMapper should convert it into a safe generic JSON
 * 500 response instead of leaking a Java stack trace to the client.
 */
@Path("/debug")
@Produces(MediaType.APPLICATION_JSON)
public class DebugResource {

    @GET
    @Path("/crash")
    public String crash() {
        throw new NullPointerException("Intentional demo exception for the global 500 mapper");
    }
}

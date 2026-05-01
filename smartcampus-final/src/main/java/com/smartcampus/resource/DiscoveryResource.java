package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Discovery Endpoint
 *
 * GET /api/v1/
 *
 * Returns API metadata and navigational links so that clients can discover
 * available resource collections, following the REST principle of a
 * uniform interface.
 *
 * Uses javax.ws.rs.* — compatible with Jersey 2.32 on Apache Tomcat 9
 * as taught in Tutorial Week 07.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("api",         "Smart Campus Sensor & Room Management API");
        response.put("version",     "1.0");
        response.put("contact",     "admin@smartcampus.ac.uk");
        response.put("description", "RESTful API for managing campus rooms and their deployed sensors.");

        Map<String, String> links = new LinkedHashMap<>();
        links.put("self",    "/api/v1");
        links.put("rooms",   "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        response.put("_links", links);

        return Response.ok(response).build();
    }
}

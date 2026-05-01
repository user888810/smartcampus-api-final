package com.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * API Request and Response Logging Filter.
 *
 * Implements both ContainerRequestFilter and ContainerResponseFilter so that
 * every HTTP transaction is logged in one cohesive class — matching the
 * LoggingFilter structure taught in Tutorial Week 09 (Step 4).
 *
 * The @Provider annotation registers this globally with Jersey's runtime,
 * so it is applied to ALL endpoints automatically with zero per-method code.
 *
 * Uses javax.ws.rs.* — compatible with Jersey 2.32 on Apache Tomcat 9.
 */
@Provider
public class ApiLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(ApiLoggingFilter.class.getName());

    /**
     * Runs before the request reaches any resource method.
     * Logs the HTTP method and full request URI.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.info("--- Incoming Request ---");
        LOGGER.info("Method: " + requestContext.getMethod());
        LOGGER.info("URI: "    + requestContext.getUriInfo().getAbsolutePath());
    }

    /**
     * Runs after the resource method has produced a response.
     * Logs the final HTTP status code returned to the client.
     */
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        LOGGER.info("--- Outgoing Response ---");
        LOGGER.info("Status: " + responseContext.getStatus());
    }
}

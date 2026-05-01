package com.smartcampus;

import com.smartcampus.exception.GlobalExceptionMapper;
import com.smartcampus.exception.LinkedResourceNotFoundExceptionMapper;
import com.smartcampus.exception.RoomNotEmptyExceptionMapper;
import com.smartcampus.exception.SensorUnavailableExceptionMapper;
import com.smartcampus.exception.WebApplicationExceptionMapper;
import com.smartcampus.filter.ApiLoggingFilter;
import com.smartcampus.resource.DebugResource;
import com.smartcampus.resource.DiscoveryResource;
import com.smartcampus.resource.RoomResource;
import com.smartcampus.resource.SensorResource;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * JAX-RS application configuration.
 *
 * The coursework brief requires a javax.ws.rs.core.Application subclass with
 * @ApplicationPath("/api/v1") to establish the versioned API entry point.
 * Classes are registered explicitly so resources, filters and exception
 * mappers are available when the WAR is deployed on Apache Tomcat 9.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {

    private final Set<Class<?>> classes;

    public SmartCampusApplication() {
        Set<Class<?>> registered = new HashSet<>();
        registered.add(DiscoveryResource.class);
        registered.add(RoomResource.class);
        registered.add(SensorResource.class);
        registered.add(DebugResource.class);
        registered.add(RoomNotEmptyExceptionMapper.class);
        registered.add(LinkedResourceNotFoundExceptionMapper.class);
        registered.add(SensorUnavailableExceptionMapper.class);
        registered.add(WebApplicationExceptionMapper.class);
        registered.add(GlobalExceptionMapper.class);
        registered.add(ApiLoggingFilter.class);
        this.classes = Collections.unmodifiableSet(registered);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }
}

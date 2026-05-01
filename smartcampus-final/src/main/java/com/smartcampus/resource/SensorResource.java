package com.smartcampus.resource;

import com.smartcampus.DataStore;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.ErrorMessage;
import com.smartcampus.model.Sensor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sensor Resource — manages the /api/v1/sensors collection.
 *
 * Implements full CRUD as taught in Tutorial Week 08
 * (Task: "Add @PUT and @DELETE methods to the TeacherResource"):
 *
 *   GET    /api/v1/sensors            – list sensors, optionally filtered by ?type=
 *   POST   /api/v1/sensors            – register a new sensor (validates roomId)
 *   GET    /api/v1/sensors/{id}       – fetch one sensor
 *   PUT    /api/v1/sensors/{id}       – update a sensor's type, status, or value
 *   DELETE /api/v1/sensors/{id}       – deregister a sensor
 *
 * Sub-resource locator:
 *   ANY    /api/v1/sensors/{id}/readings – delegates to SensorReadingResource
 *
 * Uses javax.ws.rs.* — compatible with Jersey 2.32 on Apache Tomcat 9.
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    // ------------------------------------------------------------------
    // GET /sensors[?type=CO2]  – list or filter sensors
    // ------------------------------------------------------------------
    @GET
    public Response getSensors(@QueryParam("type") String type) {
        Collection<Sensor> all = store.getSensors().values();

        if (type != null && !type.trim().isEmpty()) {
            List<Sensor> filtered = all.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
            return Response.ok(filtered).build();
        }
        return Response.ok(all).build();
    }

    // ------------------------------------------------------------------
    // POST /sensors  – register a new sensor
    // ------------------------------------------------------------------
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor == null || sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            ErrorMessage error = new ErrorMessage(400, "BAD_REQUEST", "Sensor id is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
        if (store.getSensors().containsKey(sensor.getId())) {
            ErrorMessage error = new ErrorMessage(409, "CONFLICT",
                    "A sensor with id '" + sensor.getId() + "' already exists.");
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        }
        // Validate that the referenced room actually exists.
        // Uses LinkedResourceNotFoundException → mapped to 422 by its ExceptionMapper.
        if (sensor.getRoomId() == null || !store.getRooms().containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                    "Room '" + sensor.getRoomId() + "' does not exist. " +
                    "Create the room before registering sensors in it.");
        }
        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }

        store.getSensors().put(sensor.getId(), sensor);
        store.getRooms().get(sensor.getRoomId()).getSensorIds().add(sensor.getId());
        store.getReadingsForSensor(sensor.getId()); // initialise empty reading list

        URI location = URI.create("/api/v1/sensors/" + sensor.getId());
        return Response.created(location).entity(sensor).build();
    }

    // ------------------------------------------------------------------
    // GET /sensors/{sensorId}  – fetch one sensor
    // ------------------------------------------------------------------
    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            ErrorMessage error = new ErrorMessage(404, "NOT_FOUND",
                    "Sensor '" + sensorId + "' not found.");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }
        return Response.ok(sensor).build();
    }

    // ------------------------------------------------------------------
    // PUT /sensors/{sensorId}  – update sensor details
    // Added as per Tutorial Week 08 task: "Add @PUT and @DELETE methods"
    // ------------------------------------------------------------------
    @PUT
    @Path("/{sensorId}")
    public Response updateSensor(@PathParam("sensorId") String sensorId, Sensor updatedSensor) {
        Sensor existing = store.getSensors().get(sensorId);
        if (existing == null) {
            ErrorMessage error = new ErrorMessage(404, "NOT_FOUND",
                    "Sensor '" + sensorId + "' not found.");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }
        // Update allowed fields — id and roomId are immutable via PUT
        if (updatedSensor.getType() != null) {
            existing.setType(updatedSensor.getType());
        }
        if (updatedSensor.getStatus() != null) {
            existing.setStatus(updatedSensor.getStatus());
        }
        if (updatedSensor.getCurrentValue() != 0) {
            existing.setCurrentValue(updatedSensor.getCurrentValue());
        }
        return Response.ok(existing).build();
    }

    // ------------------------------------------------------------------
    // DELETE /sensors/{sensorId}  – deregister a sensor
    // Added as per Tutorial Week 08 task: "Add @PUT and @DELETE methods"
    // Also removes the sensor ID from its parent room's sensorIds list.
    // ------------------------------------------------------------------
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            ErrorMessage error = new ErrorMessage(404, "NOT_FOUND",
                    "Sensor '" + sensorId + "' not found.");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }
        // Remove sensor from parent room's list to keep data consistent
        String roomId = sensor.getRoomId();
        if (roomId != null && store.getRooms().containsKey(roomId)) {
            store.getRooms().get(roomId).getSensorIds().remove(sensorId);
        }
        store.getSensors().remove(sensorId);
        return Response.noContent().build(); // 204 No Content
    }

    // ------------------------------------------------------------------
    // Sub-resource locator  – /sensors/{sensorId}/readings
    // Returning a class instance (not a Response) tells JAX-RS to delegate
    // further path matching to SensorReadingResource.
    // ------------------------------------------------------------------
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        if (!store.getSensors().containsKey(sensorId)) {
            ErrorMessage error = new ErrorMessage(404, "NOT_FOUND",
                    "Sensor '" + sensorId + "' not found.");
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .type(MediaType.APPLICATION_JSON)
                            .entity(error)
                            .build());
        }
        return new SensorReadingResource(sensorId);
    }
}

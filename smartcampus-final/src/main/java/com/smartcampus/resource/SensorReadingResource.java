package com.smartcampus.resource;

import com.smartcampus.DataStore;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.ErrorMessage;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

/**
 * Sensor Reading Sub-Resource
 *
 * Reached via the sub-resource locator in SensorResource:
 *   GET  /api/v1/sensors/{sensorId}/readings  – fetch reading history
 *   POST /api/v1/sensors/{sensorId}/readings  – append a new reading
 *
 * A sensor with status "MAINTENANCE" cannot accept new readings.
 * This is enforced by throwing SensorUnavailableException, which is
 * caught by its ExceptionMapper and returned as a clean 403 Forbidden —
 * demonstrating the ExceptionMapper pattern from Tutorial Week 09
 * and Lecture Week 08.
 *
 * A successful POST also updates the parent sensor's currentValue field.
 *
 * Uses javax.ws.rs.* — compatible with Jersey 2.32 on Apache Tomcat 9.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String    sensorId;
    private final DataStore store = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // ------------------------------------------------------------------
    // GET /readings  – fetch reading history for this sensor
    // ------------------------------------------------------------------
    @GET
    public Response getReadings() {
        List<SensorReading> history = store.getReadingsForSensor(sensorId);
        return Response.ok(history).build();
    }

    // ------------------------------------------------------------------
    // POST /readings  – append a new reading
    // ------------------------------------------------------------------
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = store.getSensors().get(sensorId);

        if (reading == null) {
            ErrorMessage error = new ErrorMessage(400, "BAD_REQUEST",
                    "Request body with a value field is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        // State constraint: MAINTENANCE sensors cannot receive new readings.
        // SensorUnavailableExceptionMapper converts this to HTTP 403.
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId + "' is currently under MAINTENANCE " +
                    "and cannot accept new readings.");
        }

        SensorReading newReading = new SensorReading(reading.getValue());

        // Persist the reading
        store.getReadingsForSensor(sensorId).add(newReading);

        // Side-effect: keep currentValue on the parent sensor up to date
        sensor.setCurrentValue(newReading.getValue());

        URI location = URI.create("/api/v1/sensors/" + sensorId + "/readings/" + newReading.getId());
        return Response.created(location).entity(newReading).build();
    }
}

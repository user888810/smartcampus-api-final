package com.smartcampus.resource;

import com.smartcampus.DataStore;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.ErrorMessage;
import com.smartcampus.model.Room;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Room Resource — manages the /api/v1/rooms collection.
 *
 * Implements full CRUD as taught in Tutorial Week 08 (Task: Add PUT and DELETE):
 *
 *   GET    /api/v1/rooms           – list all rooms
 *   POST   /api/v1/rooms           – create a new room
 *   GET    /api/v1/rooms/{roomId}  – fetch a specific room
 *   PUT    /api/v1/rooms/{roomId}  – update a room's details
 *   DELETE /api/v1/rooms/{roomId}  – remove a room (blocked if sensors present)
 *
 * Uses javax.ws.rs.* — compatible with Jersey 2.32 on Apache Tomcat 9.
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    // ------------------------------------------------------------------
    // GET /rooms  – list all rooms
    // ------------------------------------------------------------------
    @GET
    public Response getAllRooms() {
        Collection<Room> allRooms = store.getRooms().values();
        return Response.ok(allRooms).build();
    }

    // ------------------------------------------------------------------
    // POST /rooms  – create a room
    // ------------------------------------------------------------------
    @POST
    public Response createRoom(Room room) {
        if (room == null || room.getId() == null || room.getId().trim().isEmpty()) {
            ErrorMessage error = new ErrorMessage(400, "BAD_REQUEST", "Room id is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
        if (store.getRooms().containsKey(room.getId())) {
            ErrorMessage error = new ErrorMessage(409, "CONFLICT",
                    "A room with id '" + room.getId() + "' already exists.");
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        }
        if (room.getSensorIds() == null) {
            room.setSensorIds(Collections.synchronizedList(new ArrayList<>()));
        } else {
            room.setSensorIds(room.getSensorIds());
        }
        store.getRooms().put(room.getId(), room);

        URI location = URI.create("/api/v1/rooms/" + room.getId());
        return Response.created(location).entity(room).build();
    }

    // ------------------------------------------------------------------
    // GET /rooms/{roomId}  – fetch one room
    // ------------------------------------------------------------------
    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            ErrorMessage error = new ErrorMessage(404, "NOT_FOUND",
                    "Room '" + roomId + "' not found.");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }
        return Response.ok(room).build();
    }

    // ------------------------------------------------------------------
    // PUT /rooms/{roomId}  – update a room's name and capacity
    // Added as per Tutorial Week 08 task: "Add @PUT and @DELETE methods"
    // ------------------------------------------------------------------
    @PUT
    @Path("/{roomId}")
    public Response updateRoom(@PathParam("roomId") String roomId, Room updatedRoom) {
        Room existing = store.getRooms().get(roomId);
        if (existing == null) {
            ErrorMessage error = new ErrorMessage(404, "NOT_FOUND",
                    "Room '" + roomId + "' not found.");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }
        // Update allowed fields — id and sensorIds are immutable via PUT
        if (updatedRoom.getName() != null) {
            existing.setName(updatedRoom.getName());
        }
        if (updatedRoom.getCapacity() > 0) {
            existing.setCapacity(updatedRoom.getCapacity());
        }
        return Response.ok(existing).build();
    }

    // ------------------------------------------------------------------
    // DELETE /rooms/{roomId}  – decommission a room
    // Business rule: a room cannot be deleted while sensors are assigned.
    // The RoomNotEmptyExceptionMapper converts this to a 409 Conflict
    // response — demonstrating the ExceptionMapper pattern from
    // Tutorial Week 09 and Lecture Week 08.
    // ------------------------------------------------------------------
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            ErrorMessage error = new ErrorMessage(404, "NOT_FOUND",
                    "Room '" + roomId + "' not found.");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                    "Room '" + roomId + "' cannot be deleted: it still has " +
                    room.getSensorIds().size() + " sensor(s) assigned.");
        }
        store.getRooms().remove(roomId);
        return Response.noContent().build(); // 204 No Content
    }
}

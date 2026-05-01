package com.smartcampus;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton in-memory data store.
 *
 * Because JAX-RS resource classes are instantiated per-request by default
 * (as explained in Tutorial Week 07), all shared state must live outside them.
 * This class uses a Singleton pattern so every resource class accesses the same
 * in-memory maps — equivalent to the static MockDatabase taught in Tutorial Week 08.
 *
 * ConcurrentHashMap is used for map-level safety, and reading histories use
 * synchronized lists so concurrent appends are safer during Postman testing.
 */
public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    private final Map<String, Room>                rooms    = new ConcurrentHashMap<>();
    private final Map<String, Sensor>              sensors  = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    private DataStore() {
        seed();
    }

    public static DataStore getInstance() {
        return INSTANCE;
    }

    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------

    public Map<String, Room>   getRooms()    { return rooms;   }
    public Map<String, Sensor> getSensors()  { return sensors; }

    public List<SensorReading> getReadingsForSensor(String sensorId) {
        return readings.computeIfAbsent(sensorId, k -> Collections.synchronizedList(new ArrayList<>()));
    }

    // -----------------------------------------------------------------------
    // Seed data — available immediately on startup for Postman demos
    // -----------------------------------------------------------------------

    private void seed() {
        Room r1 = new Room("LIB-301", "Library Quiet Study",   80);
        Room r2 = new Room("LAB-101", "Computer Science Lab",  30);
        Room r3 = new Room("HALL-01", "Main Lecture Hall",    200);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);
        rooms.put(r3.getId(), r3);

        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE",      21.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001",  "CO2",         "ACTIVE",     412.0, "LIB-301");
        Sensor s3 = new Sensor("OCC-001",  "Occupancy",   "MAINTENANCE",  0.0, "LAB-101");
        Sensor s4 = new Sensor("TEMP-002", "Temperature", "OFFLINE",     19.0, "HALL-01");

        for (Sensor s : new Sensor[]{s1, s2, s3, s4}) {
            sensors.put(s.getId(), s);
            rooms.get(s.getRoomId()).getSensorIds().add(s.getId());
            readings.put(s.getId(), Collections.synchronizedList(new ArrayList<>()));
        }
    }
}

# Smart Campus Sensor & Room Management API

**Module:** 5COSC022W — Client-Server Architectures (2025/26)  
**Technology:** JAX-RS (Jersey 2.32) · Apache Tomcat 9 · Maven

---

## API Overview

This project implements a RESTful API for the University of Westminster's Smart Campus initiative. It provides a centralised interface for managing campus **Rooms** and the **Sensors** deployed within them (temperature monitors, CO2 trackers, occupancy sensors, etc.), as well as a historical log of **Sensor Readings**.

The API is built exclusively using JAX-RS (Jersey 2.32) deployed as a WAR on Apache Tomcat 9. No Spring Boot, no database — only in-memory data structures (ConcurrentHashMap, ArrayList) as required by the coursework specification.

### Resource Hierarchy

```
/api/v1                               <- Discovery (metadata + navigational links)
/api/v1/rooms                         <- Room collection
/api/v1/rooms/{roomId}                <- Individual room
/api/v1/sensors                       <- Sensor collection (supports ?type= filter)
/api/v1/sensors/{sensorId}            <- Individual sensor
/api/v1/sensors/{sensorId}/readings   <- Reading history (sub-resource locator)
/api/v1/debug/crash                   <- Demo endpoint for the global 500 mapper
```

### Seed Data (available immediately on startup)

| Room ID  | Name                  | Capacity |
|----------|-----------------------|----------|
| LIB-301  | Library Quiet Study   | 80       |
| LAB-101  | Computer Science Lab  | 30       |
| HALL-01  | Main Lecture Hall     | 200      |

| Sensor ID | Type        | Status      | Room    |
|-----------|-------------|-------------|---------|
| TEMP-001  | Temperature | ACTIVE      | LIB-301 |
| CO2-001   | CO2         | ACTIVE      | LIB-301 |
| OCC-001   | Occupancy   | MAINTENANCE | LAB-101 |
| TEMP-002  | Temperature | OFFLINE     | HALL-01 |

---

## How to Build and Run

### Prerequisites

- Java 11 or later
- Apache Maven 3.6+
- Apache Tomcat 9.x ([download](https://tomcat.apache.org/download-90.cgi))
- NetBeans IDE (recommended, as used in tutorials)

### Step 1 — Clone the repository

```bash
git clone <your-github-repo-url>
cd smartcampus-api
```

### Step 2 — Build the WAR file

```bash
mvn clean package
```

This produces `target/smartcampus-api.war`.

### Step 3 — Deploy to Apache Tomcat 9

**Option A — NetBeans (recommended):**
1. Open the project: File → Open Project
2. Right-click the project → Properties → Run → select your Tomcat 9 server
3. Right-click the project → Clean and Build
4. Right-click the project → Run

**Option B — Manual Tomcat deployment:**
```bash
cp target/smartcampus-api.war $TOMCAT_HOME/webapps/
$TOMCAT_HOME/bin/startup.sh    # Linux/Mac
$TOMCAT_HOME/bin/startup.bat   # Windows
```

### Step 4 — Verify the server is running

Navigate to:
```
http://localhost:8080/smartcampus-api/api/v1
```
You should receive the discovery JSON response.

> **Port note:** The default Tomcat port is 8080. Check `server.xml` inside your Tomcat  
> `conf/` folder if you need to confirm the connector port.

---

## Sample curl Commands

### 1. Discovery endpoint
```bash
curl -X GET http://localhost:8080/smartcampus-api/api/v1 \
     -H "Accept: application/json"
```

### 2. List all rooms
```bash
curl -X GET http://localhost:8080/smartcampus-api/api/v1/rooms \
     -H "Accept: application/json"
```

### 3. Create a new room
```bash
curl -X POST http://localhost:8080/smartcampus-api/api/v1/rooms \
     -H "Content-Type: application/json" \
     -d '{"id":"ENG-205","name":"Engineering Lab","capacity":40}'
```

### 4. Get a specific room by ID
```bash
curl -X GET http://localhost:8080/smartcampus-api/api/v1/rooms/LIB-301 \
     -H "Accept: application/json"
```

### 5. Update a room
```bash
curl -X PUT http://localhost:8080/smartcampus-api/api/v1/rooms/LIB-301 \
     -H "Content-Type: application/json" \
     -d '{"name":"Library Main Reading Room","capacity":100}'
```

### 6. Attempt to delete a room with sensors assigned (demonstrates 409 Conflict)
```bash
curl -X DELETE http://localhost:8080/smartcampus-api/api/v1/rooms/LIB-301 \
     -H "Accept: application/json"
```

### 7. Register a new sensor
```bash
curl -X POST http://localhost:8080/smartcampus-api/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{"id":"HUM-001","type":"Humidity","status":"ACTIVE","currentValue":55.0,"roomId":"LIB-301"}'
```

### 8. Filter sensors by type
```bash
curl -X GET "http://localhost:8080/smartcampus-api/api/v1/sensors?type=Temperature" \
     -H "Accept: application/json"
```

### 9. Post a sensor reading
```bash
curl -X POST http://localhost:8080/smartcampus-api/api/v1/sensors/TEMP-001/readings \
     -H "Content-Type: application/json" \
     -d '{"value":23.7}'
```

### 10. Get reading history for a sensor
```bash
curl -X GET http://localhost:8080/smartcampus-api/api/v1/sensors/TEMP-001/readings \
     -H "Accept: application/json"
```

### 11. POST reading to a MAINTENANCE sensor (demonstrates 403 Forbidden)
```bash
curl -X POST http://localhost:8080/smartcampus-api/api/v1/sensors/OCC-001/readings \
     -H "Content-Type: application/json" \
     -d '{"value":12.0}'
```

### 12. Register sensor with non-existent roomId (demonstrates 422 Unprocessable Entity)
```bash
curl -X POST http://localhost:8080/smartcampus-api/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{"id":"TEST-001","type":"Temperature","roomId":"DOES-NOT-EXIST"}'
```

### 13. Trigger the global 500 safety-net mapper
```bash
curl -X GET http://localhost:8080/smartcampus-api/api/v1/debug/crash \
     -H "Accept: application/json"
```

---

## Report — Answers to Coursework Questions

### Part 1.1 — JAX-RS Resource Lifecycle & In-Memory Data Management

By default, JAX-RS creates a **new instance of each resource class for every incoming HTTP request** (request-scoped). This means any instance variables declared inside a resource class are not shared between requests. If room or sensor data were stored directly inside `RoomResource`, each new request would start with empty data and all previously stored information would be lost.

To solve this, the application uses a **Singleton `DataStore` class** — a class with a private constructor and a static `getInstance()` method that guarantees only one instance exists for the entire lifetime of the application. Every resource class calls `DataStore.getInstance()` to access the same shared maps, regardless of which request thread is executing.

For thread-safety, **`ConcurrentHashMap`** is used instead of a plain `HashMap` because multiple requests can arrive simultaneously and attempt concurrent read/write operations on the same map. A standard `HashMap` is not thread-safe and can produce race conditions and data corruption under concurrent access. `ConcurrentHashMap` handles concurrent reads and writes safely without explicit locking. Sensor reading histories use `Collections.synchronizedList()` to protect `ArrayList` instances from concurrent modification when simultaneous POST requests attempt to append readings to the same sensor.

---

### Part 1.2 — HATEOAS and Hypermedia in RESTful Design

HATEOAS (Hypermedia as the Engine of Application State) is the highest level of REST maturity (Richardson Maturity Model Level 3). Instead of requiring clients to have hardcoded knowledge of every URL, the server **embeds navigational links directly inside responses**. The discovery endpoint at `GET /api/v1` demonstrates this by returning links to `/api/v1/rooms` and `/api/v1/sensors` within the JSON body.

The benefit over static documentation is that clients can **dynamically navigate the API by following links** returned in responses. If the server changes a URL structure, clients following embedded links rather than hardcoded paths continue to work without modification. Static documentation becomes stale the moment the API changes — HATEOAS makes the API self-describing, reducing the integration burden on client developers and eliminating the risk of clients depending on outdated URL patterns. It also allows the server to drive the application state of the client, which is the original intent of REST as defined by Roy Fielding.

---

### Part 2.1 — Returning IDs vs Full Objects for Room Lists

Returning **only IDs** reduces payload size significantly, which benefits bandwidth-limited clients such as mobile applications or IoT devices. However, it forces the client to make N additional GET requests — one per room ID — to retrieve actual room data. This is known as the **N+1 problem** and dramatically increases total latency and server load at scale; retrieving 100 rooms would require 101 HTTP round-trips.

Returning **full objects** in a single response eliminates these extra round-trips at the cost of larger individual payloads. For this API, full `Room` objects are returned because the model is lightweight (id, name, capacity, sensorIds) and clients performing room management typically need all fields to display meaningful information. The marginal bandwidth increase is justified by the elimination of multiple round-trips and the simpler client-side code.

---

### Part 2.2 — Idempotency of the DELETE Operation

The DELETE operation is **effectively idempotent** in this implementation. HTTP idempotency means that making the same request multiple times produces the same server state.

- **First DELETE** on a room with no sensors: removes the room, returns `204 No Content`.
- **Subsequent DELETE** for the same roomId: the room no longer exists, returns `404 Not Found`.

The server state is consistent across both calls — the room is absent. The difference in status codes (204 vs 404) is standard practice and does not violate idempotency, because idempotency concerns **server state**, not response codes. RFC 7231 explicitly acknowledges that a second DELETE on an already-deleted resource may return 404 while still being considered idempotent.

If a room **has sensors assigned**, all DELETE requests — first and subsequent — throw `RoomNotEmptyException`, mapped to `409 Conflict` without modifying any server state. This is also idempotent: repeated calls produce the same outcome.

---

### Part 3.1 — @Consumes and Media Type Mismatch Consequences

When `@Consumes(MediaType.APPLICATION_JSON)` is declared on a method, JAX-RS uses the **`Content-Type` header** of the incoming request to select the matching resource method. If a client sends a POST request with `Content-Type: text/plain` or `Content-Type: application/xml`, JAX-RS cannot find a resource method that declares it consumes that media type.

Jersey immediately returns **HTTP 415 Unsupported Media Type** without invoking any application code. The request body is never parsed, no resource method is called, and no application logic executes. This is JAX-RS's built-in content negotiation mechanism, which protects the API from malformed or unexpected input formats at the framework level — before any developer-written code has a chance to process potentially harmful or unexpected data.

---

### Part 3.2 — @QueryParam vs Path Parameter for Filtering

**Query parameters** (`GET /api/v1/sensors?type=CO2`) are semantically designed for filtering, searching, and sorting — optional modifications to what is fundamentally the same resource endpoint. **Path parameters** (`/api/v1/sensors/type/CO2`) imply a hierarchically distinct sub-resource, suggesting "type" is a separate resource under sensors rather than a filter criterion.

The query parameter approach is superior for four reasons. First, the endpoint `/api/v1/sensors` remains a single, stable URI for the sensors collection — the same URL retrieves all sensors or filtered sensors depending on optional parameters. Second, multiple filters compose cleanly: `?type=CO2&status=ACTIVE` requires no URL restructuring. Third, omitting the parameter naturally returns the unfiltered full collection, which is intuitive. Fourth, path parameters should identify specific resources (e.g., a sensor by ID), not describe retrieval criteria — mixing concerns into the path violates REST's resource naming conventions and makes URLs harder to cache and document.

---

### Part 4.1 — Benefits of the Sub-Resource Locator Pattern

Without the sub-resource locator pattern, every nested endpoint (`/sensors/{id}/readings`, and any future nested paths) would have to be defined inside a single `SensorResource` class. As the API grows, this class becomes a massive controller handling sensor CRUD, reading history management, and all future nested concerns — a violation of the **Single Responsibility Principle**.

The sub-resource locator (`@Path("/{sensorId}/readings")` returning a `SensorReadingResource` instance) allows each class to own one responsibility: `SensorResource` handles sensor CRUD; `SensorReadingResource` handles reading history. JAX-RS delegates further path matching to the returned instance, allowing the locator method to perform validation (checking the sensor exists) **before** handing off to the sub-resource. This creates a clean validation boundary. Each class is independently readable, testable, and maintainable. In large APIs with many nested resource types, this delegation pattern prevents any single class from growing unmanageable and allows teams to develop sub-resources in parallel without conflicts.

---

### Part 5.2 — HTTP 422 vs 404 for Missing Referenced Resources

**HTTP 404 Not Found** signals that the **URL** of the request does not correspond to any resource on the server. If a client POSTs to `/api/v1/sensors` (a valid, existing endpoint) with a `roomId` that does not exist, the URL itself is perfectly valid — the problem is the **semantic content of the JSON payload** referencing a non-existent entity.

Returning 404 would mislead the client into believing the `/sensors` endpoint does not exist, potentially causing it to retry with a different URL. **HTTP 422 Unprocessable Entity** precisely communicates that the server understood the request format, the endpoint exists, the JSON was syntactically well-formed — but the payload's semantic content is invalid (referencing a non-existent room). This distinction is critical for client error handling: 422 unambiguously instructs the developer to correct the payload data, not the URL or HTTP method.

---

### Part 5.4 — Cybersecurity Risks of Exposing Stack Traces

A raw Java stack trace exposes several categories of sensitive information:

1. **Internal class and package names** (e.g., `com.smartcampus.DataStore`) reveal the application's internal architecture, allowing attackers to map the codebase and identify specific attack surfaces.
2. **Method names and line numbers** reveal exact code execution paths, helping attackers understand control flow and identify precisely where exceptions occur — useful for crafting targeted exploits.
3. **Library names and versions** (e.g., `jersey-server-2.32.jar`, `jackson-databind-2.15.2`) allow attackers to query public vulnerability databases (such as the NVD — National Vulnerability Database) for known CVEs affecting those specific versions.
4. **File system paths** (e.g., `/home/user/webapps/smartcampus-api/...`) can reveal server directory structure, OS details, and deployment environment information.

The `GlobalExceptionMapper<Throwable>` prevents all of this by logging the complete stack trace **internally** using `java.util.logging.Logger` (server-side only), while returning only a safe, generic JSON message to the client — containing no internal technical details.

---

### Part 5.5 — JAX-RS Filters vs Manual Logging

Inserting `Logger.info()` calls manually inside every resource method violates the **DRY (Don't Repeat Yourself)** principle and the **Single Responsibility Principle**. If logging requirements change — adding correlation IDs, request timing, authentication context, or structured log formats — every resource method must be modified individually. This maintenance burden grows linearly with the size of the API and introduces a high risk of inconsistent logging across endpoints.

JAX-RS `ContainerRequestFilter` and `ContainerResponseFilter` are **cross-cutting concerns** that execute automatically for every request/response cycle without modifying any resource class. Registering a single `@Provider` filter applies logging universally across all endpoints. Resource classes remain focused purely on business logic, and logging behaviour can be enabled, disabled, or modified in one place. It also enables the filter to be conditionally applied (e.g., only in production via feature flags) without touching resource code — a clean architectural separation that scales well as the API grows.

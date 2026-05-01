# Smart Campus Sensor & Room Management API

This project is a RESTful Smart Campus API built using JAX-RS. It manages campus rooms, sensors and sensor readings, including HATEOAS links, filtering, sub-resources, validation and exception handling.

## Report

### Part 1.1 JAX-RS Resource Lifecycle

By default JAX-RS creates a new instance of each resource class per request, which means instance variables are not shared between requests. This was significant because each request was naturally isolated from others.

However, the harder part was the shared `DataStore` singleton. The issue I had was that two requests hitting the server at the same time and both writing to a normal `HashMap` could corrupt the data because `HashMap` is not built for concurrent access. I used `ConcurrentHashMap` instead, which handles concurrent writes without locking the whole map at once. This meant the data would stay consistent.

### Part 1.2 HATEOAS

HATEOAS is a principle where the API includes metadata such as links and names so clients can navigate it without needing to know all the URLs beforehand. A benefit is that if URLs change later, clients following links will still work. Static documentation can go out of date quickly and cannot always guarantee it matches what the API is actually doing.

The discovery endpoint at `GET /api/v1` gives a starting point that finds everything else. I found this useful when testing because I could start there. Clients are also able to explore the API from a single entry point without needing all the documentation first.

### Part 2.1 ID-only vs Full Object in List Responses

I went with returning full objects in the list instead of just IDs. Returning only IDs meant the client had to make a separate request for each room, which felt unnecessary, especially for something small. Therefore, I picked full objects in the list to improve efficiency.

If the objects were much bigger with a lot of fields, I would reconsider it. However, for rooms, it made more sense to send everything back at once.

### Part 2.2 Idempotency of DELETE

Idempotency is the concept that repeating the same request more than once still gives the same final result as making the request the first time. For `DELETE`, it is idempotent here because if you send the same request twice or more, the end state is the same either way: the room does not exist.

If the room exists, it gets deleted and gives a `204` response. If it has already been deleted, it gives a `404` response. The response code is different, but the actual result of the resource is the same, which is what idempotency means. It is not about getting the same response every time, but about the server ending up in the same state.

### Part 3.1 @Consumes and Content-Type Mismatches

`@Consumes(MediaType.APPLICATION_JSON)` tells JAX-RS to only accept requests with `application/json` as the content type. If the wrong content type is sent, the API returns a `415` response before it even reaches the resource method.

I kept running into this during testing because I forgot to set the header in Postman and did not understand why everything kept failing. Once I worked out what was happening, it made sense. It is better that JAX-RS handles this automatically because otherwise the content type would need to be checked manually in every method, which would become repetitive.

### Part 3.2 @QueryParam vs Path Parameter for Filtering

Query parameters made more sense for filtering than path parameters. Something like `/sensors/CO2` makes it look like `CO2` is the ID of a specific sensor. However, what was actually happening was filtering a collection, not looking up one specific sensor. Using `?type=CO2` made it clearer and it is optional, so if it is left out, all sensors are returned.

I was not sure about this at first, but after looking into it more, query parameters are clearly the better approach for filtering. They can also be combined if needed, which would become complicated with path segments.

### Part 4.1 Sub-Resource Locator Pattern

A sub-resource locator is a method that does not handle the request itself. Instead, it returns another object that will handle the request. It only has `@Path` on it with no `@GET` or `@POST`, so JAX-RS knows to pass the request on.

I used this for `/sensors/{id}/readings` so that `SensorResource` focuses on sensors and `SensorReadingResource` handles the readings side of things. Without this pattern, everything would end up in one class and would become harder to manage. This was probably the part I found most interesting.

### Part 4.2 Historical Data Management

Historical data management works by using the `GET` function to return the history and the `POST` function to add a new reading. A side effect of the `POST` function is that when a reading is posted, it also updates the parent sensor's `currentValue` automatically.

For example, if a reading of `22.5` is posted, `currentValue` will also automatically become `22.5`. Otherwise, the sensor would have an outdated value, which would not make sense.

### Part 5.2 Why 422 over 404 for Missing References

A `404` response means the URL does not exist, but `POST /api/v1/sensors` is still a valid URL. A `422` response means the request has been understood, but there is an issue with the content.

The issue is the `roomId` in the body referencing a room that does not exist. `422` made more sense because the server understood the request and the JSON was valid. The problem was specifically with the content inside it. I think it gives a clearer idea about what went wrong compared to just sending back a `404` response.

### Part 5.4 Cybersecurity Risks of Exposing Stack Traces

A stack trace is an in-depth error message that describes exactly what is happening inside the code. This means information such as class names, method names and line numbers can be shown.

This is a cybersecurity risk because returning raw stack traces tells an attacker how the application is structured. For example, `com.smartcampus.resource.SensorResource` shows the package and class structure. It can also expose which libraries and versions are being used, such as Jersey 2.32. This is important because vulnerabilities for specific libraries and versions can be searched.

Another risk is that visible line numbers can help an attacker understand which part of the application to target. Therefore, exposing stack traces creates significant cybersecurity risks. A global exception mapper helps prevent this by returning a generic message to the client while keeping the real error details in the server logs only.

### Part 5.5 JAX-RS Filters vs Manual Logging

Adding `Logger.info()` manually to every method means you have to remember to do it every time a new endpoint is added. If you forget, that endpoint will not be logged.

Using a JAX-RS filter with `@Provider` means it runs automatically on every request without needing to add logging code to each method. This is much easier to maintain.

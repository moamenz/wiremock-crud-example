# WireMock CRUD Mock Service

A self-contained **WireMock 3.x** example that mocks a `/users` REST API. Used
by client-side developers to build and test features **without
needing the real backend.

\u2705 Clone & run in 30 seconds:

```bash
git clone <repo-url>
cd wiremock-crud-example
./mvnw clean verify       # runs all 8 tests
```

Then start the server and hit it with curl:

```bash
# terminal 1 \u2014 start the server
./mvnw compile exec:java -Dexec.mainClass=com.example.MockServerMain

# terminal 2 \u2014 hit it
curl http://localhost:8080/users
curl http://localhost:8080/users/1
curl http://localhost:8080/users/999
curl -X POST http://localhost:8080/users \
  -H 'Content-Type: application/json' \
  -d '{"name":"Alice","email":"alice@example.com"}'
```

Open the WireMock admin UI for a *visual* view of stubs, requests, and to
stubs at runtime without recompiling:

```
http://localhost:8080/__admin
```


---

## End-to-End Demo Map

What is mocked and what each request returns.  Every row below is also
asserted by the JUnit suite in src/test/java/com/example/CrudMockTest.java,
so the same behaviour is verified in CI and when you run the demo.

1. GET    /users       -> 200  JSON array of 2 users (Alice and Bob)
2. GET    /users/1     -> 200  Single Alice object
3. GET    /users/999   -> 404  {"error":"User not found"}
4. POST   /users       -> 201  Created user + Location header
5. PUT    /users/1     -> 200  Alice Updated
6. DELETE /users/1     -> 204  Empty body


---

## How to Run

### Requirements

- JDK 17+  (check with:  java --version)
- Maven is NOT required - the repo ships with the Maven Wrapper (./mvnw),
  which auto-downloads Maven 3.9.9 on first run.

### Test the whole thing (8 JUnit tests)

    ./mvnw clean verify

### Start the mock server on :8080

    ./mvnw compile exec:java -Dexec.mainClass=com.example.MockServerMain

In a second terminal, hit it:

    curl -s http://localhost:8080/users
    curl -s http://localhost:8080/users/1
    curl -s http://localhost:8080/users/999
    curl -s -X POST http://localhost:8080/users -H Content-Type:application/json -d {"name":"Alice"}


---

## How to Add a New Mock

All stubs live in one file:

  src/main/java/com/example/stubs/UserStubs.java

To add a new endpoint:

1.  Copy an existing method (e.g. getUserMissing) as a template.
2.  Change the HTTP verb, URL path, and response body.
3.  Register the new stub in registerAll(...) with one extra line.
4.  Done. No YAML, no separate config file.


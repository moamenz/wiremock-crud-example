# WireMock CRUD Mock Service

A self-contained **WireMock 3.x** example that mocks a `/users` REST API. Client-side
developers can build and test features **without the real backend**.

Ready: clone, `./mvnw clean verify`, then run the server and curl it.

```
git clone https://github.com/moamenz/wiremock-crud-example.git
cd wiremock-crud-example
./mvnw clean verify      # 8 JUnit tests, ~30s on first run (Maven Wrapper auto-downloads)

# then run the server in a second terminal:
./mvnw compile exec:java -Dexec.mainClass=com.example.MockServerMain
```

Open the admin UI to browse every stub and request (and to add, edit,
or delete at runtime without recompiling):

```
http://localhost:8080/__admin
```

---

## End-to-End Demo Map

What is mocked and what each request returns. **The same responses are asserted**
by the JUnit suite in `src/test/java/com/example/CrudMockTest.java`, so CI and the
live demo agree.

| # | | # | | # | | # | | # | | # | | # | | # | | # | | # | | # | | # |-|--------|-----------|
| 1 | GET    | /users      | 200    | `[{"id":1,"name":"Alice","email":"alice@example.com"}, {"id":2,"name":"Bob","email":"bob@example.com"}]` |
| 2 | GET    | /users/1    | 200    | `{"id":1,"name":"Alice","email":"alice@example.com"}` |
| 3 | GET    | /users/999  | 404    | `{"error":"User not found"}` |
| 4 | POST   | /users      | 201    | `{"id":1,"name":"Alice","email":"alice@example.com"}` + header `Location: /users/1` |
| 5 | PUT    | /users/1    | 200    | `{"id":1,"name":"Alice Updated","email":"alice.new@example.com"}` |
| 6 | DELETE | /users/1    | 204    | (empty body) |

---

## How to Run

### Requirements

- **JDK 17 or later** (`java --version`). Other JDKs will also work.
- **No Maven install needed.** The repo ships with the **Maven Wrapper**
  (`./mvnw`, `mvnw.cmd`, and `.mvn/`). The wrapper auto-downloads
  Maven 3.9.9 on the first run. No further setup.

### One-liner: run the tests

```
./mvnw clean verify
```

### One-liner: start the mock server

```
./mvnw compile exec:java -Dexec.mainClass=com.example.MockServerMain
```

The server listens on **port 8080**. Use `Ctrl+C` to stop it.

### Hitting it (in a second terminal)

```
curl -s http://localhost:8080/users
curl -s http://localhost:8080/users/1
curl -s http://localhost:8080/users/999
curl -s -X POST http://localhost:8080/users \\
  -H 'Content-Type: application/json' \\
  -d '{"name":"Alice","email":"alice@example.com"}'
curl -s -o /dev/null -w '%{http_code}\n' -X DELETE http://localhost:8080/users/1
```

### Looking at requests and stubs

The admin UI is at `http://localhost:8080/__admin`. You can:

- See every stub, request, and response side-by-side.
- Add, edit, or delete stubs live (they reset on restart).
- Clear request history.

---

## How to Add a New Mock

**All stubs live in one file:** `src/main/java/com/example/stubs/UserStubs.java`.
There is no YAML, no JSON, no separate config file.

To add a new endpoint, follow these four steps:

1. **Copy** an existing method (e.g. `getUserMissing`) as a template.
2. **Change** the HTTP verb (`get/post/put/patch/delete`), the URL path
   (`urlEqualTo("/your-path")`), and the response body.
3. **Register** the new stub inside `registerAll(WireMockServer server)`
   with one extra line: `server.stubFor(yourNewMethod());`
4. **Done.** Rebuild and the mock is live. Add a JUnit test in
   `CrudMockTest.java` to keep CI in sync.

### Copy-and-edit example

Suppose you need a mock for `GET /orders` that returns `200` with a JSON
array. Add to `UserStubs.java`:

```java
public static MappingBuilder listOrders() {
    return get(urlEqualTo("/orders"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("[{\"id\":1,\"status\":\"shipped\"},{\"id\":2,\"status\":\"pending\"}]"));
}
```

And inside `registerAll(WireMockServer server)` add:

```java
server.stubFor(listOrders());
```

And in `CrudMockTest.java` add a `@Test` that does `station.get("/orders")`
and asserts `httpStatus 200` and the expected body.

### Stateful sequences

If you need to mock a *sequence* of calls (e.g. "first `GET /users/100` returns
404, after `POST /users/100` it returns 200"), see
`registerStatefulScenario(WireMockServer server)` in the same file. It uses
WireMock's `Scenario` API with named states (`STARTED` and `created`) and
`willSetStateTo(...)` to flip the state. Change the state names to match
your domain.

---

## Why This Is Useful for the Team

1. **Decouple client work from backend readiness.**
   Start building the client *before* the real API is deployed. Stub-first,
   no waiting for a staging environment to catch up.

2. **Test error paths on demand.**
   Toggle a stub between 200 / 404 / 500 / 401 in one place to exercise
   UI edge cases (empty state, retry path, permission-denied UI) without
   creating the specific data in a test database.

3. **Mock sequences without a state machine.**
   The `registerStatefulScenario` example shows a "first-fail-then-succeed"
   pattern using WireMock scenarios — useful for auth flows, polling,
   optimistic-update flows.

4. **One command, reproducible.**
   `./mvnw clean verify` works the same on CI, on a laptop, and in a fresh
   container. The Maven Wrapper guarantees the same Maven version everywhere.

5. **Living documentation.**
   `git diff` on `UserStubs.java` *is* the API-change log. The intent of
   each stub is expressed in code, not a config file.

---

## Project Layout

```
.
├── .github/workflows/ci.yml   ┐
├── .gitignore                 ┤
├── .mvn/                      ┤  build tooling
├── mvnw  (Java 17, Maven 3.9.9)
├── mvnw.cmd
├── pom.xml                    ┘
├── README.md
└── src
    ├── main/java/com/example
    │   ├── MockServerMain.java        ← entry point (the thing you `mvn exec:java` on)
    │   ├── client/UserApiClient.java  ← example HTTP client (OkHttp)
    │   ├── model/User.java            ← POJO (name, email, id)
    │   └── stubs/UserStubs.java       ← every stub + the stateful  scenario
    └── test/java/com/example
        └── CrudMockTest.java          ← 8 JUnit 5 tests
```

**Where to look when:**

- *"I need to add a new stub"* → `src/main/java/com/example/stubs/UserStubs.java`
- *"I need to add a new test"*  → `src/test/java/com/example/CrudMockTest.java`
- *"I need a POJO payload"*     → `src/main/java/com/example/model/User.java`
- *"I need an HTTP client"*     → `src/main/java/com/example/client/UserApiClient.java`
- *"How do I start the server?"* → `src/main/java/com/example/MockServerMain.java`
- *"What does CI run?"*         → `.github/workflows/ci.yml`

---

## Requirements

| Need | Version | Why |
|------|---------|-----|
| JDK  | 17+     | Target in the `pom.xml` (`<maven.compiler.release>17</maven.compiler.release>`). Java 21 works too. |
| Maven| **not required** | `./mvnw` auto-downloads 3.9.9 on first run. |
| WireMock | 3.10.0 | Dependency (test-scope for the test suite, compile scope for the standalone server). |
| OkHttp | 4.12.0 | Used by the example HTTP client (`UserApiClient`). Harmless to ignore if you don't use it. |
| JUnit 5 | 5.10.1 | Plain JUnit (no Mockito, no Spring, no RestAssured) — keeps the template clean. |

---

## Troubleshooting

### `mvn: command not found`

Use `./mvnw` instead. It's checked in and runs without a global Maven
install.

### `Port 8080 is already in use`

Either kill the other process, or edit `MockServerMain.java` and change
the port (also the base URL in `UserApiClient`).

### `Unable to locate a Java Runtime`

Install JDK 17+ (or any JDK 17+). `java --version` should report it.

### Tests pass but my curl doesn't get the right response

- Are you pointing at the right port? (default `8080`)
- Is the request body right? Check the Content-Type header and JSON shape.
- The admin UI at `http://localhost:8080/__admin` will show *every* request
  that arrived, so you can see what WireMock actually matched.

### Windows / Git Bash

Everything works the same. On Windows Command Prompt use `mvnw.cmd` instead
of `mvnw`. Same scripts, same results.

---

## When to Graduate

This template is intentionally a toy: 1 POJO, 6 CRUD stubs, 1 scenario, no
dynamic logic. To graduate it into a real API mock, in rough order of
impact:

1. **Move payloads to `src/main/resources/mappings/*.json`** so non-Java
   folks on the team can edit them directly (WireMock's native format).
2. **Use `wiremock-jre8-standalone.jar`** (single fat jar) to ship the mock
   as a service to the team, decoupled from your build.
3. **Add parameterized stubs** (`urlPathPattern` + `responseTemplate`) for
   per-id behavior.
4. **Add a JWT / auth stub** so client devs can exercise 401s and the
   "retry with token" path.

Migrate to a full-mock server (e.g. MockServer, WireCloud, or a Spring
Boot app with `@MockBean`s) **only when** the contract stabilizes and you
need a *shared* mock, not a *reference* one.

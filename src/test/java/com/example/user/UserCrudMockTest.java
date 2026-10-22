package com.example.user;

import com.example.user.UserApiClient;
import com.example.user.UserStubs;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests that consume the embedded WireMock "users" CRUD service.
 *
 * The server starts on a random free port for the whole test class, all stubs
 * are registered once, and the server is torn down afterwards. This is the
 * self-contained, no-external-dependency way to run the mocks.
 */
class CrudMockTest {

    private static WireMockServer server;
    private static UserApiClient client;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @BeforeAll
    static void startServer() {
        server = new WireMockServer(WireMockConfiguration.wireMockConfig().port(0));
        server.start();
        UserStubs.registerAll(server);
        client = new UserApiClient(server.baseUrl());
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @BeforeEach
    void resetScenarios() {
        // Reset scenario state so the stateful test is deterministic
        server.resetScenarios();
    }

    // ------------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------------
    @Test
    void createUser_returns201WithIdAndLocation() throws Exception {
        UserApiClient.Result res = client.create("{\"name\":\"Alice\",\"email\":\"alice@example.com\"}");

        assertEquals(201, res.status);
        assertEquals("/users/1", res.location);

        JsonNode body = MAPPER.readTree(res.body);
        assertEquals(1, body.get("id").asLong());
        assertEquals("Alice", body.get("name").asText());
        assertEquals("alice@example.com", body.get("email").asText());
    }

    // ------------------------------------------------------------------
    // READ (existing)
    // ------------------------------------------------------------------
    @Test
    void getUser_existing_returns200() throws Exception {
        UserApiClient.Result res = client.get(1);

        assertEquals(200, res.status);
        JsonNode body = MAPPER.readTree(res.body);
        assertEquals(1, body.get("id").asLong());
        assertEquals("Alice", body.get("name").asText());
    }

    // ------------------------------------------------------------------
    // READ (missing)
    // ------------------------------------------------------------------
    @Test
    void getUser_missing_returns404() throws Exception {
        UserApiClient.Result res = client.get(999);

        assertEquals(404, res.status);
        JsonNode body = MAPPER.readTree(res.body);
        assertEquals("User not found", body.get("error").asText());
    }

    // ------------------------------------------------------------------
    // UPDATE
    // ------------------------------------------------------------------
    @Test
    void updateUser_returns200WithUpdatedFields() throws Exception {
        UserApiClient.Result res = client.update(1, "{\"name\":\"Alice Updated\",\"email\":\"alice.new@example.com\"}");

        assertEquals(200, res.status);
        JsonNode body = MAPPER.readTree(res.body);
        assertEquals(1, body.get("id").asLong());
        assertEquals("Alice Updated", body.get("name").asText());
        assertEquals("alice.new@example.com", body.get("email").asText());
    }

    // ------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------
    @Test
    void deleteUser_returns204() throws Exception {
        UserApiClient.Result res = client.delete(1);

        assertEquals(204, res.status);
    }

    // ------------------------------------------------------------------
    // LIST
    // ------------------------------------------------------------------
    @Test
    void listUsers_returnsArray() throws Exception {
        UserApiClient.Result res = client.list();

        assertEquals(200, res.status);
        JsonNode body = MAPPER.readTree(res.body);
        assertTrue(body.isArray());
        assertEquals(2, body.size());
        assertEquals("Alice", body.get(0).get("name").asText());
        assertEquals("Bob", body.get(1).get("name").asText());
    }

    // ------------------------------------------------------------------
    // STATEFUL SCENARIO  (SV-replacement demo)
    //   GET /users/100 -> 404 (before create)
    //   POST /users/100 -> 201 (moves scenario to "created")
    //   GET /users/100 -> 200 (after create)
    // ------------------------------------------------------------------
    @Test
    void statefulScenario_getBeforeCreate_404_thenAfter_200() throws Exception {
        // 1) Before creation: not found
        UserApiClient.Result before = client.get(100);
        assertEquals(404, before.status);

        // 2) Create via the scenario-specific path (advances state to "created")
        int createStatus = postToScenarioCreate();
        assertEquals(201, createStatus);

        // 3) After creation: now found
        UserApiClient.Result after = client.get(100);
        assertEquals(200, after.status);
        JsonNode body = MAPPER.readTree(after.body);
        assertEquals(100, body.get("id").asLong());
        assertEquals("Carol", body.get("name").asText());
    }

    /** Helper to POST to the scenario-specific create path (/users/100). Returns status. */
    private int postToScenarioCreate() throws Exception {
        java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(server.baseUrl() + "/users/100"))
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(
                        "{\"name\":\"Carol\",\"email\":\"carol@example.com\"}"))
                .build();
        java.net.http.HttpResponse<String> res =
                java.net.http.HttpClient.newHttpClient()
                        .send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
        return res.statusCode();
    }

    // ------------------------------------------------------------------
    // REQUEST VERIFICATION  (assert the mock received expected calls)
    // ------------------------------------------------------------------
    @Test
    void verifyMockReceivedExpectedRequests() throws Exception {
        server.resetRequests();

        client.get(1);
        client.create("{\"name\":\"Dave\",\"email\":\"dave@example.com\"}");

        // Assert the mock recorded exactly these requests
        server.verify(getRequestedFor(urlEqualTo("/users/1")));
        server.verify(postRequestedFor(urlEqualTo("/users"))
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(containing("Dave")));
    }
}
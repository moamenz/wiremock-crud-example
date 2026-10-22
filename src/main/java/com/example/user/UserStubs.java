package com.example.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.user.User;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.stubbing.Scenario;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public final class UserStubs {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private UserStubs() {
    }


    public static void registerAll(WireMockServer server) {
        server.stubFor(getUserExisting());
        server.stubFor(getUserMissing());
        server.stubFor(updateUser());
        server.stubFor(deleteUser());
        server.stubFor(listUsers());
        server.stubFor(createUser());
        registerStatefulScenario(server);
    }

    public static MappingBuilder createUser() {
        return post(urlEqualTo("/users"))
                .atPriority(5)
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Location", "/users/1")
                        .withBody(jsonBody(new User(1L, "Alice", "alice@example.com"))));
    }

    public static MappingBuilder getUserExisting() {
        return get(urlEqualTo("/users/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonBody(new User(1L, "Alice", "alice@example.com"))));
    }

    public static MappingBuilder getUserMissing() {
        return get(urlPathEqualTo("/users/999"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"User not found\"}"));
    }

    public static MappingBuilder updateUser() {
        return put(urlEqualTo("/users/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonBody(new User(1L, "Alice Updated", "alice.new@example.com"))));
    }

    public static MappingBuilder deleteUser() {
        return delete(urlEqualTo("/users/1"))
                .willReturn(aResponse().withStatus(204));
    }

    public static MappingBuilder listUsers() {
        return get(urlEqualTo("/users"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonBody(new User[]{
                                new User(1L, "Alice", "alice@example.com"),
                                new User(2L, "Bob", "bob@example.com")
                        })));
    }


    public static void registerStatefulScenario(WireMockServer server) {
        String scenario = "user-lifecycle";

        server.stubFor(get(urlEqualTo("/users/100"))
                .inScenario(scenario)
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"User not found\"}")));

        server.stubFor(post(urlEqualTo("/users/100"))
                .inScenario(scenario)
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Location", "/users/100")
                        .withBody("{\"id\":100,\"name\":\"Carol\",\"email\":\"carol@example.com\"}"))
                .willSetStateTo("created"));

        server.stubFor(get(urlEqualTo("/users/100"))
                .inScenario(scenario)
                .whenScenarioStateIs("created")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":100,\"name\":\"Carol\",\"email\":\"carol@example.com\"}")));
    }

    private static String jsonBody(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize to JSON", e);
        }
    }
}

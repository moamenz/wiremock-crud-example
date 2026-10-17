package com.example;

import com.example.stubs.UserStubs;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

/**
 * Standalone WireMock server for the "users" CRUD service.
 *
 * Run with:
 *   mvn compile exec:java
 *   (or: mvn compile exec:java -Dexec.mainClass="com.example.MockServerMain")
 *
 * Once running:
 *   - Mock endpoints:  http://localhost:8080/users ...
 *   - Admin / UI:      http://localhost:8080/__admin
 *
 * Stop with Ctrl+C.
 */
public class MockServerMain {

    public static void main(String[] args) {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                // fall back to default port
            }
        }

        WireMockConfiguration config = WireMockConfiguration.wireMockConfig()
                .port(port);

        WireMockServer server = new WireMockServer(config);
        server.start();

        // Register all CRUD stubs + the stateful scenario
        UserStubs.registerAll(server);

        System.out.println("==========================================================");
        System.out.println(" WireMock 'users' CRUD mock service is RUNNING");
        System.out.println("----------------------------------------------------------");
        System.out.println(" Base URL:   http://localhost:" + port);
        System.out.println(" Admin UI:   http://localhost:" + port + "/__admin");
        System.out.println("");
        System.out.println(" Try:");
        System.out.println("   curl http://localhost:" + port + "/users");
        System.out.println("   curl http://localhost:" + port + "/users/1");
        System.out.println("   curl -X POST http://localhost:" + port + "/users \\"
                + " -H 'Content-Type: application/json' \\"
                + " -d '{\"name\":\"Alice\",\"email\":\"alice@example.com\"}'");
        System.out.println("");
        System.out.println(" Press Ctrl+C to stop.");
        System.out.println("==========================================================");

        // Keep the JVM alive until interrupted
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
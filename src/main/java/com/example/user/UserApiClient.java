package com.example.user;

import com.example.user.User;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * A minimal HTTP client that consumes the "users" service.
 *
 * In a real migration this would be the code under test (or a test harness)
 * that previously pointed at the Tricentis SV virtual service and now points
 * at the WireMock server instead. Only the base URL changes.
 */
public class UserApiClient {

    private final HttpClient http;
    private final String baseUrl;

    public UserApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /** Result wrapper so tests can inspect status + body + headers. */
    public static class Result {
        public final int status;
        public final String body;
        public final String location;

        Result(int status, String body, String location) {
            this.status = status;
            this.body = body;
            this.location = location;
        }
    }

    public Result create(String jsonBody) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/users"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        return new Result(res.statusCode(), res.body(),
                res.headers().firstValue("Location").orElse(null));
    }

    public Result get(long id) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/users/" + id))
                .GET()
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        return new Result(res.statusCode(), res.body(), null);
    }

    public Result list() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/users"))
                .GET()
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        return new Result(res.statusCode(), res.body(), null);
    }

    public Result update(long id, String jsonBody) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/users/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        return new Result(res.statusCode(), res.body(), null);
    }

    public Result delete(long id) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/users/" + id))
                .DELETE()
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        return new Result(res.statusCode(), res.body(), null);
    }
}
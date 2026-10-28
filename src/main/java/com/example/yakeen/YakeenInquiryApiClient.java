package com.example.yakeen;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class YakeenInquiryApiClient {
    private final URI baseUri;
    private final HttpClient http;

    public YakeenInquiryApiClient(URI baseUri) {
        this.baseUri = baseUri;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }
    public YakeenInquiryApiClient(String baseUrl) { this(URI.create(baseUrl)); }

    public HttpResponse<String> postInquiry(String jsonBody) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(baseUri.resolve("/YakeenInformationInquiry11"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(10))
                .build();
        return http.send(req, HttpResponse.BodyHandlers.ofString());
    }
}

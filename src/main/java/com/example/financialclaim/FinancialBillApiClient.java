package com.example.financialclaim;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * HTTP client for the Etimad "Payment Management - financial claim/bills" API.
 */
public class FinancialBillApiClient {

    private final HttpClient http;
    private final String baseUrl;

    public FinancialBillApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public static class Result {
        public final int status;
        public final String body;
        Result(int status, String body) {
            this.status = status;
            this.body = body;
        }
    }

    public Result submit(String jsonBody) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/internal/v1/paymentmanage/financial/claim/bills"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        return new Result(res.statusCode(), res.body());
    }
}
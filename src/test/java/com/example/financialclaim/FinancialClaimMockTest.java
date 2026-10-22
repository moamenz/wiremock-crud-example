package com.example.financialclaim;

import com.example.financialclaim.FinancialBillApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests that consume the embedded WireMock
 * financial claim/bills mock service.
 */
class FinancialClaimMockTest {

    private static WireMockServer server;
    private static FinancialBillApiClient client;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @BeforeAll
    static void startServer() {
        server = new WireMockServer(WireMockConfiguration.wireMockConfig().port(0));
        server.start();
        FinancialClaimStubs.registerAll(server);
        client = new FinancialBillApiClient(server.baseUrl());
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @BeforeEach
    void resetScenarios() {
        server.resetScenarios();
    }

    /** POST a valid claim and verify the mock returns 200. */
    @Test
    void submit_validAmount_returns200() throws Exception {
        String json = "{\"body\":{\"financialClaimRequestDto\":{\"amount\":1000}}}";
        FinancialBillApiClient.Result res = client.submit(json);

        assertEquals(200, res.status);
        JsonNode body = MAPPER.readTree(res.body);
        assertNotNull(body);
    }

    /** POST a negative amount and verify the mock returns 400 with an error body. */
    @Test
    void submit_invalidAmount_returns400WithErrors() throws Exception {
        String json = "{\"body\":{\"financialClaimRequestDto\":{\"amount\":-1}}}";
        FinancialBillApiClient.Result res = client.submit(json);

        assertEquals(400, res.status);
        JsonNode body = MAPPER.readTree(res.body);
        assertTrue(body.has("errors"));
        assertEquals("Bill:10028", body.get("errors").get(0).get("code").asText());
    }

    /** Scenario: first POST returns 200 (no ref), second POST returns 200 with a referenceNumber. */
    @Test
    void submit_scenarioSecondCall_returnsReferenceNumber() throws Exception {
        String json1 = "{\"body\":{\"financialClaimRequestDto\":{\"amount\":1}}}";
        String json2 = "{\"body\":{\"financialClaimRequestDto\":{\"amount\":2}}}";

        FinancialBillApiClient.Result res1 = client.submit(json1);
        assertEquals(200, res1.status);

        FinancialBillApiClient.Result res2 = client.submit(json2);
        assertEquals(200, res2.status);
        JsonNode body2 = MAPPER.readTree(res2.body);
        assertEquals("260947033882", body2.get("referenceNumber").asText());
    }
}

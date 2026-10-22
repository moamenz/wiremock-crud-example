package com.example.financialclaim;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.stubbing.Scenario;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public final class FinancialClaimStubs {

    private static final String ENDPOINT =
            "/internal/v1/paymentmanage/financial/claim/bills";
    private static final String SCENARIO = "financial-claim";

    private static final String BODY_200_PLAIN_HEAD =
            "{\"success\":true,\"errorCode\":null,";
    private static final String BODY_200_PLAIN_MID =
            "\"errorMessage\":null,";
    private static final String BODY_200_PLAIN_END =
            "\"response\":\"Created claim No 260947020717\"}";
    private static final String BODY_200_PLAIN =
            BODY_200_PLAIN_HEAD + BODY_200_PLAIN_MID + BODY_200_PLAIN_END;

    private static final String BODY_200_REF_HEAD =
            "{\"referenceNumber\":\"260947033882\",\"success\":true,";
    private static final String BODY_200_REF_END =
            "\"errorCode\":null,\"errorMessage\":null,";
    private static final String BODY_200_REF_TAIL =
            "\"response\":\"Created claim No 260947033882\"}";
    private static final String BODY_200_REF =
            BODY_200_REF_HEAD + BODY_200_REF_END + BODY_200_REF_TAIL;

    private static final String BODY_400_HEAD =
            "{\"errors\":[{\"code\":\"Bill:10028\",";
    private static final String BODY_400_MID =
            "\"detail\":\"AmountNotCorrect: Amount is not within the valid range.\",";
    private static final String BODY_400_TAIL =
            "\"type\":\"BusinessError\",\"link\":null}]}";
    private static final String BODY_400 =
            BODY_400_HEAD + BODY_400_MID + BODY_400_TAIL;

    private FinancialClaimStubs() { }

    public static void registerAll(WireMockServer server) {
        server.stubFor(errorInvalidAmount());
        server.stubFor(successFirst());
        server.stubFor(successWithRef());
    }

    public static MappingBuilder errorInvalidAmount() {
        return post(urlEqualTo(ENDPOINT))
                .atPriority(1)
                .withRequestBody(matchingJsonPath("$.body.financialClaimRequestDto.amount", equalTo("-1")))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(BODY_400));
    }

    public static MappingBuilder successFirst() {
        return post(urlEqualTo(ENDPOINT))
                .inScenario(SCENARIO)
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(BODY_200_PLAIN))
                .willSetStateTo("with_ref");
    }

    public static MappingBuilder successWithRef() {
        return post(urlEqualTo(ENDPOINT))
                .inScenario(SCENARIO)
                .whenScenarioStateIs("with_ref")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(BODY_200_REF));
    }
}

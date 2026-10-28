package com.example.yakeen;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public final class YakeenInquiryStubs {

    private static final String ENDPOINT = "/YakeenInformationInquiry11";
    private static final String BODY_REQ_HEAD =
            "{\"Request\":{\"MsgRqHdr\":{\"RqUID\":\"704469cb-1e06-42cc-86e1-a4a9f14c939b\",";
    private static final String BODY_REQ_MID =
            "\"ProcessUID\":null,\"SessionId\":null,\"SCId\":\"ETIMAD\",\"ServiceId\":\"YakeenInquiry\",";
    private static final String BODY_REQ_MID2 =
            "\"FuncId\":\"12000000\",\"RqModeSpecified\":false,\"ClientDt\":\"2026-08-10T09:37:43\",";
    private static final String BODY_REQ_TAIL =
            "\"EchoData\":null,\"Version\":\"1.0\"},\"Body\":{\"BirthDtHjr\":\"04-1411\",";
    private static final String BODY_REQ_END =
            "\"PartyId\":{\"PartyIdNum\":\"1000038065\",\"PartyIdType\":\"NationalId\",\"PartyIdTypeSpecified\":true}}}}";
    public static final String EXACT_EXPECTED_REQUEST_BODY =
            BODY_REQ_HEAD + BODY_REQ_MID + BODY_REQ_MID2 + BODY_REQ_TAIL + BODY_REQ_END;
    private static final String BODY_SUC_HEAD =
            "{\"YakeenPersonInformationInquiryRs\":{\"MsgRsHdr\":{\"RqUID\":\"704469cb-1e06-42cc-86e1-a4a9f14c939b\",";
    private static final String BODY_SUC_HEAD2 =
            "\"ResponseStatus\":{\"StatusCode\":\"I000000\",\"StatusDesc\":\"Success\"}},\"Body\":{\"PartyId\":{\"PartyIdNum\":\"1000038065\",";
    private static final String BODY_SUC_MID =
            "\"PartyIdType\":\"NationalId\",\"PartyIdTypeSpecified\":true},\"Item\":{\"FirstName\":\"\u0639\u0645\u0631\",\"FirstNameEn\":\"Omer\",";
    private static final String BODY_SUC_MID2 =
            "\"FatherName\":\"\u0639\u0628\u062f\u0627\u0644\u0644\u0647\",\"FatherNameEn\":\"Abed\",\"GrandFatherName\":\"\u062e\u0627\u0644\u062f\",\"GrandFatherNameEn\":\"khaled\",";
    private static final String BODY_SUC_MID3 =
            "\"FamilyName\":\"\u0627\u0644\u0647\u0644\u0627\u0644\",\"FamilyNameEn\":\"ALBAHLAL\",\"OccupationCode\":\"O\",\"GenderCode\":\"2\",";
    private static final String BODY_SUC_MID4 =
            "\"GregorianBirthDt\":\"1990-10-28T00:00:00\",\"GregorianBirthDtSpecified\":true,\"BirthDt\":\"1411-04-09\",";
    private static final String BODY_SUC_MID5 =
            "\"IdExpiryDt\":\"2024-12-31T00:00:00\",\"IdExpiryDtSpecified\":true,\"IdExpiryDtHjr\":\"1446-06-30\",";
    private static final String BODY_SUC_MID6 =
            "\"LifeStatus\":\"\u062d\u064a\u0639\u0651\u0643\u064b\u0627 \u0645\u064f\u0632\u0643\u064a\u06b1\",\"MaritalStatusCode\":\"4\",\"MaritalStatus\":\"\u0623\u0631\u0645\u0644\u0647\",\"TotalNumberOfDependents\":\"1\",";
    private static final String BODY_SUC_END =
            "\"PassportList\":[{\"PassportNumber\":\"CX59263\",\"PassportExpiryDt\":\"2030-05-02T00:00:00\"}]}}}}";
    public static final String SUCCESS_RESPONSE_BODY =
            BODY_SUC_HEAD + BODY_SUC_HEAD2 + BODY_SUC_MID + BODY_SUC_MID2 +
            BODY_SUC_MID3 + BODY_SUC_MID4 + BODY_SUC_MID5 + BODY_SUC_MID6 + BODY_SUC_END;
    public static final String ERROR_BODY_400 =
            "{\"ErrorResponse\":{\"Code\":\"BODY_MISMATCH\",\"Message\":\"Request body does not match the expected YakeenInquiry sample.\"}}";

    private YakeenInquiryStubs() { }

    public static String exactExpectedBody() { return EXACT_EXPECTED_REQUEST_BODY; }

    public static void registerAll(WireMockServer server) {
        server.stubFor(exactMatchSuccess());
        server.stubFor(fallback400());
    }

    public static MappingBuilder exactMatchSuccess() {
        return post(urlEqualTo(ENDPOINT))
                .atPriority(1)
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(equalTo(EXACT_EXPECTED_REQUEST_BODY))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(SUCCESS_RESPONSE_BODY));
    }

    public static MappingBuilder fallback400() {
        return post(urlEqualTo(ENDPOINT))
                .atPriority(10)
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(ERROR_BODY_400));
    }
}

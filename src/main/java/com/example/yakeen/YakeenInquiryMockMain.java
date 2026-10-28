package com.example.yakeen;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

public class YakeenInquiryMockMain {
    public static void main(String[] args) {
        int port = 8081;
        if (args.length > 0) {
            try { port = Integer.parseInt(args[0]); } catch (NumberFormatException ignored) { }
        }
        WireMockServer server = new WireMockServer(WireMockConfiguration.wireMockConfig().port(port));
        server.start();
        YakeenInquiryStubs.registerAll(server);
        System.out.println("YakeenInquiry mock service running on " + server.baseUrl());
        System.out.println("Endpoint: POST " + server.baseUrl() + "/YakeenInformationInquiry11");
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        try { Thread.currentThread().join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}

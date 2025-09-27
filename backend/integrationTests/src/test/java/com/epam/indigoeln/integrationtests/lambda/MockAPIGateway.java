package com.epam.indigoeln.integrationtests.lambda;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;

import static com.epam.indigoeln.integrationtests.lambda.LambdaUtil.sendResponse;

@Slf4j
@RequiredArgsConstructor
public class MockAPIGateway implements HttpHandler {

    private final int port;
    private final LambdaInvoker elnInvoker;
    private final LambdaInvoker reportsInvoker;

    private HttpServer httpServer;

    public void start() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        httpServer.createContext("/", this);
        httpServer.start();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (exchange.getRequestURI().getPath().startsWith("/api/eln/")) {
                elnInvoker.process(exchange);
            } else if (exchange.getRequestURI().getPath().startsWith("/internalapi/reports/")) {
                reportsInvoker.process(exchange);
            } else {
                sendResponse(exchange, Response.Status.NOT_FOUND, "[MockAPIGateway] Not found: " + exchange.getRequestURI().getPath());
            }
        } catch (Exception e) {
            log.error("Request processing failed", e);
        }
    }

    public void stop() {
        httpServer.stop(1);
    }
}

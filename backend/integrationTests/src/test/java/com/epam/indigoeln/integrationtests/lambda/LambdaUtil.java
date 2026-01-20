package com.epam.indigoeln.integrationtests.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.sun.net.httpserver.HttpExchange;
import jakarta.ws.rs.core.Response;
import one.util.streamex.EntryStream;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class LambdaUtil {

    static APIGatewayV2HTTPEvent convertRequest(HttpExchange exchange, String apiSecret) throws IOException {
        APIGatewayV2HTTPEvent.APIGatewayV2HTTPEventBuilder event = APIGatewayV2HTTPEvent.builder()
                        .withRequestContext(APIGatewayV2HTTPEvent.RequestContext.builder()
                                .withHttp(APIGatewayV2HTTPEvent.RequestContext.Http.builder()
                                                .withMethod(exchange.getRequestMethod())
                                                .withSourceIp(exchange.getRemoteAddress().getHostName())
                                                .build()
                                ).build()
                        )
                        .withRawPath(exchange.getRequestURI().getRawPath())
                        .withRawQueryString(exchange.getRequestURI().getRawQuery());
        Map<String, String> headers = EntryStream.of(exchange.getRequestHeaders().entrySet().stream())
                .mapValues(List::getLast)
                .toCustomMap(LinkedHashMap::new);
        headers.put("X-API-Secret", apiSecret);
        event = event.withHeaders(headers);
        if (exchange.getRequestBody() != null) {
            byte[] bytes = exchange.getRequestBody().readAllBytes();
            String body;
            boolean base64Encoded = false;
            try {
                body = new String(bytes, StandardCharsets.US_ASCII);
            } catch (Exception e) {
                body = Base64.getEncoder().encodeToString(bytes);
                base64Encoded = true;
            }
            event = event
                    .withBody(body)
                    .withIsBase64Encoded(base64Encoded);
        }
        return event.build();
    }

    static void sendResponse(HttpExchange exchange, Response.Status status, @Nullable String body) throws IOException {
        sendResponse(exchange, status, body != null ? body.getBytes(StandardCharsets.UTF_8) : null);
    }

    static void sendResponse(HttpExchange exchange, Response.Status status, byte @Nullable [] body) throws IOException {
        exchange.sendResponseHeaders(status.getStatusCode(), body != null ? body.length : -1);
        if (body != null) {
            exchange.getResponseBody().write(body);
        }
        exchange.close();
    }
}

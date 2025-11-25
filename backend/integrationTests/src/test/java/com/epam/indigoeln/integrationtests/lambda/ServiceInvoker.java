package com.epam.indigoeln.integrationtests.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.sun.net.httpserver.HttpExchange;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;

import java.util.concurrent.CompletableFuture;

import static com.epam.indigoeln.integrationtests.lambda.LambdaUtil.sendResponse;

@Slf4j
@RequiredArgsConstructor
public class ServiceInvoker {

    private final HttpClient httpClient;
    private final String downstream;
    private final String apiSecret;

    public void process(HttpExchange exchange) throws Exception {
        try {
            HttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest(exchange.getRequestMethod(), exchange.getRequestURI().toString());
            exchange.getRequestHeaders().forEach((k, vv) -> {
                vv.forEach(v -> request.addHeader(k, v));
            });
            if (exchange.getRequestBody() != null) {
                byte[] bytes = exchange.getRequestBody().readAllBytes();
                request.removeHeaders(HttpHeaders.CONTENT_LENGTH);
                request.setEntity(new ByteArrayEntity(bytes));
            }
            request.addHeader("X-API-Secret", apiSecret);
            HttpResponse response = httpClient.execute(HttpHost.create(downstream), request);
            byte[] bytes = null;
            if (response.getEntity() != null) {
                bytes = response.getEntity().getContent().readAllBytes();
            }
            sendResponse(exchange, Response.Status.fromStatusCode(response.getStatusLine().getStatusCode()), bytes);
        } catch (Exception e) {
            log.error("Failed processing request", e);
            sendResponse(exchange, Response.Status.INTERNAL_SERVER_ERROR, "[MockAPIGateway] Internal Error");
        }
    }

    private record Job (
            long requestID,
            APIGatewayV2HTTPEvent event,
            CompletableFuture<APIGatewayV2HTTPResponse> done
    ) {}
}

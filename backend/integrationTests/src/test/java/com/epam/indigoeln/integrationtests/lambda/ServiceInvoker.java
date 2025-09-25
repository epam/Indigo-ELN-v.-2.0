package com.epam.indigoeln.integrationtests.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.epam.indigoeln.test.FeignUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.epam.indigoeln.integrationtests.lambda.LambdaUtil.convertRequest;
import static com.epam.indigoeln.integrationtests.lambda.LambdaUtil.sendResponse;

@Slf4j
@RequiredArgsConstructor
public class ServiceInvoker {

    private final HttpClient httpClient;
    private final String downstream;
    private final String apiSecret;

    private final BlockingQueue<Job> queue = new LinkedBlockingQueue<>();
    private final Map<Long, Job> jobs = new ConcurrentHashMap<>();
    private final AtomicLong lastUsedRequestID = new AtomicLong();

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

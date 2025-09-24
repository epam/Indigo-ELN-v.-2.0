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

import java.io.IOException;
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
public class LambdaMultiplexer implements HttpHandler {

    private static final Pattern RESPONSE_URL = Pattern.compile("/2018-06-01/runtime/invocation/(.+?)/(response|error)");
    private static final Job SHUTDOWN = new Job(-1, new APIGatewayV2HTTPEvent(), new CompletableFuture<>());

    private final int port;

    private HttpServer httpServer;

    private final BlockingQueue<Job> queue = new LinkedBlockingQueue<>();
    private final Map<Long, Job> jobs = new ConcurrentHashMap<>();
    private final AtomicLong lastUsedRequestID = new AtomicLong();

    public void start() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        httpServer.createContext("/2018-06-01/runtime", this);
        httpServer.start();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        log.info("!!! handle: started");
        try {
            if (exchange.getRequestMethod().equals(HttpMethod.GET) && exchange.getRequestURI().getPath().equals("/2018-06-01/runtime/invocation/next")) {
                // wait for the next event
                log.info("!!! handle: before queue.take");
                Job job = queue.take();
                log.info("!!! handle: after queue.take");
                if (job == SHUTDOWN) {
                    queue.put(job);
                    return; // shutting down
                }
                exchange.getResponseHeaders().add("Lambda-Runtime-Aws-Request-Id", Long.toString(job.requestID));
                exchange.getResponseHeaders().add("Lambda-Runtime-Deadline-Ms", Long.toString(System.currentTimeMillis() + 29_000));
                String body = FeignUtil.OBJECT_MAPPER.writeValueAsString(job.event);
                log.debug("Sent to lambda:\n{}", body);
                sendResponse(exchange, Response.Status.OK, body);
                return;
            }
            if (exchange.getRequestMethod().equals(HttpMethod.POST) && exchange.getRequestURI().getPath().equals("/2018-06-01/runtime/init/error")) {
                String body = new String(exchange.getRequestBody().readAllBytes());
                log.error("Lambda function reported error:\n{}", body);
                sendResponse(exchange, Response.Status.ACCEPTED, (byte[]) null);
                return;
            }
            if (exchange.getRequestMethod().equals(HttpMethod.POST)) {
                Matcher matcher = RESPONSE_URL.matcher(exchange.getRequestURI().toString());
                if (matcher.matches()) {
                    byte[] bytes = exchange.getRequestBody().readAllBytes();
                    if (log.isDebugEnabled()) {
                        log.debug("Received from lambda:\n{}", new String(bytes));
                    }
                    Long requestID = Long.parseLong(matcher.group(1));
                    boolean hasError = matcher.group(2).equals("error");
                    APIGatewayV2HTTPResponse body = FeignUtil.OBJECT_MAPPER.readValue(bytes, APIGatewayV2HTTPResponse.class);
                    Job job = jobs.remove(requestID);
                    sendResponse(exchange, Response.Status.ACCEPTED, (byte[]) null);
                    job.done.complete(body);
                    return;
                }
            }
            log.warn("Lambda function called invalid endpoint: {}", exchange.getRequestURI());
            sendResponse(exchange, Response.Status.NOT_FOUND, "[MockAPIGateway] Not Found");
        } catch (InterruptedException e) {
            // just return
        } catch (Exception e) {
            log.error("Request processing failed", e);
        }
    }

    public void process(HttpExchange exchange) throws Exception {
        long requestID = lastUsedRequestID.incrementAndGet();
        Job job = new Job(requestID, convertRequest(exchange), new CompletableFuture<>());
        jobs.put(requestID, job);
        log.info("!!! process: before queue.add");
        queue.add(job);
        log.info("!!! process: after queue.add");
        try {
            log.info("!!! process: before job.done.get");
            APIGatewayV2HTTPResponse response = job.done.get();
            log.info("!!! process: after job.done.get");
            if (response.getMultiValueHeaders() != null) {
                response.getMultiValueHeaders().forEach(exchange.getResponseHeaders()::put);
            } else if (response.getHeaders() != null) {
                response.getHeaders().forEach(exchange.getResponseHeaders()::add);
            }
            byte[] bytes = null;
            String body = response.getBody();
            if (body != null) {
                bytes = response.getIsBase64Encoded() ? Base64.getDecoder().decode(body) : body.getBytes(StandardCharsets.UTF_8);
            }
            sendResponse(exchange, Response.Status.fromStatusCode(response.getStatusCode()), bytes);
        } catch (ExecutionException e) {
            log.error("Failed processing request", e);
            sendResponse(exchange, Response.Status.INTERNAL_SERVER_ERROR, "[MockAPIGateway] Internal Error");
        }
    }

    public void stop() {
        queue.add(SHUTDOWN);
        httpServer.stop(1);
    }

    private record Job (
            long requestID,
            APIGatewayV2HTTPEvent event,
            CompletableFuture<APIGatewayV2HTTPResponse> done
    ) {}
}

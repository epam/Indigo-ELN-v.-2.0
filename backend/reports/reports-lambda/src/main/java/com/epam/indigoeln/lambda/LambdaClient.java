package com.epam.indigoeln.lambda;

import com.amazonaws.services.lambda.runtime.*;
import com.epam.indigoeln.common.util.Pair;
import com.google.common.base.Preconditions;
import io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler;
import io.quarkus.runtime.Quarkus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
public class LambdaClient {

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static void main(String[] args) throws Exception {
//        Thread.sleep(30_000);
        String lambdaHost = System.getenv("AWS_LAMBDA_RUNTIME_API");
        Preconditions.checkState(lambdaHost != null, "AWS_LAMBDA_RUNTIME_API environment variable is not set");
        RequestStreamHandler handler = new QuarkusStreamHandler();
        String lambdaBaseURL = "http://" + lambdaHost + "/2018-06-01/runtime";

        try {
            for (; ; ) {
                Pair<String, byte[]> request = nextEvent(lambdaBaseURL);
                try (ByteArrayInputStream input = new ByteArrayInputStream(request.b()); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                    Context context = new ContextImpl(request.a(), "", "", "", "", "", null, null, 29_000, 512, new LambdaLoggerImpl());
                    handler.handleRequest(input, output, null);
                    byte[] responseBytes = output.toByteArray();
                    HttpResponse<byte[]> response = httpClient.send(HttpRequest.newBuilder(URI.create(lambdaBaseURL + "/invocation/" + request.a() + "/response"))
                                    .POST(HttpRequest.BodyPublishers.ofByteArray(responseBytes))
                                    .build(),
                            HttpResponse.BodyHandlers.ofByteArray());
                    if (response.statusCode() >= 300) {
                        throw new RuntimeException("Failed to send response to Lambda: " + response.statusCode() + " " + new String(response.body()));
                    }
                } catch (Exception e) {
                    byte[] errorBytes = e.getMessage().getBytes();
                    HttpResponse<byte[]> response = httpClient.send(HttpRequest.newBuilder(URI.create(lambdaBaseURL + "/invocation/" + request.a() + "/error"))
                                    .POST(HttpRequest.BodyPublishers.ofByteArray(errorBytes))
                                    .build(),
                            HttpResponse.BodyHandlers.ofByteArray());
                    if (response.statusCode() >= 300) {
                        throw new RuntimeException("Failed to send error to Lambda: " + response.statusCode() + " " + new String(response.body()));
                    }
                }
            }
        } catch (Throwable e) {
            log.error("Error in Lambda client", e);
            Quarkus.asyncExit(1);
        }
    }

    private static Pair<String, byte[]> nextEvent(String baseURL) throws Exception {
        HttpResponse<byte[]> response = httpClient.send(HttpRequest.newBuilder(URI.create(baseURL + "/invocation/next"))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofByteArray());
        System.out.println("!!! nextEvent: headers = " + response.headers().map());
        String requestID = response.headers().firstValue("Lambda-Runtime-Aws-Request-Id").orElseThrow();
        return Pair.of(requestID, response.body());
    }

    @Getter
    @RequiredArgsConstructor
    private static class ContextImpl implements Context {

        private final String awsRequestId;
        private final String logGroupName;
        private final String logStreamName;
        private final String functionName;
        private final String functionVersion;
        private final String invokedFunctionArn;
        private final CognitoIdentity identity;
        private final ClientContext clientContext;
        private final int remainingTimeInMillis;
        private final int memoryLimitInMB;
        private final LambdaLogger logger;
    }

    private static class LambdaLoggerImpl implements LambdaLogger {

        @Override
        public void log(String message) {
            log.info(message);
        }

        @Override
        public void log(byte[] message) {
            System.out.println(new String(message));
        }
    }
}

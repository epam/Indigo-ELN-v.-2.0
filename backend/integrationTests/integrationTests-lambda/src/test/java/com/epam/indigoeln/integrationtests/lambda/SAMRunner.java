package com.epam.indigoeln.integrationtests.lambda;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
public class SAMRunner {

    private final File path;
    private final int port;
    private final String loggerName;
    private final CompletableFuture<Void> started = new CompletableFuture<>();
    private Process process;

    @SneakyThrows
    void start() {
        String command = ("%s local start-api "
                + "--template %s "
                + "--host 0.0.0.0 --port %s "
                + "--warm-containers EAGER "
                + "--skip-pull-image "
                + "--debug "
        ).formatted(System.getenv().get("SAM"), path, port);
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
        processBuilder.redirectErrorStream(true);
        processBuilder.environment().put("AWS_ACCESS_KEY_ID", "testing");
        processBuilder.environment().put("AWS_SECRET_ACCESS_KEY", "testing");
        processBuilder.environment().put("AWS_SECURITY_TOKEN", "testing");
        processBuilder.environment().put("AWS_SESSION_TOKEN", "testing");
        processBuilder.environment().put("AWS_REGION", "us-east-1");
        process = processBuilder.start();
        CompletableFuture<Process> ended = process.onExit();
        new Thread(this::processOutput).start();
        try {
            CompletableFuture.anyOf(started, ended).get(10, TimeUnit.MINUTES);
            if (ended.isDone()) {
                throw new RuntimeException("SAM failed to start");
            }
        } catch (TimeoutException e) {
            throw new RuntimeException("SAM start timed out");
        }
    }

    void stop() {
        process.destroy();
    }

    @SneakyThrows
    private void processOutput() {
        Logger logger = LoggerFactory.getLogger(loggerName);
        boolean alreadyStarted = false;
        try (BufferedReader br = process.inputReader()) {
            String line;
            while ((line = br.readLine()) != null) {
                logger.info(line);
                if (!alreadyStarted && line.startsWith(" * Running on http://")) {
                    started.complete(null);
                }
            }
        }
        System.out.println("!!");
    }
}

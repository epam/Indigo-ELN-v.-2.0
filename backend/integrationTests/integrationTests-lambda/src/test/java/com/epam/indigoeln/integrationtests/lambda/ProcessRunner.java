package com.epam.indigoeln.integrationtests.lambda;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.shadow.de.siegmar.fastcsv.util.Nullable;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkState;

@Slf4j
@RequiredArgsConstructor
public class ProcessRunner {

    private final ProcessBuilder processBuilder;
    private final Logger logger;
    private final Duration timeout;
    @Nullable
    private final Predicate<String> startedPredicate;

    private final AtomicReference<Process> process = new AtomicReference<>();
    private final CompletableFuture<Void> started = new CompletableFuture<>();

    void start() {
        processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectError(ProcessBuilder.Redirect.PIPE);
        try {
            Process process = processBuilder.start();
            this.process.set(process);
            CompletableFuture<Process> ended = process.onExit();
            log.info("Process started");
            new Thread(() -> logOutput(process, false)).start();
            new Thread(() -> logOutput(process, true)).start();
            new Thread(() -> {
                ended.join();
                log.info("Process ended with exit code {}", process.exitValue());
            }).start();
            try {
                CompletableFuture.anyOf(started, ended).get(timeout.getSeconds(), TimeUnit.SECONDS);
                if (started.isDone()) {
                    log.info("Process started");
                    return;
                }
                if (startedPredicate != null && !started.isDone()) {
                    throw new RuntimeException("Process failed to start");
                }
                checkState(ended.isDone());
                if (process.exitValue() != 0) {
                    throw new RuntimeException("Process failed");
                }
            } catch (TimeoutException e) {
                throw new RuntimeException("Process timed out");
            }
        } catch (Exception e) {
            throw new RuntimeException("Process failed to start: " + e.getMessage(), e);
        }
    }

    private void logOutput(Process process, boolean error) {
        try (BufferedReader rd = error ? process.errorReader() : process.inputReader()) {
            String line;
            while ((line = rd.readLine()) != null) {
                if (error) {
                    logger.error(line);
                } else {
                    logger.info(line);
                }
                if (startedPredicate != null && startedPredicate.test(line)) {
                    started.complete(null);
                }
            }
        } catch (Exception ignore) {
            // process probably quit
        }
    }

    void close() {
        if (process.get() != null) {
            process.get().destroy();
        }
    }
}

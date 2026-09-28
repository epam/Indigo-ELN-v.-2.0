package com.epam.indigoeln.integrationtests.lambda;

import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
public class SAMRunner {

    private final File path;
    private final int port;

    private final AtomicReference<ProcessRunner> apiRunner = new AtomicReference<>();

    void build() {
        String command = ("%s build "
                + "--template %s"
        ).formatted(System.getenv().get("SAM"), path);
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
        new ProcessRunner(processBuilder, LoggerFactory.getLogger("SAM-BUILD"), Duration.ofMinutes(10), null).start();
    }

    void start() {
        String command = ("%s local start-api "
                + "--template %s "
                + "--host 0.0.0.0 --port %s "
                + "--warm-containers EAGER "
                + "--skip-pull-image "
                + "--debug "
        ).formatted(System.getenv().get("SAM"), path, port);
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
        processBuilder.environment().put("AWS_ACCESS_KEY_ID", "testing");
        processBuilder.environment().put("AWS_SECRET_ACCESS_KEY", "testing");
        processBuilder.environment().put("AWS_SECURITY_TOKEN", "testing");
        processBuilder.environment().put("AWS_SESSION_TOKEN", "testing");
        processBuilder.environment().put("AWS_REGION", "us-east-1");
        ProcessRunner runner = new ProcessRunner(processBuilder, LoggerFactory.getLogger("SAM"), Duration.ofMinutes(10), line -> line.startsWith(" * Running on http://"));
        apiRunner.set(runner);
        runner.start();
    }

    void stop() {
        if (apiRunner.get() != null) {
            apiRunner.get().close();
        }
    }
}

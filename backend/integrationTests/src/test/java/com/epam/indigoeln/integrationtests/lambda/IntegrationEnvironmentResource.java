package com.epam.indigoeln.integrationtests.lambda;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.DockerHealthcheckWaitStrategy;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

import java.io.File;
import java.util.Map;

@Slf4j
public class IntegrationEnvironmentResource implements QuarkusTestResourceLifecycleManager {

    private final LambdaInvoker elnInvoker = new LambdaInvoker(20001, "apiSecret");
    private final LambdaInvoker reportsInvoker = new LambdaInvoker(20002, "internalApiSecret");
    private final MockAPIGateway mockAPIGateway = new MockAPIGateway(28080, elnInvoker, reportsInvoker);
    private final ComposeContainer compose = new ComposeContainer(new File("docker-compose-integration-tests.yaml"))
            .withLocalCompose(true)
            .withLogConsumer("postgres", new Slf4jLogConsumer(LoggerFactory.getLogger("POSTGRES")))
            .withLogConsumer("eln-lambda", new Slf4jLogConsumer(LoggerFactory.getLogger("ELN_LAMBDA")))
            .withLogConsumer("reports-lambda", new Slf4jLogConsumer(LoggerFactory.getLogger("REPORTS_LAMBDA")))
            .waitingFor("postgres", new DockerHealthcheckWaitStrategy())
            .waitingFor("eln-lambda", new LogMessageWaitStrategy().withRegEx(".+Installed features: \\[.+"))
            .waitingFor("reports-lambda", new LogMessageWaitStrategy().withRegEx(".+Installed features: \\[.+"));

    @Override
    public Map<String, String> start() {
        log.info("Starting integration environment");
        try {
            reportsInvoker.start();
            elnInvoker.start();
            mockAPIGateway.start();
            log.info("Mock API gateway started");
            compose.start();
            log.info("Docker compose started");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Map.of();
    }

    @Override
    public void stop() {
        log.info("Stopping integration environment");
        compose.stop();
        log.info("Docker compose stopped");
        mockAPIGateway.stop();
        elnInvoker.stop();
        reportsInvoker.stop();
        log.info("Mock API gateway stopped");
    }
}

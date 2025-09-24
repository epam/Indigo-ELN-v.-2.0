package com.epam.indigoeln.integrationtests.lambda;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.DockerHealthcheckWaitStrategy;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

import java.io.File;
import java.util.Map;

@Slf4j
public class IntegrationEnvironmentResource implements QuarkusTestResourceLifecycleManager {

    private final LambdaMultiplexer elnLambdaMultiplexer = new LambdaMultiplexer(20001);
//    private final LambdaMultiplexer reportsLambdaMultiplexer = new LambdaMultiplexer(20002);
    private final MockAPIGateway mockAPIGateway = new MockAPIGateway(28080, elnLambdaMultiplexer/*, reportsLambdaMultiplexer*/);
    private final ComposeContainer compose = new ComposeContainer(new File("docker-compose-integration-tests.yaml"))
            .withLocalCompose(true)
            .withLogConsumer("postgres", new Slf4jLogConsumer(LoggerFactory.getLogger("POSTGRES")))
            .withLogConsumer("eln-lambda", new Slf4jLogConsumer(LoggerFactory.getLogger("ELN_LAMBDA")))
//            .withLogConsumer("reports-lambda", new Slf4jLogConsumer(LoggerFactory.getLogger("REPORTS_LAMBDA")))
            .withLogConsumer("reports-service", new Slf4jLogConsumer(LoggerFactory.getLogger("REPORTS_SERVICE")))
            .waitingFor("postgres", new DockerHealthcheckWaitStrategy())
            .waitingFor("eln-lambda", new LogMessageWaitStrategy().withRegEx(".+Installed features: \\[.+"))
//            .waitingFor("reports-lambda", new LogMessageWaitStrategy().withRegEx(".+Installed features: \\[.+"));
            .waitingFor("reports-service", new LogMessageWaitStrategy().withRegEx(".+Installed features: \\[.+"));

    @Override
    public Map<String, String> start() {
        System.err.println("!!! IntegrationEnvironmentResource.start");
        try {
//            reportsLambdaMultiplexer.start();
            elnLambdaMultiplexer.start();
            mockAPIGateway.start();
            System.err.println("!!! mock API gateway started");
            System.err.println("!!! current dir = " + new File(".").getAbsolutePath());
//            Thread.sleep(Long.MAX_VALUE);
            compose.start();
            System.err.println("!!!");
        } catch (Exception e) {
            System.err.println("!!! IntegrationEnvironmentResource, start failed");
            e.printStackTrace();
            stop();
            throw new RuntimeException(e);
        }
        return Map.of();
    }

    @Override
    public void stop() {
        System.err.println("!!! IntegrationEnvironmentResource.stop");
        compose.stop();
        mockAPIGateway.stop();
        elnLambdaMultiplexer.stop();
//        reportsLambdaMultiplexer.stop();
    }
}

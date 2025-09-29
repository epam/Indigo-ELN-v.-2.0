package com.epam.indigoeln.integrationtests.lambda;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
public class IntegrationEnvironmentResource implements QuarkusTestResourceLifecycleManager {

    private final LambdaInvoker elnInvoker = new LambdaInvoker(20001, "apiSecret");
    private final LambdaInvoker reportsInvoker = new LambdaInvoker(20002, "internalApiSecret");
    private final MockAPIGateway mockAPIGateway = new MockAPIGateway(28080, elnInvoker, reportsInvoker);
    private final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("public.ecr.aws/m5k0g6n7/indigoeln/indigo-eln-postgres:latest")
            .withAccessToHost(true)
            .withUsername("eln")
            .withPassword("eln")
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("POSTGRES")))
            .withExposedPorts(5432)
            .withCreateContainerCmdModifier(cmd -> {
                cmd.getHostConfig().withPortBindings(
                        new PortBinding(Ports.Binding.bindPort(25432), new ExposedPort(5432))
                );
            });
    private final GenericContainer<?> elnContainer = new GenericContainer<>("indigoeln/eln-lambda:built")
            .withAccessToHost(true)
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("ELN_LAMBDA")))
            .waitingFor(new LogMessageWaitStrategy().withRegEx(".+Installed features: \\[.+"))
            .withEnv("AWS_LAMBDA_RUNTIME_API", "host.testcontainers.internal:20001")
            .withEnv("QUARKUS_PROFILE", "integration-test")
            .withEnv("QUARKUS_DATASOURCE_JDBC_URL", "jdbc:postgresql://host.testcontainers.internal:25432/eln")
            .withEnv("QUARKUS_DATASOURCE_USERNAME", "eln")
            .withEnv("QUARKUS_DATASOURCE_PASSWORD", "eln")
            .withEnv("ELN_COGNITO_USER_POOL_ID", "xxx")
            .withEnv("ELN_API_SECRET", "apiSecret")
            .withEnv("ELN_INTERNAL_API_SECRET", "internalApiSecret")
            .withEnv("QUARKUS_REST_CLIENT_REPORTS_API_URL", "http://host.testcontainers.internal:28080")
            .withEnv("QUARKUS_REST_CLIENT_LOGGING_SCOPE", "request-response")
            .withEnv("QUARKUS_REST_CLIENT_LOGGING_BODY_LIMIT", "9999")
            .withEnv("QUARKUS_REST_CLIENT_EXTENSIONS_API_SCOPE", "all");
    private final Path reportsBuildContext = Paths.get("..", "reports", "reports-lambda");
    private final GenericContainer<?> reportsContainer = new GenericContainer<>(new ImageFromDockerfile("indigoeln/eln-lambda:built", false)
                    .withDockerfile(reportsBuildContext.resolve("Dockerfile.jvm-integrationTests"))
                    .withFileFromPath(".", reportsBuildContext)
            )
            .withAccessToHost(true)
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("REPORTS_LAMBDA")))
            .withEnv("AWS_LAMBDA_RUNTIME_API", "host.testcontainers.internal:20002")
            .withEnv("QUARKUS_PROFILE", "integration-test")
            .withEnv("ELN_API_SECRET", "internalApiSecret");
//    private final ComposeContainer compose = new ComposeContainer(new File("docker-compose-integration-tests.yaml"))
//            .withLocalCompose(true)
//            .withLogConsumer("postgres", new Slf4jLogConsumer(LoggerFactory.getLogger("POSTGRES")))
//            .withLogConsumer("eln-lambda", new Slf4jLogConsumer(LoggerFactory.getLogger("ELN_LAMBDA")))
//            .withLogConsumer("reports-lambda", new Slf4jLogConsumer(LoggerFactory.getLogger("REPORTS_LAMBDA")))
//            .waitingFor("postgres", new DockerHealthcheckWaitStrategy())
//            .waitingFor("eln-lambda", new LogMessageWaitStrategy().withRegEx(".+Installed features: \\[.+"))
//            .waitingFor("reports-lambda", new LogMessageWaitStrategy().withRegEx(".+Installed features: \\[.+"));


    public IntegrationEnvironmentResource() {
        System.out.println("!!!");
    }

    @Override
    public Map<String, String> start() {
        log.info("Starting integration environment");
        try {
            reportsInvoker.start();
            elnInvoker.start();
            mockAPIGateway.start();
            log.info("Mock API gateway started");
            postgresContainer.start();
            log.info("Postgres container started");
            elnContainer.start();
            log.info("ELN container started");
            reportsContainer.start();
            log.info("Reports container started");
//            compose.start();
//            log.info("Docker compose started");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Map.of();
    }

    @Override
    public void stop() {
        log.info("Stopping integration environment");
        reportsContainer.stop();
        log.info("Reports container stopped");
        elnContainer.stop();
        log.info("ELN container stopped");
        postgresContainer.stop();
        log.info("Postgres container stopped");
        mockAPIGateway.stop();
        elnInvoker.stop();
        reportsInvoker.stop();
        log.info("Mock API gateway stopped");
    }
}

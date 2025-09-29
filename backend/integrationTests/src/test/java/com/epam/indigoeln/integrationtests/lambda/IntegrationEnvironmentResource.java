package com.epam.indigoeln.integrationtests.lambda;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.LoggerFactory;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.DockerHealthcheckWaitStrategy;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class IntegrationEnvironmentResource implements BeforeAllCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(IntegrationEnvironmentResource.class);

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        context.getRoot().getStore(NAMESPACE).getOrComputeIfAbsent(
                "integration-environment-resource",
                key -> {
                    try {
                        return new ResourceImpl();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to start integration environment: " + e.getMessage(), e);
                    }
                }
        );
    }
}

@Slf4j
class ResourceImpl implements ExtensionContext.Store.CloseableResource {

    private final LambdaInvoker elnInvoker = new LambdaInvoker(20001, "apiSecret");
    private final LambdaInvoker reportsInvoker = new LambdaInvoker(20002, "internalApiSecret");
    private final MockAPIGateway mockAPIGateway = new MockAPIGateway(28080, elnInvoker, reportsInvoker);
    private final PostgreSQLContainer<?> postgresContainer;
    private final GenericContainer<?> elnContainer;
    private final GenericContainer<?> reportsContainer;
    private final ComposeContainer compose = new ComposeContainer(new File("docker-compose-integration-tests.yaml"))
            .withLocalCompose(true)
            .withLogConsumer("postgres", new Slf4jLogConsumer(LoggerFactory.getLogger("POSTGRES")))
            .withLogConsumer("eln-lambda", new Slf4jLogConsumer(LoggerFactory.getLogger("ELN_LAMBDA")))
            .withLogConsumer("reports-lambda", new Slf4jLogConsumer(LoggerFactory.getLogger("REPORTS_LAMBDA")))
            .waitingFor("postgres", new DockerHealthcheckWaitStrategy())
            .waitingFor("eln-lambda", new LogMessageWaitStrategy().withRegEx(".+Installed features: \\[.+"))
            .waitingFor("reports-lambda", new LogMessageWaitStrategy().withRegEx(".+Installed features: \\[.+"));

    ResourceImpl() throws Exception {
        log.info("Starting integration environment");

        reportsInvoker.start();
        elnInvoker.start();
        mockAPIGateway.start();
        Testcontainers.exposeHostPorts(20001, 20002, 28080);
        log.info("Mock API gateway started");

        postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("public.ecr.aws/m5k0g6n7/indigoeln/indigo-eln-postgres:latest").asCompatibleSubstituteFor("postgres"))
                .withAccessToHost(true)
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("POSTGRES")))
                .withStartupTimeout(Duration.ofSeconds(30))
                .withUsername("eln")
                .withPassword("eln")
                .withDatabaseName("eln")
                .withExposedPorts(5432)
                .withCreateContainerCmdModifier(cmd -> {
                    cmd.getHostConfig().withPortBindings(
                            new PortBinding(Ports.Binding.bindPort(25432), new ExposedPort(5432))
                    );
                });
        postgresContainer.start();
        log.info("Postgres container started");
        Testcontainers.exposeHostPorts(25432);

        elnContainer = new GenericContainer<>("indigoeln/eln-lambda:built")
                .withAccessToHost(true)
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("ELN_LAMBDA")))
                .waitingFor(new LogMessageWaitStrategy().withRegEx(".+Installed features: \\[.+"))
                .withStartupTimeout(Duration.ofSeconds(30))
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
        elnContainer.start();
        log.info("ELN container started");

        log.info("Building reports image");
        Process buildProcess = new ProcessBuilder("docker", "build", "-t", "indigoeln/reports-lambda:built", "-f", "src/main/docker/Dockerfile.jvm-integrationTests", ".")
                .directory(new File("../reports/reports-lambda").getAbsoluteFile())
                .inheritIO()
                .start();
        if (!buildProcess.waitFor(30, TimeUnit.SECONDS)) {
            throw new RuntimeException("Reports image wasn't built");
        }
        if (buildProcess.exitValue() != 0) {
            throw new RuntimeException("Reports image build failed");
        }
        log.info("Reports image built");
        reportsContainer = new GenericContainer<>("indigoeln/reports-lambda:built")
                .withAccessToHost(true)
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("REPORTS_LAMBDA")))
                .waitingFor(new LogMessageWaitStrategy().withRegEx(".+Installed features: \\[.+"))
                .withStartupTimeout(Duration.ofSeconds(30))
                .withEnv("AWS_LAMBDA_RUNTIME_API", "host.testcontainers.internal:20002")
                .withEnv("QUARKUS_PROFILE", "integration-test")
                .withEnv("ELN_API_SECRET", "internalApiSecret");
        reportsContainer.start();
        log.info("Reports container started");

//            compose.start();
//            log.info("Docker compose started");
    }

//    @Override
//    public void stop() {

    @Override
    public void close() throws Throwable {
        log.info("Stopping integration environment");
        reportsContainer.stop();
        log.info("Reports container stopped");
        elnContainer.stop();
        log.info("ELN container stopped");
        postgresContainer.stop();
        log.info("Postgres container stopped");
//        compose.stop();
        mockAPIGateway.stop();
        elnInvoker.stop();
        reportsInvoker.stop();
        log.info("Mock API gateway stopped");
    }
}

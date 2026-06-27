package com.epam.indigoeln.integrationtests.lambda;

import com.epam.indigoeln.test.BaseTest;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.LoggerFactory;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import java.io.File;
import java.net.URI;
import java.time.Duration;

public class LambdaIntegrationEnvironmentResource implements BeforeAllCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(LambdaIntegrationEnvironmentResource.class);

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        BaseTest.setIntegrationTest(true);
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
class ResourceImpl implements AutoCloseable {

    private final SAMRunner samRunner;
    private final PostgreSQLContainer<?> postgresContainer;
    private final GenericContainer<?> motoContainer;

    ResourceImpl() throws Exception {
        log.info("Starting integration environment");

        log.info("Starting PostgreSQL...");
        postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("public.ecr.aws/m5k0g6n7/indigoeln/indigo-eln-postgres:latest").asCompatibleSubstituteFor("postgres"))
                .withAccessToHost(true)
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("POSTGRES")))
                .withStartupTimeout(Duration.ofSeconds(30))
                .withUsername("eln")
                .withPassword("eln")
                .withDatabaseName("eln")
                .withExposedPorts(5432)
                .withCopyToContainer(
                        Transferable.of("CREATE DATABASE signature OWNER eln"),
                        "/docker-entrypoint-initdb.d/99_create_db.sql")
                .withCreateContainerCmdModifier(cmd -> {
                    cmd.getHostConfig().withPortBindings(
                            new PortBinding(Ports.Binding.bindPort(25432), new ExposedPort(5432))
                    );
                });
        postgresContainer.start();
        log.info("Postgres container started");
        Testcontainers.exposeHostPorts(25432);

        log.info("Starting Moto (S3)...");
        motoContainer = new GenericContainer<>(DockerImageName.parse("motoserver/moto"))
                .withExposedPorts(5000)
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("MOTO")))
                .withCreateContainerCmdModifier(cmd -> cmd.getHostConfig().withPortBindings(
                        new PortBinding(Ports.Binding.bindPort(4566), new ExposedPort(5000))
                ));
        motoContainer.start();
        try (S3Client s3 = S3Client.builder()
                .endpointOverride(URI.create("http://localhost:4566"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test", "test")))
                .region(Region.US_EAST_1)
                .forcePathStyle(true)
                .build()) {
            s3.createBucket(CreateBucketRequest.builder().bucket("indigoeln-data").build());
        }
        log.info("Moto started, bucket 'indigoeln-data' created");

        log.info("Building SAM-compatible ELN lambda...");
        Process elnBuilder = new ProcessBuilder("docker", "build"
                , "-f", "../../eln/eln-lambda/src/main/docker/Dockerfile.native.integrationtests"
                , "-t", "indigoeln/eln-lambda:built.integrationtests"
                , "../../eln/eln-lambda/build")
                .inheritIO()
                .start();
        int elnBuilderResult = elnBuilder.waitFor();
        if (elnBuilderResult != 0) {
            throw new RuntimeException("Failed to build SAM-compatible ELN lambda, exit code " + elnBuilderResult);
        }

        log.info("Starting SAM...");
        samRunner = new SAMRunner(new File("sam.integrationtests.yaml"), 28080, "SAM");
        samRunner.start();
        log.info("Integration environment started");
    }

    @Override
    public void close() {
        log.info("Stopping integration environment");
        samRunner.stop();
        log.info("SAM stopped");
        postgresContainer.stop();
        log.info("Postgres container stopped");
        motoContainer.stop();
        log.info("Moto container stopped");
    }
}

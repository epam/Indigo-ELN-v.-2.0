package com.epam.indigoeln.integrationtests.lambda;

import com.epam.indigoeln.test.SharedTestEnvironment;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.LoggerFactory;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import java.io.File;
import java.net.URI;
import java.time.Duration;

@Slf4j
public class LambdaIntegrationEnvironmentResource implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        SharedTestEnvironment.install(context, Impl::new);
    }

    class Impl extends SharedTestEnvironment {

        private final SAMRunner samRunner;
        private final PostgreSQLContainer postgresContainer;
        private final GenericContainer<?> motoContainer;

        Impl() {
            postgresContainer = new PostgreSQLContainer(DockerImageName.parse("public.ecr.aws/m5k0g6n7/indigoeln/postgres-combined:latest").asCompatibleSubstituteFor("postgres"))
                    .withAccessToHost(true)
                    .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("POSTGRES")))
                    .withStartupTimeout(Duration.ofSeconds(30))
                    .withUsername("postgres")
                    .withPassword("postgres")
                    .withDatabaseName("postgres")
                    .withExposedPorts(5432)
                    .withCreateContainerCmdModifier(cmd -> {
                        cmd.getHostConfig().withPortBindings(
                                new PortBinding(Ports.Binding.bindPort(25432), new ExposedPort(5432))
                        );
                    });
            motoContainer = new GenericContainer<>(DockerImageName.parse("motoserver/moto"))
                    .withExposedPorts(5000)
                    .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("MOTO")))
                    .withCreateContainerCmdModifier(cmd -> cmd.getHostConfig().withPortBindings(
                            new PortBinding(Ports.Binding.bindPort(4566), new ExposedPort(5000))
                    ));
            samRunner = new SAMRunner(new File("sam.integrationtests.yaml"), 28080);
        }

        protected void start() {
            log.info("Starting PostgreSQL...");
            postgresContainer.start();
            log.info("Postgres container started");
            Testcontainers.exposeHostPorts(25432);

            log.info("Starting Moto (S3)...");
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

            log.info("Building SAM-compatible images...");
            samRunner.build();

            log.info("Starting SAM...");
            samRunner.start();
        }

        @Override
        protected void stop() {
            samRunner.stop();
            log.info("SAM stopped");
            postgresContainer.stop();
            log.info("Postgres container stopped");
            motoContainer.stop();
            log.info("Moto container stopped");
        }
    }
}

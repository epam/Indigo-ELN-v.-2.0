package com.epam.indigoeln.integrationtests.aws;

import com.epam.indigoeln.test.BaseTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.TestAbortedException;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import java.io.File;
import java.net.URI;
import java.time.Duration;

public class AwsIntegrationEnvironmentResource implements BeforeAllCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(AwsIntegrationEnvironmentResource.class);
    private static final String RESOURCE = "resource";

    @Override
    public void beforeAll(ExtensionContext context) {
        BaseTest.setIntegrationTest(true);
        ResourceImpl resource = (ResourceImpl) context.getRoot().getStore(NAMESPACE).computeIfAbsent(RESOURCE, _ -> new ResourceImpl());
        if (resource.failed) {
            throw new TestAbortedException("Integration environment not started");
        }
        if (!resource.started) {
            try {
                resource.start();
            } catch (Exception e) {
                throw new RuntimeException("Failed to start integration environment", e);
            }
        }
    }
}

@Slf4j
class ResourceImpl implements AutoCloseable {

    private final ComposeContainer composeContainer;
    boolean started;
    boolean failed;
    boolean stopped;

    ResourceImpl() {
        log.info("Starting integration environment via Docker Compose");

        composeContainer = new ComposeContainer(new File("docker-compose.integrationtests.yaml"))
                .withBuild(true)
                .withRemoveVolumes(true)
                .waitingFor("eln-aws", Wait.forHealthcheck().withStartupTimeout(Duration.ofMinutes(5)))
                .waitingFor("reports-aws", Wait.forHealthcheck().withStartupTimeout(Duration.ofMinutes(5)))
                .waitingFor("signature-aws", Wait.forHealthcheck().withStartupTimeout(Duration.ofMinutes(5)))
                .withTailChildContainers(true);
    }

    void start() {
        try {
            composeContainer.start();
            createBucket();
            started = true;
            log.info("Integration environment started");
        } catch (Exception e) {
            failed = true;
            close();
            throw e;
        }
    }

    private void createBucket() {
        try (S3Client s3 = S3Client.builder()
                .endpointOverride(URI.create("http://localhost:34566"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test", "test")))
                .region(Region.US_EAST_1)
                .forcePathStyle(true)
                .build()) {
            s3.createBucket(CreateBucketRequest.builder().bucket("indigoeln-data").build());
        }
        log.info("Bucket 'indigoeln-data' created");
    }

    @Override
    public void close() {
        if (!stopped) {
            stopped = true;
            log.info("Stopping integration environment");
            composeContainer.stop();
            log.info("Integration environment stopped");
        }
    }
}

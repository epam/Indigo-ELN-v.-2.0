package com.epam.indigoeln.integrationtests.aws;

import com.epam.indigoeln.test.ComposeRunner;
import com.epam.indigoeln.test.SharedTestEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import java.io.File;
import java.net.URI;

@Slf4j
public class AwsIntegrationEnvironmentResource implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        SharedTestEnvironment.install(context, Impl::new);
    }

    class Impl extends SharedTestEnvironment {

        final ComposeRunner composeRunner;

        Impl() {
            composeRunner = new ComposeRunner(new File("docker-compose.integrationtests-aws.yaml"), "postgres", "eln-aws", "reports-aws", "signature-aws", "sampleregistration-aws");
        }

        protected void start() {
            composeRunner.start();
            createBucket();
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
        public void stop() {
            composeRunner.stop();
        }
    }
}

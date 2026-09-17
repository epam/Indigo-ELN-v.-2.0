package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.integrationtests.aws.AwsIntegrationEnvironmentResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.extension.ExtendWith;

@QuarkusIntegrationTest
@ExtendWith(AwsIntegrationEnvironmentResource.class)
public class ExperimentModelServiceIT extends ExperimentModelServiceTest {
}

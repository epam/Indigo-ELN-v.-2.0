package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.integrationtests.lambda.IntegrationEnvironmentResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.extension.ExtendWith;

@QuarkusIntegrationTest
@ExtendWith(IntegrationEnvironmentResource.class)
public class ExperimentModelServiceIT extends ExperimentModelServiceTest {
}

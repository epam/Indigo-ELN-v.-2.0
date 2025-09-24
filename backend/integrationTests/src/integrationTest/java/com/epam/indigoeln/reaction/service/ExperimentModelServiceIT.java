package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.integrationtests.lambda.IntegrationEnvironmentResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;

@QuarkusIntegrationTest
@QuarkusTestResource(IntegrationEnvironmentResource.class)
public class ExperimentModelServiceIT extends ExperimentModelServiceTest {
}

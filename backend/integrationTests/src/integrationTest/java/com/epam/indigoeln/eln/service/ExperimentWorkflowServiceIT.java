package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.integrationtests.lambda.IntegrationEnvironmentResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;

@QuarkusIntegrationTest
@QuarkusTestResource(value = IntegrationEnvironmentResource.class, restrictToAnnotatedClass = true)
class ExperimentWorkflowServiceIT extends ExperimentWorkflowServiceTest {
}

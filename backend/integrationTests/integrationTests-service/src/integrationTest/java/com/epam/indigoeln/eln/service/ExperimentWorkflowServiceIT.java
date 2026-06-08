package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.integrationtests.service.ServiceIntegrationEnvironmentResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.extension.ExtendWith;

@QuarkusIntegrationTest
@ExtendWith(ServiceIntegrationEnvironmentResource.class)
class ExperimentWorkflowServiceIT extends ExperimentWorkflowServiceTest {
}

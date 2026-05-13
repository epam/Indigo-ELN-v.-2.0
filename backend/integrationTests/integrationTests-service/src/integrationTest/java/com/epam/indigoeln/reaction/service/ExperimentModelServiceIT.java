package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.integrationtests.service.ServiceIntegrationEnvironmentResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.extension.ExtendWith;

@QuarkusIntegrationTest
@ExtendWith(ServiceIntegrationEnvironmentResource.class)
public class ExperimentModelServiceIT extends ExperimentModelServiceTest {
}

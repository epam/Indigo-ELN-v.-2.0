package com.epam.indigoeln.reports.service;

import com.epam.indigoeln.integrationtests.lambda.ServiceIntegrationEnvironmentResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.extension.ExtendWith;

@QuarkusIntegrationTest
@ExtendWith(ServiceIntegrationEnvironmentResource.class)
public class ReportsServiceIT extends ReportsServiceTest {
}

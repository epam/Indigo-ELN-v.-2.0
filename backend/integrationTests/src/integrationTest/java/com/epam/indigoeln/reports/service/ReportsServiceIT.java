package com.epam.indigoeln.reports.service;

import com.epam.indigoeln.integrationtests.lambda.IntegrationEnvironmentResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;

@QuarkusIntegrationTest
@QuarkusTestResource(IntegrationEnvironmentResource.class)
public class ReportsServiceIT extends ReportsServiceTest {
}

package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.integrationtests.lambda.IntegrationEnvironmentResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;

@QuarkusIntegrationTest
@QuarkusTestResource(IntegrationEnvironmentResource.class)
class SignatureTemplateServiceIT extends SignatureTemplateServiceTest {
}

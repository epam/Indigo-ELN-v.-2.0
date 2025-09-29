package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.integrationtests.lambda.IntegrationEnvironmentResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.extension.ExtendWith;

@QuarkusIntegrationTest
@ExtendWith(IntegrationEnvironmentResource.class)
public class DictionaryServiceIT extends DictionaryServiceTest {
}

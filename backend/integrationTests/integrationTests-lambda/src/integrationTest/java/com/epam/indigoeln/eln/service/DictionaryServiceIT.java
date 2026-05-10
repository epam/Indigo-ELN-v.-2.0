package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.integrationtests.lambda.LambdaIntegrationEnvironmentResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.extension.ExtendWith;

@QuarkusIntegrationTest
@ExtendWith(LambdaIntegrationEnvironmentResource.class)
public class DictionaryServiceIT extends DictionaryServiceTest {
}

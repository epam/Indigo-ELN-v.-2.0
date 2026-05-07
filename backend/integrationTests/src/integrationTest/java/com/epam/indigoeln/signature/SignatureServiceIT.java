package com.epam.indigoeln.signature;

import com.epam.indigoeln.integrationtests.lambda.IntegrationEnvironmentResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.extension.ExtendWith;

@QuarkusIntegrationTest
@ExtendWith(IntegrationEnvironmentResource.class)
public class SignatureServiceIT extends SignatureServiceTest {
}

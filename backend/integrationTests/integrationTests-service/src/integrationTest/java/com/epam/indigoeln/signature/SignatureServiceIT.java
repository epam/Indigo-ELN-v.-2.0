package com.epam.indigoeln.signature;

import com.epam.indigoeln.integrationtests.lambda.ServiceIntegrationEnvironmentResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.extension.ExtendWith;

@QuarkusIntegrationTest
@ExtendWith(ServiceIntegrationEnvironmentResource.class)
public class SignatureServiceIT extends SignatureServiceTest {
}

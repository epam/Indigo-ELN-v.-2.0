package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.integrationtests.aws.AwsIntegrationEnvironmentResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.extension.ExtendWith;

@QuarkusIntegrationTest
@ExtendWith(AwsIntegrationEnvironmentResource.class)
public class ProjectServiceIT extends ProjectServiceTest {
}

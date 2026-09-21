package com.epam.indigoeln.sampleregistration.service;

import com.epam.indigoeln.sampleregistration.api.SampleRegistrationAdminClient;
import com.epam.indigoeln.sampleregistration.api.SampleRegistrationClient;
import com.epam.indigoeln.test.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;


@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SampleRegistrationServiceTest extends BaseTest {

    SampleRegistrationClient sampleRegistrationClient;
    SampleRegistrationAdminClient sampleRegistrationAdminClient;

    @BeforeAll
    void setup() {
        sampleRegistrationClient = buildClient(SampleRegistrationClient.class);
        sampleRegistrationAdminClient = buildClient(SampleRegistrationAdminClient.class);
        sampleRegistrationAdminClient.migrate();
    }

    @Test
    @Order(100)
    void testRegisterSample() {
//        sampleRegistrationClient.registerSample(SampleRegistrationRequest.builder()
    }

    @Test
    @Order(200)
    void testSearchSamples() {

    }
}

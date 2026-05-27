package com.epam.indigoeln;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.integrationtests.lambda.LambdaIntegrationEnvironmentResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusIntegrationTest
@ExtendWith(LambdaIntegrationEnvironmentResource.class)
public class IPv6SupportIT extends ELNBaseTest {

    @Test
    void testIPv6Support() {
        assertThat(miscClient.getInfo().getIpv6Support()).isTrue();
    }
}

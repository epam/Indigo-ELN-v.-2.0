package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.ELNBaseTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
@QuarkusTest
@TestSecurity(user = ELNBaseTest.ADMIN_USERNAME)
class SupportServiceTest extends ELNBaseTest {

    @Test
    void testInsertTestData() {
        Map<String, String> result = miscClient.insertTestData();
        assertThat(Integer.parseInt(result.get("projects"))).isBetween(4, 5);
        assertThat(Integer.parseInt(result.get("notebooks"))).isPositive();
        assertThat(Integer.parseInt(result.get("experiments"))).isPositive();
        assertThat(Integer.parseInt(result.get("attachments"))).isGreaterThanOrEqualTo(0);
    }
}

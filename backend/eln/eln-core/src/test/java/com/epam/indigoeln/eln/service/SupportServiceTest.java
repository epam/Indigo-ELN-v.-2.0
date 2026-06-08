package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.ELNBaseTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;


@QuarkusTest
@TestSecurity(user = ELNBaseTest.ADMIN_USERNAME)
class SupportServiceTest extends ELNBaseTest {

    @Test
    void testInsertTestData() {
        miscClient.insertTestData();
    }
}

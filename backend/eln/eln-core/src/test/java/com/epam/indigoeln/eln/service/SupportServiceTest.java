package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.TemplateComponent;
import com.epam.indigoeln.eln.model.TemplateDetailsDTO;
import com.epam.indigoeln.eln.model.TemplateRequest;
import com.epam.indigoeln.eln.model.TemplateTab;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.Test;

import java.util.List;


@QuarkusTest
@JwtSecurity
@TestSecurity(user = ELNBaseTest.ADMIN_USERNAME)
class SupportServiceTest extends ELNBaseTest {

    @Test
    void testInsertTestData() {
        TemplateDetailsDTO template = templateClient.createTemplate(new TemplateRequest(
                "Test Template",
                List.of(new TemplateTab("Test Tab", List.of(new TemplateComponent.ExperimentDetails())))
        ));
        miscClient.insertTestData(template.getId());
    }
}

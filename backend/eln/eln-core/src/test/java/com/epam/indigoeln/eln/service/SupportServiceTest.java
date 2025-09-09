package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.BaseTest;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.util.ResponseWithHeaders;
import com.epam.indigoeln.eln.util.TestHelper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.ws.rs.core.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.epam.indigoeln.eln.util.CustomAssertions.assertThatACL;
import static com.epam.indigoeln.eln.util.CustomAssertions.assertThatClientCall;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;


@QuarkusTest
@JwtSecurity
@TestSecurity(user = TestHelper.ADMIN_USERNAME)
class SupportServiceTest extends BaseTest {

    @Test
    void testFlyway() {
        assertThatClientCall(() -> miscClient.migrate())
                .isSuccessful();
    }

    @Test
    void testInsertTestData() {
        TemplateDetailsDTO template = templateClient.createTemplate(new TemplateRequest(
                "Test Template",
                List.of(new TemplateTab("Test Tab", List.of(new TemplateComponent.ExperimentDetails())))
        ));
        miscClient.insertTestData(template.getId());
    }
}

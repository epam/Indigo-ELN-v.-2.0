package com.epam.indigoeln.sampleregistration.service;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.units.MolarityUnit;
import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.sampleregistration.api.SampleRegistrationAdminClient;
import com.epam.indigoeln.sampleregistration.api.SampleRegistrationClient;
import com.epam.indigoeln.sampleregistration.model.SRSFindSamplesRequest;
import com.epam.indigoeln.sampleregistration.model.SRSSampleDTO;
import com.epam.indigoeln.sampleregistration.model.STRCodeCompound;
import com.epam.indigoeln.sampleregistration.model.STRCodeSample;
import com.epam.indigoeln.sampleregistration.model.SampleRegistrationRequest;
import com.epam.indigoeln.sampleregistration.model.SampleRegistrationResponse;
import com.epam.indigoeln.test.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SampleRegistrationServiceTest extends BaseTest {

    SampleRegistrationClient sampleRegistrationClient;
    SampleRegistrationAdminClient sampleRegistrationAdminClient;

    String molfile = ModelUtil.loadResourceAsString("/compound.mdl");
    UUID stereoisomerCode = UUID.randomUUID();
    UUID saltCode = UUID.randomUUID();
    UUID healthHazard1 = UUID.randomUUID();
    UUID healthHazard2 = UUID.randomUUID();
    UUID compoundState = UUID.randomUUID();

    @BeforeAll
    void setup() {
        sampleRegistrationClient = buildClient(SampleRegistrationClient.class);
        sampleRegistrationAdminClient = buildClient(SampleRegistrationAdminClient.class);
        sampleRegistrationAdminClient.migrate();
        cleanupDatabase();
    }

    @Test
    @Order(100)
    void testRegisterSample() {
        SampleRegistrationResponse response = sampleRegistrationClient.registerSample(SampleRegistrationRequest.builder()
                .molfile(molfile)
                .stereoisomerCode(stereoisomerCode)
                .saltCode(saltCode)
                .saltCodeNumeric(5)
                .saltEQ100(200)
                .nbkBatchNumber("nbk1")
                .molWeight(100.0) // !!! make BigDecimal
                .exactMass(150.0)
                .chemicalName("chemicalName")
                .density(BigDecimal.valueOf(200))
                .molarity(BigDecimal.valueOf(250))
                .molarityUnit(MolarityUnit.MM)
                .purity(BigDecimal.valueOf(50))
                .healthHazards(List.of(healthHazard1, healthHazard2))
                .compoundState(compoundState)
                .batchComment("batchComment1")
                .build()
        );
        STRCodeSample strCode = response.strCode();
        assertThat(strCode.getCompoundCode()).isEqualTo(1);
        assertThat(strCode.getSaltCode()).isEqualTo(5);
        assertThat(strCode.getSampleCode()).isEqualTo(1);
    }

    @Test
    @Order(101)
    void testFindSample() {
        Page<SRSSampleDTO> page = sampleRegistrationClient.find(SRSFindSamplesRequest.builder().quickSearch("STR-00000001-05-001").build(), 0, 10);
        assertThat(page.getItems()).singleElement().satisfies(s -> {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(s.getId()).isNotNull();
                softly.assertThat(s.getNbkBatchNumber()).isEqualTo("nbk1");
                softly.assertThat(s.getStrCodeCompound()).isEqualTo(STRCodeCompound.parse("STR-00000001-05"));
                softly.assertThat(s.getStrCodeSample()).isEqualTo(STRCodeSample.parse("STR-00000001-05-001"));
                softly.assertThat(s.getMolFormula()).isEqualTo("C<sub>4</sub>H<sub>6</sub>O<sub>3</sub>");
                softly.assertThat(s.getMolWeight()).isCloseTo(BigDecimal.valueOf(100.0), Percentage.withPercentage(0.01));
                softly.assertThat(s.getName()).isEqualTo("chemicalName");
                softly.assertThat(s.getSaltCode()).isEqualTo(saltCode);
                softly.assertThat(s.getCompoundID()).isNotNull();
                softly.assertThat(s.getInchi()).isNull();
            });
        });
    }

    @Test
    @Order(200)
    void testSearchSamples() {

    }

    @SuppressWarnings("SqlWithoutWhere")
    private void cleanupDatabase() {
        try (Connection connection = databasePool.get().getConnection()) {
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                // experiments, notebooks, projects
                statement.executeUpdate("delete from SRS_Sample");
                statement.executeUpdate("delete from SRS_Compound");
                statement.executeUpdate("alter sequence srs_compound_str_code_compound_seq restart");
            }
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clean up test database", e);
        }
    }
}

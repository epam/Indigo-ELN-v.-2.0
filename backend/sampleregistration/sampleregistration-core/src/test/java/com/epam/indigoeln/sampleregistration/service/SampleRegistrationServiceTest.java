package com.epam.indigoeln.sampleregistration.service;

import com.epam.indigoeln.common.model.NbkBatchNumber;
import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.search.NumericSearch;
import com.epam.indigoeln.common.model.search.StructuralSearch;
import com.epam.indigoeln.common.model.search.TextSearch;
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
import com.epam.indigoeln.sampleregistration.migrations.V3_0_1__layout_compound_molfiles;
import com.epam.indigoeln.test.BaseTest;
import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.data.Percentage;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.Context;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.epam.indigoeln.test.ClientUtil.uploadForm;
import static com.epam.indigoeln.test.MolfileTestUtil.atomCoordinates;
import static com.epam.indigoeln.test.MolfileTestUtil.withoutCoordinates;
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
                .saltEQ(new BigDecimal("2.0"))
                .nbkBatchNumber(NbkBatchNumber.parse("12345678-1234-005"))
                .molWeight(100.0)
                .exactMass(150.0)
                .chemicalName("chemicalName")
                .density(BigDecimal.valueOf(200))
                .molarity(BigDecimal.valueOf(250))
                .molarityUnit(MolarityUnit.MM)
                .purity(BigDecimal.valueOf(50))
                .healthHazards(Set.of(healthHazard1, healthHazard2))
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
        Page<SRSSampleDTO> page = sampleRegistrationClient.find(SRSFindSamplesRequest.builder().quickSearch("STR-00000001-05-001").build(), Paging.DEFAULT);
        assertThat(page.getItems()).singleElement().satisfies(s -> {
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(s.getId()).isNotNull();
                softly.assertThat(s.getNbkBatchNumber()).isEqualTo(NbkBatchNumber.parse("12345678-1234-005"));
                softly.assertThat(s.getStrCodeCompound()).isEqualTo(STRCodeCompound.parse("STR-00000001-05"));
                softly.assertThat(s.getStrCodeSample()).isEqualTo(STRCodeSample.parse("STR-00000001-05-001"));
                softly.assertThat(s.getMolFormula()).isEqualTo("C<sub>4</sub>H<sub>6</sub>O<sub>3</sub>");
                softly.assertThat(s.getMolWeight()).isCloseTo(BigDecimal.valueOf(100.0), Percentage.withPercentage(0.01));
                softly.assertThat(s.getName()).isEqualTo("chemicalName");
                softly.assertThat(s.getSaltCode()).isEqualTo(saltCode);
                softly.assertThat(s.getSaltEQ()).isEqualTo(new BigDecimal("2"));
            });
        });
    }

    @Test
    @Order(103)
    void testFindSampleTreatsLikeWildcardsLiterally() {
        assertThat(sampleRegistrationClient.find(SRSFindSamplesRequest.builder().chemicalName(new TextSearch.ContainsSearch("lName")).build(), Paging.DEFAULT).getItems()).hasSize(1);
        // neither is in "chemicalName"; as wildcards both would match it
        assertThat(sampleRegistrationClient.find(SRSFindSamplesRequest.builder().chemicalName(new TextSearch.ContainsSearch("_")).build(), Paging.DEFAULT).getItems()).isEmpty();
        assertThat(sampleRegistrationClient.find(SRSFindSamplesRequest.builder().chemicalName(new TextSearch.StartsWithSearch("%Name")).build(), Paging.DEFAULT).getItems()).isEmpty();
    }

    @Test
    @Order(104)
    void testFindSampleByMolWeight() {
        // the sample's molWeight is 100.0; "equals" matches to the precision of the search value
        assertThat(sampleRegistrationClient.find(SRSFindSamplesRequest.builder().molWeight(new NumericSearch.Equals(100.0)).build(), Paging.DEFAULT).getItems()).hasSize(1);
        assertThat(sampleRegistrationClient.find(SRSFindSamplesRequest.builder().molWeight(new NumericSearch.Equals(100.04)).build(), Paging.DEFAULT).getItems()).isEmpty();
        assertThat(sampleRegistrationClient.find(SRSFindSamplesRequest.builder().molWeight(new NumericSearch.Equals(100.4)).build(), Paging.DEFAULT).getItems()).isEmpty();
        assertThat(sampleRegistrationClient.find(SRSFindSamplesRequest.builder().molWeight(new NumericSearch.Equals(101.0)).build(), Paging.DEFAULT).getItems()).isEmpty();
    }

    @Test
    @Order(200)
    void testLoadCompoundsFromFile() throws IOException {
        sampleRegistrationClient.loadCompoundsFromFile(UUID.randomUUID(), UUID.randomUUID(), 0, uploadForm("compounds.sdf", ModelUtil.loadResource("/compounds.sdf")));
    }

    @ParameterizedTest
    @Order(201)
    @ValueSource(strings = {"benzene-1,2,3,5-tetrol", "benzene", "tetrol"})
    void testFindSampleByChemicalName(String quickSearch) {
        // compounds.sdf (loaded by testLoadCompoundsFromFile) contains a compound named "benzene-1,2,3,5-tetrol"
        Page<SRSSampleDTO> page = sampleRegistrationClient.find(SRSFindSamplesRequest.builder().quickSearch(quickSearch).build(), Paging.DEFAULT);
        assertThat(page.getItems()).extracting(SRSSampleDTO::getName).contains("benzene-1,2,3,5-tetrol");
    }

    @Test
    @Order(202)
    void testFindSampleByMolFormula() {
        Page<SRSSampleDTO> page = sampleRegistrationClient.find(SRSFindSamplesRequest.builder().quickSearch("C6H6O4").build(), Paging.DEFAULT);
        assertThat(page.getItems()).isNotEmpty().allSatisfy(s ->
                assertThat(s.getMolFormula()).isEqualTo("C<sub>6</sub>H<sub>6</sub>O<sub>4</sub>"));
    }

    @Test
    @Order(203)
    void testFindSamplesSortedByChemicalName() {
        Page<SRSSampleDTO> page = sampleRegistrationClient.find(SRSFindSamplesRequest.builder().chemicalName(new TextSearch.ContainsSearch("benzene")).build(), Paging.DEFAULT);
        // the database collation ignores punctuation and case
        assertThat(page.getItems()).extracting(SRSSampleDTO::getName).hasSizeGreaterThan(1)
                .isSortedAccordingTo(Comparator.comparing(name -> name.replaceAll("[^A-Za-z0-9]", "").toLowerCase()));
    }

    @Test
    @Order(204)
    void testFindSampleByStructureAndQuickSearch() {
        // sorted by similarity, then by text rank
        Page<SRSSampleDTO> page = sampleRegistrationClient.find(SRSFindSamplesRequest.builder()
                .structure(new StructuralSearch(StructuralSearch.Type.SUBSTRUCTURE, molfile))
                .quickSearch("chemicalName")
                .build(), Paging.DEFAULT);
        assertThat(page.getItems()).extracting(SRSSampleDTO::getName).containsExactly("chemicalName");
    }

    @Test
    @Order(300)
    void testReindexSearchVectors() throws SQLException {
        try (Connection connection = databasePool.get().getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("update SRS_Sample set search_vector = ''");
        }
        SRSFindSamplesRequest request = SRSFindSamplesRequest.builder().quickSearch("benzene-1,2,3,5-tetrol").build();
        assertThat(sampleRegistrationClient.find(request, Paging.DEFAULT).getItems()).isEmpty();

        Map<String, String> result = sampleRegistrationAdminClient.reindexSearchVectors();
        assertThat(Integer.parseInt(result.get("samples"))).isEqualTo(101); // 1 registered + 100 loaded from compounds.sdf
        assertThat(sampleRegistrationClient.find(request, Paging.DEFAULT).getItems()).isNotEmpty();
    }

    @Test
    @Order(400)
    void testRegisterSampleBeyond999() throws SQLException {
        try (Connection connection = databasePool.get().getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("update SRS_Sample set str_code = 'STR-00000001-05-999' where str_code = 'STR-00000001-05-001'");
        }
        SampleRegistrationRequest request = SampleRegistrationRequest.builder()
                .molfile(molfile)
                .stereoisomerCode(stereoisomerCode)
                .saltCode(saltCode)
                .saltCodeNumeric(5)
                .saltEQ(new BigDecimal("2"))
                .nbkBatchNumber(NbkBatchNumber.parse("12345678-1234-006"))
                .molWeight(100.0)
                .exactMass(150.0)
                .build();
        assertThat(sampleRegistrationClient.registerSample(request).strCode()).hasToString("STR-00000001-05-1000");
        // 999 sorts after 1000 as a string, so this one fails unless the last code is found numerically
        assertThat(sampleRegistrationClient.registerSample(request).strCode()).hasToString("STR-00000001-05-1001");
    }

    @Test
    @Order(500)
    void testRegisterSampleLaysOutStructureWithoutCoordinates() throws SQLException {
        STRCodeSample strCode = registerFromSmiles("OC(=O)c1ccc(I)cc1F", "12345678-1234-007");
        assertThat(atomCoordinates(compoundMolfile(strCode))).hasSize(11).doesNotHaveDuplicates();
    }

    @Test
    @Order(501)
    void testMigrationLaysOutStoredMolfileWithoutCoordinates() throws Exception {
        STRCodeSample strCode = registerFromSmiles("OC(=O)c1ccc(I)cc1Cl", "12345678-1234-008");
        try (Connection connection = databasePool.get().getConnection()) {
            // as stored before compounds were laid out on creation
            try (PreparedStatement st = connection.prepareStatement("update SRS_Compound set mol_file = ? where id = (select compound_id from SRS_Sample where str_code = ?)")) {
                st.setString(1, withoutCoordinates(compoundMolfile(strCode)));
                st.setString(2, strCode.toString());
                st.executeUpdate();
            }
            assertThat(atomCoordinates(compoundMolfile(strCode))).hasSize(11).containsOnly("0.0000 0.0000");

            new V3_0_1__layout_compound_molfiles().migrate(new Context() {
                @Override
                public Configuration getConfiguration() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Connection getConnection() {
                    return connection;
                }
            });
        }
        assertThat(atomCoordinates(compoundMolfile(strCode))).hasSize(11).doesNotHaveDuplicates();
    }

    private STRCodeSample registerFromSmiles(String smiles, String nbkBatchNumber) {
        return sampleRegistrationClient.registerSample(SampleRegistrationRequest.builder()
                .molfile(smiles)
                .stereoisomerCode(stereoisomerCode)
                .saltCode(saltCode)
                .saltCodeNumeric(5)
                .saltEQ(new BigDecimal("2"))
                .nbkBatchNumber(NbkBatchNumber.parse(nbkBatchNumber))
                .molWeight(100.0)
                .exactMass(150.0)
                .build()).strCode();
    }

    private String compoundMolfile(STRCodeSample strCode) throws SQLException {
        try (Connection connection = databasePool.get().getConnection();
             PreparedStatement st = connection.prepareStatement("select c.mol_file from SRS_Compound c join SRS_Sample s on s.compound_id = c.id where s.str_code = ?")) {
            st.setString(1, strCode.toString());
            try (ResultSet rs = st.executeQuery()) {
                assertThat(rs.next()).isTrue();
                return rs.getString(1);
            }
        }
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

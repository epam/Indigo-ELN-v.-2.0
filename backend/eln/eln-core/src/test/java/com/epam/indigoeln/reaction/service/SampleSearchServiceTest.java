package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.common.model.NbkBatchNumber;
import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.units.MolarityUnit;
import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.SearchCatalog;
import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.BuiltInDictionary;
import com.epam.indigoeln.eln.model.ComponentStateRef;
import com.epam.indigoeln.eln.model.HealthHazardRef;
import com.epam.indigoeln.eln.model.SaltCodeRef;
import com.epam.indigoeln.eln.model.SampleSource;
import com.epam.indigoeln.eln.model.StereoisomerCodeRef;
import com.epam.indigoeln.reaction.model.CompoundRef;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import com.epam.indigoeln.sampleregistration.model.SRSCompoundDTO;
import com.epam.indigoeln.sampleregistration.model.SRSSampleDTO;
import com.epam.indigoeln.sampleregistration.model.STRCodeCompound;
import com.epam.indigoeln.sampleregistration.model.STRCodeSample;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import static com.epam.indigoeln.eln.test.ReactionInputSampleAssert.assertThat;
import static com.epam.indigoeln.test.ClientUtil.uploadForm;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@QuarkusTest
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
public class SampleSearchServiceTest extends MutationsTestBase {

    SaltCodeRef saltCode;
    StereoisomerCodeRef stereoisomerCode;
    HealthHazardRef healthHazardRef;
    ComponentStateRef componentStateRef;

    private static final String REACTION_RXN = "/reaction.rxn";
    private static final String COMPOUND_SDF = "/ring-substructure.mol";

    @BeforeAll
    void beforeAll() {
        if (integrationTest) {
            sampleRegistrationClient.loadCompoundsFromFile(uploadForm("compounds.sdf", ModelUtil.loadResource("/compounds.sdf")));
        }
        saltCode = dictionaryClient.getNth(BuiltInDictionary.SALT_CODE, 1);
        stereoisomerCode = dictionaryClient.getNth(BuiltInDictionary.STEREOISOMER_CODE, 1);
        healthHazardRef = dictionaryClient.getNth(BuiltInDictionary.HEALTH_HAZARD, 0);
        componentStateRef = dictionaryClient.getNth(BuiltInDictionary.COMPONENT_STATE, 0);
    }

    @BeforeEach
    void setUp(TestInfo testInfo) {
        initExperiment("SampleSearchServiceTest");

        if (!integrationTest) {
            SRSCompoundDTO compound = new SRSCompoundDTO();
            compound.setCanSmiles("C1C=CC=CC=1");
            compound.setMolFile(ModelUtil.loadResourceAsString("/ring-substructure.mol"));
            compound.setSaltCode(saltCode.getId());
            compound.setStereoisomerCode(stereoisomerCode.getId());
            compound.setSaltEQ100(200);
            SRSSampleDTO sample = new SRSSampleDTO();
            sample.setStrCodeSample(new STRCodeSample(1, 1, 1));
            sample.setStrCodeCompound(new STRCodeCompound(1, 1));
            sample.setMolFormula("C");
            sample.setMolWeight(BigDecimal.ONE);
            sample.setNbkBatchNumber(new NbkBatchNumber("00000001-0005", 4));
            sample.setChemicalName("chemicalName");
            sample.setDensity(new BigDecimal(10));
            sample.setMolarity(new BigDecimal(20));
            sample.setMolarityUnit(MolarityUnit.MM);
            sample.setPurity(new BigDecimal(60));
            sample.setHealthHazards(List.of(healthHazardRef.getId()));
            sample.setCompoundState(componentStateRef.getId());
            sample.setBatchComment("batchComment");
            doReturn(
                    Page.of(Paging.DEFAULT, 1, List.of(sample)),
                    Page.of(Paging.DEFAULT, 0, List.of())
            ).when(sampleRegistrationClient).find(any(), any());
            doReturn(compound).when(sampleRegistrationClient).getCompound(sample.getStrCodeCompound());
        }
    }

    @AfterEach
    void tearDown(TestInfo testInfo) throws Exception {
        experiment.generateDetailsReport(new File("build/experiment-" + testInfo.getTestMethod().get().getName() + ".html"));
    }

    @Test
    void testAnalyzeRXN() {
        experiment.mutateSetSchemeFromResource(REACTION_RXN);
        experiment.mutateResolveInputs();
        verifySample(experiment.input(1));
    }

    @Test
    void testAddSample() {
        Page<SampleDTO> found = compoundClient.search(new FindSamplesRequest().withCatalog(SearchCatalog.SRS), Paging.DEFAULT);
        experiment.mutate(new ReactionMutation.AddInput(experiment.reaction().getAnchor(), found.getItems().getFirst()));
        verifySample(experiment.input(1));
    }

    @Test
    void testMarkSample() {
        Page<SampleDTO> found = compoundClient.search(new FindSamplesRequest().withCatalog(SearchCatalog.SRS), Paging.DEFAULT);

        SampleDTO sample = compoundClient.markSample(found.getItems().getFirst());
        assertThat(sample.getSource()).isEqualTo(SampleSource.SRS);
        assertThat(sample.getSampleKey()).isEqualTo(new STRCodeSample(1, 1, 1).toString());
        assertThat(sample.getNbkBatchNumber()).isEqualTo(new NbkBatchNumber("00000001-0005", 4));
        assertThat(sample.getDensity()).isEqualByComparingTo("10");
        assertThat(sample.getMolarity()).isEqualByComparingTo("20");
        assertThat(sample.getMolarityUnit()).isEqualTo(MolarityUnit.MM);
//                    assertThat(sample).hasPurity(60.0); // TODO purity gets overwritten
        assertThat(sample.getHealthHazards()).containsExactly(healthHazardRef);
        assertThat(sample.getBatchComment()).isEqualTo("batchComment");
        assertThat(sample.isMarked()).isTrue();

        Page<SampleDTO> found2 = compoundClient.search(new FindSamplesRequest().withCatalog(SearchCatalog.MY_MATERIALS), Paging.DEFAULT);
        assertThat(found2.getItems()).singleElement().satisfies(sample2 -> {
            assertThat(sample.getSource()).isEqualTo(SampleSource.SRS);
            assertThat(sample.getSampleKey()).isEqualTo(new STRCodeSample(1, 1, 1).toString());
            assertThat(sample.getNbkBatchNumber()).isEqualTo(new NbkBatchNumber("00000001-0005", 4));
            assertThat(sample.getDensity()).isEqualByComparingTo("10");
            assertThat(sample.getMolarity()).isEqualByComparingTo("20");
            assertThat(sample.getMolarityUnit()).isEqualTo(MolarityUnit.MM);
//                    assertThat(sample).hasPurity(60.0); // TODO purity gets overwritten
            assertThat(sample.getHealthHazards()).containsExactly(healthHazardRef);
            assertThat(sample.getCompoundState()).isEqualTo(componentStateRef);
            assertThat(sample.getBatchComment()).isEqualTo("batchComment");
            assertThat(sample.isMarked()).isTrue();
        });

        SampleDTO sample2 = compoundClient.unmarkSample(found.getItems().getFirst());
        assertThat(sample2.isMarked()).isFalse();
        Page<SampleDTO> found3 = compoundClient.search(new FindSamplesRequest().withCatalog(SearchCatalog.MY_MATERIALS), Paging.DEFAULT);
        assertThat(found3.getItems()).isEmpty();
    }

    private void verifySample(ReactionInput input) {
        assertThat(input).satisfies(row -> {
            assertThat(row.getCompound()).isInstanceOf(CompoundRef.Stored.class);
            assertThat(row.getCompound().getSaltCode()).isEqualTo(saltCode);
            assertThat(row.getCompound().getSaltEQ()).isEqualTo(2.0);
            assertThat(row.getCompound().getStereoisomerCode()).isEqualTo(stereoisomerCode);
            assertThat(row.getCompound().getCompoundKey()).isEqualTo(new STRCodeCompound(1, 1).toString());
            assertThat(row.getChemicalName()).isEqualTo("chemicalName"); // TODO
            assertThat(row.getSamples()).singleElement().satisfies(sample -> {
                assertThat(sample.getSampleSource()).isEqualTo(SampleSource.SRS);
                assertThat(sample.getSampleKey()).isEqualTo(new STRCodeSample(1, 1, 1).toString());
                assertThat(sample.getNbkBatchNumber()).isEqualTo(new NbkBatchNumber("00000001-0005", 4));
                assertThat(sample).hasDensity(10.0);
                assertThat(sample).hasMolarity(20.0, MolarityUnit.MM);
//                    assertThat(sample).hasPurity(60.0); // TODO purity gets overwritten
                assertThat(sample.getHealthHazards()).containsExactly(healthHazardRef);
                assertThat(sample.getComment()).isEqualTo("batchComment");
            });
        });
    }
}

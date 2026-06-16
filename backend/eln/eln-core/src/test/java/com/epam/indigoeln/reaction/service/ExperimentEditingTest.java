package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.BuiltInDictionary;
import com.epam.indigoeln.eln.model.SaltCodeRef;
import com.epam.indigoeln.eln.model.StereoisomerCodeRef;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputSampleMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputMutation;
import com.epam.indigoeln.reaction.model.units.DensityUnit;
import com.epam.indigoeln.reaction.util.CalculationReportBuilder;
import com.epam.indigoeln.reaction.util.MutationsTestUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static com.epam.indigoeln.eln.test.EnteredValueAssert.assertThat;
import static com.epam.indigoeln.reaction.model.units.MolUnit.MMOL;
import static com.epam.indigoeln.reaction.model.units.VolumeUnit.ML;
import static com.epam.indigoeln.reaction.model.units.WeightUnit.MG;

@QuarkusTest
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
public class ExperimentEditingTest extends MutationsTestBase {

    SaltCodeRef saltCode;
    StereoisomerCodeRef stereoisomerCode;

    @BeforeAll
    void beforeAll(@TempDir Path tempDir) {
        saltCode = dictionaryClient.getNth(BuiltInDictionary.SALT_CODE, 1);
        stereoisomerCode = dictionaryClient.<StereoisomerCodeRef>getDictionary(BuiltInDictionary.STEREOISOMER_CODE).get(1);
    }

    @BeforeEach
    void setUp(TestInfo testInfo) {
        reportBuilder = new CalculationReportBuilder(new File("build/calculations-" + testInfo.getTestMethod().get().getName() + ".html"));
        initExperiment("MutationsTest");
    }

    @AfterEach
    void tearDown() {
        reportBuilder.close();
    }

    @Test
    void testCase1() {
        loadScheme("/reaction-testCase1.rxn", true);
        assertThat(input1.getCompound().getMolWeight()).hasValue(46.07);
        assertThat(input2.getCompound().getMolWeight()).hasValue(60.05);
        assertThat(output1.getCompound().getMolWeight()).hasValue(88.11);
        assertThat(output2.getCompound().getMolWeight()).hasValue(18.01); // !!!

        applyMutation(new ReactionInputSampleMutation.SetInputMol(input1Sample1.getAnchor(), "100", MMOL));
        assertThat(input2Sample1.getMol()).hasValue(100, MMOL);
        assertThat(output2.getTheoMol()).hasValue(100, MMOL);
        assertThat(output2.getTheoMol()).hasValue(100, MMOL);
        assertThat(input1Sample1.getWeight()).hasValue(4607, MG);
        assertThat(input2Sample1.getWeight()).hasValue(6005, MG);
        assertThat(output1.getTheoWeight()).hasValue(8811, MG);
        assertThat(output2.getTheoWeight()).hasValue(1801, MG);

        applyMutation(new ReactionInputSampleMutation.SetInputDensity(input1Sample1.getAnchor(), "0.789", DensityUnit.G_ML));
        assertThat(input1Sample1.getVolume()).hasValue(5.8390, ML);

        applyMutation(new ReactionInputSampleMutation.SetInputDensity(input2Sample1.getAnchor(), "1.05", DensityUnit.G_ML));
        assertThat(input2Sample1.getVolume()).hasValue(5.7190, ML);
    }

    private void loadScheme(String resourceName, boolean undoRedo) {
        String rxnFile = new String(ModelUtil.loadResource(getClass(), resourceName));
        applyMutation(new ReactionMutation.SetScheme(reaction.getAnchor(), rxnFile), false);
    }

    private ReactionMutation.ResolveInputs prepareResolveInputs() {
        return MutationsTestUtil.prepareResolveInputs(experiment, reaction.getAnchor(), experimentClient, compoundClient);
    }

    private void addOutputSample() {
        applyMutation(new ReactionOutputMutation.AddProductSample(output1.getAnchor()), false);
    }
}

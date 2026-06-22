package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.BuiltInDictionary;
import com.epam.indigoeln.eln.model.SaltCodeRef;
import com.epam.indigoeln.eln.model.StereoisomerCodeRef;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static com.epam.indigoeln.eln.test.ReactionInputAssert.assertThat;
import static com.epam.indigoeln.eln.test.ReactionInputSampleAssert.assertThat;
import static com.epam.indigoeln.eln.test.ReactionOutputAssert.assertThat;
import static com.epam.indigoeln.reaction.model.units.DensityUnit.G_ML;
import static com.epam.indigoeln.reaction.model.units.MolUnit.MMOL;
import static com.epam.indigoeln.reaction.model.units.MolUnit.MOL;
import static com.epam.indigoeln.reaction.model.units.VolumeUnit.ML;
import static com.epam.indigoeln.reaction.model.units.WeightUnit.G;
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
        initExperiment("MutationsTest");
    }

    @AfterEach
    void tearDown(TestInfo testInfo) throws Exception {
        experiment.generateDetailsReport(new File("build/experiment-" + testInfo.getTestMethod().get().getName() + ".html"));
    }

    @Test
    void testCase1() {
        experiment.mutateSetSchemeFromResource("/reaction-testCase1.rxn");
        assertThat(experiment.input(1)).compound().hasMolWeight(46.07);
        assertThat(experiment.input(2)).compound().hasMolWeight(60.05);
        assertThat(experiment.output(1)).compound().hasMolWeight(88.11);
        assertThat(experiment.output(2)).compound().hasMolWeight(18.01);

        experiment.mutateSetInputMol(1, 1, "100", MolUnit.MMOL);
        assertThat(experiment.inputSample(1, 1)).hasWeight(4607, MG);
        assertThat(experiment.inputSample(2, 1)).hasMol(100, MMOL).hasWeight(6005, MG);
        assertThat(experiment.output(1)).hasTheoMol(100, MMOL).hasTheoWeight(8811, MG);
        assertThat(experiment.output(2)).hasTheoMol(100, MMOL).hasTheoWeight(1801, MG);

        experiment.mutateSetInputDensity(1, 1, "0.789", G_ML);
        assertThat(experiment.inputSample(1, 1)).hasVolume(5.8390, ML);

        experiment.mutateSetInputDensity(2, 1, "1.05", G_ML);
        assertThat(experiment.inputSample(2, 1)).hasVolume(5.7190, ML);
    }

    @Test
    void testCase2() {
        experiment.mutateSetSchemeFromResource("/reaction-testCase1.rxn");

        experiment.mutateSetInputMol(2, 1, "100", MolUnit.MMOL);
        experiment.mutateSetInputRowLimiting(2);
        assertThat(experiment.inputSample(1, 1)).hasWeight(4607, MG);
        assertThat(experiment.inputSample(2, 1)).hasMol(100, MMOL).hasWeight(6005, MG);
        assertThat(experiment.output(1)).hasTheoMol(100, MMOL).hasTheoWeight(8811, MG);
        assertThat(experiment.output(2)).hasTheoMol(100, MMOL).hasTheoWeight(1801, MG);

        experiment.mutateSetInputDensity(1, 1, "0.789", G_ML);
        assertThat(experiment.inputSample(1, 1)).hasVolume(5.8390, ML);

        experiment.mutateSetInputDensity(2, 1, "1.05", G_ML);
        assertThat(experiment.inputSample(2, 1)).hasVolume(5.7190, ML);
    }

    @Test
    void testCase3() {
        experiment.mutateSetSchemeFromResource("/reaction-testCase1.rxn");

        experiment.mutateSetInputWeight(1, 1, "10", G);
        experiment.mutateSetInputRowEQ(2, "2");
        assertThat(experiment.inputSample(1, 1)).hasMol(0.21706, MOL);
        assertThat(experiment.inputSample(2, 1)).hasMol(0.43412, MOL).hasWeight(26.069, G);
        assertThat(experiment.output(1)).hasTheoMol(0.21706, MOL).hasTheoWeight(19.125, G);
        assertThat(experiment.output(2)).hasTheoMol(0.21706, MOL).hasTheoWeight(3.9093, G);

        experiment.mutateSetInputDensity(1, 1, "0.789", G_ML);
        assertThat(experiment.inputSample(1, 1)).hasVolume(12.674, ML);

        experiment.mutateSetInputDensity(2, 1, "1.05", G_ML);
        assertThat(experiment.inputSample(2, 1)).hasVolume(24.828, ML);
    }

    @Test
    void testCase4() {
        experiment.mutateSetSchemeFromResource("/reaction-testCase1.rxn");
        experiment.mutateSetInputRowLimiting(2);

        experiment.mutateSetInputVolume(2, 1, "10", ML);
        experiment.mutateSetInputDensity(2, 1, "1.05", G_ML);
        experiment.mutateSetInputPurity(2, 1, "95");
        assertThat(experiment.inputSample(2, 1)).hasMol(0.16611, MOL).hasWeight(10.5, G);
        assertThat(experiment.inputSample(1, 1)).hasWeight(7.6528, G);
        assertThat(experiment.output(1)).hasTheoWeight(14.636, G);
        assertThat(experiment.output(2)).hasTheoWeight(2.9917, G);

        experiment.mutateSetInputDensity(1, 1, "0.789", G_ML);
        assertThat(experiment.inputSample(1, 1)).hasVolume(9.6993, ML);
    }

//    @Test
//    void testCase5() {
//        experiment.mutateSetSchemeFromResource("/reaction-testCase1.rxn");
        // !!! solvent toluene
//    }

//    @Test
//    void testCase6() {
//        experiment.mutateSetSchemeFromResource("/reaction-testCase1.rxn");
    // !!! solvent sulphuric acid
//    }

//    @Test
//    void testCase7() {
        // missing test data
//    }

    @Test
    void testCase8() {
        experiment.mutateSetSchemeFromResource("/reaction-testCase1.rxn");
        experiment.mutateSetInputRowLimiting(2);
        experiment.mutateSetInputRowEQ(1, "1.5");
        experiment.mutateSetInputDensity(1, 1, "0.789", G_ML);
        experiment.mutateSetInputVolume(2, 1, "5", ML);
        experiment.mutateSetInputDensity(2, 1, "1.05", G_ML);

        assertThat(experiment.inputSample(1, 1)).hasMol(0.13114, MOL).hasWeight(6.0417, G).hasVolume(7.6574, ML);
        assertThat(experiment.inputSample(2, 1)).hasMol(0.087427, MOL).hasWeight(5.25, G);
        assertThat(experiment.output(1)).hasTheoMol(0.087427, MOL).hasTheoWeight(7.7032, G);
        assertThat(experiment.output(2)).hasTheoMol(0.087427, MOL).hasTheoWeight(1.5746, G);
    }

    // conflict!
//    @Test
//    void testCase9() {
//        experiment.mutateSetSchemeFromResource("/reaction-testCase1.rxn");
//        experiment.mutateSetInputRowEQ(2, "1.1");
//        experiment.mutateSetInputVolume(1, 1, "50", ML);
//        experiment.mutateSetInputDensity(1, 1, "0.789", G_ML);
//        experiment.mutateSetInputMolarity(1, 1, "1.5", M);
//        experiment.mutateSetInputDensity(2, 1, "1.05", G_ML);
//
//        assertThat(experiment.inputSample(1, 1)).hasMol(75, MMOL).hasWeight(3.4553, G);
//        assertThat(experiment.inputSample(2, 1)).hasMol(82.5, MMOL).hasWeight(4.9541, G);
//        assertThat(experiment.output(1)).hasTheoWeight(6.6083, G);
//        assertThat(experiment.output(2)).hasTheoWeight(1.3508, G);
//    }
}

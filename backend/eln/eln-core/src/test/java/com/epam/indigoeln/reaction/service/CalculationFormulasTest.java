package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.BuiltInDictionary;
import com.epam.indigoeln.eln.model.SaltCodeRef;
import com.epam.indigoeln.eln.model.StereoisomerCodeRef;
import com.epam.indigoeln.reaction.model.OutputSampleAnchor;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static com.epam.indigoeln.eln.test.EnteredValueAssert.assertThat;
import static com.epam.indigoeln.eln.test.ReactionInputAssert.assertThat;
import static com.epam.indigoeln.eln.test.ReactionInputSampleAssert.assertThat;
import static com.epam.indigoeln.reaction.model.units.DensityUnit.G_ML;
import static com.epam.indigoeln.reaction.model.units.MolUnit.MOL;
import static com.epam.indigoeln.reaction.model.units.MolarityUnit.M;
import static com.epam.indigoeln.reaction.model.units.VolumeUnit.L;
import static com.epam.indigoeln.reaction.model.units.VolumeUnit.ML;
import static com.epam.indigoeln.reaction.model.units.WeightUnit.G;

@QuarkusTest
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
public class CalculationFormulasTest extends MutationsTestBase {

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
    // F1.1. input.mol = ∑ sample.mol
    void testF1_1() {
        experiment.mutateSetSchemeFromResource("/reaction-testCase1.rxn");
        experiment.mutateSetInputMol(1, 1, "2", MOL);

        assertThat(experiment.input(1)).hasMol(2.0, MOL);
    }

    @Test
    // F1.2. sample.mol = input.mol - ∑ otherSample.mol
    void testF1_2() {
        experiment.mutateSetSchemeFromResource("/reaction-testCase1.rxn");
        experiment.mutateSetInputRowMol(1, "2", MOL);

        assertThat(experiment.inputSample(1, 1)).hasMol(2.0, MOL);
    }

    @Test
    // F2.1. nonLimiting.mol = limiting.mol / limiting.eq * nonLimiting.eq
    void testF2_1() {
        experiment.mutateSetSchemeFromResource("/reaction-testCase1.rxn");
        experiment.mutateSetInputRowMol(1, "6", MOL);
        experiment.mutateSetInputRowEQ(1, "2");
        experiment.mutateSetInputRowEQ(2, "3");

        assertThat(experiment.input(2)).hasMol(9.0, MOL);
    }

    @Test
    // F2.2. nonLimiting.eq = nonLimiting.mol / limiting.mol * limiting.eq
    void testF2_2() {
        experiment.mutateSetSchemeFromResource("/reaction-testCase1.rxn");
        experiment.mutateSetInputRowMol(2, "6", MOL);
        experiment.mutateSetInputRowEQ(1, "2");
        experiment.mutateSetInputRowMol(1, "3", MOL);

        assertThat(experiment.input(2)).hasEq(4.0);
    }

/*
    @Test
    // F2.3. limiting.eq = limiting.mol * nonLimiting.eq / nonLimiting.mol
    void testF2_3() {
        experiment.mutateSetSchemeFromResource("/reaction-testCase1.rxn");
        experiment.mutateSetInputRowMol(1, "6", MOL);
        experiment.mutateSetInputRowEQ(2, "2");
        experiment.mutateSetInputRowMol(2, "3", MOL);

        assertThat(experiment.input(1)).hasEq(4.0);
    }
*/

    @Test
    // F3.1. sample.mol = sample.weight * sample.purity / molWeight
    void testF3_1() {
        experiment.mutateAddEmptyInput();
        experiment.mutateSetInputWeight(1, 1, "100", G);
        experiment.mutateSetInputPurity(1, 1, "50");
        experiment.mutate(new ReactionInputMutation.SetInputCompoundMolWeight(experiment.input(1).getAnchor(), "200"));

        assertThat(experiment.inputSample(1, 1)).hasMol(0.25, MOL);
    }

    @Test
    // F3.2. sample.weight = sample.mol * molWeight / sample.purity
    void testF3_2() {
        experiment.mutateAddEmptyInput();
        experiment.mutateSetInputMol(1, 1, "10", MOL);
        experiment.mutate(new ReactionInputMutation.SetInputCompoundMolWeight(experiment.input(1).getAnchor(), "200"));
        experiment.mutateSetInputPurity(1, 1, "50");

        assertThat(experiment.inputSample(1, 1)).hasWeight(4000, G);
    }

    @Test
    // F3.3. sample.actualMol = sample.actualWeight * sample.purity / molWeight
    void testF3_3() {
        experiment.mutateAddNoProductSample();
        OutputSampleAnchor os = experiment.outputSample(1, 1).getAnchor();
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputActualWeight(os, "100", G));
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputPurity(os, "50"));
        experiment.mutate(new ReactionOutputMutation.SetOutputCompoundMolWeight(experiment.output(1).getAnchor(), "25"));

        assertThat(experiment.outputSample(1, 1).getActualMol()).hasValue(2, MOL);
    }

    @Test
    // F3.4. sample.actualWeight = sample.actualMol * molWeight / sample.purity
    void testF3_4() {
        experiment.mutateAddNoProductSample();
        OutputSampleAnchor os = experiment.outputSample(1, 1).getAnchor();
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputActualMol(os, "2", MOL));
        experiment.mutate(new ReactionOutputMutation.SetOutputCompoundMolWeight(experiment.output(1).getAnchor(), "25"));
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputPurity(os, "50"));

        assertThat(experiment.outputSample(1, 1).getActualWeight()).hasValue(100, G);
    }

    @Test
    // F4.1. sample.mol = sample.molarity * sample.volume
    void testF4_1() {
        experiment.mutateAddEmptyInput();
        experiment.mutateSetInputMolarity(1, 1, "10", M);
        experiment.mutateSetInputVolume(1, 1, "20", L);

        assertThat(experiment.inputSample(1, 1).getMol()).hasValue(200, MOL);
    }

    @Test
    // F4.2. sample.molarity = sample.mol / sample.volume
    void testF4_2() {
        experiment.mutateAddEmptyInput();
        experiment.mutateSetInputMol(1, 1, "10", MOL);
        experiment.mutateSetInputVolume(1, 1, "20", L);

        assertThat(experiment.inputSample(1, 1).getMolarity()).hasValue(0.5, M);
    }

    @Test
    // F4.3. sample.volume = sample.mol / sample.molarity
    void testF4_3() {
        experiment.mutateAddEmptyInput();
        experiment.mutateSetInputMol(1, 1, "10", MOL);
        experiment.mutateSetInputMolarity(1, 1, "20", M);

        assertThat(experiment.inputSample(1, 1).getVolume()).hasValue(0.5, L);
    }

    @Test
    // F4.4. sample.actualMol = sample.molarity * sample.volume
    void testF4_4() {
        experiment.mutateAddNoProductSample();
        OutputSampleAnchor os = experiment.outputSample(1, 1).getAnchor();
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputMolarity(os, "10", M));
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputVolume(os, "20", L));

        assertThat(experiment.outputSample(1, 1).getActualMol()).hasValue(200, MOL);
    }

    @Test
    // F4.5. sample.molarity = sample.actualMol / sample.volume
    void testF4_5() {
        experiment.mutateAddNoProductSample();
        OutputSampleAnchor os = experiment.outputSample(1, 1).getAnchor();
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputActualMol(os, "10", MOL));
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputVolume(os, "20", L));

        assertThat(experiment.outputSample(1, 1).getMolarity()).hasValue(0.5, M);
    }

    @Test
    // F4.6. sample.volume = sample.actualMol / sample.molarity
    void testF4_6() {
        experiment.mutateAddNoProductSample();
        OutputSampleAnchor os = experiment.outputSample(1, 1).getAnchor();
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputActualMol(os, "10", MOL));
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputMolarity(os, "20", M));

        assertThat(experiment.outputSample(1, 1).getVolume()).hasValue(0.5, L);
    }

    @Test
    // F5.1. sample.weight = sample.volume * sample.density
    void testF5_1() {
        experiment.mutateAddEmptyInput();
        experiment.mutateSetInputVolume(1, 1, "10", ML);
        experiment.mutateSetInputDensity(1, 1, "20", G_ML);

        assertThat(experiment.inputSample(1, 1).getWeight()).hasValue(200, G);
    }

    @Test
    // F5.2. sample.volume = sample.weight / sample.density
    void testF5_2() {
        experiment.mutateAddEmptyInput();
        experiment.mutateSetInputWeight(1, 1, "10", G);
        experiment.mutateSetInputDensity(1, 1, "20", G_ML);

        assertThat(experiment.inputSample(1, 1).getVolume()).hasValue(0.5, ML);
    }

    @Test
    // F5.3. sample.density = sample.weight / sample.volume
    void testF5_3() {
        experiment.mutateAddEmptyInput();
        experiment.mutateSetInputWeight(1, 1, "10", G);
        experiment.mutateSetInputVolume(1, 1, "20", ML);

        assertThat(experiment.inputSample(1, 1).getDensity()).hasValue(0.5, G_ML);
    }

    @Test
    // F5.4. sample.actualWeight = sample.volume * sample.density
    void testF5_4() {
        experiment.mutateAddNoProductSample();
        OutputSampleAnchor os = experiment.outputSample(1, 1).getAnchor();
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputVolume(os, "10", ML));
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputDensity(os, "20", G_ML));

        assertThat(experiment.outputSample(1, 1).getActualWeight()).hasValue(200, G);
    }

    @Test
    // F5.5. sample.volume = sample.actualWeight / sample.density
    void testF5_5() {
        experiment.mutateAddNoProductSample();
        OutputSampleAnchor os = experiment.outputSample(1, 1).getAnchor();
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputActualWeight(os, "10", G));
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputDensity(os, "20", G_ML));

        assertThat(experiment.outputSample(1, 1).getVolume()).hasValue(0.5, ML);
    }

    @Test
    // F5.6. sample.density = sample.actualWeight / sample.volume
    void testF5_6() {
        experiment.mutateAddNoProductSample();
        OutputSampleAnchor os = experiment.outputSample(1, 1).getAnchor();
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputActualWeight(os, "10", G));
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputVolume(os, "20", ML));

        assertThat(experiment.outputSample(1, 1).getDensity()).hasValue(0.5, G_ML);
    }

    @Test
    // F6.1. output.theoMol = limiting.mol / limiting.eq * output.eq
    void testF6_1() {
        experiment.mutateAddEmptyInput();
        experiment.mutateSetInputRowMol(1, "10", MOL);
        experiment.mutateSetInputRowEQ(1, "2");
        experiment.mutateAddNoProductSample();
        OutputSampleAnchor os = experiment.outputSample(1, 1).getAnchor();
        experiment.mutate(new ReactionOutputMutation.SetOutputRowEQ(experiment.output(1).getAnchor(), "4"));

        assertThat(experiment.output(1).getTheoMol()).hasValue(20, MOL);
    }

    @Test
    // F7.1. output.theoWeight = output.theoMol * molWeight
    void testF7_1() {
        experiment.mutateAddEmptyInput();
        experiment.mutateAddNoProductSample();
        experiment.mutateSetInputRowMol(1, "10", MOL);

        assertThat(experiment.output(1).getTheoMol()).hasValue(10, MOL);

        experiment.mutate(new ReactionOutputMutation.SetOutputCompoundMolWeight(experiment.output(1).getAnchor(), "100"));

        assertThat(experiment.output(1).getTheoWeight()).hasValue(1000, G);
    }

    @Test
    // F8. outputSample.yield = outputSample.actualMol / output.theoMol
    void testF8_1() {
        experiment.mutateAddEmptyInput();
        experiment.mutateAddNoProductSample();
        OutputSampleAnchor os = experiment.outputSample(1, 1).getAnchor();
        experiment.mutateSetInputRowMol(1, "10", MOL);

        assertThat(experiment.output(1).getTheoMol()).hasValue(10, MOL);

        experiment.mutate(new ReactionOutputSampleMutation.SetOutputActualMol(os, "6", MOL));

        assertThat(experiment.outputSample(1, 1).getYieldValue()).hasValue(60);
    }

    @Test
    // F8.2. outputSample.actualMol = outputSample.yield * output.theoMol
    void testF8_2() {
        experiment.mutateAddEmptyInput();
        experiment.mutateAddNoProductSample();
        OutputSampleAnchor os = experiment.outputSample(1, 1).getAnchor();

        // theoMol = limiting.mol / limiting.eq * eq
        experiment.mutateSetInputRowMol(1, "20", MOL);
        experiment.mutate(new ReactionOutputMutation.SetOutputCompoundMolWeight(experiment.output(1).getAnchor(), "50"));
        assertThat(experiment.output(1).getTheoMol()).hasValue(20, MOL);
        assertThat(experiment.output(1).getTheoWeight()).hasValue(1000, G);

        // yield = actualWeight * purity / theoWeight
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputActualWeight(os, "10", G));
        assertThat(experiment.outputSample(1, 1).getYieldValue()).hasValue(1);

        // actualMol = yield * theoMol
        assertThat(experiment.outputSample(1, 1).getActualMol()).hasValue(0.2, MOL);
    }

    @Test
    // F9.1. outputSample.yield = outputSample.actualWeight * outputSample.purity / output.theoWeight
    void testF9_1() {
        experiment.mutateAddEmptyInput();
        experiment.mutateAddNoProductSample();
        OutputSampleAnchor os = experiment.outputSample(1, 1).getAnchor();
        experiment.mutateSetInputRowMol(1, "10", MOL);

        assertThat(experiment.output(1).getTheoMol()).hasValue(10, MOL);

        experiment.mutate(new ReactionOutputSampleMutation.SetOutputActualWeight(os, "20", G));
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputPurity(os, "50"));
        experiment.mutate(new ReactionOutputMutation.SetOutputCompoundMolWeight(experiment.output(1).getAnchor(), "100"));

        assertThat(experiment.outputSample(1, 1).getYieldValue()).hasValue(1);
    }

    @Test
    // F9.2. outputSample.actualWeight = outputSample.yield / outputSample.purity * output.theoWeight
    void testF9_2() {
        experiment.mutateAddEmptyInput();
        experiment.mutateAddNoProductSample();
        OutputSampleAnchor os = experiment.outputSample(1, 1).getAnchor();

        // theoMol = limiting.mol
        experiment.mutateSetInputRowMol(1, "10", MOL);
        assertThat(experiment.output(1).getTheoMol()).hasValue(10, MOL);

        // theoWeight = theoMol * molWeight
        experiment.mutate(new ReactionOutputMutation.SetOutputCompoundMolWeight(experiment.output(1).getAnchor(), "50"));
        assertThat(experiment.output(1).getTheoWeight()).hasValue(500, G);

        // yield = actualMol / theoMol
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputActualMol(os, "2", MOL));
        assertThat(experiment.outputSample(1, 1).getYieldValue()).hasValue(20);

        // actualWeight = yield / purity * theoWeight
        assertThat(experiment.outputSample(1, 1).getActualWeight()).hasValue(100, G);
    }
}

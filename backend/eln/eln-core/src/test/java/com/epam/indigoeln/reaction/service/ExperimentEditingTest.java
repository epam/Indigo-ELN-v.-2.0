package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.BuiltInDictionary;
import com.epam.indigoeln.eln.model.SaltCodeRef;
import com.epam.indigoeln.eln.model.StereoisomerCodeRef;
import com.epam.indigoeln.reaction.model.OutputAnchor;
import com.epam.indigoeln.reaction.model.OutputSampleAnchor;
import com.epam.indigoeln.reaction.model.ReactionRole;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
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
import static com.epam.indigoeln.reaction.model.units.MolarityUnit.M;
import static com.epam.indigoeln.reaction.model.units.VolumeUnit.ML;
import static com.epam.indigoeln.reaction.model.units.WeightUnit.G;
import static com.epam.indigoeln.reaction.model.units.WeightUnit.MG;
import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(experiment.input(1)).compound().hasMolWeight(46.069);
        assertThat(experiment.input(2)).compound().hasMolWeight(60.052);
        assertThat(experiment.output(1)).compound().hasMolWeight(88.106);
        assertThat(experiment.output(2)).compound().hasMolWeight(18.0150);

        experiment.mutateSetInputMol(1, 1, "100", MolUnit.MMOL);
        experiment.mutateSetInputDensity(1, 1, "0.789", G_ML);
        experiment.mutateSetInputDensity(2, 1, "1.05", G_ML);

        assertThat(experiment.inputSample(1, 1)).hasWeight(4606.9, MG).hasVolume(5.8389, ML);
        assertThat(experiment.inputSample(2, 1)).hasMol(100, MMOL).hasWeight(6005.2, MG).hasVolume(5.7192, ML);
        assertThat(experiment.output(1)).hasTheoMol(100, MMOL).hasTheoWeight(8810.6, MG);
        assertThat(experiment.output(2)).hasTheoMol(100, MMOL).hasTheoWeight(1801.5, MG);
    }

    @Test
    void testCase2() {
        experiment.mutateSetSchemeFromResource("/reaction-testCase1.rxn");

        experiment.mutateSetInputMol(2, 1, "100", MolUnit.MMOL);
        experiment.mutateSetInputRowLimiting(2);
        experiment.mutateSetInputDensity(1, 1, "0.789", G_ML);
        experiment.mutateSetInputDensity(2, 1, "1.05", G_ML);

        assertThat(experiment.inputSample(1, 1)).hasWeight(4606.9, MG).hasVolume(5.8389, ML);
        assertThat(experiment.inputSample(2, 1)).hasMol(100, MMOL).hasWeight(6005.2, MG).hasVolume(5.7192, ML);
        assertThat(experiment.output(1)).hasTheoMol(100, MMOL).hasTheoWeight(8810.6, MG);
        assertThat(experiment.output(2)).hasTheoMol(100, MMOL).hasTheoWeight(1801.5, MG);
    }

    @Test
    void testCase3() {
        experiment.mutateSetSchemeFromResource("/reaction-testCase1.rxn");

        experiment.mutateSetInputWeight(1, 1, "10", G);
        experiment.mutateSetInputRowEQ(2, "2");
        experiment.mutateSetInputDensity(1, 1, "0.789", G_ML);
        experiment.mutateSetInputDensity(2, 1, "1.05", G_ML);

        assertThat(experiment.inputSample(1, 1)).hasMol(0.21707, MOL).hasVolume(12.674, ML);
        assertThat(experiment.inputSample(2, 1)).hasMol(0.43413, MOL).hasWeight(26.070, G).hasVolume(24.829, ML);
        assertThat(experiment.output(1)).hasTheoMol(0.21707, MOL).hasTheoWeight(19.125, G);
        assertThat(experiment.output(2)).hasTheoMol(0.21707, MOL).hasTheoWeight(3.9104, G);
    }

    @Test
    void testCase4() {
        experiment.mutateSetSchemeFromResource("/reaction-testCase1.rxn");
        experiment.mutateSetInputRowLimiting(2);

        experiment.mutateSetInputVolume(2, 1, "10", ML);
        experiment.mutateSetInputDensity(2, 1, "1.05", G_ML);
        experiment.mutateSetInputPurity(2, 1, "95");
        experiment.mutateSetInputDensity(1, 1, "0.789", G_ML);

        assertThat(experiment.inputSample(1, 1)).hasWeight(7.6523, G).hasVolume(9.6988, ML);
        assertThat(experiment.inputSample(2, 1)).hasMol(0.16611, MOL).hasWeight(10.5, G);
        assertThat(experiment.output(1)).hasTheoWeight(14.635, G);
        assertThat(experiment.output(2)).hasTheoWeight(2.9924, G);
    }

    @Test
    @Disabled // conflict, waiting answer from BA
    void testCase5() {
        experiment.mutateSetSchemeFromResource("/reaction-testCase5.rxn");
        experiment.mutateSetInputRowRole(3, ReactionRole.SOLVENT);

        assertThat(experiment.input(3)).compound().hasMolWeight(92.141);

        experiment.mutateSetInputRowEQ(2, "1.5");
        experiment.mutateSetInputVolume(1, 1, "100", ML);
        experiment.mutateSetInputDensity(1, 1, "0.789", G_ML);
        experiment.mutateSetInputMolarity(1, 1, "2", M);
        experiment.mutateSetInputDensity(2, 1, "1.05", G_ML);
        experiment.mutateSetInputVolume(3, 1, "50", ML);
        experiment.mutateSetInputDensity(3, 1, "0.867", G_ML);

        assertThat(experiment.inputSample(1, 1)).hasMol(200, MMOL).hasWeight(9.2138, G);
        assertThat(experiment.inputSample(2, 1)).hasMol(300, MMOL).hasWeight(18.016, G).hasVolume(17.158, ML);
        assertThat(experiment.inputSample(3, 1)).hasWeight(43.350, G);
        assertThat(experiment.output(1)).hasTheoMol(200, MMOL).hasTheoWeight(17.621, G);
        assertThat(experiment.output(2)).hasTheoMol(200, MMOL).hasTheoWeight(3.6030, G);
    }

    @Test
    void testCase6() {
        experiment.mutateSetSchemeFromResource("/reaction-testCase6.rxn");
        experiment.mutateSetInputRowRole(3, ReactionRole.REAGENT);

        assertThat(experiment.input(3)).compound().hasMolWeight(98.072);

        experiment.mutateSetInputWeight(1, 1, "5", G);
        experiment.mutateSetInputDensity(1, 1, "0.789", G_ML);
        experiment.mutateSetInputPurity(1, 1, "0.98");
        experiment.mutateSetInputRowEQ(2, "1.2");
        experiment.mutateSetInputDensity(2, 1, "1.04", G_ML);
        experiment.mutateSetInputPurity(2, 1, "0.99");
        experiment.mutateSetInputRowEQ(3, "0.05");
        experiment.mutateSetInputDensity(3, 1, "1.84", G_ML);
        experiment.mutateSetInputPurity(3, 1, "0.98");

        assertThat(experiment.inputSample(1, 1)).hasMol(0.0010636, MOL).hasVolume(6.3371, ML);
        assertThat(experiment.inputSample(2, 1)).hasMol(0.0012763, MOL).hasWeight(7.7421, G).hasVolume(7.4444, ML);
        assertThat(experiment.inputSample(3, 1)).hasMol(0.000053181, MOL).hasWeight(0.5322, G).hasVolume(0.28924, ML);
        assertThat(experiment.output(1)).hasTheoMol(0.0010636, MOL).hasTheoWeight(0.093711, G);
        assertThat(experiment.output(2)).hasTheoMol(0.0010636, MOL).hasTheoWeight(0.019161, G);
    }

    @Test
    @Disabled // waiting answer from BA
    void testCase7() {
        experiment.mutateSetSchemeFromResource("/reaction-testCase1.rxn");
        experiment.mutateSetInputWeight(1, 1, "0.5", G);
        experiment.mutateSetInputDensity(1, 1, "0.789", G_ML);
        experiment.mutateSetInputWeight(2, 1, "2", G);
        experiment.mutateSetInputVolume(2, 1, "1.05", ML);
        experiment.mutateSetInputMolarity(2, 1, "100", M);
        experiment.mutateSetInputPurity(2, 1, "1");

        assertThat(experiment.inputSample(1, 1)).hasMol(0.010853, MOL).hasVolume(0.63371, ML);
        assertThat(experiment.inputSample(2, 1)); // TODO eq and mol
        assertThat(experiment.output(1)); // TODO
        assertThat(experiment.output(2)); // TODO
    }

    @Test
    void testCase8() {
        experiment.mutateSetSchemeFromResource("/reaction-testCase1.rxn");
        experiment.mutateSetInputRowLimiting(2);
        experiment.mutateSetInputRowEQ(1, "1.5");
        experiment.mutateSetInputDensity(1, 1, "0.789", G_ML);
        experiment.mutateSetInputVolume(2, 1, "5", ML);
        experiment.mutateSetInputDensity(2, 1, "1.05", G_ML);

        assertThat(experiment.inputSample(1, 1)).hasMol(0.13114, MOL).hasWeight(6.0413, G).hasVolume(7.6569, ML);
        assertThat(experiment.inputSample(2, 1)).hasMol(0.087424, MOL).hasWeight(5.25, G);
        assertThat(experiment.output(1)).hasTheoMol(0.087424, MOL).hasTheoWeight(7.7026, G);
        assertThat(experiment.output(2)).hasTheoMol(0.087424, MOL).hasTheoWeight(1.5749, G);
    }

    @Test
    @Disabled // conflict, waiting answer from BA
    void testCase9() {
        experiment.mutateSetSchemeFromResource("/reaction-testCase1.rxn");
        experiment.mutateSetInputRowEQ(2, "1.1");
        experiment.mutateSetInputVolume(1, 1, "50", ML);
        experiment.mutateSetInputDensity(1, 1, "0.789", G_ML);
        experiment.mutateSetInputMolarity(1, 1, "1.5", M);
        experiment.mutateSetInputDensity(2, 1, "1.05", G_ML);

        assertThat(experiment.inputSample(1, 1)).hasMol(75, MMOL).hasWeight(3.4553, G);
        assertThat(experiment.inputSample(2, 1)).hasMol(82.5, MMOL).hasWeight(4.9541, G);
        assertThat(experiment.output(1)).hasTheoWeight(6.6083, G);
        assertThat(experiment.output(2)).hasTheoWeight(1.3508, G);
    }

    @Test
    void testCase10() {
        experiment.mutateSetSchemeFromResource("/reaction-testCase1.rxn");
        experiment.mutateSetInputWeight(1, 1, "7.5", G);
        experiment.mutateSetInputDensity(1, 1, "0.789", G_ML);
        experiment.mutateSetInputPurity(1, 1, "85");

        experiment.mutateSetInputRowEQ(2, "1.3");
        experiment.mutateSetInputDensity(2, 1, "1.05", G_ML);
        experiment.mutateSetInputPurity(2, 1, "92");

        assertThat(experiment.inputSample(1, 1)).hasMol(0.13838, MOL).hasVolume(9.50570, ML);
        assertThat(experiment.inputSample(2, 1)).hasMol(0.17989, MOL).hasWeight(11.742, G).hasVolume(11.183, ML);
        assertThat(experiment.output(1)).hasTheoMol(0.13838, MOL).hasTheoWeight(12.192, G);
        assertThat(experiment.output(2)).hasTheoMol(0.13838, MOL).hasTheoWeight(2.4929, G);
    }

    @Test
    void testCase11() {
        experiment.mutateSetSchemeFromResource("/reaction-testCase1.rxn");
        experiment.mutateSetInputRowLimiting(2);
        experiment.mutateSetInputRowEQ(1, "2.5");
        experiment.mutateSetInputDensity(1, 1, "0.789", G_ML);
        experiment.mutateSetInputRowMol(2, "50", MMOL);
        experiment.mutateSetInputDensity(2, 1, "1.05", G_ML);

        assertThat(experiment.inputSample(1, 1)).hasMol(125, MMOL).hasWeight(5758.6, MG).hasVolume(7.2986, ML);
        assertThat(experiment.inputSample(2, 1)).hasWeight(3002.6, MG).hasVolume(2.8596, ML);
        assertThat(experiment.output(1)).hasTheoMol(50, MMOL).hasTheoWeight(4405.3, MG);
        assertThat(experiment.output(2)).hasTheoMol(50, MMOL).hasTheoWeight(900.75, MG);
    }

    @Test
    void testCase12() {
        experiment.mutateSetSchemeFromResource("/reaction-testCase1.rxn");
        experiment.mutateSetInputVolume(1, 1, "25", ML);
        experiment.mutateSetInputDensity(1, 1, "0.789", G_ML);
        experiment.mutateSetInputPurity(1, 1, "75");
        experiment.mutateSetInputDensity(2, 1, "1.05", G_ML);

        assertThat(experiment.inputSample(1, 1)).hasMol(0.32112, MOL).hasWeight(19.725, G);
        assertThat(experiment.inputSample(2, 1)).hasMol(0.32112, MOL).hasWeight(19.284, G).hasVolume(18.366, ML);
        assertThat(experiment.output(1)).hasTheoMol(0.32112, MOL).hasTheoWeight(28.293, G);
        assertThat(experiment.output(2)).hasTheoMol(0.32112, MOL).hasTheoWeight(5.785, G);
    }

    @Test
    void testCase13() {
        experiment.mutateSetSchemeFromResource("/reaction-testCase6.rxn");
        experiment.mutateSetInputRowMol(1, "250", MMOL);
        experiment.mutateSetInputDensity(1, 1, "0.789", G_ML);
        experiment.mutateSetInputRowEQ(2, "0.95");
        experiment.mutateSetInputDensity(2, 1, "1.05", G_ML);
        experiment.mutateSetInputRowEQ(3, "0.1");
        experiment.mutateSetInputDensity(3, 1, "1.84", G_ML);
        experiment.mutateSetInputPurity(3, 1, "98");

        assertThat(experiment.inputSample(1, 1)).hasWeight(11517, MG).hasVolume(14.597, ML);
        assertThat(experiment.inputSample(2, 1)).hasMol(237.50, MMOL).hasWeight(14262, MG).hasVolume(13.583, ML);
        assertThat(experiment.inputSample(3, 1)).hasMol(25, MMOL).hasWeight(2501.8, MG).hasVolume(1.3597, ML);
        assertThat(experiment.output(1)).hasTheoMol(250, MMOL).hasTheoWeight(22026, MG);
        assertThat(experiment.output(2)).hasTheoMol(250, MMOL).hasTheoWeight(4503.7, MG);
    }

    @Test
    @Disabled // conflict, waiting answer from BA
    void testCase14() {
        experiment.mutateSetSchemeFromResource("/reaction-testCase14.rxn");
        experiment.mutateSetInputVolume(1, 1, "200", ML);
        experiment.mutateSetInputDensity(1, 1, "0.789", G_ML);
        experiment.mutateSetInputMolarity(1, 1, "0.5", M);
        experiment.mutateSetInputRowEQ(2, "3");
        experiment.mutateSetInputDensity(2, 1, "1.05", G_ML);
        experiment.mutateSetInputVolume(3, 1, "100", ML);
        experiment.mutateSetInputDensity(3, 1, "1.33", G_ML);

        assertThat(experiment.inputSample(1, 1)).hasMol(100, MMOL).hasWeight(4.6069, G);
        assertThat(experiment.inputSample(2, 1)).hasMol(300, MMOL).hasWeight(18.016, G).hasVolume(17.158, ML);
        assertThat(experiment.inputSample(3, 1)).hasWeight(133, G);
        assertThat(experiment.output(1)).hasTheoMol(100, MMOL).hasTheoWeight(8.8106, MG);
        assertThat(experiment.output(2)).hasTheoMol(100, MMOL).hasTheoWeight(1.8015, MG);
    }

    @Test
    void testEnterAllValues() {
        experiment.mutateSetSchemeFromResource("/reaction-testCase1.rxn");

        experiment.mutateSetInputRowMol(1, "1000", MMOL);
        assertThat(experiment.lastMutationResponse().getDebugMessages()).isEmpty();
        experiment.mutateSetInputRowEQ(1, "2");
        assertThat(experiment.lastMutationResponse().getDebugMessages()).isEmpty();
        experiment.mutateSetInputMol(1, 1, "1000", MMOL);
        assertThat(experiment.lastMutationResponse().getDebugMessages()).isEmpty();
        experiment.mutateSetInputPurity(1, 1, "90");
        assertThat(experiment.lastMutationResponse().getDebugMessages()).isEmpty();
        experiment.mutateSetInputWeight(1, 1, "51187.777", MG); // exact value 51187.776724497475
        assertThat(experiment.lastMutationResponse().getDebugMessages()).isEmpty();
        experiment.mutateSetInputMolarity(1, 1, "1.5", M);
        assertThat(experiment.lastMutationResponse().getDebugMessages()).isEmpty();
        experiment.mutateSetInputVolume(1, 1, "666.66667", ML); // exact value 666.6666702547958
        assertThat(experiment.lastMutationResponse().getDebugMessages()).isEmpty();
        experiment.mutateSetInputDensity(1, 1, "0.076781665", G_ML); // exact value 0.0767816650867462
        assertThat(experiment.lastMutationResponse().getDebugMessages()).isEmpty();

        experiment.mutateSetInputRowMol(2, "3000", MMOL);
        assertThat(experiment.lastMutationResponse().getDebugMessages()).isEmpty();
        experiment.mutateSetInputRowEQ(2, "6");
        assertThat(experiment.lastMutationResponse().getDebugMessages()).isEmpty();
        experiment.mutateSetInputMol(2, 1, "3000", MMOL);
        assertThat(experiment.lastMutationResponse().getDebugMessages()).isEmpty();
        experiment.mutateSetInputPurity(2, 1, "75");
        assertThat(experiment.lastMutationResponse().getDebugMessages()).isEmpty();
        experiment.mutateSetInputWeight(2, 1, "240207.99", MG); // exact value 240207.99446105957
        assertThat(experiment.lastMutationResponse().getDebugMessages()).isEmpty();
        experiment.mutateSetInputMolarity(2, 1, "2", M);
        assertThat(experiment.lastMutationResponse().getDebugMessages()).isEmpty();
        experiment.mutateSetInputVolume(2, 1, "1500", ML);
        assertThat(experiment.lastMutationResponse().getDebugMessages()).isEmpty();
        experiment.mutateSetInputDensity(2, 1, "0.16013866", G_ML); // exact value 0.1601386629740397
        assertThat(experiment.lastMutationResponse().getDebugMessages()).isEmpty();

        experiment.mutateAddProductSample(1);
        OutputAnchor output = experiment.output(1).getAnchor();
        experiment.mutate(new ReactionOutputMutation.SetOutputRowEQ(output, "4"));
        assertThat(experiment.lastMutationResponse().getDebugMessages()).isEmpty();

        OutputSampleAnchor outputSample = experiment.outputSample(1, 1).getAnchor();
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputActualMol(outputSample, "1500", MMOL));
        assertThat(experiment.lastMutationResponse().getDebugMessages()).isEmpty();
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputPurity(outputSample, "60"));
        assertThat(experiment.lastMutationResponse().getDebugMessages()).isEmpty();
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputActualWeight(outputSample, "220265.0", MG)); // exact value 220264.995098114
        assertThat(experiment.lastMutationResponse().getDebugMessages()).isEmpty();
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputMolarity(outputSample, "0.5", M));
        assertThat(experiment.lastMutationResponse().getDebugMessages()).isEmpty();
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputVolume(outputSample, "3000.0001", ML)); // exact value 3000.0000667634818
        assertThat(experiment.lastMutationResponse().getDebugMessages()).isEmpty();
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputDensity(outputSample, "0.073421665", G_ML)); // exact value 0.07342166503270467
        assertThat(experiment.lastMutationResponse().getDebugMessages()).isEmpty();
    }
}

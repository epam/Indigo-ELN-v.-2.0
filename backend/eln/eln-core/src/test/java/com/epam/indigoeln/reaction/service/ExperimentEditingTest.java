package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.BuiltInDictionary;
import com.epam.indigoeln.eln.model.SaltCodeRef;
import com.epam.indigoeln.eln.model.StereoisomerCodeRef;
import com.epam.indigoeln.reaction.model.ReactionRole;
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
    @Disabled // until resolve questions with BAs
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
    @Disabled // until resolve questions with BAs
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

        assertThat(experiment.inputSample(1, 1)).hasMol(0.10636, MMOL).hasVolume(6.3371, ML);
        assertThat(experiment.inputSample(2, 1)).hasMol(0.12763, MMOL).hasWeight(0.0077421, G).hasVolume(0.0074444, ML);
        assertThat(experiment.inputSample(3, 1)).hasMol(0.0053181, MMOL).hasWeight(0.0005322, G).hasVolume(0.0002893, ML);
        assertThat(experiment.output(1)).hasTheoMol(0.10636, MMOL).hasTheoWeight(0.0093711, G);
        assertThat(experiment.output(2)).hasTheoMol(0.10636, MMOL).hasTheoWeight(0.0019161, G);
    }

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

        assertThat(experiment.inputSample(1, 1)).hasMol(0.13114, MOL).hasWeight(6.0413, G).hasVolume(7.6569, ML);
        assertThat(experiment.inputSample(2, 1)).hasMol(0.087424, MOL).hasWeight(5.25, G);
        assertThat(experiment.output(1)).hasTheoMol(0.087424, MOL).hasTheoWeight(7.7026, G);
        assertThat(experiment.output(2)).hasTheoMol(0.087424, MOL).hasTheoWeight(1.5749, G);
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

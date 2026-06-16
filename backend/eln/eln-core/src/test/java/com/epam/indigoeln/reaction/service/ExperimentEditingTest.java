package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.BuiltInDictionary;
import com.epam.indigoeln.eln.model.SaltCodeRef;
import com.epam.indigoeln.eln.model.StereoisomerCodeRef;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputSampleMutation;
import com.epam.indigoeln.reaction.model.units.DensityUnit;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

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
        initExperiment("MutationsTest");
    }

    @Test
    void testCase1() {
        experiment.mutateSetSchemeFromResource("/reaction-testCase1.rxn");
        assertThat(experiment.input(1).getCompound().getMolWeight()).hasValue(46.07);
        assertThat(experiment.input(2).getCompound().getMolWeight()).hasValue(60.05);
        assertThat(experiment.output(1).getCompound().getMolWeight()).hasValue(88.11);
        assertThat(experiment.output(2).getCompound().getMolWeight()).hasValue(18.01); // !!!

        experiment.mutate(new ReactionInputSampleMutation.SetInputMol(experiment.inputSample(1, 1).getAnchor(), "100", MolUnit.MMOL));
        assertThat(experiment.inputSample(2, 1).getMol()).hasValue(100, MMOL);
        assertThat(experiment.output(2).getTheoMol()).hasValue(100, MMOL);
        assertThat(experiment.output(2).getTheoMol()).hasValue(100, MMOL);
        assertThat(experiment.inputSample(1, 1).getWeight()).hasValue(4607, MG);
        assertThat(experiment.inputSample(2, 1).getWeight()).hasValue(6005, MG);
        assertThat(experiment.output(1).getTheoWeight()).hasValue(8811, MG);
        assertThat(experiment.output(2).getTheoWeight()).hasValue(1801, MG);

        experiment.mutate(new ReactionInputSampleMutation.SetInputDensity(experiment.inputSample(1, 1).getAnchor(), "0.789", DensityUnit.G_ML));
        assertThat(experiment.inputSample(1, 1).getVolume()).hasValue(5.8390, ML);

        experiment.mutate(new ReactionInputSampleMutation.SetInputDensity(experiment.inputSample(2, 1).getAnchor(), "1.05", DensityUnit.G_ML));
        assertThat(experiment.inputSample(2, 1).getVolume()).hasValue(5.7190, ML);
    }
}

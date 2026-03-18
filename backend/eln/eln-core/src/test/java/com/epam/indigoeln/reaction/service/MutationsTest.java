package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.compound.model.FindSamplesRequest;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.BuiltInDictionary;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.Page;
import com.epam.indigoeln.eln.model.Paging;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.*;
import com.epam.indigoeln.reaction.model.outputsample.*;
import com.epam.indigoeln.reaction.model.units.*;
import com.epam.indigoeln.reaction.util.CalculationReportBuilder;
import com.epam.indigoeln.reaction.util.MutationsTestUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static com.epam.indigoeln.common.util.ModelUtil.loadResource;
import static com.epam.indigoeln.test.ClientCallAssert.assertThatClientCall;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
public class MutationsTest extends MutationsTestBase {

    @BeforeAll
    void beforeAll(@TempDir Path tempDir) {
        miscClient.loadCompoundsFromFileClient("compounds.sdf", tempDir, loadResource(getClass(), "/Compound_000000001_000500000.1.sdf"));
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
    void testAddEmptyInputToEmptyReaction() {
        applyMutation(new ReactionMutation.AddEmptyInput(reaction.getAnchor()));
        assertThat(input1).isNotNull();
        assertThat(input1.getCompound()).isInstanceOf(CompoundRef.Unknown.class);
        assertThat(input1Sample1).isNotNull();
    }

    @Test
    void testAddInputToEmptyReaction() {
        Page<SampleDTO> samples = compoundClient.findSamples(new FindSamplesRequest(), Paging.DEFAULT);
        applyMutation(new ReactionMutation.AddInput(reaction.getAnchor(), samples.getItems().getFirst().getId()));
        assertThat(input1).isNotNull();
        assertThat(input1.getCompound()).isInstanceOf(CompoundRef.Stored.class);
        assertThat(input1Sample1).isNotNull();
        assertThat(input1Sample1.getSampleId()).isEqualTo(samples.getItems().getFirst().getId());
    }

    @Test
    void testIncorrectAnchor() {
        assertThatClientCall(() -> {
            experimentClient.mutateExperimentModel2Raw(experiment.getId(), experiment.getRevision(), "{\"type\": \"AddEmptyInput\", \"anchor\": \"invalid\"}");
        }).isBadRequest("Cannot construct instance of `com.epam.indigoeln.reaction.model.ReactionAnchor");
    }

    @Test
    void testUnknownField() {
        assertThatClientCall(() -> {
            experimentClient.mutateExperimentModel2Raw(experiment.getId(), experiment.getRevision(), "{\"type\": \"AddEmptyInput\", \"anchor\": \"00000000-0000-0000-0000-000000000001\", \"unknownField\": 123}");
        }).isBadRequest("Unrecognized field \"unknownField\"");
    }

    @Test
    void testLoadReaction() {
        String rxnFile = new String(loadResource(getClass(), "/reaction.rxn"));
        applyMutation(new ReactionMutation.SetScheme(reaction.getAnchor(), rxnFile));
        assertThat(input1).isNotNull();
        assertThat(input1.getCompound()).isInstanceOf(CompoundRef.Virtual.class);
        assertThat(input1Sample1).isNotNull();
        assertThat(input2).isNotNull();
        assertThat(input2.getCompound()).isInstanceOf(CompoundRef.Virtual.class);
        assertThat(input2Sample1).isNotNull();
        assertThat(output1).isNotNull();
        assertThat(output1.getCompound()).isInstanceOf(CompoundRef.Virtual.class);
    }

    @Test
    void testResolveInputs() {
        loadScheme();
        applyMutation(prepareResolveInputs());
        assertThat(input1.getCompound()).isInstanceOf(CompoundRef.Stored.class);
        assertThat(input1Sample1.getSampleId()).isNotNull();
    }

    @Test
    void testRemoveInput() {
        loadScheme();
        InputAnchor removedAnchor = input1.getAnchor();
        applyMutation(new ReactionInputMutation.RemoveInput(removedAnchor));
        assertThat(reaction.getInputs()).hasSize(1);
        assertThat(input1.getAnchor()).isNotEqualTo(removedAnchor);
    }

    @Test
    void testSetInputRowEQ() {
        loadScheme();
        applyMutation(new ReactionInputMutation.SetInputRowEQ(input1.getAnchor(), "2", null));
        assertThat(input1.getEq().getValue()).isEqualTo(2.0);
        assertThat(input1.getEq().getStringValue()).isEqualTo("2");
    }

    @Test
    void testUnsetInputRowEq() {
        loadScheme();
        applyMutation(new ReactionInputSampleMutation.SetInputWeight(input1Sample1.getAnchor(), "100", WeightUnit.G, null));
        applyMutation(new ReactionInputSampleMutation.SetInputWeight(input2Sample1.getAnchor(), "200", WeightUnit.G, null));
        applyMutation(new ReactionInputMutation.SetInputRowEQ(input2.getAnchor(), "2", null));
        applyMutation(new ReactionInputSampleMutation.SetInputWeight(input1Sample1.getAnchor(), null, null, null));
        applyMutation(new ReactionInputMutation.SetInputRowEQ(input2.getAnchor(), null, null));
    }

    @Test
    void testUnsetInputRowEq2() {
        loadScheme();
        applyMutation(new ReactionInputSampleMutation.SetInputWeight(input1Sample1.getAnchor(), "100.0", WeightUnit.G, null), false);
        applyMutation(new ReactionInputMutation.SetInputRowEQ(input2.getAnchor(), "2", null), false);
        applyMutation(new ReactionInputSampleMutation.SetInputWeight(input1Sample1.getAnchor(), "10", WeightUnit.G, null), false);
        applyMutation(new ReactionInputSampleMutation.SetInputWeight(input2Sample1.getAnchor(), "20", WeightUnit.G, null), false);
        applyMutation(new ReactionInputMutation.SetInputRowEQ(input2.getAnchor(), null, null), false);
        applyMutation(new ReactionInputSampleMutation.SetInputWeight(input1Sample1.getAnchor(), "10.20", WeightUnit.G, null), false);
        applyMutation(new ReactionInputMutation.SetInputRowEQ(input1.getAnchor(), "1", null), false);
        applyMutation(new ReactionInputSampleMutation.SetInputWeight(input1Sample1.getAnchor(), null, null, null), false);
    }

    @Test
    void testSetInputRowSaltCodeAndEQ() {
        loadScheme();
        DictionaryItemRef saltCode = dictionaryClient.getSaltCodes().get(1);
        applyMutation(new ReactionInputMutation.SetInputRowSaltCode(input1.getAnchor(), saltCode));
        assertThat(input1.getCompound()).isInstanceOf(CompoundRef.Virtual.class);
        assertThat(input1.getCompound().getSaltCode()).isEqualTo(saltCode);
        applyMutation(new ReactionInputMutation.SetInputRowSaltEQ(input1.getAnchor(), 2.0));
        assertThat(input1.getCompound().getSaltCode()).isEqualTo(saltCode);
        assertThat(input1.getCompound().getSaltEQ()).isCloseTo(2.0, Offset.offset(1e-6));
    }

    @Test
    void testSetInputRowStereoisomerCode() {
        loadScheme();
        DictionaryItemRef stereoisomerCode = dictionaryClient.getDictionary(BuiltInDictionary.STEREOISOMER_CODE).get(1);
        applyMutation(new ReactionInputMutation.SetInputCompoundStereoisomerCode(input1.getAnchor(), stereoisomerCode));
        assertThat(input1.getCompound()).isInstanceOf(CompoundRef.Virtual.class);
        assertThat(input1.getCompound().getStereoisomerCode()).isEqualTo(stereoisomerCode);
    }

    @Test
    void testSetInputCompoundMolWeight() {
        applyMutation(new ReactionMutation.AddEmptyInput(reaction.getAnchor()), false);
        applyMutation(new ReactionInputMutation.SetInputCompoundMolWeight(input1.getAnchor(), "100.0", null));
        assertThat(input1.getCompound().getMolWeight().getValue()).isEqualTo(100.0);
        assertThat(input1.getCompound().getMolWeight().getSource()).isEqualTo(EnteredValueSource.userEntered(experiment.getRevision()));
    }

    @Test
    void testSetInputRowLimiting() {
        loadScheme();
        applyMutation(new ReactionInputMutation.SetInputRowLimiting(input2.getAnchor()));
        assertThat(input1.isLimiting()).isFalse();
        assertThat(input2.isLimiting()).isTrue();
    }

    @Test
    void testSetInputRowChemicalName() {
        applyMutation(new ReactionMutation.AddEmptyInput(reaction.getAnchor()), false);
        applyMutation(new ReactionInputMutation.SetInputRowChemicalName(input1.getAnchor(), "newChemicalName"));
        assertThat(input1.getChemicalName()).isEqualTo("newChemicalName");
    }

    @Test
    void testSetInputRowMol() {
        applyMutation(new ReactionMutation.AddEmptyInput(reaction.getAnchor()), false);
        applyMutation(new ReactionInputMutation.SetInputRowMol(input1.getAnchor(), "10.0", MolUnit.MMOL, null));
        assertThat(input1.getMol().getValue()).isEqualTo(10.0);
        assertThat(input1.getMol().getUnit()).isEqualTo(MolUnit.MMOL);
    }

    @Test
    void testSetInputRowRole() {
        loadScheme();
        applyMutation(new ReactionInputMutation.SetInputRowRole(input1.getAnchor(), ReactionRole.CATALYST));
        assertThat(input1.getRole()).isEqualTo(ReactionRole.CATALYST);
    }

    @Test
    void testSetInputMol() {
        applyMutation(new ReactionMutation.AddEmptyInput(reaction.getAnchor()), false);
        applyMutation(new ReactionInputSampleMutation.SetInputMol(input1Sample1.getAnchor(), "10.0", MolUnit.MMOL, null));
        assertThat(input1Sample1.getMol().getValue()).isEqualTo(10.0);
        assertThat(input1Sample1.getMol().getUnit()).isEqualTo(MolUnit.MMOL);
        assertThat(input1.getMol().getValue()).isEqualTo(10.0);
        assertThat(input1.getMol().getUnit()).isEqualTo(MolUnit.MMOL);
    }

    @Test
    void testSetInputWeight() {
        applyMutation(new ReactionMutation.AddEmptyInput(reaction.getAnchor()), false);
        applyMutation(new ReactionInputSampleMutation.SetInputWeight(input1Sample1.getAnchor(), "10.0", WeightUnit.G, null));
        assertThat(input1Sample1.getWeight().getValue()).isEqualTo(10.0);
        assertThat(input1Sample1.getWeight().getUnit()).isEqualTo(WeightUnit.G);
    }

    @Test
    void testSetInputDensity() {
        applyMutation(new ReactionMutation.AddEmptyInput(reaction.getAnchor()), false);
        applyMutation(new ReactionInputSampleMutation.SetInputDensity(input1Sample1.getAnchor(), "10.0", DensityUnit.G_ML, null));
        assertThat(input1Sample1.getDensity().getValue()).isEqualTo(10.0);
        assertThat(input1Sample1.getDensity().getUnit()).isEqualTo(DensityUnit.G_ML);
    }

    @Test
    void testSetInputMolarity() {
        applyMutation(new ReactionMutation.AddEmptyInput(reaction.getAnchor()), false);
        applyMutation(new ReactionInputSampleMutation.SetInputMolarity(input1Sample1.getAnchor(), "10.0", MolarityUnit.MM, null));
        assertThat(input1Sample1.getMolarity().getValue()).isEqualTo(10.0);
        assertThat(input1Sample1.getMolarity().getUnit()).isEqualTo(MolarityUnit.MM);
    }

    @Test
    void testSetInputVolume() {
        applyMutation(new ReactionMutation.AddEmptyInput(reaction.getAnchor()), false);
        applyMutation(new ReactionInputSampleMutation.SetInputVolume(input1Sample1.getAnchor(), "10.0", VolumeUnit.ML, null));
        assertThat(input1Sample1.getVolume().getValue()).isEqualTo(10.0);
        assertThat(input1Sample1.getVolume().getUnit()).isEqualTo(VolumeUnit.ML);
    }

    @Test
    void testSetInputPurity() {
        applyMutation(new ReactionMutation.AddEmptyInput(reaction.getAnchor()), false);
        applyMutation(new ReactionInputSampleMutation.SetInputPurity(input1Sample1.getAnchor(), "10", null));
        assertThat(input1Sample1.getPurity().getValue()).isEqualTo(10.0);
    }

    @Test
    void testSetInputHealthHazards() {
        applyMutation(new ReactionMutation.AddEmptyInput(reaction.getAnchor()), false);
        DictionaryItemRef healthHazard = dictionaryClient.getDictionary(BuiltInDictionary.HEALTH_HAZARD).getFirst();
        applyMutation(new ReactionInputSampleMutation.SetInputHealthHazards(input1Sample1.getAnchor(), List.of(healthHazard)));
        assertThat(input1Sample1.getHealthHazards()).containsExactly(healthHazard);
    }

    @Test
    void testSetInputComment() {
        loadScheme();
        applyMutation(prepareResolveInputs(), false);
        applyMutation(new ReactionInputSampleMutation.SetInputComment(input1Sample1.getAnchor(), "newComment"));
        assertThat(input1Sample1.getComment()).isEqualTo("newComment");
    }

    @Test
    void testAddProductSample() {
        loadScheme();
        applyMutation(new ReactionOutputMutation.AddProductSample(output1.getAnchor()));
        assertThat(output1Sample1).isNotNull();
    }

    @Test
    void testSetOutputRowType() {
        loadScheme();
        applyMutation(new ReactionOutputMutation.SetOutputRowType(output1.getAnchor(), ReactionOutputType.INTERMEDIATE));
        assertThat(output1.getType()).isEqualTo(ReactionOutputType.INTERMEDIATE);
    }

    @Test
    void testSetOutputRowName() {
        loadScheme();
        applyMutation(new ReactionOutputMutation.SetOutputRowName(output1.getAnchor(), "newOutputName"));
        assertThat(output1.getOutputName()).isEqualTo("newOutputName");
    }

    @Test
    void testSetOutputRowChemicalName() {
        loadScheme();
        applyMutation(new ReactionOutputMutation.SetOutputRowChemicalName(output1.getAnchor(), "newChemicalName"));
        assertThat(output1.getChemicalName()).isEqualTo("newChemicalName");
    }

    @Test
    void testSetOutputRowSaltCodeAndEQ() {
        loadScheme();
        DictionaryItemRef saltCode = dictionaryClient.getSaltCodes().get(1);
        applyMutation(new ReactionOutputMutation.SetOutputRowSaltCode(output1.getAnchor(), saltCode));
        assertThat(output1.getCompound()).isInstanceOf(CompoundRef.Virtual.class);
        assertThat(output1.getCompound().getSaltCode()).isEqualTo(saltCode);
        applyMutation(new ReactionOutputMutation.SetOutputRowSaltEQ(output1.getAnchor(), 2.0));
        assertThat(output1.getCompound().getSaltCode()).isEqualTo(saltCode);
        assertThat(output1.getCompound().getSaltEQ()).isCloseTo(2.0, Offset.offset(1e-6));
    }

    @Test
    void testSetOutputRowStereoisomerCode() {
        loadScheme();
        DictionaryItemRef stereoisomerCode = dictionaryClient.getDictionary(BuiltInDictionary.STEREOISOMER_CODE).get(1);
        applyMutation(new ReactionOutputMutation.SetOutputCompoundStereoisomerCode(output1.getAnchor(), stereoisomerCode));
        assertThat(output1.getCompound()).isInstanceOf(CompoundRef.Virtual.class);
        assertThat(output1.getCompound().getStereoisomerCode()).isEqualTo(stereoisomerCode);
    }

// TODO need unknown compound for that
//    @Test
//    void testSetOutputCompoundMolWeight() {
//        loadScheme();
//        applyMutation(new ReactionOutputMutation.SetOutputCompoundMolWeight(output1.getAnchor(), 100.0, null));
//        assertThat(output1.getCompound().getMolWeight().getValue()).isEqualTo(100.0);
//        assertThat(output1.getCompound().getMolWeight().getSource()).isEqualTo(EnteredValueSource.USER_LAST_ENTERED);
//    }

    @Test
    void testSetOutputRowEQ() {
        loadScheme();
        applyMutation(new ReactionOutputMutation.SetOutputRowEQ(output1.getAnchor(), "2.0", null));
        assertThat(output1.getEq().getValue()).isEqualTo(2.0);
    }

    @Test
    void testSetOutputHealthHazards() {
        loadScheme();
        addOutputSample();
        DictionaryItemRef healthHazard = dictionaryClient.getDictionary(BuiltInDictionary.HEALTH_HAZARD).getFirst();
        applyMutation(new ReactionOutputSampleMutation.SetOutputHealthHazards(output1Sample1.getAnchor(), List.of(healthHazard)));
        assertThat(output1Sample1.getHealthHazards()).containsExactly(healthHazard);
    }

    @Test
    void testSetOutputActualMol() {
        loadScheme();
        addOutputSample();
        applyMutation(new ReactionOutputSampleMutation.SetOutputActualMol(output1Sample1.getAnchor(), "10.0", MolUnit.MMOL, null));
        assertThat(output1Sample1.getActualMol().getValue()).isEqualTo(10.0);
        assertThat(output1Sample1.getActualMol().getUnit()).isEqualTo(MolUnit.MMOL);
    }

    @Test
    void testSetOutputActualWeight() {
        loadScheme();
        addOutputSample();
        applyMutation(new ReactionOutputSampleMutation.SetOutputActualWeight(output1Sample1.getAnchor(), "10.0", WeightUnit.G, null));
        assertThat(output1Sample1.getActualWeight().getValue()).isEqualTo(10.0);
        assertThat(output1Sample1.getActualWeight().getUnit()).isEqualTo(WeightUnit.G);
    }

    @Test
    void testRegisterSample() {
        loadScheme();
        addOutputSample();
        applyMutation(new ReactionOutputSampleMutation.RegisterSample(output1Sample1.getAnchor()), false); // register sample is not undoable
        assertThat(output1Sample1.getRegistrationStatus()).isEqualTo(SampleRegistrationStatus.REGISTERED);
        assertThat(output1Sample1.getSampleId()).isNotNull();
        assertThat(output1Sample1.getStrCode()).isNotNull();
    }

    @Test
    void testSetOutputComponentState() {
        loadScheme();
        addOutputSample();
        DictionaryItemRef componentState = dictionaryClient.getDictionary(BuiltInDictionary.COMPONENT_STATE).getFirst();
        applyMutation(new ReactionOutputSampleMutation.SetOutputComponentState(output1Sample1.getAnchor(), componentState));
        assertThat(output1Sample1.getComponentState()).isEqualTo(componentState);
    }

    @Test
    void testSetOutputHandlingPrecautions() {
        loadScheme();
        addOutputSample();
        DictionaryItemRef handlingPrecautions = dictionaryClient.getDictionary(BuiltInDictionary.HANDLING_PRECAUTIONS).getFirst();
        applyMutation(new ReactionOutputSampleMutation.SetOutputHandlingPrecautions(output1Sample1.getAnchor(), List.of(handlingPrecautions)));
        assertThat(output1Sample1.getHandlingPrecautions()).containsExactly(handlingPrecautions);
    }

    @Test
    void testSetOutputCompoundProtection() {
        loadScheme();
        addOutputSample();
        DictionaryItemRef compoundProtection = dictionaryClient.getDictionary(BuiltInDictionary.COMPOUND_PROTECTION).getFirst();
        applyMutation(new ReactionOutputSampleMutation.SetOutputCompoundProtection(output1Sample1.getAnchor(), List.of(compoundProtection)));
        assertThat(output1Sample1.getCompoundProtection()).containsExactly(compoundProtection);
    }

    @Test
    void testSetOutputStorageInstructions() {
        loadScheme();
        addOutputSample();
        DictionaryItemRef storageInstructions = dictionaryClient.getDictionary(BuiltInDictionary.STORAGE_INSTRUCTIONS).getFirst();
        applyMutation(new ReactionOutputSampleMutation.SetOutputStorageInstructions(output1Sample1.getAnchor(), List.of(storageInstructions)));
        assertThat(output1Sample1.getStorageInstructions()).containsExactly(storageInstructions);
    }

    @Test
    void testSetOutputSolubilityInSolvents() {
        loadScheme();
        addOutputSample();
        DictionaryItemRef solvent = dictionaryClient.getDictionary(BuiltInDictionary.SOLVENT).getFirst();
        SolubidityInSolvent solubidityInSolvent = new SolubidityInSolvent.Quantitative(solvent, "comment", ComparisonOperator.EQUALS, 10.0, DensityUnit.G_ML);
        applyMutation(new ReactionOutputSampleMutation.SetOutputSolubilityInSolvents(output1Sample1.getAnchor(), List.of(solubidityInSolvent)));
        assertThat(output1Sample1.getSolubilityInSolvents()).containsExactly(solubidityInSolvent);
    }

    @Test
    void testSetOutputResidualSolvents() {
        loadScheme();
        addOutputSample();
        DictionaryItemRef solvent = dictionaryClient.getDictionary(BuiltInDictionary.SOLVENT).getFirst();
        ResidualSolvent residualSolvent = new ResidualSolvent(solvent, 10.0, "comment");
        applyMutation(new ReactionOutputSampleMutation.SetOutputResidualSolvents(output1Sample1.getAnchor(), List.of(residualSolvent)));
        assertThat(output1Sample1.getResidualSolvents()).containsExactly(residualSolvent);
    }

    @Test
    void testSetOutputMeltingPoint() {
        loadScheme();
        addOutputSample();
        MeltingPoint meltingPoint = new MeltingPoint(-10.0, 10.0, "comment");
        applyMutation(new ReactionOutputSampleMutation.SetOutputMeltingPoint(output1Sample1.getAnchor(), meltingPoint));
        assertThat(output1Sample1.getMeltingPoint()).isEqualTo(meltingPoint);
    }

    @Test
    void testSetOutputPurityCalculations() {
        loadScheme();
        addOutputSample();
        PurityCalculation purityCalculation = new PurityCalculation(PurityCalculationType.MS, ComparisonOperator.EQUALS, 0.1, "comment");
        applyMutation(new ReactionOutputSampleMutation.SetOutputPurityCalculations(output1Sample1.getAnchor(), List.of(purityCalculation)));
        assertThat(output1Sample1.getPurityCalculations()).containsExactly(purityCalculation);
    }

    @Test
    void testSetOutputExternalSupplier() {
        loadScheme();
        addOutputSample();
        DictionaryItemRef supplier = dictionaryClient.getDictionary(BuiltInDictionary.EXTERNAL_SUPPLIER).getFirst();
        ExternalSupplier externalSupplier = new ExternalSupplier(supplier, "1111");
        applyMutation(new ReactionOutputSampleMutation.SetOutputExternalSupplier(output1Sample1.getAnchor(), externalSupplier));
        assertThat(output1Sample1.getExternalSupplier()).isEqualTo(externalSupplier);
    }

    @Test
    void testSetOutputSourceAndSourceDetails() {
        loadScheme();
        addOutputSample();
        DictionaryItemRef source = dictionaryClient.getDictionary(BuiltInDictionary.SAMPLE_SOURCE).getFirst();
        applyMutation(new ReactionOutputSampleMutation.SetOutputSource(output1Sample1.getAnchor(), source));
        assertThat(output1Sample1.getSource()).isEqualTo(source);
        DictionaryItemRef sourceDetails = dictionaryClient.getDictionary(BuiltInDictionary.SAMPLE_SOURCE_DETAILS).getFirst();
        applyMutation(new ReactionOutputSampleMutation.SetOutputSourceDetails(output1Sample1.getAnchor(), sourceDetails));
        assertThat(output1Sample1.getSourceDetails()).isEqualTo(sourceDetails);
    }

    @Test
    void testSetOutputBatchComment() {
        loadScheme();
        addOutputSample();
        applyMutation(new ReactionOutputSampleMutation.SetOutputBatchComment(output1Sample1.getAnchor(), "batchComment"));
        assertThat(output1Sample1.getBatchComment()).isEqualTo("batchComment");
    }

    @Test
    void testSetOutputStructureComment() {
        loadScheme();
        addOutputSample();
        applyMutation(new ReactionOutputSampleMutation.SetOutputStructureComment(output1Sample1.getAnchor(), "structureComment"));
        assertThat(output1Sample1.getStructureComment()).isEqualTo("structureComment");
    }

    @Test
    void testSetOutputDensity() {
        loadScheme();
        addOutputSample();
        applyMutation(new ReactionOutputSampleMutation.SetOutputDensity(output1Sample1.getAnchor(), "10.0", DensityUnit.G_ML, null));
        assertThat(output1Sample1.getDensity().getValue()).isEqualTo(10.0);
        assertThat(output1Sample1.getDensity().getUnit()).isEqualTo(DensityUnit.G_ML);
    }

    @Test
    void testSetOutputMolarity() {
        loadScheme();
        addOutputSample();
        applyMutation(new ReactionOutputSampleMutation.SetOutputMolarity(output1Sample1.getAnchor(), "10.0", MolarityUnit.MM, null));
        assertThat(output1Sample1.getMolarity().getValue()).isEqualTo(10.0);
        assertThat(output1Sample1.getMolarity().getUnit()).isEqualTo(MolarityUnit.MM);
    }

    @Test
    void testSetOutputVolume() {
        loadScheme();
        addOutputSample();
        applyMutation(new ReactionOutputSampleMutation.SetOutputVolume(output1Sample1.getAnchor(), "10.0", VolumeUnit.ML, null));
        assertThat(output1Sample1.getVolume().getValue()).isEqualTo(10.0);
        assertThat(output1Sample1.getVolume().getUnit()).isEqualTo(VolumeUnit.ML);
    }

    @Test
    void testSetOutputPurity() {
        loadScheme();
        addOutputSample();
        applyMutation(new ReactionOutputSampleMutation.SetOutputPurity(output1Sample1.getAnchor(), "10", null));
        assertThat(output1Sample1.getPurity().getValue()).isEqualTo(10);
    }

    @Test
    void testRemoveProductSample() {
        loadScheme();
        addOutputSample();
        applyMutation(new ReactionOutputSampleMutation.RemoveProductSample(output1Sample1.getAnchor()));
        assertThat(output1.getSamples()).isEmpty();
    }

    @Test
    void testSetExperimentSignificantFigures() {
        loadScheme();
        applyMutation(new ReactionInputSampleMutation.SetInputWeight(input1Sample1.getAnchor(), "1234", WeightUnit.G, null));
        assertThat(experiment.getModel().getSignificantFigures()).isEqualTo(5);
        assertThat(input1Sample1.getWeight().getStringValue()).isEqualTo("1234");
        assertThat(input1Sample1.getMol().getStringValue()).isEqualTo("8.9343");
        applyMutation(new ExperimentMutation.SetExperimentSignificantFigures(3));
        assertThat(experiment.getModel().getSignificantFigures()).isEqualTo(3);
        assertThat(input1Sample1.getWeight().getStringValue()).isEqualTo("1234");
        assertThat(input1Sample1.getMol().getStringValue()).isEqualTo("8.93");
    }

    @Test
    void testSetBatchCreator() {
        applyMutation(new ExperimentMutation.SetBatchCreator(getMaggieUserRef()));
        assertThat(experiment.getBatchCreator()).isEqualTo(getMaggieUserRef());
    }

    private void loadScheme() {
        String rxnFile = new String(ModelUtil.loadResource(getClass(), "/reaction.rxn"));
        applyMutation(new ReactionMutation.SetScheme(reaction.getAnchor(), rxnFile));
    }

    private ReactionMutation.ResolveInputs prepareResolveInputs() {
        return MutationsTestUtil.prepareResolveInputs(experiment, reaction.getAnchor(), experimentClient, compoundClient);
    }

    private void addOutputSample() {
        applyMutation(new ReactionOutputMutation.AddProductSample(output1.getAnchor()), false);
    }
}

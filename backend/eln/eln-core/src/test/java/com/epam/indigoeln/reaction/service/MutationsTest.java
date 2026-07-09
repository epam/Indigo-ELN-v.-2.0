package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.SampleSearchResult;
import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.*;
import com.epam.indigoeln.reaction.model.outputsample.*;
import com.epam.indigoeln.reaction.model.units.*;
import com.epam.indigoeln.test.ClientUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.validation.constraints.NotNull;
import one.util.streamex.IntStreamEx;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static com.epam.indigoeln.common.model.Paging.DEFAULT_PAGE_SIZE;
import static com.epam.indigoeln.common.util.ModelUtil.loadResource;
import static com.epam.indigoeln.compound.model.search.SearchCatalog.ELN;
import static com.epam.indigoeln.eln.test.EnteredValueAssert.assertThat;
import static com.epam.indigoeln.eln.test.ReactionInputAssert.assertThat;
import static com.epam.indigoeln.eln.test.ReactionInputSampleAssert.assertThat;
import static com.epam.indigoeln.eln.test.ReactionOutputSampleAssert.assertThat;
import static com.epam.indigoeln.reaction.model.units.DensityUnit.G_ML;
import static com.epam.indigoeln.reaction.model.units.MolUnit.MMOL;
import static com.epam.indigoeln.reaction.model.units.MolarityUnit.MM;
import static com.epam.indigoeln.reaction.model.units.VolumeUnit.ML;
import static com.epam.indigoeln.reaction.model.units.WeightUnit.G;
import static com.epam.indigoeln.test.ClientCallAssert.assertThatClientCall;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@QuarkusTest
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
public class MutationsTest extends MutationsTestBase {

    SaltCodeRef saltCode;
    StereoisomerCodeRef stereoisomerCode;

    @BeforeAll
    void beforeAll(@TempDir Path tempDir) {
        miscClient.loadCompoundsFromFileClient("compounds.sdf", loadResource(getClass(), "/Compound_000000001_000500000.1.sdf"));
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
    void testAddEmptyInputToEmptyReaction() {
        experiment.mutateAddEmptyInput();
        Assertions.assertThat(experiment.input(1)).isNotNull();
        assertThat(experiment.input(1).getCompound()).isInstanceOf(CompoundRef.Unknown.class);
        Assertions.assertThat(experiment.inputSample(1, 1)).isNotNull();
    }

    @Test
    void testAddInputToEmptyReaction() {
        SampleSearchResult samples = compoundClient.search(new FindSamplesRequest().withCatalogs(Set.of(ELN)), null, null, DEFAULT_PAGE_SIZE);
        experiment.mutate(new ReactionMutation.AddInput(experiment.reaction().getAnchor(), samples.items().getFirst().getId()));
        Assertions.assertThat(experiment.input(1)).isNotNull();
        assertThat(experiment.input(1).getCompound()).isInstanceOf(CompoundRef.Stored.class);
        Assertions.assertThat(experiment.inputSample(1, 1)).isNotNull();
        assertThat(experiment.inputSample(1, 1).getSampleId()).isEqualTo(samples.items().getFirst().getId());
    }

    @Test
    void testIncorrectAnchor() {
        assertThatClientCall(() -> {
            experimentClient.mutateExperimentModel4Raw(experiment.id(), 1, "{\"type\": \"AddEmptyInput\", \"anchor\": \"invalid\"}");
        }).isBadRequest("Cannot construct instance of `com.epam.indigoeln.reaction.model.ReactionAnchor");
    }

    @Test
    void testUnknownField() {
        assertThatClientCall(() -> {
            experimentClient.mutateExperimentModel4Raw(experiment.id(), 1, "{\"type\": \"AddEmptyInput\", \"anchor\": \"00000000-0000-0000-0000-000000000001\", \"unknownField\": 123}");
        }).isBadRequest("Unrecognized field \"unknownField\"");
    }

    @Test
    void testLoadReaction() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        Assertions.assertThat(experiment.input(1)).isNotNull();
        assertThat(experiment.input(1).getCompound()).isInstanceOf(CompoundRef.Virtual.class);
        Assertions.assertThat(experiment.inputSample(1, 1)).isNotNull();
        Assertions.assertThat(experiment.input(2)).isNotNull();
        assertThat(experiment.input(2).getCompound()).isInstanceOf(CompoundRef.Virtual.class);
        Assertions.assertThat(experiment.inputSample(2, 1)).isNotNull();
        assertThat(experiment.output(1)).isNotNull();
        assertThat(experiment.output(1).getCompound()).isInstanceOf(CompoundRef.Virtual.class);
    }

//    @Test // duplicate compounds are currently restricted
    void testLoadReactionUpdated() {
        // A + B + A => P + R
        experiment.mutateSetSchemeFromResource("/reaction-with-duplicates.rxn");
        InputAnchor a1 = experiment.input(1).getAnchor();
        InputAnchor b = experiment.input(2).getAnchor();
        InputAnchor a2 = experiment.input(3).getAnchor();
        OutputAnchor p = experiment.output(1).getAnchor();
        OutputAnchor r = experiment.output(2).getAnchor();
        // A + B + C + A + A => R + P
        experiment.mutateSetSchemeFromResource("/reaction-with-duplicates-updated.rxn");
        assertThat(experiment.input(1).getAnchor()).isEqualTo(a1);
        assertThat(experiment.input(2).getAnchor()).isEqualTo(b);
        assertThat(experiment.input(4).getAnchor()).isEqualTo(a2);
        assertThat(experiment.output(1).getAnchor()).isEqualTo(r);
        assertThat(experiment.output(2).getAnchor()).isEqualTo(p);
    }

    @Test
    void testLoadSameScheme() {
        experiment.mutateSetSchemeFromResource("/reaction2.rxn");
        experiment.mutateSetSchemeFromResource("/reaction2.rxn");
    }

    @Test
    void testResolveInputs() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateResolveInputs();
        assertThat(experiment.input(1).getCompound()).isInstanceOf(CompoundRef.Stored.class);
        assertThat(experiment.inputSample(1, 1).getSampleId()).isNotNull();
        assertThat(experiment.lastMutationResponse().getReactionImages()).containsOnlyKeys(experiment.reaction().getAnchor());
    }

    @Test
    void testRemoveInputRow() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        InputAnchor removedAnchor = experiment.input(1).getAnchor();
        experiment.mutate(new ReactionInputMutation.RemoveInputRow(removedAnchor));
        assertThat(experiment.reaction().getInputs()).hasSize(1);
        assertThat(experiment.input(1).getAnchor()).isNotEqualTo(removedAnchor);
    }

    @Test
    void testRemoveInput() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        @NotNull InputSampleAnchor removedAnchor = experiment.inputSample(1, 1).getAnchor();
        assertThat(experiment.reaction().getInputs()).hasSize(2);
        experiment.mutate(new ReactionInputSampleMutation.RemoveInput(removedAnchor));

        assertThat(experiment.reaction().getInputs()).hasSize(1);
    }

    @Test
    void testSetInputRowEQ() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateSetInputRowEQ(1, "2");
        assertThat(experiment.input(1).getEq()).hasValue(2).hasStringValue("2");
    }

    @Test
    void testUnsetInputRowEq() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateSetInputWeight(1, 1, "100", G);
        experiment.mutateSetInputWeight(2, 1, "200", G);
        experiment.mutateSetInputRowEQ(2, "2");
        assertThat(experiment.input(2)).hasEq(2.0);
        experiment.mutateSetInputWeight(1, 1, null, null);
        experiment.mutateSetInputRowEQ(2, null);
        assertThat(experiment.input(2)).hasEq(1.0);
    }

    @Test
    void testUnsetInputRowEq2() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutate(new ReactionInputSampleMutation.SetInputWeight(experiment.inputSample(1, 1).getAnchor(), "100.0", G), false);
        experiment.mutate(new ReactionInputMutation.SetInputRowEQ(experiment.input(2).getAnchor(), "2"), false);
        experiment.mutate(new ReactionInputSampleMutation.SetInputWeight(experiment.inputSample(1, 1).getAnchor(), "10", G), false);
        experiment.mutate(new ReactionInputSampleMutation.SetInputWeight(experiment.inputSample(2, 1).getAnchor(), "20", G), false);
        experiment.mutate(new ReactionInputMutation.SetInputRowEQ(experiment.input(2).getAnchor(), null), false);
        experiment.mutate(new ReactionInputSampleMutation.SetInputWeight(experiment.inputSample(1, 1).getAnchor(), "10.20", G), false);
        experiment.mutate(new ReactionInputMutation.SetInputRowEQ(experiment.input(1).getAnchor(), "1"), false);
        experiment.mutate(new ReactionInputSampleMutation.SetInputWeight(experiment.inputSample(1, 1).getAnchor(), null, null), false);
    }

    @Test
    void testSetInputRowSaltCodeAndEQ() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutate(new ReactionInputMutation.SetInputRowSaltCode(experiment.input(1).getAnchor(), saltCode));
        assertThat(experiment.input(1).getCompound()).isInstanceOf(CompoundRef.Virtual.class);
        assertThat(experiment.input(1).getCompound().getSaltCode()).isEqualTo(saltCode);
        experiment.mutate(new ReactionInputMutation.SetInputRowSaltEQ(experiment.input(1).getAnchor(), 2.0));
        assertThat(experiment.input(1).getCompound().getSaltCode()).isEqualTo(saltCode);
        assertThat(experiment.input(1).getCompound().getSaltEQ()).isCloseTo(2.0, Offset.offset(1e-6));
    }

    @Test
    void testSetInputRowStereoisomerCode() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutate(new ReactionInputMutation.SetInputCompoundStereoisomerCode(experiment.input(1).getAnchor(), stereoisomerCode));
        assertThat(experiment.input(1).getCompound()).isInstanceOf(CompoundRef.Virtual.class);
        assertThat(experiment.input(1).getCompound().getStereoisomerCode()).isEqualTo(stereoisomerCode);
    }

    @Test
    void testSetInputCompoundMolWeight() {
        experiment.mutateAddEmptyInput();
        experiment.mutate(new ReactionInputMutation.SetInputCompoundMolWeight(experiment.input(1).getAnchor(), "100.0"));
        assertThat(experiment.input(1).getCompound().getMolWeight()).hasValue(100).isUserEntered(experiment.revision());
    }

    @Test
    void testSetInputRowLimiting() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateSetInputRowLimiting(2);
        assertThat(experiment.input(1).isLimiting()).isFalse();
        assertThat(experiment.input(2).isLimiting()).isTrue();
    }

    @Test
    void testSetInputRowChemicalName() {
        experiment.mutateAddEmptyInput();
        experiment.mutate(new ReactionInputMutation.SetInputRowChemicalName(experiment.input(1).getAnchor(), "newChemicalName"));
        assertThat(experiment.input(1).getChemicalName()).isEqualTo("newChemicalName");
    }

    @Test
    void testSetInputRowMol() {
        experiment.mutateAddEmptyInput();
        experiment.mutateSetInputRowMol(1, "10.0", MMOL);
        assertThat(experiment.input(1)).hasMol(10, MMOL);
    }

    @Test
    void testSetInputRowRole() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateSetInputRowRole(1, ReactionRole.CATALYST);
        assertThat(experiment.input(1).getRole()).isEqualTo(ReactionRole.CATALYST);
    }

    @Test
    void testSetInputRowRoleAndBack() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateSetInputRowRole(1, ReactionRole.CATALYST);
        experiment.mutateSetInputRowRole(1, ReactionRole.REAGENT);
        experiment.mutateSetInputRowRole(1, ReactionRole.REACTANT);
        assertThat(experiment.input(1).getRole()).isEqualTo(ReactionRole.REACTANT);
    }

    @Test
    void testSetInputMol() {
        experiment.mutateAddEmptyInput();
        experiment.mutateSetInputMol(1, 1, "10.0", MolUnit.MMOL);
        assertThat(experiment.inputSample(1, 1)).hasMol(10, MMOL);
        assertThat(experiment.input(1)).hasMol(10, MMOL);
    }

    @Test
    void testSetInputWeight() {
        experiment.mutateAddEmptyInput();
        experiment.mutateSetInputWeight(1, 1, "10.0", G);
        assertThat(experiment.inputSample(1, 1)).hasWeight(10, G);
    }

    @Test
    void testSetInputDensity() {
        experiment.mutateAddEmptyInput();
        experiment.mutateSetInputDensity(1, 1, "10.0", G_ML);
        assertThat(experiment.inputSample(1, 1)).hasDensity(10);
    }

    @Test
    void testSetInputMolarity() {
        experiment.mutateAddEmptyInput();
        experiment.mutateSetInputMolarity(1, 1, "10.0", MM);
        assertThat(experiment.inputSample(1, 1)).hasMolarity(10, MM);
    }

    @Test
    void testSetInputVolume() {
        experiment.mutateAddEmptyInput();
        experiment.mutateSetInputVolume(1, 1, "10.0", ML);
        assertThat(experiment.inputSample(1, 1)).hasVolume(10, ML);
    }

    @Test
    void testSetInputPurity() {
        experiment.mutateAddEmptyInput();
        experiment.mutateSetInputPurity(1, 1, "10");
        assertThat(experiment.inputSample(1, 1)).hasPurity(10);
    }

    @Test
    void testUnsetInputPurity() {
        experiment.mutateAddEmptyInput();
        experiment.mutateSetInputPurity(1, 1, "10");
        assertThat(experiment.inputSample(1, 1)).hasPurity(10);
        experiment.mutateSetInputPurity(1, 1, null);
        assertThat(experiment.inputSample(1, 1)).hasPurity(100);
    }

    @Test
    void testSetInputHealthHazards() {
        experiment.mutateAddEmptyInput();
        HealthHazardRef healthHazard = dictionaryClient.getFirst(BuiltInDictionary.HEALTH_HAZARD);
        experiment.mutate(new ReactionInputSampleMutation.SetInputHealthHazards(experiment.inputSample(1, 1).getAnchor(), List.of(healthHazard)));
        assertThat(experiment.inputSample(1, 1).getHealthHazards()).containsExactly(healthHazard);
    }

    @Test
    void testSetInputComment() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateResolveInputs();
        experiment.mutate(new ReactionInputSampleMutation.SetInputComment(experiment.inputSample(1, 1).getAnchor(), "newComment"));
        assertThat(experiment.inputSample(1, 1).getComment()).isEqualTo("newComment");
    }

    @Test
    void testAddProductSample() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateAddProductSample(1);
        Assertions.assertThat(experiment.outputSample(1, 1)).isNotNull();
    }

    @Test
    void testSetOutputRowType() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutate(new ReactionOutputMutation.SetOutputRowType(experiment.output(1).getAnchor(), ReactionOutputType.INTERMEDIATE));
        assertThat(experiment.output(1).getType()).isEqualTo(ReactionOutputType.INTERMEDIATE);
    }

    @Test
    void testSetOutputRowName() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutate(new ReactionOutputMutation.SetOutputRowName(experiment.output(1).getAnchor(), "newOutputName"));
        assertThat(experiment.output(1).getOutputName()).isEqualTo("newOutputName");
    }

    @Test
    void testSetOutputRowChemicalName() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutate(new ReactionOutputMutation.SetOutputRowChemicalName(experiment.output(1).getAnchor(), "newChemicalName"));
        assertThat(experiment.output(1).getChemicalName()).isEqualTo("newChemicalName");
    }

    @Test
    void testSetOutputRowSaltCodeAndEQ() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutate(new ReactionOutputMutation.SetOutputRowSaltCode(experiment.output(1).getAnchor(), saltCode));
        assertThat(experiment.output(1).getCompound()).isInstanceOf(CompoundRef.Virtual.class);
        assertThat(experiment.output(1).getCompound().getSaltCode()).isEqualTo(saltCode);
        experiment.mutate(new ReactionOutputMutation.SetOutputRowSaltEQ(experiment.output(1).getAnchor(), 2.0));
        assertThat(experiment.output(1).getCompound().getSaltCode()).isEqualTo(saltCode);
        assertThat(experiment.output(1).getCompound().getSaltEQ()).isCloseTo(2.0, Offset.offset(1e-6));
    }

    @Test
    void testSetOutputRowStereoisomerCode() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutate(new ReactionOutputMutation.SetOutputCompoundStereoisomerCode(experiment.output(1).getAnchor(), stereoisomerCode));
        assertThat(experiment.output(1).getCompound()).isInstanceOf(CompoundRef.Virtual.class);
        assertThat(experiment.output(1).getCompound().getStereoisomerCode()).isEqualTo(stereoisomerCode);
    }

    @Test
    void testSetOutputCompoundMolWeight() {
        experiment.mutateAddNoProductSample();
        experiment.mutate(new ReactionOutputMutation.SetOutputCompoundMolWeight(experiment.output(1).getAnchor(), "100.0"));
        assertThat(experiment.output(1).getCompound().getMolWeight()).hasValue(100).isUserEntered();
    }

    @Test
    void testSetOutputRowEQ() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutate(new ReactionOutputMutation.SetOutputRowEQ(experiment.output(1).getAnchor(), "2.0"));
        assertThat(experiment.output(1).getEq()).hasValue(2).hasStringValue("2.0");
    }

    @Test
    void testSetOutputHealthHazards() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateAddProductSample(1);
        HealthHazardRef healthHazard = dictionaryClient.getFirst(BuiltInDictionary.HEALTH_HAZARD);
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputHealthHazards(experiment.outputSample(1, 1).getAnchor(), List.of(healthHazard)));
        assertThat(experiment.outputSample(1, 1).getHealthHazards()).containsExactly(healthHazard);
    }

    @Test
    void testSetOutputActualMol() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateAddProductSample(1);
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputActualMol(experiment.outputSample(1, 1).getAnchor(), "10.0", MolUnit.MMOL));
        assertThat(experiment.outputSample(1, 1)).hasActualMol(10, MMOL);
    }

    @Test
    void testSetOutputActualWeight() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateAddProductSample(1);
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputActualWeight(experiment.outputSample(1, 1).getAnchor(), "10.0", WeightUnit.G));
        assertThat(experiment.outputSample(1, 1)).hasActualWeight(10, G);
    }

    @Test
    void testRegisterSample() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateAddProductSample(1);
        experiment.mutate(new ReactionOutputSampleMutation.RegisterSample(experiment.outputSample(1, 1).getAnchor()), false); // register sample is not undoable
        assertThat(experiment.outputSample(1, 1).getRegistrationStatus()).isEqualTo(SampleRegistrationStatus.REGISTERED);
        assertThat(experiment.outputSample(1, 1).getSampleId()).isNotNull();
        assertThat(experiment.outputSample(1, 1).getStrCode()).isNotNull();
    }

    @Test
    void testSetOutputComponentState() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateAddProductSample(1);
        ComponentStateRef componentState = dictionaryClient.getFirst(BuiltInDictionary.COMPONENT_STATE);
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputComponentState(experiment.outputSample(1, 1).getAnchor(), componentState));
        assertThat(experiment.outputSample(1, 1).getComponentState()).isEqualTo(componentState);
    }

    @Test
    void testSetOutputHandlingPrecautions() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateAddProductSample(1);
        HandlingPrecautionsRef handlingPrecautions = dictionaryClient.getFirst(BuiltInDictionary.HANDLING_PRECAUTIONS);
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputHandlingPrecautions(experiment.outputSample(1, 1).getAnchor(), List.of(handlingPrecautions)));
        assertThat(experiment.outputSample(1, 1).getHandlingPrecautions()).containsExactly(handlingPrecautions);
    }

    @Test
    void testSetOutputCompoundProtection() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateAddProductSample(1);
        CompoundProtectionRef compoundProtection = dictionaryClient.getFirst(BuiltInDictionary.COMPOUND_PROTECTION);
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputCompoundProtection(experiment.outputSample(1, 1).getAnchor(), List.of(compoundProtection)));
        assertThat(experiment.outputSample(1, 1).getCompoundProtection()).containsExactly(compoundProtection);
    }

    @Test
    void testSetOutputStorageInstructions() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateAddProductSample(1);
        StorageInstructionsRef storageInstructions = dictionaryClient.getFirst(BuiltInDictionary.STORAGE_INSTRUCTIONS);
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputStorageInstructions(experiment.outputSample(1, 1).getAnchor(), List.of(storageInstructions)));
        assertThat(experiment.outputSample(1, 1).getStorageInstructions()).containsExactly(storageInstructions);
    }

    @Test
    void testSetOutputSolubilityInSolvents() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateAddProductSample(1);
        SolventRef solvent = dictionaryClient.getFirst(BuiltInDictionary.SOLVENT);
        SolubidityInSolvent solubidityInSolvent = new SolubidityInSolvent.Quantitative(solvent, "comment", ComparisonOperator.EQUALS, 10.0, G_ML);
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputSolubilityInSolvents(experiment.outputSample(1, 1).getAnchor(), List.of(solubidityInSolvent)));
        assertThat(experiment.outputSample(1, 1).getSolubilityInSolvents()).containsExactly(solubidityInSolvent);
    }

    @Test
    void testSetOutputResidualSolvents() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateAddProductSample(1);
        SolventRef solvent = dictionaryClient.getFirst(BuiltInDictionary.SOLVENT);
        ResidualSolvent residualSolvent = new ResidualSolvent(solvent, 10.0, "comment");
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputResidualSolvents(experiment.outputSample(1, 1).getAnchor(), List.of(residualSolvent)));
        assertThat(experiment.outputSample(1, 1).getResidualSolvents()).containsExactly(residualSolvent);
    }

    @Test
    void testSetOutputMeltingPoint() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateAddProductSample(1);
        MeltingPoint meltingPoint = new MeltingPoint(-10.0, 10.0, "comment");
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputMeltingPoint(experiment.outputSample(1, 1).getAnchor(), meltingPoint));
        assertThat(experiment.outputSample(1, 1).getMeltingPoint()).isEqualTo(meltingPoint);
    }

    @Test
    void testSetOutputPurityCalculations() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateAddProductSample(1);
        PurityCalculation purityCalculation = new PurityCalculation(PurityCalculationType.MS, ComparisonOperator.EQUALS, 0.1, "comment");
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputPurityCalculations(experiment.outputSample(1, 1).getAnchor(), List.of(purityCalculation)));
        assertThat(experiment.outputSample(1, 1).getPurityCalculations()).containsExactly(purityCalculation);
    }

    @Test
    void testSetOutputExternalSupplier() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateAddProductSample(1);
        ExternalSupplierRef supplier = dictionaryClient.getFirst(BuiltInDictionary.EXTERNAL_SUPPLIER);
        ExternalSupplier externalSupplier = new ExternalSupplier(supplier, "1111");
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputExternalSupplier(experiment.outputSample(1, 1).getAnchor(), externalSupplier));
        assertThat(experiment.outputSample(1, 1).getExternalSupplier()).isEqualTo(externalSupplier);
    }

    @Test
    void testSetOutputSourceAndSourceDetails() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateAddProductSample(1);
        SampleSourceRef source = dictionaryClient.getFirst(BuiltInDictionary.SAMPLE_SOURCE);
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputSource(experiment.outputSample(1, 1).getAnchor(), source));
        assertThat(experiment.outputSample(1, 1).getSource()).isEqualTo(source);
        SampleSourceDetailsRef sourceDetails = dictionaryClient.getFirst(BuiltInDictionary.SAMPLE_SOURCE_DETAILS);
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputSourceDetails(experiment.outputSample(1, 1).getAnchor(), sourceDetails));
        assertThat(experiment.outputSample(1, 1).getSourceDetails()).isEqualTo(sourceDetails);
    }

    @Test
    void testSetOutputBatchComment() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateAddProductSample(1);
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputBatchComment(experiment.outputSample(1, 1).getAnchor(), "batchComment"));
        assertThat(experiment.outputSample(1, 1).getBatchComment()).isEqualTo("batchComment");
    }

    @Test
    void testSetOutputStructureComment() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateAddProductSample(1);
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputStructureComment(experiment.outputSample(1, 1).getAnchor(), "structureComment"));
        assertThat(experiment.outputSample(1, 1).getStructureComment()).isEqualTo("structureComment");
    }

    @Test
    void testSetOutputDensity() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateAddProductSample(1);
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputDensity(experiment.outputSample(1, 1).getAnchor(), "10.0", DensityUnit.G_ML));
        assertThat(experiment.outputSample(1, 1)).hasDensity(10);
    }

    @Test
    void testSetOutputMolarity() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateAddProductSample(1);
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputMolarity(experiment.outputSample(1, 1).getAnchor(), "10.0", MolarityUnit.MM));
        assertThat(experiment.outputSample(1, 1)).hasMolarity(10, MM);
    }

    @Test
    void testSetOutputVolume() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateAddProductSample(1);
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputVolume(experiment.outputSample(1, 1).getAnchor(), "10.0", VolumeUnit.ML));
        assertThat(experiment.outputSample(1, 1)).hasVolume(10, ML);
    }

    @Test
    void testSetOutputPurity() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateAddProductSample(1);
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputPurity(experiment.outputSample(1, 1).getAnchor(), "10"));
        assertThat(experiment.outputSample(1, 1)).hasPurity(10);
    }

    @Test
    void testRemoveProductSample() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateAddProductSample(1);
        experiment.mutate(new ReactionOutputSampleMutation.RemoveProductSample(experiment.outputSample(1, 1).getAnchor()));
        assertThat(experiment.output(1).getSamples()).isEmpty();
    }

    @Test
    void testSetExperimentSignificantFigures() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");

        experiment.mutateSetInputWeight(1, 1, "1234", G);
        assertThat(experiment.model().getSignificantFigures()).isEqualTo(5);
        assertThat(experiment.inputSample(1, 1).getWeight()).hasStringValue("1234");
        assertThat(experiment.inputSample(1, 1).getMol()).hasStringValue("8.9341");

        experiment.mutate(new ExperimentMutation.SetExperimentSignificantFigures(3));
        assertThat(experiment.model().getSignificantFigures()).isEqualTo(3);
        assertThat(experiment.inputSample(1, 1).getWeight()).hasStringValue("1234");
        assertThat(experiment.inputSample(1, 1).getMol()).hasStringValue("8.93");
    }

    @Test
    void testSetBatchCreator() {
        experiment.mutate(new ExperimentMutation.SetBatchCreator(MAGGIE_USER_REF));
        assertThat(experiment.experiment().getBatchCreator()).isEqualTo(MAGGIE_USER_REF);
    }

    @Test
    void testSetOutputSaltCode() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateAddProductSample(1);
        experiment.mutateAddProductSample(1);
        assertThat(experiment.output(2).isIntended()).isTrue();
        OutputSampleAnchor anchor = experiment.outputSample(1, 1).getAnchor();
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputSaltCode(anchor, saltCode), false);
        assertThat(experiment.output(3).isIntended()).isFalse();
        assertThat(experiment.output(3).getSamples()).singleElement().satisfies(s -> {
            assertThat(s.getAnchor()).isEqualTo(anchor);
        });
    }

    @Test
    void testSetOutputSaltEQ() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateAddProductSample(1);
        experiment.mutateAddProductSample(1);
        assertThat(experiment.output(2).isIntended()).isTrue();
        OutputSampleAnchor anchor = experiment.outputSample(1, 1).getAnchor();
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputSaltCode(anchor, saltCode), false);
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputSaltEQ(anchor, 2.0), false);
        assertThat(experiment.output(3).isIntended()).isFalse();
        assertThat(experiment.output(3).getSamples()).singleElement().satisfies(s -> {
            assertThat(s.getAnchor()).isEqualTo(anchor);
        });
    }

    @Test
    void testSetOutputStereoisomerCode() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateAddProductSample(1);
        experiment.mutateAddProductSample(1);
        assertThat(experiment.output(2).isIntended()).isTrue();
        OutputSampleAnchor anchor = experiment.outputSample(1, 1).getAnchor();
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputStereoisomerCode(anchor, stereoisomerCode), false);
        assertThat(experiment.output(3).isIntended()).isFalse();
        assertThat(experiment.output(3).getSamples()).singleElement().satisfies(s -> {
            assertThat(s.getAnchor()).isEqualTo(anchor);
        });
    }

    @Test
    void testSetOutputMolfile() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateAddProductSample(1);
        experiment.mutateAddProductSample(1);
        assertThat(experiment.output(2).isIntended()).isTrue();
        OutputSampleAnchor anchor = experiment.outputSample(1, 1).getAnchor();
        String molfile = new String(ModelUtil.loadResource(getClass(), "/updated-molfile.mol"));
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputMolfile(anchor, molfile), false);
        assertThat(experiment.output(3).isIntended()).isFalse();
        assertThat(experiment.output(3).getSamples()).singleElement().satisfies(s -> {
            assertThat(s.getAnchor()).isEqualTo(anchor);
        });
    }

    @Test
    void testConflicts() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateSetInputWeight(1, 1, "100", G);
        experiment.mutateSetInputWeight(2, 1, "200", G);
        experiment.mutateSetInputRowEQ(1, "1");
        experiment.mutateSetInputRowEQ(2, "2");
        assertThat(experiment.inputSample(2, 1).getWeight()).isOverwritten();

        experiment.mutateSetInputVolume(2, 1, "2", ML);
        assertThat(experiment.inputSample(2, 1).getWeight()).isNotOverwritten();
    }

    @Test
    void testUpdateNonLimitingInput() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutate(new ReactionInputSampleMutation.SetInputWeight(experiment.inputSample(1, 1).getAnchor(), "100", G), false);
        experiment.mutate(new ReactionInputSampleMutation.SetInputWeight(experiment.inputSample(2, 1).getAnchor(), "200", G), false);
    }

    @Test
    void testCannotHaveDuplicateMoleculesInScheme() {
        assertThatClientCall(() -> {
            experiment.mutateSetSchemeFromResource("/duplicate-input.rxn");
        }).isBadRequest("Reaction contains duplicate input compounds");
    }

    @Test
    void testImportSDF() {
        experimentClient.importSDF(experiment.id(), experiment.reaction().getAnchor(), ClientUtil.createFileUpload("file.sdf", loadResource(getClass(), "/Compound_000000001_000500000.1.sdf")));
    }

    @Test
    void testAddSampleAndMakeItIntended() {
        experiment.mutate(new ReactionMutation.AddNoProductSample(experiment.reaction().getAnchor()));
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputMolfile(experiment.outputSample(1, 1).getAnchor(), ModelUtil.loadResourceAsString(getClass(), "/ring-substructure.mol")));
        experiment.mutate(new ReactionOutputMutation.SetOutputRowIntended(experiment.output(1).getAnchor(), true));
    }

    @Test
    void testEditingOnlyAllowedOnOpenExperiment() {
        experimentClient.completeExperiment(experiment.id());
        assertThatClientCall(() -> experiment.mutateAddEmptyInput())
                .isBadRequest("Experiment is COMPLETED, must be OPEN or REOPEN");
    }

    @Test
    void testParallelMutations() {
        ReactionAnchor reactionAnchor = experiment.reaction().getAnchor();
        assertThatNoException().isThrownBy(() -> {
            IntStreamEx.range(4).parallel().map(i -> {
                withUser(JOHN_USERNAME, () -> {
                    experimentClient.mutateExperimentModel(experiment.id(), 1, false, new ReactionMutation.AddNoProductSample(reactionAnchor));
                    experiment.mutate(new ReactionMutation.AddNoProductSample(reactionAnchor));
                });
                return 1;
            }).toArray();
        });
    }
}

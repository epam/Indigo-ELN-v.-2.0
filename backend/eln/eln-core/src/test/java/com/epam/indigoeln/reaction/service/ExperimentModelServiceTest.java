package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.SampleSearchResult;
import com.epam.indigoeln.compound.model.search.SearchCatalog;
import com.epam.indigoeln.compound.model.search.TextSearch;
import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.*;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.epam.indigoeln.common.util.ModelUtil.loadResource;
import static com.epam.indigoeln.eln.model.BuiltInDictionary.SALT_CODE;
import static com.epam.indigoeln.eln.model.BuiltInDictionary.THERAPEUTIC_AREA;
import static com.epam.indigoeln.eln.test.ReactionInputAssert.assertThat;
import static com.epam.indigoeln.eln.test.ReactionInputSampleAssert.assertThat;
import static com.epam.indigoeln.eln.test.ReactionOutputSampleAssert.assertThat;
import static com.epam.indigoeln.reaction.model.units.WeightUnit.G;
import static com.epam.indigoeln.reaction.model.units.WeightUnit.KG;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ExperimentModelServiceTest extends MutationsTestBase {

    @BeforeAll
    void setUpClass() {
        miscClient.loadCompoundsFromFileClient("compounds.sdf", loadResource(getClass(), "/Compound_000000001_000500000.1.sdf"));
        withUser(JOHN_USERNAME, () -> initExperiment("ExperimentModelServiceTest"));
    }

    @Test
    @Order(100)
    void testLoadReaction() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        Assertions.assertThat(experiment.input(1)).isNotNull();
        assertThat(experiment.input(1).getCompound()).isInstanceOf(CompoundRef.Virtual.class);
        Assertions.assertThat(experiment.input(2)).isNotNull();
        assertThat(experiment.input(2).getCompound()).isInstanceOf(CompoundRef.Virtual.class);
        assertThat(experiment.output(1).getCompound()).isInstanceOf(CompoundRef.Virtual.class);
        assertThat(experiment.output(2).getCompound()).isInstanceOf(CompoundRef.Virtual.class);
    }

    @Test
    @Order(200)
    void testResolveInputs() {
        experiment.mutateResolveInputs();
        assertThat(experiment.input(1).getCompound()).isInstanceOf(CompoundRef.Stored.class);
        assertThat(experiment.inputSample(1, 1).getSampleId()).isNotNull();
        assertThat(experiment.lastMutationResponse().getReactionImages()).containsOnlyKeys(experiment.reaction().getAnchor());
    }

    @Test
    @Order(210)
    void testSetInputRowSaltCode() {
        SaltCodeRef saltCode = dictionaryClient.getNth(SALT_CODE, 1);
        experiment.mutate(new ReactionInputMutation.SetInputRowSaltCode(experiment.input(2).getAnchor(), saltCode));
        assertThat(experiment.input(2).getCompound()).isInstanceOf(CompoundRef.Virtual.class);
        assertThat(experiment.input(2).getCompound().getSaltCode()).isEqualTo(saltCode);
    }

    @Test
    @Order(211)
    void testSetInputRowSaltEQ() {
        experiment.mutate(new ReactionInputMutation.SetInputRowSaltEQ(experiment.input(2).getAnchor(), 2.0));
        assertThat(experiment.input(2).getCompound().getSaltEQ()).isCloseTo(2.0, Offset.offset(1e-6));
    }

    @Test
    @Order(250)
    void testSetInputRoleToCatalyst() {
        experiment.mutateSetInputRowRole(2, ReactionRole.CATALYST);
        assertThat(experiment.input(2).getRole()).isEqualTo(ReactionRole.CATALYST);
    }

    @Test
    @Order(251)
    void testSetInputRoleToSolvent() {
        experiment.mutateSetInputRowRole(2, ReactionRole.SOLVENT);
        assertThat(experiment.input(2).getRole()).isEqualTo(ReactionRole.SOLVENT);
    }

    @Test
    @Order(252)
    void testSetInputRoleBack() {
        experiment.mutateSetInputRowRole(2, ReactionRole.REACTANT);
        assertThat(experiment.input(2).getRole()).isEqualTo(ReactionRole.REACTANT);
    }

    @Test
    @Order(300)
    void testSelectSaltCode() {
        SaltCodeRef saltCode = dictionaryClient.getNth(BuiltInDictionary.SALT_CODE, 1);
        experiment.mutate(new ReactionOutputMutation.SetOutputRowSaltCode(experiment.output(1).getAnchor(), saltCode));
        assertThat(experiment.output(1).getCompound()).isInstanceOf(CompoundRef.Virtual.class);
        assertThat(experiment.output(1).getCompound().getSaltCode()).isEqualTo(saltCode);
    }

    @Test
    @Order(400)
    void testSelectSaltEQ() {
        experiment.mutate(new ReactionOutputMutation.SetOutputRowSaltEQ(experiment.output(1).getAnchor(), 0.5));
        assertThat(experiment.output(1).getCompound().getSaltEQ()).isCloseTo(0.5, Offset.offset(1e-6));
    }

    @Test
    @Order(500)
    void testSetInputWeight() {
        experiment.mutateSetInputWeight(1, 1, "100.0", G);
        assertThat(experiment.inputSample(1, 1)).hasWeight(100, G);
    }

    @Test
    @Order(501)
    void testSetInputWeightInKG() {
        experiment.mutateSetInputWeight(1, 1, "0.1", KG);
        assertThat(experiment.inputSample(1, 1)).hasWeight(0.1, KG);
    }

    @Test
    @Order(600)
    void testSetInputEQ() {
        experiment.mutateSetInputRowEQ(2, "2");
        assertThat(experiment.input(2)).hasEq(2.0);
    }

    @Test
    @Order(620)
    void testAddEmptyInput() {
        int inputCount = experiment.reaction().getInputs().size();
        experiment.mutateAddEmptyInput();
        assertThat(experiment.reaction().getInputs()).hasSize(inputCount + 1);
        assertThat(experiment.input(inputCount + 1).getCompound()).isInstanceOf(CompoundRef.Unknown.class);
    }

    @Test
    @Order(621)
    void testRemoveEmptyInput() {
        List<ReactionInput> inputs = experiment.reaction().getInputs();
        InputAnchor removedAnchor = inputs.getLast().getAnchor();
        experiment.mutate(new ReactionInputMutation.RemoveInputRow(removedAnchor));
        assertThat(experiment.reaction().getInputs()).hasSize(inputs.size() - 1);
        assertThat(experiment.reaction().getInputs()).extracting(ReactionInput::getAnchor).doesNotContain(removedAnchor);
    }

    @Test
    @Order(700)
    void testAddProductSample() {
        experiment.mutateAddProductSample(2);
        Assertions.assertThat(experiment.outputSample(2, 1)).isNotNull();
    }

    @Test
    @Order(800)
    void testSetOutputActualMol() {
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputActualMol(experiment.outputSample(2, 1).getAnchor(), "200.0", MolUnit.MMOL));
        assertThat(experiment.outputSample(2, 1)).hasActualMol(200, MolUnit.MMOL);
    }

    @Test
    @Order(900)
    void testSetOutputPurity() {
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputPurity(experiment.outputSample(2, 1).getAnchor(), "0.5"));
        assertThat(experiment.outputSample(2, 1)).hasPurity(0.5);
    }

    @Test
    @Order(1000)
    void testSetActualWeight() {
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputActualWeight(experiment.outputSample(2, 1).getAnchor(), "10.0", G));
        assertThat(experiment.outputSample(2, 1)).hasActualWeight(10, G);
    }

    @Test
    @Order(1100)
    void testRegisterSample() {
        experiment.mutate(new ReactionOutputSampleMutation.RegisterSample(experiment.outputSample(2, 1).getAnchor()), false);
        assertThat(experiment.outputSample(2, 1).getRegistrationStatus()).isEqualTo(SampleRegistrationStatus.REGISTERED);
        assertThat(experiment.outputSample(2, 1).getSampleId()).isNotNull();
        assertThat(experiment.outputSample(2, 1).getStrCode()).isNotNull();
    }

    @Test
    @Order(1101)
    void testAddAnotherOutputSample() {
        experiment.mutateAddProductSample(2);
        assertThat(experiment.output(2).getSamples()).hasSize(2);
    }

    @Test
    @Order(1102)
    void testRegisterAnotherSample() {
        experiment.mutate(new ReactionOutputSampleMutation.RegisterSample(experiment.outputSample(2, 2).getAnchor()), false);
        assertThat(experiment.outputSample(2, 2).getRegistrationStatus()).isEqualTo(SampleRegistrationStatus.REGISTERED);
        assertThat(experiment.outputSample(2, 2).getSampleId()).isNotNull();
    }


    @Test
    @Order(1300)
    void testAddInput() {
        SampleSearchResult foundSamples = compoundClient.search(new FindSamplesRequest()
                .withCatalogs(Set.of(SearchCatalog.ELN))
                .withMolecularFormula(new TextSearch.ExactSearch("C12H22N2O2"))
                , Paging.DEFAULT_PAGE_SIZE
        );
        assertThat(foundSamples.items()).isNotEmpty();
        int inputCount = experiment.reaction().getInputs().size();
        UUID sampleId = checkNotNull(foundSamples.items().getFirst().getId());
        experiment.mutate(new ReactionMutation.AddInput(experiment.reaction().getAnchor(), sampleId));
        assertThat(experiment.reaction().getInputs()).hasSize(inputCount + 1);
        assertThat(experiment.input(inputCount + 1).getCompound()).isInstanceOf(CompoundRef.Stored.class);
        assertThat(experiment.inputSample(inputCount + 1, 1).getSampleId()).isEqualTo(sampleId);
    }

    @Test
    @Order(1400)
    void testEditProperties() {
        TherapeuticAreaRef therapeuticArea = dictionaryClient.getFirst(THERAPEUTIC_AREA);
        ExperimentDetailsDTO experiment2 = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        experimentClient.editExperiment(experiment.id(), new ExperimentEditRequest(
                JsonNullable.of("new title"),
                JsonNullable.of(therapeuticArea),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.of(Set.of(experiment2.toRef())),
                JsonNullable.undefined(),
                JsonNullable.undefined()
        ));
        experiment.invalidate();
        assertThat(experiment.experiment().getTitle()).isEqualTo("new title");
        assertThat(experiment.experiment().getTherapeuticArea()).isEqualTo(therapeuticArea);
        assertThat(experiment.experiment().getLinkedExperiments()).containsExactly(experiment2.toRef());

        experimentClient.updateExperimentAccess(experiment.id(), AccessForm.of(LISA_USERNAME, AccessLevel.EDIT));
        experiment.invalidate();
        assertThat(experiment.experiment().getAcl())
                .filteredOn(entry -> LISA_USERNAME.equals(entry.getUsername()))
                .extracting(ACLEntryDTO::getLevel)
                .containsExactly(AccessLevel.EDIT);
    }
}

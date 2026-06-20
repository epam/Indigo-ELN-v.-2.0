package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.SampleSearchResult;
import com.epam.indigoeln.compound.model.search.SearchCatalog;
import com.epam.indigoeln.compound.model.search.TextSearch;
import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.model.AccessLevel;
import com.epam.indigoeln.eln.model.BuiltInDictionary;
import com.epam.indigoeln.eln.model.ExperimentDetailsDTO;
import com.epam.indigoeln.eln.model.ExperimentRequest;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionRole;
import com.epam.indigoeln.reaction.model.mutation.*;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.List;
import java.util.Set;

import static com.epam.indigoeln.common.util.ModelUtil.loadResource;
import static com.epam.indigoeln.eln.model.BuiltInDictionary.SALT_CODE;
import static com.epam.indigoeln.eln.model.BuiltInDictionary.THERAPEUTIC_AREA;
import static com.google.common.base.Preconditions.checkNotNull;

@QuarkusTest
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
public class ExperimentModelServiceTest extends MutationsTestBase {

    @BeforeAll
    void setUpClass() {
        miscClient.loadCompoundsFromFileClient("compounds.sdf", loadResource(getClass(), "/Compound_000000001_000500000.1.sdf"));
        withUser(JOHN_USERNAME, () -> {
            initExperiment("ExperimentModelServiceTest");
        });
    }

    @Test
    @Order(100)
    void testLoadReaction() {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
    }

    @Test
    @Order(200)
    void testResolveInputs() {
        experiment.mutateResolveInputs();
    }

    @Test
    @Order(210)
    void testSetInputRowSaltCode() {
        experiment.mutate(new ReactionInputMutation.SetInputRowSaltCode(experiment.input(2).getAnchor(), dictionaryClient.getNth(SALT_CODE, 1)));
    }

    @Test
    @Order(211)
    void testSetInputRowSaltEQ() {
        experiment.mutate(new ReactionInputMutation.SetInputRowSaltEQ(experiment.input(2).getAnchor(), 2.0));
    }

    @Test
    @Order(250)
    void testSetInputRoleToCatalyst() {
        experiment.mutateSetInputRowRole(2, ReactionRole.CATALYST);
    }

    @Test
    @Order(251)
    void testSetInputRoleToSolvent() {
        experiment.mutateSetInputRowRole(2, ReactionRole.SOLVENT);
    }

    @Test
    @Order(252)
    void testSetInputRoleBack() {
        experiment.mutateSetInputRowRole(2, ReactionRole.REACTANT);
    }

    @Test
    @Order(300)
    void testSelectSaltCode() {
        experiment.mutate(new ReactionOutputMutation.SetOutputRowSaltCode(experiment.output(1).getAnchor(), dictionaryClient.getNth(BuiltInDictionary.SALT_CODE, 1)));
    }

    @Test
    @Order(400)
    void testSelectSaltEQ() {
        experiment.mutate(new ReactionOutputMutation.SetOutputRowSaltEQ(experiment.output(1).getAnchor(), 0.5));
    }

    @Test
    @Order(500)
    void testSetInputWeight() {
        experiment.mutateSetInputWeight(1, 1, "100.0", WeightUnit.G);
    }

    @Test
    @Order(501)
    void testSetInputWeightInKG() {
        experiment.mutateSetInputWeight(1, 1, "0.1", WeightUnit.KG);
    }

    @Test
    @Order(600)
    void testSetInputEQ() {
        experiment.mutateSetInputRowEQ(2, "2");
    }

    @Test
    @Order(620)
    void testAddEmptyInput() {
        experiment.mutateAddEmptyInput();
    }

    @Test
    @Order(621)
    void testRemoveEmptyInput() {
        List<ReactionInput> inputs = experiment.reaction().getInputs();
        experiment.mutate(new ReactionInputMutation.RemoveInputRow(inputs.getLast().getAnchor()));
    }

    @Test
    @Order(700)
    void testAddProductSample() {
        experiment.mutateAddProductSample(2);
    }

    @Test
    @Order(800)
    void testSetOutputActualMol() {
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputActualMol(experiment.outputSample(2, 1).getAnchor(), "200.0", MolUnit.MMOL));
    }

    @Test
    @Order(900)
    void testSetOutputPurity() {
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputPurity(experiment.outputSample(2, 1).getAnchor(), "0.5"));
    }

    @Test
    @Order(1000)
    void testSetActualWeight() {
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputActualWeight(experiment.outputSample(2, 1).getAnchor(), "10.0", WeightUnit.G));
    }

    @Test
    @Order(1100)
    void testRegisterSample() {
        experiment.mutate(new ReactionOutputSampleMutation.RegisterSample(experiment.outputSample(2, 1).getAnchor()), false);
    }

    @Test
    @Order(1101)
    void testAddAnotherOutputSample() {
        experiment.mutateAddProductSample(2);
    }

    @Test
    @Order(1102)
    void testRegisterAnotherSample() {
        experiment.mutate(new ReactionOutputSampleMutation.RegisterSample(experiment.outputSample(2, 2).getAnchor()), false);
    }

//    @Test
//    @Order(1200)
//    void testProtectDictionaryItemsFromDeletion() {
//        applyMutation(new ReactionOutputSampleMutation.SetOutputHealthHazards(experiment.outputSample(2, 1).getAnchor(), List.of(healthHazard)));
//        assertThatClientCall(() -> dictionaryClient.removeDictionaryItem(HEALTH_HAZARD, healthHazard.getId()))
//                .isBadRequest("This word is selected in other inputs. Please deactivate the word to remove it from available options of the inputs");
//    }

    @Test
    @Order(1300)
    void testAddInput() {
        SampleSearchResult foundSamples = compoundClient.search(new FindSamplesRequest()
                .withCatalogs(Set.of(SearchCatalog.ELN))
                .withMolecularFormula(new TextSearch.ExactSearch("C12H22N2O2"))
                , null, null, Paging.DEFAULT_PAGE_SIZE
        );
        experiment.mutate(new ReactionMutation.AddInput(experiment.reaction().getAnchor(), checkNotNull(foundSamples.items().getFirst().getId())));
    }

    @Test
    @Order(1400)
    void testEditProperties() {
        ExperimentDetailsDTO experiment2 = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        experiment.mutate(new ExperimentMutation.EditExperimentAttributes(
                JsonNullable.of("new title"),
                JsonNullable.of(dictionaryClient.getFirst(THERAPEUTIC_AREA)),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.of(Set.of(experiment2.toRef())),
                JsonNullable.undefined(),
                JsonNullable.undefined()
        ));
        experiment.mutate(new ExperimentMutation.EditExperimentAccess(
                AccessForm.of(LISA_USERNAME, AccessLevel.EDIT)
        ), false);
    }
}

package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.SampleSearchResult;
import com.epam.indigoeln.compound.model.search.SearchCatalog;
import com.epam.indigoeln.compound.model.search.TextSearch;
import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.model.AccessLevel;
import com.epam.indigoeln.eln.model.ExperimentDetailsDTO;
import com.epam.indigoeln.eln.model.ExperimentRequest;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.ReactionRole;
import com.epam.indigoeln.reaction.model.mutation.*;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.epam.indigoeln.reaction.util.CalculationReportBuilder;
import com.epam.indigoeln.reaction.util.MutationsTestUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openapitools.jackson.nullable.JsonNullable;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static com.epam.indigoeln.common.util.ModelUtil.loadResource;
import static com.epam.indigoeln.eln.model.BuiltInDictionary.SALT_CODE;
import static com.epam.indigoeln.eln.model.BuiltInDictionary.THERAPEUTIC_AREA;
import static com.google.common.base.Preconditions.checkNotNull;

@QuarkusTest
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
public class ExperimentModelServiceTest extends MutationsTestBase {

    ReactionOutputSample output2Sample2;

    @BeforeAll
    void setUpClass(@TempDir Path tempDir) {
        miscClient.loadCompoundsFromFileClient("compounds.sdf", loadResource(getClass(), "/Compound_000000001_000500000.1.sdf"));
        withUser(JOHN_USERNAME, () -> {
            initExperiment("ExperimentModelServiceTest");
        });
        reportBuilder = new CalculationReportBuilder(new File("build/calculations.html"));
    }

    @AfterAll
    void tearDown() {
        reportBuilder.close();
    }

    @Override
    @SuppressWarnings({"DataFlowIssue", "ConstantValue"})
    protected void modelUpdated() {
        super.modelUpdated();
        output2Sample2 = output2 != null && output2.getSamples().size() >= 2 ? output2.getSamples().get(1) : null;
    }

    @Test
    @Order(100)
    void testLoadReaction() {
        String rxnFile = new String(ModelUtil.loadResource(getClass(), "/reaction.rxn"));
        applyMutation(new ReactionMutation.SetScheme(reaction.getAnchor(), rxnFile), false);
    }

    @Test
    @Order(200)
    void testResolveInputs() {
        ReactionMutation.ResolveInputs mutation = MutationsTestUtil.prepareResolveInputs(experiment, reaction.getAnchor(), experimentClient, compoundClient);
        applyMutation(mutation);
    }

    @Test
    @Order(210)
    void testSetInputRowSaltCode() {
        ReactionInputMutation.SetInputRowSaltCode mutation = new ReactionInputMutation.SetInputRowSaltCode(input2.getAnchor(), dictionaryClient.getNth(SALT_CODE, 1));
        applyMutation(mutation);
    }

    @Test
    @Order(211)
    void testSetInputRowSaltEQ() {
        ReactionInputMutation.SetInputRowSaltEQ mutation = new ReactionInputMutation.SetInputRowSaltEQ(input2.getAnchor(), 2.0);
        applyMutation(mutation);
    }

    @Test
    @Order(250)
    void testSetInputRoleToCatalyst() {
        ReactionInputMutation.SetInputRowRole mutation = new ReactionInputMutation.SetInputRowRole(input2.getAnchor(), ReactionRole.CATALYST);
        applyMutation(mutation);
    }

    @Test
    @Order(251)
    void testSetInputRoleToSolvent() {
        ReactionInputMutation.SetInputRowRole mutation = new ReactionInputMutation.SetInputRowRole(input2.getAnchor(), ReactionRole.SOLVENT);
        applyMutation(mutation);
    }

    @Test
    @Order(252)
    void testSetInputRoleBack() {
        ReactionInputMutation.SetInputRowRole mutation = new ReactionInputMutation.SetInputRowRole(input2.getAnchor(), ReactionRole.REACTANT);
        applyMutation(mutation);
    }

    @Test
    @Order(300)
    void testSelectSaltCode() {
        applyMutation(new ReactionOutputMutation.SetOutputRowSaltCode(output1.getAnchor(), dictionaryClient.getNth(SALT_CODE, 1)));
    }

    @Test
    @Order(400)
    void testSelectSaltEQ() {
        applyMutation(new ReactionOutputMutation.SetOutputRowSaltEQ(output1.getAnchor(), 0.5));
    }

    @Test
    @Order(500)
    void testSetInputWeight() {
        applyMutation(new ReactionInputSampleMutation.SetInputWeight(input1Sample1.getAnchor(), "100.0", WeightUnit.G));
    }

    @Test
    @Order(501)
    void testSetInputWeightInKG() {
        applyMutation(new ReactionInputSampleMutation.SetInputWeight(input1Sample1.getAnchor(), "0.1", WeightUnit.KG));
    }

    @Test
    @Order(600)
    void testSetInputEQ() {
        applyMutation(new ReactionInputMutation.SetInputRowEQ(input2.getAnchor(), "2"));
    }

    @Test
    @Order(620)
    void testAddEmptyInput() {
        applyMutation(new ReactionMutation.AddEmptyInput(reaction.getAnchor()));
    }

    @Test
    @Order(621)
    void testRemoveEmptyInput() {
        List<ReactionInput> inputs = experiment.getModel().getReactions().getFirst().getInputs();
        applyMutation(new ReactionInputMutation.RemoveInputRow(inputs.getLast().getAnchor()));
    }

    @Test
    @Order(700)
    void testAddProductSample() {
        applyMutation(new ReactionOutputMutation.AddProductSample(output2.getAnchor()));
    }

    @Test
    @Order(800)
    void testSetOutputActualMol() {
        applyMutation(new ReactionOutputSampleMutation.SetOutputActualMol(output2Sample1.getAnchor(), "200.0", MolUnit.MMOL));
    }

    @Test
    @Order(900)
    void testSetOutputPurity() {
        applyMutation(new ReactionOutputSampleMutation.SetOutputPurity(output2Sample1.getAnchor(), "0.5"));
    }

    @Test
    @Order(1000)
    void testSetActualWeight() {
        applyMutation(new ReactionOutputSampleMutation.SetOutputActualWeight(output2Sample1.getAnchor(), "10.0", WeightUnit.G));
    }

    @Test
    @Order(1100)
    void testRegisterSample() {
        applyMutation(new ReactionOutputSampleMutation.RegisterSample(output2Sample1.getAnchor()), false);
    }

    @Test
    @Order(1101)
    void testAddAnotherOutputSample() {
        applyMutation(new ReactionOutputMutation.AddProductSample(output2.getAnchor()));
    }

    @Test
    @Order(1102)
    void testRegisterAnotherSample() {
        applyMutation(new ReactionOutputSampleMutation.RegisterSample(output2Sample2.getAnchor()), false);
    }

//    @Test
//    @Order(1200)
//    void testProtectDictionaryItemsFromDeletion() {
//        applyMutation(new ReactionOutputSampleMutation.SetOutputHealthHazards(output2Sample1.getAnchor(), List.of(healthHazard)));
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
        applyMutation(new ReactionMutation.AddInput(reaction.getAnchor(), checkNotNull(foundSamples.items().getFirst().getId())));
    }

    @Test
    @Order(1400)
    void testEditProperties() {
        ExperimentDetailsDTO experiment2 = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        applyMutation(new ExperimentMutation.EditExperimentAttributes(
                JsonNullable.of("new title"),
                JsonNullable.of(dictionaryClient.getFirst(THERAPEUTIC_AREA)),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.of(Set.of(experiment2.toRef())),
                JsonNullable.undefined(),
                JsonNullable.undefined()
        ));
        applyMutation(new ExperimentMutation.EditExperimentAccess(
                AccessForm.of(LISA_USERNAME, AccessLevel.EDIT)
        ), false);
    }
}

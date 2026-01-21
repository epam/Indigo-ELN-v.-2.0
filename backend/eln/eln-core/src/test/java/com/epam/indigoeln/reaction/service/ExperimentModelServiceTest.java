package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.compound.model.FindSamplesRequest;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.TextSearch;
import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.BuiltInDictionary;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.Page;
import com.epam.indigoeln.eln.model.Paging;
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

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static com.epam.indigoeln.common.util.ModelUtil.loadResource;

@QuarkusTest
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
@SuppressWarnings("SequencedCollectionMethodCanBeUsed")
public class ExperimentModelServiceTest extends MutationsTestBase {

    DictionaryItemRef healthHazard;

    ReactionOutputSample output2Sample2;

    @BeforeAll
    void setUpClass(@TempDir Path tempDir) {
        miscClient.loadCompoundsFromFileClient("compounds.sdf", tempDir, loadResource(getClass(), "/Compound_000000001_000500000.1.sdf"));
        healthHazard = dictionaryClient.getDictionary(BuiltInDictionary.HEALTH_HAZARD).getFirst();
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
        applyMutation(experimentClient.analyzeScheme(experiment.getId(), reaction.getAnchor(), rxnFile));
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
        ReactionInputMutation.SetInputRowSaltCode mutation = new ReactionInputMutation.SetInputRowSaltCode(input2.getAnchor(), dictionaryClient.getSaltCodes().get(1));
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
        applyMutation(new ReactionOutputMutation.SetOutputRowSaltCode(output1.getAnchor(), dictionaryClient.getSaltCodes().get(1)));
    }

    @Test
    @Order(400)
    void testSelectSaltEQ() {
        applyMutation(new ReactionOutputMutation.SetOutputRowSaltEQ(output1.getAnchor(), 0.5));
    }

    @Test
    @Order(500)
    void testSetInputWeight() {
        applyMutation(new ReactionInputSampleMutation.SetInputWeight(input1Sample1.getAnchor(), 100.0, WeightUnit.G, null));
    }

    @Test
    @Order(501)
    void testSetInputWeightInKG() {
        applyMutation(new ReactionInputSampleMutation.SetInputWeight(input1Sample1.getAnchor(), 0.1, WeightUnit.KG, null));
    }

    @Test
    @Order(600)
    void testSetInputEQ() {
        applyMutation(new ReactionInputMutation.SetInputRowEQ(input2.getAnchor(), 2.0, null));
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
        applyMutation(new ReactionInputMutation.RemoveInput(inputs.getLast().getAnchor()));
    }

    @Test
    @Order(700)
    void testAddProductSample() {
        applyMutation(new ReactionOutputMutation.AddProductSample(output2.getAnchor()));
    }

    @Test
    @Order(800)
    void testSetOutputActualMol() {
        applyMutation(new ReactionOutputSampleMutation.SetOutputActualMol(output2Sample1.getAnchor(), 200.0, MolUnit.MMOL, null));
    }

    @Test
    @Order(900)
    void testSetOutputPurity() {
        applyMutation(new ReactionOutputSampleMutation.SetOutputPurity(output2Sample1.getAnchor(), 0.5, null));
    }

    @Test
    @Order(1000)
    void testSetActualWeight() {
        applyMutation(new ReactionOutputSampleMutation.SetOutputActualWeight(output2Sample1.getAnchor(), 10.0, WeightUnit.G, null));
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
//        assertThatClientCall(() -> dictionaryClient.removeDictionaryItem(BuiltInDictionary.HEALTH_HAZARD, healthHazard.getId()))
//                .isBadRequest("This word is selected in other inputs. Please deactivate the word to remove it from available options of the inputs");
//    }

    @Test
    @Order(1300)
    void testAddInput() {
        Page<SampleDTO> foundSamples = compoundClient.findSamples(new FindSamplesRequest()
                .withMolecularFormula(new TextSearch.ExactSearch("C12 H22 N2 O2"))
                , Paging.DEFAULT
        );
        applyMutation(new ReactionMutation.AddInput(reaction.getAnchor(), foundSamples.getItems().getFirst().getId()));
    }
}

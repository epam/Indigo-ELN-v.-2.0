package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.compound.model.FindSamplesRequest;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.StructureSearchType;
import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.api.MutateModelForm;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionRole;
import com.epam.indigoeln.reaction.model.mutation.*;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.epam.indigoeln.reaction.util.CalculationReportBuilder;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import lombok.SneakyThrows;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.common.util.ModelUtil.loadResource;

@QuarkusTest
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
@SuppressWarnings("SequencedCollectionMethodCanBeUsed")
public class ExperimentModelServiceTest extends ELNBaseTest {

    ExperimentDetailsDTO experiment;
    ExperimentModel model;
    CalculationReportBuilder reportBuilder;
    UUID reactionAnchor;
    UUID input1Anchor;
    UUID input1Sample1Anchor;
    UUID input2Anchor;
    UUID output1Anchor;
    UUID output2Anchor;
    UUID output2Sample1Anchor;
    UUID output2Sample2Anchor;

    byte @Nullable[] picture = null;

    @BeforeAll
    void setUp(@TempDir Path tempDir) throws Exception {
        reportBuilder = new CalculationReportBuilder(new File("calculations.html"));
        miscClient.loadCompoundsFromFileClient("compounds.sdf", tempDir, loadResource(getClass(), "/Compound_000000001_000500000.1.sdf"));
        System.out.println(model);
    }

    @AfterAll
    void tearDown() {
        reportBuilder.close();
    }

    @Test
    @Order(0)
    void testCreateExperiment() {
        ProjectDetailsDTO project = projectClient.createProject(new ProjectRequest("ExperimentModelServiceTest"));
        NotebookDetailsDTO notebook = notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
        experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        model = experimentClient.getExperimentModel(experiment.getId());
        reactionAnchor = model.getReactions().getFirst().getAnchor();
    }

    @Test
    @Order(100)
    void testLoadReaction() throws Exception {
        String molFile = new String(ModelUtil.loadResource(getClass(), "/reaction.rxn"));
        applyMutation(new ReactionMutation.SetScheme(reactionAnchor, molFile));
        input1Anchor = model.getReactions().getFirst().getInputs().get(0).getAnchor();
        input2Anchor = model.getReactions().getFirst().getInputs().get(1).getAnchor();
        output1Anchor = model.getReactions().getFirst().getOutputs().get(0).getAnchor();
        output2Anchor = model.getReactions().getFirst().getOutputs().get(1).getAnchor();
    }

    @Test
    @Order(200)
    void testResolveInputs() {
        ReactionMutation.ResolveInputs mutation = new ReactionMutation.ResolveInputs(reactionAnchor, new HashMap<>());
        for (ReactionInput input : model.getReactions().getFirst().getInputs()) {
            List<SampleDTO> samples = compoundClient.findSamples(new FindSamplesRequest()
                    .withStructureSearchType(StructureSearchType.SUBSTRUCTURE)
                    .withStructure(input.getCompound().getMolFile())
            );
            System.out.println("Found samples: " + samples);
            if (!samples.isEmpty()) {
                mutation.inputSamples().put(input.getAnchor(), samples.getFirst().getId());
            }
        }
        applyMutation(mutation);
        input1Sample1Anchor = model.getReactions().getFirst().getInputs().get(0).getSamples().get(0).getAnchor();
    }

    @Test
    @Order(250)
    void testSetInputRoleToCatalyst() {
        ReactionInputMutation.SetInputRole mutation = new ReactionInputMutation.SetInputRole(input2Anchor, ReactionRole.CATALYST);
        applyMutation(mutation);
    }

    @Test
    @Order(251)
    void testSetInputRoleToSolvent() {
        ReactionInputMutation.SetInputRole mutation = new ReactionInputMutation.SetInputRole(input2Anchor, ReactionRole.SOLVENT);
        applyMutation(mutation);
    }

    @Test
    @Order(252)
    void testSetInputRoleBack() {
        ReactionInputMutation.SetInputRole mutation = new ReactionInputMutation.SetInputRole(input2Anchor, ReactionRole.REACTANT);
        applyMutation(mutation);
    }

    @Test
    @Order(300)
    void testSelectSaltCode() {
        applyMutation(new ReactionOutputMutation.SetOutputSaltCode(output1Anchor, dictionaryClient.getSaltCodes().getFirst()));
    }

    @Test
    @Order(400)
    void testSelectSaltEQ() {
        applyMutation(new ReactionOutputMutation.SetOutputSaltEQ(output1Anchor, 0.5));
    }

    @Test
    @Order(500)
    void testSetInputWeight() {
        applyMutation(new ReactionInputSampleMutation.SetInputWeight(input1Sample1Anchor, 100.0, WeightUnit.G));
    }

    @Test
    @Order(501)
    void testSetInputWeightInKG() {
        applyMutation(new ReactionInputSampleMutation.SetInputWeight(input1Sample1Anchor, 0.1, WeightUnit.KG));
    }

    @Test
    @Order(600)
    void testSetInputEQ() {
        applyMutation(new ReactionInputMutation.SetInputEQ(input2Anchor, 2.0));
    }

    @Test
    @Order(620)
    void testAddEmptyInput() {
        applyMutation(new ReactionMutation.AddEmptyInput(reactionAnchor));
    }

    @Test
    @Order(621)
    void testRemoveEmptyInput() {
        List<ReactionInput> inputs = model.getReactions().getFirst().getInputs();
        applyMutation(new ReactionMutation.RemoveInput(reactionAnchor, inputs.getLast().getAnchor()));
    }

    @Test
    @Order(700)
    void testAddProductSample() {
        applyMutation(new ReactionOutputMutation.AddProductSample(output2Anchor));
        output2Sample1Anchor = model.getReactions().getFirst().getOutputs().get(1).getSamples().get(0).getAnchor();
    }

    @Test
    @Order(800)
    void testSetOutputActualMol() {
        applyMutation(new ReactionOutputSampleMutation.SetOutputActualMol(output2Sample1Anchor, 200.0, MolUnit.MMOL));
    }

    @Test
    @Order(900)
    void testSetOutputPurity() {
        applyMutation(new ReactionOutputSampleMutation.SetOutputPurity(output2Sample1Anchor, 0.5));
    }

    @Test
    @Order(1000)
    void testSetActualWeight() {
        applyMutation(new ReactionOutputSampleMutation.SetOutputActualWeight(output2Sample1Anchor, 10.0, WeightUnit.G));
    }

    @Test
    @Order(1100)
    void testRegisterSample() {
        applyMutation(new ReactionOutputSampleMutation.RegisterSample(output2Sample1Anchor));
    }

    @Test
    @Order(1101)
    void testAddAnotherOutputSample() {
        applyMutation(new ReactionOutputMutation.AddProductSample(output2Anchor));
        output2Sample2Anchor = model.getReactions().getFirst().getOutputs().get(1).getSamples().get(1).getAnchor();
    }

    @Test
    @Order(1102)
    void testRegisterAnotherSample() {
        applyMutation(new ReactionOutputSampleMutation.RegisterSample(output2Sample2Anchor));
    }

    @SneakyThrows
    private void applyMutation(Mutation mutation) {
        System.out.println("Applying mutation: " + mutation);
        reportBuilder.addMutation(mutation);
        model = experimentClient.mutateExperimentModel(experiment.getId(), new MutateModelForm(model, mutation));
        Response pictureResponse = experimentClient.getExperimentPictureClient(experiment.getId());
        byte[] newPicture = (byte[]) pictureResponse.getEntity();
        if (picture == null || newPicture != null && !Arrays.equals(picture, newPicture)) {
            picture = newPicture;
            reportBuilder.addPicture(picture, pictureResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));
        }
        reportBuilder.addModel(model);
        System.out.println(model);
    }
}

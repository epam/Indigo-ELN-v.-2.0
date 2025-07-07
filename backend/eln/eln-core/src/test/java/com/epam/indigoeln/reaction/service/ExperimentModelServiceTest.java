package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.compound.model.FindSamplesRequest;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.StructureSearchType;
import com.epam.indigoeln.eln.BaseTest;
import com.epam.indigoeln.eln.api.MutateModelForm;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.util.ResponseWithHeaders;
import com.epam.indigoeln.eln.util.TestHelper;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.mutation.*;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.epam.indigoeln.reaction.util.CalculationReportBuilder;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.ws.rs.core.HttpHeaders;
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

@QuarkusTest
@TestSecurity(user = TestHelper.JOHN_USERNAME)
public class ExperimentModelServiceTest extends BaseTest {

    ExperimentDetailsDTO experiment;
    ExperimentModel model;
    CalculationReportBuilder reportBuilder;

    @Nullable
    byte[] picture = null;

    @BeforeAll
    void setUp(@TempDir Path tempDir) throws Exception {
        reportBuilder = new CalculationReportBuilder(new File("calculations.html"));
        miscClient.loadCompoundsFromFileClient("compounds.sdf", tempDir, getClass().getResourceAsStream("/Compound_000000001_000500000.1.sdf").readAllBytes());
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
        experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(getEmptyTemplateID()));
        model = experimentClient.getExperimentModel(experiment.getId());
    }

    @Test
    @Order(100)
    void testLoadReaction() throws Exception {
        String molFile = new String(getClass().getResourceAsStream("/reaction.rxn").readAllBytes());
        applyMutation(new ReactionMutation.SetScheme(0, molFile));
    }

    @Test
    @Order(200)
    void testResolveInputs() {
        ReactionMutation.ResolveInputs mutation = new ReactionMutation.ResolveInputs(0, new HashMap<>());
        int inputNo = 0;
        for (ReactionInput input : model.getReactions().getFirst().getInputs()) {
            List<SampleDTO> samples = miscClient.findSamples(new FindSamplesRequest(
                    StructureSearchType.SUBSTRUCTURE,
                    input.getCompound().getMolFile()
            ));
            System.out.println("Found samples for input " + inputNo + ": " + samples);
            if (!samples.isEmpty()) {
                mutation.inputSamples().put(inputNo++, samples.getFirst().getId());
            }
        }
        applyMutation(mutation);
    }

    @Test
    @Order(300)
    void testSelectSaltCode() {
        applyMutation(new ReactionOutputMutation.SetOutputSaltCode(0, 0, dictionaryClient.getSaltCodes().getFirst()));
    }

    @Test
    @Order(400)
    void testSelectSaltEQ() {
        applyMutation(new ReactionOutputMutation.SetOutputSaltEQ(0, 0, 0.5));
    }

    @Test
    @Order(500)
    void testSetInputWeight() {
        applyMutation(new ReactionInputSampleMutation.SetInputWeight(0, 0, 0, 100.0, WeightUnit.G));
    }

    @Test
    @Order(600)
    void testSetInputEQ() {
        applyMutation(new ReactionInputMutation.SetInputEQ(0, 1, 2.0));
    }

    @Test
    @Order(700)
    void testAddProductSample() {
        applyMutation(new ReactionOutputMutation.AddProductSample(0, 1));
    }

    @Test
    @Order(800)
    void testSetOutputActualMol() {
        applyMutation(new ReactionOutputSampleMutation.SetOutputActualMol(0, 1, 0, 200.0, MolUnit.MMOL));
    }

    @Test
    @Order(900)
    void testSetOutputPurity() {
        applyMutation(new ReactionOutputSampleMutation.SetOutputPurity(0, 1, 0, 0.5));
    }

    @Test
    @Order(1000)
    void testSetActualWeight() {
        applyMutation(new ReactionOutputSampleMutation.SetOutputActualWeight(0, 1, 0, 10.0, WeightUnit.G));
    }

    @SneakyThrows
    private void applyMutation(Mutation mutation) {
        System.out.println("Applying mutation: " + mutation);
        reportBuilder.addMutation(mutation);
        model = experimentClient.mutateExperimentModel(experiment.getId(), new MutateModelForm(model, mutation));
        ResponseWithHeaders pictureResponse = experimentClient.getExperimentPictureClient(experiment.getId());
        byte[] newPicture = pictureResponse.getContent().readAllBytes();
        if (picture == null || newPicture != null && !Arrays.equals(picture, newPicture)) {
            picture = newPicture;
            reportBuilder.addPicture(picture, pictureResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE).iterator().next());
        }
        reportBuilder.addModel(model);
        System.out.println(model);
    }
}

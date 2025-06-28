package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.compound.model.FindSamplesRequest;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.StructureSearchType;
import com.epam.indigoeln.eln.api.MutateModelForm;
import com.epam.indigoeln.eln.client.ExperimentsClient;
import com.epam.indigoeln.eln.client.MiscClient;
import com.epam.indigoeln.eln.client.NotebooksClient;
import com.epam.indigoeln.eln.client.ProjectsClient;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.util.FeignUtil;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.mutation.*;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.epam.indigoeln.reaction.util.CalculationReportBuilder;
import io.quarkus.test.junit.QuarkusTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ExperimentModelRealTest {

    ExperimentDetailsDTO experiment;
    ExperimentModel model;
    CalculationReportBuilder reportBuilder;
    ProjectsClient projectsClient;
    NotebooksClient notebooksClient;
    ExperimentsClient experimentsClient;
    MiscClient miscClient;

    @BeforeAll
    void setup(@TempDir Path tempDir) throws Exception {
        URI baseURI = URI.create("https://indigo-eln-dev.test.lifescience.opensource.epam.com/");
        AtomicReference<String> testUsername = new AtomicReference<>();
        AtomicReference<String> authorization = new AtomicReference<>(
                "Bearer eyJraWQiOiJUbTFZSmg5UUJjZkQrVnBKVlc1WEQ3UEV5NEh1d2gxeUlvVlZwcmYxb0UwPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiIwNDY4ZTQyOC1hMGYxLTcwN2YtYWE1Yy0zMjgzZGY3NWY2ZjgiLCJpc3MiOiJodHRwczpcL1wvY29nbml0by1pZHAudXMtZWFzdC0xLmFtYXpvbmF3cy5jb21cL3VzLWVhc3QtMV82RGlyZ3RRMXAiLCJjbGllbnRfaWQiOiJhNGtraDAwb2IyM2w3dXBtODhoaDNtbWo1Iiwib3JpZ2luX2p0aSI6IjcwOWNiYmU5LTMzZmUtNGY1NS1hMzI2LWQxZjMwYmFjMzVkZiIsImV2ZW50X2lkIjoiNWU4ZjI1YjgtZWE3My00MDkxLWJlYWMtMWM3ZjU3YjE3ZmViIiwidG9rZW5fdXNlIjoiYWNjZXNzIiwic2NvcGUiOiJhd3MuY29nbml0by5zaWduaW4udXNlci5hZG1pbiIsImF1dGhfdGltZSI6MTc1MTA0NzQxMCwiZXhwIjoxNzUxMTE4NzQ4LCJpYXQiOjE3NTExMTUxNDgsImp0aSI6IjIyNWVhOTg1LWU4ZDktNDlhNS1hNmQzLTJkZmRmYWI2ZDIzZSIsInVzZXJuYW1lIjoiYWxpY2UifQ.gJwViaWCvUepJgl9-w3__FzjBaG-R2_UNDqtBIyAASr1rwf32hAdmW6h7S7icOVp0_XExE3D4-II2zTR8uZli_UXO6X9KVeCmhEAFAHeJgjtYYlTkG9CIbDlbeu2H1TJnIKGdQGMHRXd6GVSjv7ChAluKhZlMfTkQm6JVXKvKbqSCQsKiYDZDPT3eCNXl0kW_3oACyNf3_Lbhz8XXyCDwzoOGOcOhmFwVga8KOoiVwVxinjUG53qWFHO_VhfVUR62fNkTH3FlxDn65ZrJR_hqLlI9GtQ5nCNBoieiAFtPCg-GNG7RHar5h9DMRnyuH9g-tuMIQOYDGVhULASU7y9Hg"
        );
        projectsClient = FeignUtil.buildFeignClient(baseURI, ProjectsClient.class, testUsername, authorization);
        notebooksClient = FeignUtil.buildFeignClient(baseURI, NotebooksClient.class, testUsername, authorization);
        experimentsClient = FeignUtil.buildFeignClient(baseURI, ExperimentsClient.class, testUsername, authorization);
        miscClient = FeignUtil.buildFeignClient(baseURI, MiscClient.class, testUsername, authorization);

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
        ProjectDetailsDTO project = projectsClient.createProject(new ProjectRequest("ExperimentModelServiceTest"));
        NotebookDetailsDTO notebook = notebooksClient.createNotebook(project.getId(), new NotebookRequest("ExperimentModelServiceTest"));
        experiment = experimentsClient.createExperiment(notebook.getId(), new ExperimentRequest("ExperimentModelServiceTest"));
        model = experimentsClient.getExperimentModel(experiment.getId());
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
        applyMutation(new ReactionOutputMutation.SetOutputSaltCode(0, 0, miscClient.getSaltCodes().getFirst()));
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
        model = experimentsClient.mutateExperimentModel(experiment.getId(), new MutateModelForm(model, mutation));
//        model = FeignUtil.OBJECT_MAPPER.readValue(modelStr.get("data"), ExperimentModel.class);
        reportBuilder.addModel(model);
        System.out.println(model);
    }
}

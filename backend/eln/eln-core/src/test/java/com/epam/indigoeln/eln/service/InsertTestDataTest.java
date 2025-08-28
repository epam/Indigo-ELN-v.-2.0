package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.compound.model.FindSamplesRequest;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.StructureSearchType;
import com.epam.indigoeln.eln.api.MutateModelForm;
import com.epam.indigoeln.eln.client.*;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.util.FeignUtil;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.mutation.*;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;
import org.wildfly.common.Assert;

import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static com.epam.indigoeln.common.util.ModelUtil.loadResource;
import static com.epam.indigoeln.eln.util.TestHelper.*;


// no @QuarkusTest - only works with remote backend
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnabledIfEnvironmentVariable(named = "RUN_ENV_TESTS", matches = "true")
class InsertTestDataTest {

    ProjectClient projectClient;
    NotebookClient notebookClient;
    ExperimentClient experimentClient;
    MiscClient miscClient;
    UserClient userClient;
    TemplateClient templateClient;
    DictionaryClient dictionaryClient;
    CompoundClient compoundClient;

    @BeforeEach
    void setup() {
        URI baseURI = URI.create("https://indigo-eln-dev.test.lifescience.opensource.epam.com/");
        AtomicReference<String> testUsername = new AtomicReference<>();
        String token = System.getenv("TOKEN");
        Assert.assertNotNull(token);
        AtomicReference<String> authorization = new AtomicReference<>(token);
        projectClient = FeignUtil.buildFeignClient(baseURI, ProjectClient.class, testUsername, authorization);
        notebookClient = FeignUtil.buildFeignClient(baseURI, NotebookClient.class, testUsername, authorization);
        experimentClient = FeignUtil.buildFeignClient(baseURI, ExperimentClient.class, testUsername, authorization);
        miscClient = FeignUtil.buildFeignClient(baseURI, MiscClient.class, testUsername, authorization);
        templateClient = FeignUtil.buildFeignClient(baseURI, TemplateClient.class, testUsername, authorization);
        dictionaryClient = FeignUtil.buildFeignClient(baseURI, DictionaryClient.class, testUsername, authorization);
        userClient = FeignUtil.buildFeignClient(baseURI, UserClient.class, testUsername, authorization);
        compoundClient = FeignUtil.buildFeignClient(baseURI, CompoundClient.class, testUsername, authorization);
    }

//    @Test
    @Order(1)
    void flyway() {
        Map<String, String> result = miscClient.migrate();
        System.out.println(result);
    }

//    @Test
    @Order(2)
    void insertUsers() {
        userClient.createUser(new UserRequest("alice@eln.com", "Alice", "Smith", "password", List.of(ROLE_CONTENT_EDITOR, ROLE_TEMPLATE_EDITOR)));
        userClient.createUser(new UserRequest("bob@eln.com", "Bob", "Johnson", "password", List.of(ROLE_ADMINISTRATOR)));
        userClient.createUser(new UserRequest("charlie@eln.com", "Charlie", "Williams", "password", List.of(ROLE_CONTENT_EDITOR)));
    }

//    @Test
    @Order(2)
    void insertTemplate() {
        TemplateDetailsDTO template = templateClient.createTemplate(new TemplateRequest(
                "Default Template",
                List.of(
                        new TemplateComponent.ExperimentDetails(),
                        new TemplateComponent.ExperimentDescription(),
                        new TemplateComponent.Attachments(),
                        new TemplateComponent.ReactionScheme(),
                        new TemplateComponent.StoichiometryTable(true, true),
                        new TemplateComponent.Batches()
                )
        ));
        System.out.println(template);
    }

//    @Test
    @Order(3)
    void insertTestData() {
        TemplateDTO template = findDefaultTemplate();
        Map<String, String> result = miscClient.insertTestData(template.getId());
        System.out.println(result);
    }

//    @Test
    @Order(4)
    void fillExperiment(@TempDir Path tempDir) {
        miscClient.loadCompoundsFromFileClient("compounds.sdf", tempDir, loadResource(getClass(), "/Compound_000000001_000500000.1.sdf"));

        // create experiment
        Page<ProjectDTO> existingProjects = projectClient.getProjects("ProjectWithData", SortOrder.EARLIEST, null, Paging.DEFAULT);
        ProjectDetailsDTO project = existingProjects.getItems().isEmpty()
                ? projectClient.createProject(new ProjectRequest("ProjectWithData"))
                : projectClient.getProject(existingProjects.getItems().getFirst().getId());
        Page<NotebookDTO> existingNotebooks = notebookClient.getProjectNotebooks(project.getId(), "88888888", SortOrder.EARLIEST, null, Paging.DEFAULT);
        NotebookDetailsDTO notebook = existingNotebooks.getItems().isEmpty()
                ? notebookClient.createNotebook(project.getId(), new NotebookRequest("88888884"))
                : notebookClient.getNotebook(existingNotebooks.getItems().getFirst().getId());
        TemplateDTO template = findDefaultTemplate();
        DictionaryItemRef therapeuticArea = dictionaryClient.getDictionary(Dictionary.THERAPEUTIC_AREA).getFirst();
        DictionaryItemRef projectCode = dictionaryClient.getDictionary(Dictionary.PROJECT_CODE).getFirst();
        ExperimentDetailsDTO experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(
                template.getId(),
                "Experiment with data",
                therapeuticArea,
                projectCode
        ));

        // add attachment
        experimentClient.createExperimentAttachment(experiment.getId(), "attachment.txt", tempDir, "This is attachment".getBytes());

        // load initial model
        ExperimentModel model = experimentClient.getExperimentModel(experiment.getId());
        UUID reactionAnchor = model.getReactions().getFirst().getAnchor();

        // load reaction
        String molFile = new String(ModelUtil.loadResource(getClass(), "/reaction.rxn"));
        model = applyMutation(experiment, model, new ReactionMutation.SetScheme(reactionAnchor, molFile));
        UUID input1Anchor = model.getReactions().getFirst().getInputs().get(0).getAnchor();
        UUID input2Anchor = model.getReactions().getFirst().getInputs().get(1).getAnchor();
        UUID output1Anchor = model.getReactions().getFirst().getOutputs().get(0).getAnchor();
        UUID output2Anchor = model.getReactions().getFirst().getOutputs().get(1).getAnchor();

        // resolve inputs
        ReactionMutation.ResolveInputs mutation = new ReactionMutation.ResolveInputs(reactionAnchor, new HashMap<>());
        for (ReactionInput input : model.getReactions().getFirst().getInputs()) {
            List<SampleDTO> samples = compoundClient.findSamples(new FindSamplesRequest(
                    StructureSearchType.SUBSTRUCTURE,
                    input.getCompound().getMolFile()
            ));
            System.out.println("Found samples: " + samples);
            if (!samples.isEmpty()) {
                mutation.inputSamples().put(input.getAnchor(), samples.getFirst().getId());
            }
        }
        model = applyMutation(experiment, model, mutation);
        UUID input1Sample1Anchor = model.getReactions().getFirst().getInputs().get(0).getSamples().get(0).getAnchor();

        // select salt code
        model = applyMutation(experiment, model, new ReactionOutputMutation.SetOutputSaltCode(output1Anchor, dictionaryClient.getSaltCodes().getFirst()));

        // select salt eq
        model = applyMutation(experiment, model, new ReactionOutputMutation.SetOutputSaltEQ(output1Anchor, 0.5));

        // set input weight
        model = applyMutation(experiment, model, new ReactionInputSampleMutation.SetInputWeight(input1Sample1Anchor, 100.0, WeightUnit.G));

        // set input eq
        model = applyMutation(experiment, model, new ReactionInputMutation.SetInputEQ(input2Anchor, 2.0));

        // add product sample
        model = applyMutation(experiment, model, new ReactionOutputMutation.AddProductSample(output2Anchor));
        UUID output2Sample1Anchor = model.getReactions().getFirst().getOutputs().get(1).getSamples().get(0).getAnchor();

        // set output actual mol
        model = applyMutation(experiment, model, new ReactionOutputSampleMutation.SetOutputActualMol(output2Sample1Anchor, 200.0, MolUnit.MMOL));

        // set output purity
        model = applyMutation(experiment, model, new ReactionOutputSampleMutation.SetOutputPurity(output2Sample1Anchor, 0.5));

        // set actual weight
        model = applyMutation(experiment, model, new ReactionOutputSampleMutation.SetOutputActualWeight(output2Sample1Anchor, 10.0, WeightUnit.G));

        // register sample
        model = applyMutation(experiment, model, new ReactionOutputSampleMutation.RegisterSample(output2Sample1Anchor));

        // add another output sample
        model = applyMutation(experiment, model, new ReactionOutputMutation.AddProductSample(output2Anchor));
        UUID output2Sample2Anchor = model.getReactions().getFirst().getOutputs().get(1).getSamples().get(1).getAnchor();

        // register another sample
        model = applyMutation(experiment, model, new ReactionOutputSampleMutation.RegisterSample(output2Sample2Anchor));
    }

    private TemplateDTO findDefaultTemplate() {
        return templateClient.getTemplates(Paging.ALL).getItems().stream()
                .filter(t -> t.getName().equals("Default Template"))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Default Template not found"));
    }

    @SneakyThrows
    private ExperimentModel applyMutation(ExperimentDetailsDTO experiment, ExperimentModel model, Mutation mutation) {
        System.out.println("Applying mutation: " + mutation);
        ExperimentModel model1 = experimentClient.mutateExperimentModel(experiment.getId(), new MutateModelForm(model, mutation));
        System.out.println(model1);
        return model1;
    }
}

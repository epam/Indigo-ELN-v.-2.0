package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.eln.api.MutateModelForm;
import com.epam.indigoeln.eln.client.*;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.*;
import com.epam.indigoeln.reaction.model.outputsample.*;
import com.epam.indigoeln.reaction.model.units.DensityUnit;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.epam.indigoeln.reaction.util.MutationsTestUtil;
import com.epam.indigoeln.test.FeignUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.epam.indigoeln.common.util.ModelUtil.loadResource;
import static com.epam.indigoeln.eln.ELNBaseTest.*;
import static org.assertj.core.api.Assertions.assertThat;


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
    SignatureClient signatureClient;

    @BeforeEach
    void setup() {
        URI baseURI = URI.create("https://indigo-eln-dev.test.lifescience.opensource.epam.com/");
        AtomicReference<String> testUsername = new AtomicReference<>();
        String token = System.getenv("TOKEN");
        assertThat(token).describedAs("TOKEN environment variable").isNotNull();
        AtomicReference<String> authorization = new AtomicReference<>(token);

        projectClient = FeignUtil.buildFeignClient(baseURI, ProjectClient.class, testUsername, authorization);
        notebookClient = FeignUtil.buildFeignClient(baseURI, NotebookClient.class, testUsername, authorization);
        experimentClient = FeignUtil.buildFeignClient(baseURI, ExperimentClient.class, testUsername, authorization);
        miscClient = FeignUtil.buildFeignClient(baseURI, MiscClient.class, testUsername, authorization);
        templateClient = FeignUtil.buildFeignClient(baseURI, TemplateClient.class, testUsername, authorization);
        dictionaryClient = FeignUtil.buildFeignClient(baseURI, DictionaryClient.class, testUsername, authorization);
        userClient = FeignUtil.buildFeignClient(baseURI, UserClient.class, testUsername, authorization);
        compoundClient = FeignUtil.buildFeignClient(baseURI, CompoundClient.class, testUsername, authorization);
        signatureClient = FeignUtil.buildFeignClient(baseURI, SignatureClient.class, testUsername, authorization);
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
        String password = System.getenv("PASSWORD");
        userClient.createUser(new UserRequest("alice@eln.com", "Alice", "Smith", password, List.of(ROLE_CONTENT_EDITOR, ROLE_TEMPLATE_EDITOR)));
        userClient.createUser(new UserRequest("bob@eln.com", "Bob", "Johnson", password, List.of(ROLE_ADMINISTRATOR)));
        userClient.createUser(new UserRequest("charlie@eln.com", "Charlie", "Williams", password, List.of(ROLE_CONTENT_EDITOR)));

        Map<String, List<RoleRef>> userMatrix = Map.of(
                "user_noroles", List.of(),
                "user_p", List.of(ROLE_PROJECT_CREATOR),
                "user_t", List.of(ROLE_TEMPLATE_EDITOR),
                "user_tp", List.of(ROLE_TEMPLATE_EDITOR, ROLE_PROJECT_CREATOR),
                "user_c", List.of(ROLE_CONTENT_EDITOR),
                "user_cp", List.of(ROLE_CONTENT_EDITOR, ROLE_PROJECT_CREATOR),
                "user_ct", List.of(ROLE_CONTENT_EDITOR, ROLE_TEMPLATE_EDITOR),
                "user_ctp", List.of(ROLE_CONTENT_EDITOR, ROLE_TEMPLATE_EDITOR, ROLE_PROJECT_CREATOR)
        );
        userMatrix.forEach((username, roles) -> {
            userClient.createUser(new UserRequest(username + "@eln.com", username, username, password, roles));
        });
    }

    //    @Test
    @Order(3)
    void insertTestData() {
        Map<String, String> result = miscClient.insertTestData();
        System.out.println(result);
    }

    //    @Test
    @Order(4)
    void loadCompounds(@TempDir Path tempDir) {
        miscClient.loadCompoundsFromFileClient("compounds.sdf", tempDir, loadResource(getClass(), "/Compound_000000001_000500000.1.sdf"));
    }

//    @Test
    @Order(5)
    void insertSignatureTemplate() {
        UserRef bob = userClient.suggestUsers("Bob").getFirst();
        signatureClient.createSignatureTemplate(new SignatureTemplateRequest("Author and Bob", List.of(
                new SignatureBlock(null, SignatureReason.AUTHOR),
                new SignatureBlock(bob, SignatureReason.WITNESS)
        )));
    }

//    @Test
    @Order(6)
    void fillExperiment(@TempDir Path tempDir) {
        ExperimentDetailsDTO experiment = createExperiment("ProjectWithData", "88888888", templateClient.getByName("Default"), "Experiment with data");

        // add attachment
        experimentClient.createExperimentAttachment(experiment.getId(), "attachment.txt", tempDir, "This is attachment".getBytes());

        // load initial model
        ExperimentModel model = experimentClient.getExperiment(experiment.getId()).getModel();
        ReactionAnchor reactionAnchor = model.getReactions().getFirst().getAnchor();

        // load reaction
        String rxnFile = new String(ModelUtil.loadResource(getClass(), "/reaction.rxn"));
        model = applyMutation(experiment, model, new ReactionMutation.SetScheme(reactionAnchor, rxnFile));
        InputAnchor input1Anchor = model.getReactions().getFirst().getInputs().get(0).getAnchor();
        InputAnchor input2Anchor = model.getReactions().getFirst().getInputs().get(1).getAnchor();
        OutputAnchor output1Anchor = model.getReactions().getFirst().getOutputs().get(0).getAnchor();
        OutputAnchor output2Anchor = model.getReactions().getFirst().getOutputs().get(1).getAnchor();

        // resolve inputs
        ReactionMutation.ResolveInputs mutation = MutationsTestUtil.prepareResolveInputs(experiment, reactionAnchor, experimentClient, compoundClient);
        model = applyMutation(experiment, model, mutation);
        InputSampleAnchor input1Sample1Anchor = model.getReactions().getFirst().getInputs().get(0).getSamples().get(0).getAnchor();

        // select salt code
        model = applyMutation(experiment, model, new ReactionOutputMutation.SetOutputRowSaltCode(output1Anchor, dictionaryClient.getSaltCodes().get(1)));

        // select salt eq
        model = applyMutation(experiment, model, new ReactionOutputMutation.SetOutputRowSaltEQ(output1Anchor, 0.5));

        // select stereoisomer code
        StereoisomerCodeRef stereoisomerCode = dictionaryClient.getFirst(BuiltInDictionary.STEREOISOMER_CODE);
        model = applyMutation(experiment, model, new ReactionOutputMutation.SetOutputCompoundStereoisomerCode(output1Anchor, stereoisomerCode));

        // set input weight
        model = applyMutation(experiment, model, new ReactionInputSampleMutation.SetInputWeight(input1Sample1Anchor, "100.0", WeightUnit.G));

        // set input eq
        model = applyMutation(experiment, model, new ReactionInputMutation.SetInputRowEQ(input2Anchor, "2"));

        // add product sample
        model = applyMutation(experiment, model, new ReactionOutputMutation.AddProductSample(output2Anchor));
        OutputSampleAnchor output2Sample1Anchor = model.getReactions().getFirst().getOutputs().get(1).getSamples().get(0).getAnchor();

        // set output actual mol
        model = applyMutation(experiment, model, new ReactionOutputSampleMutation.SetOutputActualMol(output2Sample1Anchor, "200.0", MolUnit.MMOL));

        // set output purity
        model = applyMutation(experiment, model, new ReactionOutputSampleMutation.SetOutputPurity(output2Sample1Anchor, "0.5"));

        // set actual weight
        model = applyMutation(experiment, model, new ReactionOutputSampleMutation.SetOutputActualWeight(output2Sample1Anchor, "10.0", WeightUnit.G));

        // fill secondary fields
        SampleSourceRef source = dictionaryClient.getFirst(BuiltInDictionary.SAMPLE_SOURCE);
        model = applyMutation(experiment, model, new ReactionOutputSampleMutation.SetOutputSource(output2Sample1Anchor, source));
        SampleSourceDetailsRef sourceDetails = dictionaryClient.getFirst(BuiltInDictionary.SAMPLE_SOURCE_DETAILS);
        model = applyMutation(experiment, model, new ReactionOutputSampleMutation.SetOutputSourceDetails(output2Sample1Anchor, sourceDetails));
        ExternalSupplierRef externalSupplier = dictionaryClient.getFirst(BuiltInDictionary.EXTERNAL_SUPPLIER);
        model = applyMutation(experiment, model, new ReactionOutputSampleMutation.SetOutputExternalSupplier(output2Sample1Anchor, new ExternalSupplier(externalSupplier, "12345678")));
        model = applyMutation(experiment, model, new ReactionOutputSampleMutation.SetOutputBatchComment(output2Sample1Anchor, "batch comment"));
        model = applyMutation(experiment, model, new ReactionOutputSampleMutation.SetOutputStructureComment(output2Sample1Anchor, "structure comment"));
        ComponentStateRef componentState = dictionaryClient.getFirst(BuiltInDictionary.COMPONENT_STATE);
        model = applyMutation(experiment, model, new ReactionOutputSampleMutation.SetOutputComponentState(output2Sample1Anchor, componentState));
        CompoundProtectionRef compoundProtection = dictionaryClient.getFirst(BuiltInDictionary.COMPOUND_PROTECTION);
        model = applyMutation(experiment, model, new ReactionOutputSampleMutation.SetOutputCompoundProtection(output2Sample1Anchor, List.of(compoundProtection)));
        StorageInstructionsRef storageInstructions = dictionaryClient.getFirst(BuiltInDictionary.STORAGE_INSTRUCTIONS);
        model = applyMutation(experiment, model, new ReactionOutputSampleMutation.SetOutputStorageInstructions(output2Sample1Anchor, List.of(storageInstructions)));
        HealthHazardRef healthHazards = dictionaryClient.getFirst(BuiltInDictionary.HEALTH_HAZARD);
        model = applyMutation(experiment, model, new ReactionOutputSampleMutation.SetOutputHealthHazards(output2Sample1Anchor, List.of(healthHazards)));
        HandlingPrecautionsRef handlingPrecautions = dictionaryClient.getFirst(BuiltInDictionary.HANDLING_PRECAUTIONS);
        model = applyMutation(experiment, model, new ReactionOutputSampleMutation.SetOutputHandlingPrecautions(output2Sample1Anchor, List.of(handlingPrecautions)));
        model = applyMutation(experiment, model, new ReactionOutputSampleMutation.SetOutputMeltingPoint(output2Sample1Anchor, new MeltingPoint(-10.0, 20.0, "comment")));
        SolventRef solvent = dictionaryClient.getFirst(BuiltInDictionary.SOLVENT);
        model = applyMutation(experiment, model, new ReactionOutputSampleMutation.SetOutputResidualSolvents(output2Sample1Anchor, List.of(
                new ResidualSolvent(solvent, 1.5, "comment")
        )));
        model = applyMutation(experiment, model, new ReactionOutputSampleMutation.SetOutputSolubilityInSolvents(output2Sample1Anchor, List.of(
                new SolubidityInSolvent.Qualitative(solvent, "comment", SolubidityQualitativeType.PRECIPITATE),
                new SolubidityInSolvent.Quantitative(solvent, "comment", ComparisonOperator.APPROXIMATELY, 0.5, DensityUnit.G_ML)
        )));

        // submit and reopen
        SignatureTemplateDTO signatureTemplate = signatureClient.getSignatureTemplates(Paging.ALL).getItems().stream()
                .filter(t -> t.getName().equals("Author and Bob"))
                .findFirst().orElseThrow();
        experimentClient.completeAndSubmitExperiment(experiment.getId(), signatureTemplate.getId());
        experimentClient.reopenExperiment(experiment.getId());
        model = experimentClient.getExperiment(experiment.getId()).getModel();

        // register sample
        model = applyMutation(experiment, model, new ReactionOutputSampleMutation.RegisterSample(output2Sample1Anchor));

        // add another output sample
        model = applyMutation(experiment, model, new ReactionOutputMutation.AddProductSample(output2Anchor));
        OutputSampleAnchor output2Sample2Anchor = model.getReactions().getFirst().getOutputs().get(1).getSamples().get(1).getAnchor();

        // register another sample
        model = applyMutation(experiment, model, new ReactionOutputSampleMutation.RegisterSample(output2Sample2Anchor));

        // submit and reopen
        experimentClient.completeAndSubmitExperiment(experiment.getId(), signatureTemplate.getId());
        experimentClient.reopenExperiment(experiment.getId());
    }

//    @Test
    @Order(7)
    void submitExperiment() {
        ExperimentDetailsDTO experiment = createExperiment("ProjectWithData", "88888888", templateClient.getByName("Default"), "Experiment to submit");
        SignatureTemplateDTO signatureTemplate = signatureClient.getSignatureTemplates(Paging.ALL).getItems().stream()
                .filter(t -> t.getName().equals("Author and Bob"))
                .findFirst().orElseThrow();
        experimentClient.completeAndSubmitExperiment(experiment.getId(), signatureTemplate.getId());
    }

    private ExperimentDetailsDTO createExperiment(String projectName, String notebookName, TemplateDTO template, String experimentDescription) {
        // create experiment
        Page<ProjectDTO> existingProjects = projectClient.getProjects(projectName, SortOrder.EARLIEST, null, Paging.DEFAULT);
        ProjectDetailsDTO project = existingProjects.getItems().isEmpty()
                ? projectClient.createProject(new ProjectRequest(projectName))
                : projectClient.getProject(existingProjects.getItems().getFirst().getId());
        Page<NotebookDTO> existingNotebooks = notebookClient.getProjectNotebooks(project.getId(), notebookName, SortOrder.EARLIEST, null, Paging.DEFAULT);
        NotebookDetailsDTO notebook = existingNotebooks.getItems().isEmpty()
                ? notebookClient.createNotebook(project.getId(), new NotebookRequest(notebookName))
                : notebookClient.getNotebook(existingNotebooks.getItems().getFirst().getId());
        TherapeuticAreaRef therapeuticArea = dictionaryClient.getFirst(BuiltInDictionary.THERAPEUTIC_AREA);
        ProjectCodeRef projectCode = dictionaryClient.getFirst(BuiltInDictionary.PROJECT_CODE);
        return experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(
                template.getId(),
                experimentDescription,
                therapeuticArea,
                projectCode
        ));
    }

    @SneakyThrows
    private ExperimentModel applyMutation(ExperimentDetailsDTO experiment, ExperimentModel model, Mutation mutation) {
        System.out.println("Applying mutation: " + mutation);
        ExperimentModel model1 = experimentClient.mutateExperimentModel(experiment.getId(), new MutateModelForm(model, mutation));
        System.out.println(model1);
        return model1;
    }
}

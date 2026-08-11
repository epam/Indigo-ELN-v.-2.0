package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.SortOrder;
import com.epam.indigoeln.common.model.UserRef;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.SampleSearchResult;
import com.epam.indigoeln.eln.client.*;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.reaction.model.ComparisonOperator;
import com.epam.indigoeln.reaction.model.SampleRegistrationStatus;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import com.epam.indigoeln.reaction.model.outputsample.*;
import com.epam.indigoeln.reaction.model.units.DensityUnit;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.epam.indigoeln.reaction.util.ExperimentObject;
import com.epam.indigoeln.signature.api.SignatureClient;
import com.epam.indigoeln.signature.model.SignatureReason;
import com.epam.indigoeln.signature.model.SignatureTemplateBlock;
import com.epam.indigoeln.signature.model.SignatureTemplateDTO;
import com.epam.indigoeln.signature.model.SignatureTemplateRequest;
import com.epam.indigoeln.test.FeignUtil;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static com.epam.indigoeln.common.model.Paging.DEFAULT_PAGE_SIZE;
import static com.epam.indigoeln.common.util.ModelUtil.loadResource;
import static com.epam.indigoeln.compound.model.search.SearchCatalog.ELN;
import static com.epam.indigoeln.eln.ELNBaseTest.*;
import static com.epam.indigoeln.eln.model.ExperimentStatus.REOPEN;
import static com.epam.indigoeln.eln.model.ExperimentStatus.SUBMITTED;
import static com.epam.indigoeln.eln.test.EnteredValueAssert.assertThat;
import static com.epam.indigoeln.eln.test.ReactionInputSampleAssert.assertThat;
import static com.epam.indigoeln.eln.test.ReactionOutputSampleAssert.assertThat;
import static com.epam.indigoeln.reaction.model.units.MolUnit.MMOL;
import static com.epam.indigoeln.reaction.model.units.WeightUnit.G;
import static org.assertj.core.api.Assertions.assertThat;


// no @QuarkusTest - only works with remote backend
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnabledIfEnvironmentVariable(named = "RUN_ENV_TESTS", matches = "true")
class InsertTestDataTest {

    private static final String SIGNATURE_TEMPLATE_AUTHOR_AND_BOB = "Author and Bob";
    private static final String PROJECT_WITH_DATA = "ProjectWithData";
    private static final String NOTEBOOK_88888888 = "88888888";
    private static final String TEMPLATE_DEFAULT = "Default";
    private static final String EXPERIMENT_WITH_DATA = "Experiment with data";
    private static final String EXPERIMENT_TO_SUBMIT = "Experiment to submit";
    private static final String ATTACHMENT_FILENAME = "attachment.txt";
    private static final String BATCH_COMMENT = "batch comment";
    private static final String COMMENT = "comment";

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
        AtomicReference<@Nullable String> testUsername = new AtomicReference<>();
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

    @Test
    @Order(1)
    void flyway() {
        Map<String, String> result = miscClient.migrate();
        assertThat(result).containsKeys("migrations executed", "total time");
        assertThat(Integer.parseInt(result.get("migrations executed"))).isGreaterThanOrEqualTo(0);
    }

    @Test
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
        userMatrix.forEach((username, roles) ->
                userClient.createUser(new UserRequest(username + "@eln.com", username, username, password, roles)));

        assertThat(userClient.getUser("alice@eln.com").getDisplayName()).isEqualTo("Alice Smith");
        assertThat(userClient.getUser("bob@eln.com").getRoles()).extracting(RoleRef::getName).contains("Administrator");
        assertThat(userClient.suggestUsers("user_ctp")).extracting(UserRef::getUsername).contains("user_ctp@eln.com");
    }

    @Test
    @Order(3)
    void insertTestData() {
        Map<String, String> result = miscClient.insertTestData();
        assertThat(Integer.parseInt(result.get("projects"))).isBetween(4, 5);
        assertThat(Integer.parseInt(result.get("notebooks"))).isPositive();
        assertThat(Integer.parseInt(result.get("experiments"))).isPositive();
        assertThat(Integer.parseInt(result.get("attachments"))).isGreaterThanOrEqualTo(0);

        Page<ProjectDTO> projects = projectClient.getProjects("Test Project", SortOrder.EARLIEST, null, Paging.DEFAULT);
        assertThat(projects.getItems()).isNotEmpty();
    }

    @Test
    @Order(4)
    void loadCompounds() {
        miscClient.loadCompoundsFromFileClient("compounds.sdf", loadResource("/Compound_000000001_000500000.1.sdf"));

        SampleSearchResult samples = compoundClient.search(new FindSamplesRequest().withCatalogs(Set.of(ELN)), DEFAULT_PAGE_SIZE);
        assertThat(samples.items()).isNotEmpty();
    }

    @Test
    @Order(5)
    void insertSignatureTemplate() {
        UserRef bob = userClient.suggestUsers("Bob").getFirst();
        signatureClient.createTemplate(new SignatureTemplateRequest(SIGNATURE_TEMPLATE_AUTHOR_AND_BOB, List.of(
                new SignatureTemplateBlock(null, SignatureReason.AUTHOR),
                new SignatureTemplateBlock(bob, SignatureReason.WITNESS)
        )));

        assertThat(signatureClient.getTemplates()).extracting(SignatureTemplateDTO::getName).contains(SIGNATURE_TEMPLATE_AUTHOR_AND_BOB);
    }

    @Test
    @Order(6)
    void fillExperiment() {
        ExperimentObject experiment = createExperiment(PROJECT_WITH_DATA, NOTEBOOK_88888888, templateClient.getByName(TEMPLATE_DEFAULT), EXPERIMENT_WITH_DATA);

        // add attachment
        experimentClient.createExperimentAttachment(experiment.id(), ATTACHMENT_FILENAME, "This is attachment".getBytes());

        // load reaction
        experiment.mutateSetSchemeFromResource("/reaction.rxn");

        // resolve inputs
        experiment.mutateResolveInputs();

        // select salt code
        experiment.mutate(new ReactionOutputMutation.SetOutputRowSaltCode(experiment.output(1).getAnchor(), dictionaryClient.getNth(BuiltInDictionary.SALT_CODE, 1)));

        // select salt eq
        experiment.mutate(new ReactionOutputMutation.SetOutputRowSaltEQ(experiment.output(1).getAnchor(), 0.5));

        // select stereoisomer code
        StereoisomerCodeRef stereoisomerCode = dictionaryClient.getFirst(BuiltInDictionary.STEREOISOMER_CODE);
        experiment.mutate(new ReactionOutputMutation.SetOutputCompoundStereoisomerCode(experiment.output(1).getAnchor(), stereoisomerCode));

        // set input weight
        experiment.mutateSetInputWeight(1, 1, "100.0", WeightUnit.G);

        // set input eq
        experiment.mutateSetInputRowEQ(2, "2");

        // add product sample
        experiment.mutateAddProductSample(2);

        // set output actual mol
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputActualMol(experiment.outputSample(2, 1).getAnchor(), "200.0", MolUnit.MMOL));

        // set output purity
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputPurity(experiment.outputSample(2, 1).getAnchor(), "0.5"));

        // set actual weight
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputActualWeight(experiment.outputSample(2, 1).getAnchor(), "10.0", WeightUnit.G));

        // fill secondary fields
        SampleSourceRef source = dictionaryClient.getFirst(BuiltInDictionary.SAMPLE_SOURCE);
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputSource(experiment.outputSample(2, 1).getAnchor(), source));
        SampleSourceDetailsRef sourceDetails = dictionaryClient.getFirst(BuiltInDictionary.SAMPLE_SOURCE_DETAILS);
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputSourceDetails(experiment.outputSample(2, 1).getAnchor(), sourceDetails));
        ExternalSupplierRef externalSupplier = dictionaryClient.getFirst(BuiltInDictionary.EXTERNAL_SUPPLIER);
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputExternalSupplier(experiment.outputSample(2, 1).getAnchor(), new ExternalSupplier(externalSupplier, "12345678")));
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputBatchComment(experiment.outputSample(2, 1).getAnchor(), BATCH_COMMENT));
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputStructureComment(experiment.outputSample(2, 1).getAnchor(), "structure comment"));
        ComponentStateRef componentState = dictionaryClient.getFirst(BuiltInDictionary.COMPONENT_STATE);
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputComponentState(experiment.outputSample(2, 1).getAnchor(), componentState));
        CompoundProtectionRef compoundProtection = dictionaryClient.getFirst(BuiltInDictionary.COMPOUND_PROTECTION);
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputCompoundProtection(experiment.outputSample(2, 1).getAnchor(), List.of(compoundProtection)));
        StorageInstructionsRef storageInstructions = dictionaryClient.getFirst(BuiltInDictionary.STORAGE_INSTRUCTIONS);
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputStorageInstructions(experiment.outputSample(2, 1).getAnchor(), List.of(storageInstructions)));
        HealthHazardRef healthHazards = dictionaryClient.getFirst(BuiltInDictionary.HEALTH_HAZARD);
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputHealthHazards(experiment.outputSample(2, 1).getAnchor(), List.of(healthHazards)));
        HandlingPrecautionsRef handlingPrecautions = dictionaryClient.getFirst(BuiltInDictionary.HANDLING_PRECAUTIONS);
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputHandlingPrecautions(experiment.outputSample(2, 1).getAnchor(), List.of(handlingPrecautions)));
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputMeltingPoint(experiment.outputSample(2, 1).getAnchor(), new MeltingPoint(-10.0, 20.0, COMMENT)));
        SolventRef solvent = dictionaryClient.getFirst(BuiltInDictionary.SOLVENT);
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputResidualSolvents(experiment.outputSample(2, 1).getAnchor(), List.of(
                new ResidualSolvent(solvent, 1.5, COMMENT)
        )));
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputSolubilityInSolvents(experiment.outputSample(2, 1).getAnchor(), List.of(
                new SolubidityInSolvent.Qualitative(solvent, COMMENT, SolubidityQualitativeType.PRECIPITATE),
                new SolubidityInSolvent.Quantitative(solvent, COMMENT, ComparisonOperator.APPROXIMATELY, 0.5, DensityUnit.G_ML)
        )));

        // submit and reopen
        SignatureTemplateDTO signatureTemplate = signatureClient.getTemplates().stream()
                .filter(t -> t.getName().equals(SIGNATURE_TEMPLATE_AUTHOR_AND_BOB))
                .findFirst().orElseThrow();
        experimentClient.completeAndSubmitExperiment(experiment.id(), signatureTemplate.getId());
        experimentClient.reopenExperiment(experiment.id());

        // register sample
        experiment.mutate(new ReactionOutputSampleMutation.RegisterSample(experiment.outputSample(2, 1).getAnchor()));

        // add another output sample
        experiment.mutateAddProductSample(2);

        // register another sample
        experiment.mutate(new ReactionOutputSampleMutation.RegisterSample(experiment.outputSample(2, 2).getAnchor()));

        // submit and reopen
        experimentClient.completeAndSubmitExperiment(experiment.id(), signatureTemplate.getId());
        experimentClient.reopenExperiment(experiment.id());
    }

    @Test
    @Order(7)
    void submitExperiment() {
        ExperimentObject experiment = createExperiment(PROJECT_WITH_DATA, NOTEBOOK_88888888, templateClient.getByName(TEMPLATE_DEFAULT), EXPERIMENT_TO_SUBMIT);
        SignatureTemplateDTO signatureTemplate = signatureClient.getTemplates().stream()
                .filter(t -> t.getName().equals(SIGNATURE_TEMPLATE_AUTHOR_AND_BOB))
                .findFirst().orElseThrow();
        ExperimentDetailsDTO submitted = experimentClient.completeAndSubmitExperiment(experiment.id(), signatureTemplate.getId());
        assertThat(submitted.getStatus()).isEqualTo(SUBMITTED);
    }

    @Test
    @Order(8)
    void verifyFilledExperiment() {
        ExperimentObject experiment = findExperiment(PROJECT_WITH_DATA, NOTEBOOK_88888888, EXPERIMENT_WITH_DATA);

        assertThat(experiment.status()).isEqualTo(REOPEN);
        assertThat(experiment.experiment().getDescription()).isEqualTo(EXPERIMENT_WITH_DATA);
        assertThat(experiment.experiment().getAttachments()).extracting(AttachmentDTO::getName).contains(ATTACHMENT_FILENAME);
        assertThat(experiment.reaction().getOutputs()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(experiment.inputSample(1, 1)).hasWeight(100, G);
        assertThat(experiment.outputSample(2, 1).getRegistrationStatus()).isEqualTo(SampleRegistrationStatus.REGISTERED);
        assertThat(experiment.outputSample(2, 1).getSampleId()).isNotNull();
        assertThat(experiment.outputSample(2, 1)).hasActualMol(200, MMOL);
        assertThat(experiment.outputSample(2, 1)).hasActualWeight(10, G);
        assertThat(experiment.outputSample(2, 1).getPurity()).hasValue(0.5);
        assertThat(experiment.outputSample(2, 1).getBatchComment()).isEqualTo(BATCH_COMMENT);
        assertThat(experiment.outputSample(2, 2).getRegistrationStatus()).isEqualTo(SampleRegistrationStatus.REGISTERED);
        assertThat(experiment.outputSample(2, 2).getSampleId()).isNotNull();
    }

    @Test
    @Order(9)
    void verifySubmittedExperiment() {
        ExperimentObject experiment = findExperiment(PROJECT_WITH_DATA, NOTEBOOK_88888888, EXPERIMENT_TO_SUBMIT);
        assertThat(experiment.status()).isEqualTo(SUBMITTED);
    }

    private ExperimentObject createExperiment(String projectName, String notebookName, TemplateDTO template, String experimentDescription) {
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
        ExperimentRequest request = new ExperimentRequest(
                template.getId(),
                experimentDescription,
                therapeuticArea,
                projectCode
        );
        ExperimentDetailsDTO experiment = experimentClient.createExperiment(notebook.getId(), request);
        return new ExperimentObject(experiment, experimentClient, compoundClient, miscClient);
    }

    private ExperimentObject findExperiment(String projectName, String notebookName, String experimentDescription) {
        Page<ProjectDTO> projects = projectClient.getProjects(projectName, SortOrder.EARLIEST, null, Paging.DEFAULT);
        assertThat(projects.getItems()).isNotEmpty();
        ProjectDetailsDTO project = projectClient.getProject(projects.getItems().getFirst().getId());
        Page<NotebookDTO> notebooks = notebookClient.getProjectNotebooks(project.getId(), notebookName, SortOrder.EARLIEST, null, Paging.DEFAULT);
        assertThat(notebooks.getItems()).isNotEmpty();
        NotebookDetailsDTO notebook = notebookClient.getNotebook(notebooks.getItems().getFirst().getId());
        Page<ExperimentDTO> experiments = experimentClient.getNotebookExperiments(notebook.getId(), experimentDescription, null, null, null, Paging.DEFAULT);
        assertThat(experiments.getItems()).hasSize(1);
        return new ExperimentObject(experimentClient.getExperiment(experiments.getItems().getFirst().getId()), experimentClient, compoundClient, miscClient);
    }
}

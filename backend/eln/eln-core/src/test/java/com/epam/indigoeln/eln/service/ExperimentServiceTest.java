package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.api.MutateModelForm;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.epam.indigoeln.common.util.ModelUtil.loadResource;
import static com.epam.indigoeln.test.ClientCallAssert.assertThatClientCall;
import static org.assertj.core.api.Assertions.assertThat;


@QuarkusTest
@JwtSecurity
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
class ExperimentServiceTest extends ELNBaseTest {

    ProjectDetailsDTO project;
    NotebookDetailsDTO notebook;
    List<DictionaryItemRef> therapeuticAreas;
    List<DictionaryItemRef> projectCodes;

    @BeforeEach
    void setUp() {
        therapeuticAreas = dictionaryClient.getDictionary(BuiltInDictionary.THERAPEUTIC_AREA);
        projectCodes = dictionaryClient.getDictionary(BuiltInDictionary.PROJECT_CODE);
        project = projectClient.createProject(new ProjectRequest("ExperimentServiceTest" + UUID.randomUUID()));
        notebook = notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
    }

    @Test
    void testCreateExperimentValidation() {
        assertThatClientCall(() -> experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(null)))
                .isBadRequest("must not be null");
    }

    @Test
    void testCreateExperimentBadDictionary() {
        assertThatClientCall(() -> experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID, null, new DictionaryItemRef(UUID.randomUUID(), "Invalid"), null)))
                .isNotFound(".+ in dictionary THERAPEUTIC_AREA not found");
    }

    @Test
    void testCreateExperiment() {
        ExperimentDetailsDTO experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID
                , "description"
                , therapeuticAreas.getFirst()
                , projectCodes.getFirst()
        ));
        assertThat(experiment.getId()).isNotNull();
        assertThat(experiment.getName()).startsWith(notebook.getName() + "-").matches("\\d{8}-\\d{4}");
        assertThat(experiment.getCreatedBy().getDisplayName()).isEqualTo(JOHN_DISPLAY_NAME);
        assertThat(experiment.getCreatedAt()).isNotNull();
        assertThat(experiment.getModifiedBy().getDisplayName()).isEqualTo(JOHN_DISPLAY_NAME);
        assertThat(experiment.getModifiedAt()).isNotNull();
        assertThat(experiment.getStatus()).isEqualTo(ExperimentStatus.OPEN);
        assertThat(experiment.getDescription()).isEqualTo("description");
        assertThat(experiment.getTherapeuticArea()).isEqualTo(therapeuticAreas.getFirst());
        assertThat(experiment.getProjectCode()).isEqualTo(projectCodes.getFirst());
        assertThat(experiment.getMarked()).isFalse();
        assertThat(experiment.getTemplateId()).isEqualTo(emptyTemplateID);
    }

    @Test
    void testGetExperiment() {
        ExperimentDetailsDTO createdExperiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        ExperimentDetailsDTO loadedExperiment = experimentClient.getExperiment(createdExperiment.getId());
        assertThat(loadedExperiment).usingRecursiveComparison().isEqualTo(createdExperiment);
    }

    @Test
    void testGetExperiments() {
        ExperimentDetailsDTO createdExperiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        Page<ExperimentDTO> experiments = experimentClient.getProjectExperiments(project.getId(), null, null, Paging.DEFAULT);
        assertThat(experiments.getItems()).hasSize(1).first().satisfies(experiment -> {
            assertThat(experiment.getId()).isNotNull();
            assertThat(experiment.getName()).isEqualTo(createdExperiment.getName());
            assertThat(experiment.getCreatedBy().getDisplayName()).isEqualTo(JOHN_DISPLAY_NAME);
            assertThat(experiment.getCreatedAt()).isNotNull();
            assertThat(experiment.getModifiedBy().getDisplayName()).isEqualTo(JOHN_DISPLAY_NAME);
            assertThat(experiment.getModifiedAt()).isNotNull();
        });
    }

    @Test
    void testGetExperimentsSortedByEarliest() {
        experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));

        Page<ExperimentDTO> experiments = experimentClient.getProjectExperiments(project.getId(), SortOrder.EARLIEST, null, Paging.DEFAULT);

        assertThat(experiments.getItems())
                .isSortedAccordingTo(Comparator.comparing(ExperimentDTO::getModifiedAt));
    }

    @Test
    void testGetExperimentsSortedByLatest() {
        experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));

        Page<ExperimentDTO> experiments = experimentClient.getProjectExperiments(project.getId(), SortOrder.LATEST, null, Paging.DEFAULT);

        assertThat(experiments.getItems())
                .isSortedAccordingTo(Comparator.comparing(ExperimentDTO::getModifiedAt).reversed());
    }

    @Test
    void testGetExperimentsCreatedByMe() {
        withUser(ELNBaseTest.JOHN_USERNAME, () -> {
            experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
            experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        });

        withUser(BART_USERNAME, () -> {
            experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        });

        withUser(ELNBaseTest.JOHN_USERNAME, () -> {
            Page<ExperimentDTO> experiments = experimentClient.getProjectExperiments(project.getId(), null, true, Paging.DEFAULT);

            assertThat(experiments.getItems())
                    .allSatisfy(experiment -> assertThat(experiment.getCreatedBy().getDisplayName()).isEqualTo(JOHN_DISPLAY_NAME));

            assertThat(experiments.getItems())
                    .noneSatisfy(experiment -> assertThat(experiment.getCreatedBy().getDisplayName()).isEqualTo(BART_DISPLAY_NAME));
        });
    }

    @Test
    void testEditExperiment() {
        ExperimentDetailsDTO experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID
                , "d"
                , therapeuticAreas.getFirst()
                , projectCodes.getFirst()
        ));
        ExperimentDetailsDTO notModified = experimentClient.editExperiment(experiment.getId(), new ExperimentEditRequest(null, null));
        assertThat(notModified).usingRecursiveComparison(COMPARE_WITHOUT_MODIFIED_AT).isEqualTo(experiment);
        ExperimentDetailsDTO modified = experimentClient.editExperiment(experiment.getId(), new ExperimentEditRequest(
                Optional.of(therapeuticAreas.get(1)),
                Optional.of(projectCodes.get(1)
                )));
        assertThat(modified.getName()).isEqualTo(experiment.getName());
        assertThat(modified.getTherapeuticArea()).isEqualTo(therapeuticAreas.get(1));
        assertThat(modified.getProjectCode()).isEqualTo(projectCodes.get(1));
        ExperimentDetailsDTO saved = experimentClient.getExperiment(experiment.getId());
        assertThat(saved).usingRecursiveComparison().isEqualTo(modified);
    }

    @Test
    void testMarkExperiment() {
        ExperimentDetailsDTO experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        assertThat(experiment.getMarked()).isFalse();
        assertThat(experimentClient.getMarkedExperiments()).isEmpty();

        assertThat(experimentClient.markExperiment(experiment.getId())).isTrue();
        Page<ExperimentDTO> experiments = experimentClient.getProjectExperiments(project.getId(), null, null, Paging.DEFAULT);
        assertThat(experiments.getItems()).singleElement().satisfies(e -> {
            assertThat(e.getMarked()).isTrue();
        });
        experiments = experimentClient.getNotebookExperiments(notebook.getId(), null, null, Paging.DEFAULT);
        assertThat(experiments.getItems()).singleElement().satisfies(e -> {
            assertThat(e.getMarked()).isTrue();
        });
        ExperimentDetailsDTO loadedExperiment = experimentClient.getExperiment(experiment.getId());
        assertThat(loadedExperiment.getMarked()).isTrue();
        assertThat(experimentClient.getMarkedExperiments()).singleElement().satisfies(e -> {
            assertThat(e.getId()).isEqualTo(experiment.getId());
            assertThat(e.getMarked()).isTrue();
        });

        assertThat(experimentClient.unmarkExperiment(experiment.getId())).isFalse();
        experiments = experimentClient.getProjectExperiments(project.getId(), null, null, Paging.DEFAULT);
        assertThat(experiments.getItems()).singleElement().satisfies(e -> {
            assertThat(e.getMarked()).isFalse();
        });
        experiments = experimentClient.getNotebookExperiments(notebook.getId(), null, null, Paging.DEFAULT);
        assertThat(experiments.getItems()).singleElement().satisfies(e -> {
            assertThat(e.getMarked()).isFalse();
        });
        loadedExperiment = experimentClient.getExperiment(experiment.getId());
        assertThat(loadedExperiment.getMarked()).isFalse();
        assertThat(experimentClient.getMarkedExperiments()).isEmpty();
    }

    @Test
    void testCreateAttachment(@TempDir Path tempDir) {
        ExperimentDetailsDTO experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        List<AttachmentDTO> attachments = experimentClient.createExperimentAttachment(experiment.getId(), "attachment.txt", tempDir, "content".getBytes());
        assertThat(attachments).singleElement().satisfies(a -> {
            assertThat(a.getId()).isNotNull();
            assertThat(a.getName()).isEqualTo("attachment.txt");
            assertThat(a.getCreatedBy().getDisplayName()).isEqualTo(JOHN_DISPLAY_NAME);
            assertThat(a.getCreatedAt()).isNotNull();
            assertThat(a.getModifiedBy().getDisplayName()).isEqualTo(JOHN_DISPLAY_NAME);
            assertThat(a.getModifiedAt()).isNotNull();
        });
    }

    @Test
    void testDownloadAttachment(@TempDir Path tempDir) throws Exception {
        ExperimentDetailsDTO experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        List<AttachmentDTO> attachments = experimentClient.createExperimentAttachment(experiment.getId(), "attachment.txt", tempDir, "content".getBytes());
        Response response = experimentClient.downloadExperimentAttachmentClient(experiment.getId(), attachments.getFirst().getId());
        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION)).containsExactly("attachment; filename=attachment.txt");
        assertThat((byte[]) response.getEntity()).asString().isEqualTo("content");
    }

    @Test
    void testDeleteAttachment(@TempDir Path tempDir) {
        ExperimentDetailsDTO experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        List<AttachmentDTO> attachments = experimentClient.createExperimentAttachment(experiment.getId(), "attachment.txt", tempDir, "content".getBytes());
        experimentClient.deleteExperimentAttachment(experiment.getId(), attachments.getFirst().getId());
        experiment = experimentClient.getExperiment(experiment.getId());
        assertThat(experiment.getAttachments()).isEmpty();
    }

    @Test
    @SneakyThrows
    void testGetPicture() {
        ExperimentDetailsDTO experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        Response response = experimentClient.getExperimentPictureClient(experiment.getId());
        assertThat((byte[]) response.getEntity()).containsExactly(ExperimentService.EMPTY_PICTURE);
        ExperimentModel model = experimentClient.getExperimentModel(experiment.getId());
        String molFile = new String(loadResource(getClass(), "/reaction.rxn"));
        experimentClient.mutateExperimentModel(experiment.getId(), new MutateModelForm(model, new ReactionMutation.SetScheme(model.getReactions().getFirst().getAnchor(), molFile)));
        response = experimentClient.getExperimentPictureClient(experiment.getId());
//        assertThat(response).isNotEqualTo(ExperimentService.EMPTY_PICTURE);
        Files.write(Paths.get("picture.svg"), (byte[]) response.getEntity());
    }
}

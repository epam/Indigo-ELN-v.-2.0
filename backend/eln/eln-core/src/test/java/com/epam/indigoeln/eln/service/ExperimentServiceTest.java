package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.BaseTest;
import com.epam.indigoeln.eln.api.MutateModelForm;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.util.ResponseWithHeaders;
import com.epam.indigoeln.eln.util.TestHelper;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.epam.indigoeln.eln.service.CustomAssertions.assertThatClientCall;
import static org.assertj.core.api.Assertions.assertThat;


@QuarkusTest
@JwtSecurity
@TestSecurity(user = TestHelper.JOHN_USERNAME)
class ExperimentServiceTest extends BaseTest {

    ProjectDetailsDTO project;
    NotebookDetailsDTO notebook;
    List<DictionaryItemRef> therapeuticAreas;
    List<DictionaryItemRef> projectCodes;

    @BeforeEach
    void setUp() {
        therapeuticAreas = miscClient.getDictionary(Dictionary.THERAPEUTIC_AREA);
        projectCodes = miscClient.getDictionary(Dictionary.PROJECT_CODE);
        project = projectsClient.createProject(new ProjectRequest("ExperimentServiceTest" + UUID.randomUUID()));
        notebook = notebooksClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
    }

    @Test
    void testCreateExperimentValidation() {
        assertThatClientCall(() -> experimentsClient.createExperiment(notebook.getId(), new ExperimentRequest(null)))
                .isBadRequest("must not be null");
    }

    @Test
    void testCreateExperimentBadDictionary() {
        assertThatClientCall(() -> experimentsClient.createExperiment(notebook.getId(), new ExperimentRequest(getEmptyTemplateID(), null, new DictionaryItemRef(UUID.randomUUID(), "Invalid"), null)))
                .isNotFound("THERAPEUTIC_AREA .+ not found");
    }

    @Test
    void testCreateExperiment() {
        ExperimentDetailsDTO experiment = experimentsClient.createExperiment(notebook.getId(), new ExperimentRequest(getEmptyTemplateID()
                , "description"
                , therapeuticAreas.getFirst()
                , projectCodes.getFirst()
        ));
        assertThat(experiment.getId()).isNotNull();
        assertThat(experiment.getName()).startsWith(notebook.getName() + "-").matches("\\d{8}-\\d{4}");
        assertThat(experiment.getCreatedBy().getDisplayName()).isEqualTo(TestHelper.JOHN_DISPLAY_NAME);
        assertThat(experiment.getCreatedAt()).isNotNull();
        assertThat(experiment.getModifiedBy().getDisplayName()).isEqualTo(TestHelper.JOHN_DISPLAY_NAME);
        assertThat(experiment.getModifiedAt()).isNotNull();
        assertThat(experiment.getStatus()).isEqualTo(ExperimentStatus.OPEN);
        assertThat(experiment.getDescription()).isEqualTo("description");
        assertThat(experiment.getTherapeuticArea()).isEqualTo(therapeuticAreas.getFirst());
        assertThat(experiment.getProjectCode()).isEqualTo(projectCodes.getFirst());
        assertThat(experiment.getMarked()).isFalse();
    }

    @Test
    void testGetExperiment() {
        ExperimentDetailsDTO createdExperiment = experimentsClient.createExperiment(notebook.getId(), new ExperimentRequest(getEmptyTemplateID()));
        ExperimentDetailsDTO loadedExperiment = experimentsClient.getExperiment(createdExperiment.getId());
        assertThat(loadedExperiment).usingRecursiveComparison().isEqualTo(createdExperiment);
    }

    @Test
    void testGetExperiments() {
        ExperimentDetailsDTO createdExperiment = experimentsClient.createExperiment(notebook.getId(), new ExperimentRequest(getEmptyTemplateID()));
        Page<ExperimentDTO> experiments = experimentsClient.getProjectExperiments(project.getId(), Paging.DEFAULT);
        assertThat(experiments.getItems()).hasSize(1).first().satisfies(experiment -> {
            assertThat(experiment.getId()).isNotNull();
            assertThat(experiment.getName()).isEqualTo(createdExperiment.getName());
            assertThat(experiment.getCreatedBy().getDisplayName()).isEqualTo(TestHelper.JOHN_DISPLAY_NAME);
            assertThat(experiment.getCreatedAt()).isNotNull();
            assertThat(experiment.getModifiedBy().getDisplayName()).isEqualTo(TestHelper.JOHN_DISPLAY_NAME);
            assertThat(experiment.getModifiedAt()).isNotNull();
        });
    }

    @Test
    void testEditExperiment() {
        ExperimentDetailsDTO experiment = experimentsClient.createExperiment(notebook.getId(), new ExperimentRequest(getEmptyTemplateID()
                , "d"
                , therapeuticAreas.getFirst()
                , projectCodes.getFirst()
        ));
        ExperimentDetailsDTO notModified = experimentsClient.editExperiment(experiment.getId(), new ExperimentEditRequest(null, null));
        assertThat(notModified).usingRecursiveComparison(TestHelper.COMPARE_WITHOUT_MODIFIED_AT).isEqualTo(experiment);
        ExperimentDetailsDTO modified = experimentsClient.editExperiment(experiment.getId(), new ExperimentEditRequest(
                Optional.of(therapeuticAreas.get(1)),
                Optional.of(projectCodes.get(1)
        )));
        assertThat(modified.getName()).isEqualTo(experiment.getName());
        assertThat(modified.getTherapeuticArea()).isEqualTo(therapeuticAreas.get(1));
        assertThat(modified.getProjectCode()).isEqualTo(projectCodes.get(1));
        ExperimentDetailsDTO saved = experimentsClient.getExperiment(experiment.getId());
        assertThat(saved).usingRecursiveComparison().isEqualTo(modified);
    }

    @Test
    void testMarkExperiment() {
        ExperimentDetailsDTO experiment = experimentsClient.createExperiment(notebook.getId(), new ExperimentRequest(getEmptyTemplateID()));
        assertThat(experiment.getMarked()).isFalse();
        assertThat(experimentsClient.getMarkedExperiments()).isEmpty();

        assertThat(experimentsClient.markExperiment(experiment.getId())).isTrue();
        Page<ExperimentDTO> experiments = experimentsClient.getProjectExperiments(project.getId(), Paging.DEFAULT);
        assertThat(experiments.getItems()).singleElement().satisfies(e -> {
            assertThat(e.getMarked()).isTrue();
        });
        experiments = experimentsClient.getNotebookExperiments(notebook.getId(), Paging.DEFAULT);
        assertThat(experiments.getItems()).singleElement().satisfies(e -> {
            assertThat(e.getMarked()).isTrue();
        });
        ExperimentDTO loadedExperiment = experimentsClient.getExperiment(experiment.getId());
        assertThat(loadedExperiment.getMarked()).isTrue();
        assertThat(experimentsClient.getMarkedExperiments()).singleElement().satisfies(e -> {
            assertThat(e.getId()).isEqualTo(experiment.getId());
            assertThat(e.getMarked()).isTrue();
        });

        assertThat(experimentsClient.unmarkExperiment(experiment.getId())).isFalse();
        experiments = experimentsClient.getProjectExperiments(project.getId(), Paging.DEFAULT);
        assertThat(experiments.getItems()).singleElement().satisfies(e -> {
            assertThat(e.getMarked()).isFalse();
        });
        experiments = experimentsClient.getNotebookExperiments(notebook.getId(), Paging.DEFAULT);
        assertThat(experiments.getItems()).singleElement().satisfies(e -> {
            assertThat(e.getMarked()).isFalse();
        });
        loadedExperiment = experimentsClient.getExperiment(experiment.getId());
        assertThat(loadedExperiment.getMarked()).isFalse();
        assertThat(experimentsClient.getMarkedExperiments()).isEmpty();
    }

    @Test
    void testCreateAttachment(@TempDir Path tempDir) {
        ExperimentDetailsDTO experiment = experimentsClient.createExperiment(notebook.getId(), new ExperimentRequest(getEmptyTemplateID()));
        List<AttachmentDTO> attachments = experimentsClient.createExperimentAttachment(experiment.getId(), "attachment.txt", tempDir, "content".getBytes());
        assertThat(attachments).singleElement().satisfies(a -> {
            assertThat(a.getId()).isNotNull();
            assertThat(a.getName()).isEqualTo("attachment.txt");
            assertThat(a.getCreatedBy().getDisplayName()).isEqualTo(TestHelper.JOHN_DISPLAY_NAME);
            assertThat(a.getCreatedAt()).isNotNull();
            assertThat(a.getModifiedBy().getDisplayName()).isEqualTo(TestHelper.JOHN_DISPLAY_NAME);
            assertThat(a.getModifiedAt()).isNotNull();
        });
    }

    @Test
    void testDownloadAttachment(@TempDir Path tempDir) throws Exception {
        ExperimentDetailsDTO experiment = experimentsClient.createExperiment(notebook.getId(), new ExperimentRequest(getEmptyTemplateID()));
        List<AttachmentDTO> attachments = experimentsClient.createExperimentAttachment(experiment.getId(), "attachment.txt", tempDir, "content".getBytes());
        ResponseWithHeaders response = experimentsClient.downloadExperimentAttachmentClient(experiment.getId(), attachments.getFirst().getId());
        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION)).containsExactly("attachment; filename=attachment.txt");
        assertThat(response.getContent()).hasContent("content");
    }

    @Test
    void testDeleteAttachment(@TempDir Path tempDir) {
        ExperimentDetailsDTO experiment = experimentsClient.createExperiment(notebook.getId(), new ExperimentRequest(getEmptyTemplateID()));
        List<AttachmentDTO> attachments = experimentsClient.createExperimentAttachment(experiment.getId(), "attachment.txt", tempDir, "content".getBytes());
        experimentsClient.deleteExperimentAttachment(experiment.getId(), attachments.getFirst().getId());
        experiment = experimentsClient.getExperiment(experiment.getId());
        assertThat(experiment.getAttachments()).isEmpty();
    }

    @Test
    @SneakyThrows
    void testGetPicture() {
        ExperimentDetailsDTO experiment = experimentsClient.createExperiment(notebook.getId(), new ExperimentRequest(getEmptyTemplateID()));
        ResponseWithHeaders response = experimentsClient.getExperimentPictureClient(experiment.getId());
        assertThat(response.getContent()).hasBinaryContent(ExperimentService.EMPTY_PICTURE);
        ExperimentModel model = experimentsClient.getExperimentModel(experiment.getId());
        String molFile = new String(getClass().getResourceAsStream("/reaction.rxn").readAllBytes());
        experimentsClient.mutateExperimentModel(experiment.getId(), new MutateModelForm(model, new ReactionMutation.SetScheme(0, molFile)));
        response = experimentsClient.getExperimentPictureClient(experiment.getId());
//        assertThat(response).isNotEqualTo(ExperimentService.EMPTY_PICTURE);
        Files.write(Paths.get("picture.svg"), response.getContent().readAllBytes());
    }
}

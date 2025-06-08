package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.BaseTest;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.util.ResponseWithHeaders;
import com.epam.indigoeln.eln.util.TestHelper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.ws.rs.core.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
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
    List<DictionaryRef> therapeuticAreas;
    List<DictionaryRef> projectCodes;

    @BeforeEach
    void setUp() {
        therapeuticAreas = miscClient.getDictionary(Dictionary.THERAPEUTIC_AREA);
        projectCodes = miscClient.getDictionary(Dictionary.PROJECT_CODE);
        project = projectsClient.createProject(new ProjectRequest("ExperimentServiceTest"));
        notebook = notebooksClient.createNotebook(project.getId(), new NotebookRequest("ExperimentServiceTest"));
    }

    @Test
    void testCreateExperimentValidation() {
        assertThatClientCall(() -> experimentsClient.createExperiment(notebook.getId(), new ExperimentRequest(null)))
                .isBadRequest();
    }

    @Test
    void testCreateExperimentBadDictionary() {
        assertThatClientCall(() -> experimentsClient.createExperiment(notebook.getId(), new ExperimentRequest("testCreateExperimentBadDictionary", null, new DictionaryRef(UUID.randomUUID(), "Invalid"), null)))
                .isBadRequest();
    }

    @Test
    void testCreateExperiment() {
        ExperimentDetailsDTO experiment = experimentsClient.createExperiment(notebook.getId(), new ExperimentRequest("testCreateExperiment"
                , "description"
                , therapeuticAreas.getFirst()
                , projectCodes.getFirst()
        ));
        assertThat(experiment.getId()).isNotNull();
        assertThat(experiment.getName()).isEqualTo("testCreateExperiment");
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
        ExperimentDetailsDTO createdExperiment = experimentsClient.createExperiment(notebook.getId(), new ExperimentRequest("testGetExperiment"));
        ExperimentDetailsDTO loadedExperiment = experimentsClient.getExperiment(createdExperiment.getId());
        assertThat(loadedExperiment).usingRecursiveComparison().isEqualTo(createdExperiment);
    }

    @Test
    void testGetExperiments() {
        ExperimentDetailsDTO createdExperiment = experimentsClient.createExperiment(notebook.getId(), new ExperimentRequest("testGetExperiments"));
        Page<ExperimentDTO> experiments = experimentsClient.getProjectExperiments(project.getId(), Paging.DEFAULT);
        assertThat(experiments.getItems()).hasSize(1).first().satisfies(experiment -> {
            assertThat(experiment.getId()).isNotNull();
            assertThat(experiment.getName()).isEqualTo("testGetExperiments");
            assertThat(experiment.getCreatedBy().getDisplayName()).isEqualTo(TestHelper.JOHN_DISPLAY_NAME);
            assertThat(experiment.getCreatedAt()).isNotNull();
            assertThat(experiment.getModifiedBy().getDisplayName()).isEqualTo(TestHelper.JOHN_DISPLAY_NAME);
            assertThat(experiment.getModifiedAt()).isNotNull();
        });
    }

    @Test
    void testEditExperiment() {
        ExperimentDetailsDTO experiment = experimentsClient.createExperiment(notebook.getId(), new ExperimentRequest("testEditExperiment"
                , "d"
                , therapeuticAreas.getFirst()
                , projectCodes.getFirst()
        ));
        ExperimentDetailsDTO notModified = experimentsClient.editExperiment(experiment.getId(), new ExperimentEditRequest(null, null, null));
        assertThat(notModified).usingRecursiveComparison(TestHelper.COMPARE_WITHOUT_MODIFIED_AT).isEqualTo(experiment);
        ExperimentDetailsDTO modified = experimentsClient.editExperiment(experiment.getId(), new ExperimentEditRequest(Optional.of("testEditExperiment_new")
                , Optional.of(therapeuticAreas.get(1))
                , Optional.of(projectCodes.get(1)
        )));
        assertThat(modified.getName()).isEqualTo("testEditExperiment_new");
        assertThat(modified.getTherapeuticArea()).isEqualTo(therapeuticAreas.get(1));
        assertThat(modified.getProjectCode()).isEqualTo(projectCodes.get(1));
        ExperimentDetailsDTO saved = experimentsClient.getExperiment(experiment.getId());
        assertThat(saved).usingRecursiveComparison().isEqualTo(modified);
    }

    @Test
    void testMarkExperiment() {
        ExperimentDetailsDTO experiment = experimentsClient.createExperiment(notebook.getId(), new ExperimentRequest("testMarkExperiment"));
        assertThat(experiment.getMarked()).isFalse();

        assertThat(experimentsClient.markExperiment(experiment.getId())).isTrue();
        Page<ExperimentDTO> experiments = experimentsClient.getProjectExperiments(project.getId(), Paging.DEFAULT);
        assertThat(experiments.getItems()).singleElement().satisfies(e -> {
            assertThat(e.getMarked()).isTrue();
        });
        experiments = experimentsClient.getNotebookExperiments(notebook.getId(), Paging.DEFAULT);
        assertThat(experiments.getItems()).singleElement().satisfies(e -> {
            assertThat(e.getMarked()).isTrue();
        });
        experiment = experimentsClient.getExperiment(experiment.getId());
        assertThat(experiment.getMarked()).isTrue();

        assertThat(experimentsClient.unmarkExperiment(experiment.getId())).isFalse();
        experiments = experimentsClient.getProjectExperiments(project.getId(), Paging.DEFAULT);
        assertThat(experiments.getItems()).singleElement().satisfies(e -> {
            assertThat(e.getMarked()).isFalse();
        });
        experiments = experimentsClient.getNotebookExperiments(notebook.getId(), Paging.DEFAULT);
        assertThat(experiments.getItems()).singleElement().satisfies(e -> {
            assertThat(e.getMarked()).isFalse();
        });
        experiment = experimentsClient.getExperiment(experiment.getId());
        assertThat(experiment.getMarked()).isFalse();
    }

    @Test
    void testCreateAttachment(@TempDir Path tempDir) {
        ExperimentDetailsDTO experiment = experimentsClient.createExperiment(notebook.getId(), new ExperimentRequest("testCreateAttachment"));
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
        ExperimentDetailsDTO experiment = experimentsClient.createExperiment(notebook.getId(), new ExperimentRequest("testDownloadAttachment"));
        List<AttachmentDTO> attachments = experimentsClient.createExperimentAttachment(experiment.getId(), "attachment.txt", tempDir, "content".getBytes());
        ResponseWithHeaders response = experimentsClient.downloadExperimentAttachmentClient(experiment.getId(), attachments.getFirst().getId());
        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION)).containsExactly("attachment; filename=attachment.txt");
        assertThat(response.getValue().asInputStream()).hasContent("content");
    }

    @Test
    void testDeleteAttachment(@TempDir Path tempDir) {
        ExperimentDetailsDTO experiment = experimentsClient.createExperiment(notebook.getId(), new ExperimentRequest("testDeleteAttachment"));
        List<AttachmentDTO> attachments = experimentsClient.createExperimentAttachment(experiment.getId(), "attachment.txt", tempDir, "content".getBytes());
        experimentsClient.deleteExperimentAttachment(experiment.getId(), attachments.getFirst().getId());
        experiment = experimentsClient.getExperiment(experiment.getId());
        assertThat(experiment.getAttachments()).isEmpty();
    }
}

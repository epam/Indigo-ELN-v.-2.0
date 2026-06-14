package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.SortOrder;
import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import com.epam.indigoeln.test.FeignUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openapitools.jackson.nullable.JsonNullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.epam.indigoeln.common.util.ContentDispositionUtil.extractFilename;
import static com.epam.indigoeln.common.util.ModelUtil.loadResourceAsString;
import static com.epam.indigoeln.eln.model.ApplicationPermission.*;
import static com.epam.indigoeln.test.ClientCallAssert.assertThatClientCall;
import static org.assertj.core.api.Assertions.assertThat;


@QuarkusTest
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
class ExperimentServiceTest extends ELNBaseTest {

    ProjectDetailsDTO project;
    NotebookDetailsDTO notebook;
    List<TherapeuticAreaRef> therapeuticAreas;
    List<ProjectCodeRef> projectCodes;

    @BeforeEach
    void setUp() {
        therapeuticAreas = dictionaryClient.getDictionary(BuiltInDictionary.THERAPEUTIC_AREA);
        projectCodes = dictionaryClient.getDictionary(BuiltInDictionary.PROJECT_CODE);
        project = projectClient.createProject(new ProjectRequest("ExperimentServiceTest" + UUID.randomUUID()));
        notebook = notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
    }

    @Test
    void testCreateExperimentValidation() {
        //noinspection DataFlowIssue
        assertThatClientCall(() -> experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(null)))
                .isBadRequest("must not be null");
    }

    @Test
    void testCreateExperimentBadDictionary() {
        assertThatClientCall(() -> experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID, null, new TherapeuticAreaRef(UUID.randomUUID(), "Invalid", true, false, BuiltInDictionary.THERAPEUTIC_AREA.getId()), null)))
                .isBadRequest("DICTIONARY_ITEM .+ not found");
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
        assertThat(experiment.getCurrentPermissions()).containsExactlyInAnyOrder(VIEW_EXPERIMENTS, EDIT_EXPERIMENTS, MANAGE_EXPERIMENT_ACCESS, DELETE_EXPERIMENTS, SUBMIT_EXPERIMENTS);
        assertThat(experiment.getRevision()).isOne();
        assertThat(experimentClient.getExperimentRevisions(experiment.getId(), null))
                .hasSize(1)
                .first().satisfies(revision -> {
                    assertThat(revision.getRevision()).isOne();
                    assertThat(revision.getDate()).isEqualTo(experiment.getCreatedAt());
                    assertThat(revision.getUser()).isEqualTo(JOHN_USER_REF);
                    assertThat(revision.getSummary()).isEqualTo("Experiment created");
                });
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
        Page<ExperimentDTO> experiments = experimentClient.getProjectExperiments(project.getId(), null, null, null, Paging.DEFAULT);
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

        Page<ExperimentDTO> experiments = experimentClient.getProjectExperiments(project.getId(), null, SortOrder.EARLIEST, null, Paging.DEFAULT);

        assertThat(experiments.getItems())
                .isSortedAccordingTo(Comparator.comparing(ExperimentDTO::getModifiedAt));
    }

    @Test
    void testGetExperimentsSortedByLatest() {
        experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));

        Page<ExperimentDTO> experiments = experimentClient.getProjectExperiments(project.getId(), null, SortOrder.LATEST, null, Paging.DEFAULT);

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
            Page<ExperimentDTO> experiments = experimentClient.getProjectExperiments(project.getId(), null, null, true, Paging.DEFAULT);

            assertThat(experiments.getItems())
                    .allSatisfy(experiment -> assertThat(experiment.getCreatedBy().getDisplayName()).isEqualTo(JOHN_DISPLAY_NAME));

            assertThat(experiments.getItems())
                    .noneSatisfy(experiment -> assertThat(experiment.getCreatedBy().getDisplayName()).isEqualTo(BART_DISPLAY_NAME));
        });
    }

    @Test
    void testEditExperimentNothingToUpdate() {
        ExperimentDetailsDTO experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID
                , "d"
                , therapeuticAreas.getFirst()
                , projectCodes.getFirst()
        ));
        assertThatClientCall(() -> {
            experimentClient.editExperiment(experiment.getId(), new ExperimentEditRequest());
        }).isBadRequest("Nothing to update");
    }

    @Test
    void testEditExperiment() {
        ExperimentDetailsDTO experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID
                , "d"
                , therapeuticAreas.getFirst()
                , projectCodes.getFirst()
        ));
        ExperimentDetailsDTO e2 = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID, "e2", therapeuticAreas.getFirst(), projectCodes.getFirst()));
        ExperimentDetailsDTO e3 = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID, "e3", therapeuticAreas.getFirst(), projectCodes.getFirst()));
        ExperimentDetailsDTO e4 = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID, "e4", therapeuticAreas.getFirst(), projectCodes.getFirst()));
        ExperimentDetailsDTO modified = experimentClient.editExperiment(experiment.getId(), new ExperimentEditRequest(
                JsonNullable.of("newTitle"),
                JsonNullable.of(therapeuticAreas.get(1)),
                JsonNullable.of(projectCodes.get(1)),
                JsonNullable.of("newDescription"),
                JsonNullable.of("newLiterature"),
                JsonNullable.of(Set.of(e2.toRef())),
                JsonNullable.of(Set.of(e3.toRef())),
                JsonNullable.of(Set.of(e4.toRef()))
        ));
        assertThat(modified.getName()).isEqualTo(experiment.getName());
        assertThat(modified.getTitle()).isEqualTo("newTitle");
        assertThat(modified.getTherapeuticArea()).isEqualTo(therapeuticAreas.get(1));
        assertThat(modified.getProjectCode()).isEqualTo(projectCodes.get(1));
        assertThat(modified.getDescription()).isEqualTo("newDescription");
        assertThat(modified.getLiterature()).isEqualTo("newLiterature");
        assertThat(modified.getLinkedExperiments()).containsExactly(e2.toRef());
        assertThat(modified.getContinuedFrom()).containsExactly(e3.toRef());
        assertThat(modified.getContinuedTo()).containsExactly(e4.toRef());
        ExperimentDetailsDTO saved = experimentClient.getExperiment(experiment.getId());
        assertThat(saved).usingRecursiveComparison().isEqualTo(modified);
        assertThat(experimentClient.getExperimentRevisions(experiment.getId(), true))
                .hasSize(2)
                .last().satisfies(revision -> {
                    assertThat(revision.getRevision()).isEqualTo(2);
                    assertThat(revision.getDate()).isEqualTo(modified.getModifiedAt());
                    assertThat(revision.getUser()).isEqualTo(JOHN_USER_REF);
                    assertThat(revision.getSummary()).matches("Edit: multiple attributes");
                });
    }

    @Test
    void testMarkExperiment() {
        ExperimentDetailsDTO experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        assertThat(experiment.getMarked()).isFalse();
        assertThat(experimentClient.getMarkedExperiments()).isEmpty();

        assertThat(experimentClient.markExperiment(experiment.getId())).isTrue();
        Page<ExperimentDTO> experiments = experimentClient.getProjectExperiments(project.getId(), null, null, null, Paging.DEFAULT);
        assertThat(experiments.getItems()).singleElement().satisfies(e -> {
            assertThat(e.getMarked()).isTrue();
        });
        experiments = experimentClient.getNotebookExperiments(notebook.getId(), null, null, null, Paging.DEFAULT);
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
        experiments = experimentClient.getProjectExperiments(project.getId(), null, null, null, Paging.DEFAULT);
        assertThat(experiments.getItems()).singleElement().satisfies(e -> {
            assertThat(e.getMarked()).isFalse();
        });
        experiments = experimentClient.getNotebookExperiments(notebook.getId(), null, null, null, Paging.DEFAULT);
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
        List<AttachmentDTO> attachments = experimentClient.createExperimentAttachment(experiment.getId(), "attachment.txt", "content".getBytes());
        assertThat(attachments).singleElement().satisfies(a -> {
            assertThat(a.getId()).isNotNull();
            assertThat(a.getName()).isEqualTo("attachment.txt");
            assertThat(a.getCreatedBy().getDisplayName()).isEqualTo(JOHN_DISPLAY_NAME);
            assertThat(a.getCreatedAt()).isNotNull();
            assertThat(a.getModifiedBy().getDisplayName()).isEqualTo(JOHN_DISPLAY_NAME);
            assertThat(a.getModifiedAt()).isNotNull();
        });
        assertThat(experimentClient.getExperimentRevisions(experiment.getId(), true))
                .last().satisfies(revision -> {
                    assertThat(revision.getSummary()).isEqualTo("Created attachment: attachment.txt, 7 bytes");
                });
    }

    @Test
    void testDownloadAttachment(@TempDir Path tempDir) throws Exception {
        ExperimentDetailsDTO experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        List<AttachmentDTO> attachments = experimentClient.createExperimentAttachment(experiment.getId(), "attachment.txt", "content".getBytes());
        try (Response response = experimentClient.downloadExperimentAttachment(experiment.getId(), attachments.getFirst().getId())) {
            assertThat(extractFilename(response.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION))).isEqualTo("attachment.txt");
            assertThat((byte[]) response.getEntity()).asString().isEqualTo("content");
        }
    }

    @Test
    void testDeleteAttachment(@TempDir Path tempDir) {
        ExperimentDetailsDTO experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        List<AttachmentDTO> attachments = experimentClient.createExperimentAttachment(experiment.getId(), "attachment.txt", "content".getBytes());
        experimentClient.deleteExperimentAttachment(experiment.getId(), attachments.getFirst().getId());
        experiment = experimentClient.getExperiment(experiment.getId());
        assertThat(experiment.getAttachments()).isEmpty();
        assertThat(experimentClient.getExperimentRevisions(experiment.getId(), true))
                .last().satisfies(revision -> {
                    assertThat(revision.getSummary()).isEqualTo("Deleted attachment: attachment.txt");
                });
    }

    @Test
    @SneakyThrows
    void testGetPicture() {
        ExperimentDetailsDTO experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        ExperimentModel model = experiment.getModel();
        Reaction reaction = model.getReactions().getFirst();
        byte[] response = experimentClient.getExperimentPicture(experiment.getId(), experiment.getRevision());
        assertThat(response).containsExactly(ExperimentService.EMPTY_PICTURE);
        response = experimentClient.getReactionPicture(experiment.getId(), reaction.getAnchor(), experiment.getRevision());
        assertThat(response).containsExactly(ExperimentService.EMPTY_PICTURE);

        String rxnFile = loadResourceAsString(getClass(), "/reaction.rxn");
        experimentClient.mutateExperimentModel4(experiment.getId(), experiment.getRevision(), new ReactionMutation.SetScheme(model.getReactions().getFirst().getAnchor(), rxnFile));

        model = experimentClient.getExperiment(experiment.getId()).getModel();
        reaction = model.getReactions().getFirst();

        response = experimentClient.getExperimentPicture(experiment.getId(), experiment.getRevision());
        assertThat(response).isNotEqualTo(ExperimentService.EMPTY_PICTURE);
        Files.write(Paths.get("picture.svg"), response);
        response = experimentClient.getReactionPicture(experiment.getId(), reaction.getAnchor(), experiment.getRevision());
        assertThat(response).isNotEqualTo(ExperimentService.EMPTY_PICTURE);
        assertThat(FeignUtil.getLastResponse().headers().get(HttpHeaders.CONTENT_TYPE).iterator().next()).isEqualTo("image/svg+xml");
        //noinspection deprecation
        CacheControl cacheControl = CacheControl.valueOf(FeignUtil.getLastResponse().headers().get("Cache-Control").iterator().next());
        assertThat(cacheControl.getMaxAge()).isPositive();
    }

    @Test
    void testUpdateAccess() {
        ExperimentDetailsDTO experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        experimentClient.updateExperimentAccess(experiment.getId(), AccessForm.of(MAGGIE_USERNAME, AccessLevel.EDIT));
        assertThat(experimentClient.getExperimentRevisions(experiment.getId(), true))
                .hasSize(2)
                .last().satisfies(revision -> {
                    assertThat(revision.getRevision()).isEqualTo(2);
                    assertThat(revision.getUser()).isEqualTo(JOHN_USER_REF);
                    assertThat(revision.getSummary()).isEqualTo("Edited Team: granted maggie EDIT access");
                });
        experimentClient.updateExperimentAccess(experiment.getId(), AccessForm.of(MAGGIE_USERNAME, AccessLevel.NONE));
        assertThat(experimentClient.getExperimentRevisions(experiment.getId(), true))
                .hasSize(3)
                .last().satisfies(revision -> {
                    assertThat(revision.getSummary()).isEqualTo("Edited Team: removed maggie");
                });
    }

    @Test
    void testSuggestExperiments() {
        NotebookDetailsDTO notebook2 = notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
        ExperimentDetailsDTO e1 = experimentClient.createExperiment(notebook2.getId(), new ExperimentRequest(emptyTemplateID));
        ExperimentDetailsDTO e2 = experimentClient.createExperiment(notebook2.getId(), new ExperimentRequest(emptyTemplateID));
        assertThat(experimentClient.suggestExperiments(notebook2.getName())).containsExactly(e1.toRef(), e2.toRef());
    }

    @Test
    void testQuickSearch() {
        ExperimentDetailsDTO e1 = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID, "description1 common", null, null));
        ExperimentDetailsDTO e2 = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID, "description2 common", null, null));

        Page<ExperimentDTO> result1 = experimentClient.getNotebookExperiments(notebook.getId(), e1.getName(), null, null, Paging.DEFAULT);
        assertThat(result1.getItems()).map(ExperimentDTO::getName).containsOnly(e1.getName());

        Page<ExperimentDTO> result2 = experimentClient.getNotebookExperiments(notebook.getId(), "description1", null, null, Paging.DEFAULT);
        assertThat(result2.getItems()).map(ExperimentDTO::getName).containsOnly(e1.getName());

        Page<ExperimentDTO> result3 = experimentClient.getNotebookExperiments(notebook.getId(), e1.getName().substring(4), null, null, Paging.DEFAULT);
        assertThat(result2.getItems()).map(ExperimentDTO::getName).containsOnly(e1.getName());
    }

    @Test
    @SneakyThrows
    void testExportSDF() {
        ExperimentDetailsDTO experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID, "An experiment", therapeuticAreas.getFirst(), projectCodes.getFirst()));
        ExperimentModel model = experiment.getModel();

        String rxnFile = loadResourceAsString(getClass(), "/reaction.rxn");
        experimentClient.mutateExperimentModel(experiment.getId(), new ReactionMutation.SetScheme(model.getReactions().getFirst().getAnchor(), rxnFile));

        byte[] result = experimentClient.exportSDF(experiment.getId());
        assertThat(result).asString().containsIgnoringWhitespaces(">  <molWeight>\n" +
                "180.16", ">  <chemicalName>");
    }
}

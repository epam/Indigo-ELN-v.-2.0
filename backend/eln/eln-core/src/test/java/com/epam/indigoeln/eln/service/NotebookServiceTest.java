package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.SortOrder;
import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.model.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.*;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static com.epam.indigoeln.common.util.ContentDispositionUtil.extractFilename;
import static com.epam.indigoeln.eln.model.ApplicationPermission.*;
import static com.epam.indigoeln.eln.test.ACLListAssert.assertThatACL;
import static com.epam.indigoeln.test.ClientCallAssert.assertThatClientCall;
import static org.assertj.core.api.Assertions.assertThat;


@QuarkusTest
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
class NotebookServiceTest extends ELNBaseTest {

    ProjectDetailsDTO project;

    @BeforeEach
    void setUp() {
        project = projectClient.createProject(new ProjectRequest("NotebookServiceTest" + UUID.randomUUID()));
    }

    @Test
    void testCreateNotebookValidation() {
        assertThatClientCall(() -> notebookClient.createNotebook(project.getId(), new NotebookRequest(null)))
                .isBadRequest("must not be empty");
    }

    @Test
    void testNameFormatValidation() {
        assertThatClientCall(() -> notebookClient.createNotebook(project.getId(), new NotebookRequest("not digits")))
                .isBadRequest("Notebook Name is invalid, use 8 digits only");
    }

    @Test
    void testDuplicateNames() {
        NotebookDetailsDTO notebook = createNotebook(project.getId());
        assertThatClientCall(() -> notebookClient.createNotebook(project.getId(), new NotebookRequest(notebook.getName())))
                .isBadRequest("Unique name is required");
    }

    @Test
    void testRenameDuplicateNames() {
        NotebookDetailsDTO notebook1 = createNotebook(project.getId());
        NotebookDetailsDTO notebook2 = createNotebook(project.getId());
        assertThatClientCall(() -> notebookClient.editNotebook(notebook2.getId(), new NotebookEditRequest().withName(JsonNullable.of(notebook1.getName()))))
                .isBadRequest("Unique name is required");
    }

    @Test
    void testCheckNotebookNameExistenceEndpointSuccessWhenExists() {
        NotebookDetailsDTO notebook = createNotebook(project.getId());

        assertThatClientCall(() -> notebookClient.checkNotebookNameExistence(notebook.getName()))
                .isSuccessfulWithResult(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getExists()).isTrue();
                });
    }

    @Test
    void testCheckNotebookNameExistenceEndpointSuccessWhenNotExists() {
        String name = notebookClient.getNextNotebookNumber();

        assertThatClientCall(() -> notebookClient.checkNotebookNameExistence(name))
                .isSuccessfulWithResult(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getExists()).isFalse();
                });
    }

    @Test
    void testCheckNotebookNameExistenceEndpointValidationEmptyName() {
        assertThatClientCall(() -> notebookClient.checkNotebookNameExistence(""))
                .isBadRequest("must not be empty");
    }

    @Test
    void testCheckNotebookNameExistenceWhenExists() {
        NotebookDetailsDTO notebook = createNotebook(project.getId());

        NotebookExistenceCheckDTO result = notebookClient.checkNotebookNameExistence(notebook.getName());

        assertThat(result).isNotNull();
        assertThat(result.getExists()).isTrue();
    }

    @Test
    void testCheckNotebookNameExistenceWhenNotExists() {
        String name = notebookClient.getNextNotebookNumber();

        NotebookExistenceCheckDTO result = notebookClient.checkNotebookNameExistence(name);

        assertThat(result).isNotNull();
        assertThat(result.getExists()).isFalse();
    }

    @Test
    void testCheckNotebookNameExistenceWithMultipleNotebooks() {
        NotebookDetailsDTO notebook1 = createNotebook(project.getId());
        NotebookDetailsDTO notebook2 = createNotebook(project.getId());

        NotebookExistenceCheckDTO result1 = notebookClient.checkNotebookNameExistence(notebook1.getName());
        NotebookExistenceCheckDTO result2 = notebookClient.checkNotebookNameExistence(notebook2.getName());
        NotebookExistenceCheckDTO result3 = notebookClient.checkNotebookNameExistence(notebookClient.getNextNotebookNumber());

        assertThat(result1.getExists()).isTrue();
        assertThat(result2.getExists()).isTrue();
        assertThat(result3.getExists()).isFalse();
    }

    @Test
    void testGetNextNotebookNumber() {
        NotebookDetailsDTO notebook = createNotebook(project.getId());

        String next = notebookClient.getNextNotebookNumber();

        assertThat(next).isEqualTo("%08d".formatted(Integer.parseInt(notebook.getName()) + 1));
    }

    @Test
    void testCreateNotebook() {
        NotebookDetailsDTO notebook = createNotebook(project.getId());
        assertThat(notebook.getId()).isNotNull();
        assertThat(notebook.getName()).matches("\\d{8}");
        assertThat(notebook.getCreatedBy().getDisplayName()).isEqualTo(JOHN_DISPLAY_NAME);
        assertThat(notebook.getCreatedAt()).isNotNull();
        assertThat(notebook.getModifiedBy().getDisplayName()).isEqualTo(JOHN_DISPLAY_NAME);
        assertThat(notebook.getModifiedAt()).isNotNull();
        assertThat(notebook.getExperimentCount()).isZero();
        assertThat(notebook.getExperimentCountByStatus()).isEmpty();
        assertThat(notebook.getAttachments()).isEmpty();
        assertThat(notebook.getCurrentPermissions()).containsExactlyInAnyOrder(VIEW_NOTEBOOKS, EDIT_NOTEBOOKS, MANAGE_NOTEBOOK_ACCESS, DELETE_NOTEBOOKS);
        assertThat(notebook.getRevision()).isOne();
        assertThat(notebookClient.getNotebookRevisions(notebook.getId()))
                .hasSize(1)
                .first().satisfies(revision -> {
                    assertThat(revision.getRevision()).isOne();
                    assertThat(revision.getDate()).isEqualTo(notebook.getCreatedAt());
                    assertThat(revision.getUser()).isEqualTo(JOHN_USER_REF);
                    assertThat(revision.getSummary()).isEqualTo("Create notebook");
                });
    }

    @Test
    void testGetNotebook() {
        NotebookDetailsDTO createdNotebook = createNotebook(project.getId());
        NotebookDetailsDTO loadedNotebook = notebookClient.getNotebook(createdNotebook.getId());
        assertThat(loadedNotebook).usingRecursiveComparison().isEqualTo(createdNotebook);
    }

    @Test
    void testGetNotebooks() {
        NotebookDetailsDTO createdNotebook = createNotebook(project.getId());
        Page<NotebookDTO> notebooks = notebookClient.getProjectNotebooks(project.getId(), null, null, null, Paging.DEFAULT);
        assertThat(notebooks.getItems()).hasSize(1).first().satisfies(notebook -> {
            assertThat(notebook.getId()).isNotNull();
            assertThat(notebook.getName()).isEqualTo(createdNotebook.getName());
            assertThat(notebook.getCreatedBy().getDisplayName()).isEqualTo(JOHN_DISPLAY_NAME);
            assertThat(notebook.getCreatedAt()).isNotNull();
            assertThat(notebook.getModifiedBy().getDisplayName()).isEqualTo(JOHN_DISPLAY_NAME);
            assertThat(notebook.getModifiedAt()).isNotNull();
            assertThat(notebook.getExperimentCount()).isZero();
            assertThat(notebook.getExperimentCountByStatus()).isEmpty();
        });
    }

    @Test
    void testGetNotebooksSortedByEarliest() {
        createNotebook(project.getId());
        createNotebook(project.getId());
        createNotebook(project.getId());

        Page<NotebookDTO> notebooks = notebookClient.getProjectNotebooks(project.getId(), null, SortOrder.EARLIEST, null, Paging.DEFAULT);

        assertThat(notebooks.getItems())
                .isSortedAccordingTo(Comparator.comparing(NotebookDTO::getModifiedAt));
    }

    @Test
    void testGetNotebooksSortedByLatest() {
        createNotebook(project.getId());
        createNotebook(project.getId());
        createNotebook(project.getId());

        Page<NotebookDTO> notebooks = notebookClient.getProjectNotebooks(project.getId(), null, SortOrder.LATEST, null, Paging.DEFAULT);

        assertThat(notebooks.getItems())
                .isSortedAccordingTo(Comparator.comparing(NotebookDTO::getModifiedAt).reversed());
    }

    @Test
    void testGetNotebooksCreatedByMe() {
        withUser(ELNBaseTest.JOHN_USERNAME, () -> {
            createNotebook(project.getId());
            createNotebook(project.getId());
        });

        withUser(BART_USERNAME, () -> {
            createNotebook(project.getId());
        });

        withUser(ELNBaseTest.JOHN_USERNAME, () -> {
            Page<NotebookDTO> notebooks = notebookClient.getProjectNotebooks(project.getId(), null, null, true, Paging.DEFAULT);

            assertThat(notebooks.getItems())
                    .allSatisfy(experiment -> assertThat(experiment.getCreatedBy().getDisplayName()).isEqualTo(JOHN_DISPLAY_NAME));

            assertThat(notebooks.getItems())
                    .noneSatisfy(experiment -> assertThat(experiment.getCreatedBy().getDisplayName()).isEqualTo(BART_DISPLAY_NAME));
        });
    }

    @Test
    void testEditNotebookNoChanges() {
        NotebookDetailsDTO notebook = createNotebook(project.getId(), "d");
        assertThatClientCall(() -> notebookClient.editNotebook(notebook.getId(), new NotebookEditRequest(JsonNullable.undefined(), JsonNullable.undefined())))
                .isBadRequest("Nothing to update");
    }

    @Test
    void testEditNotebook() {
        NotebookDetailsDTO notebook = createNotebook(project.getId(), "d");
        String newName = notebookClient.getNextNotebookNumber();
        NotebookDetailsDTO modified = notebookClient.editNotebook(notebook.getId(), new NotebookEditRequest(JsonNullable.of(newName), JsonNullable.of("d2")));
        assertThat(modified.getName()).isEqualTo(newName);
        assertThat(modified.getDescription()).isEqualTo("d2");
        NotebookDetailsDTO saved = notebookClient.getNotebook(notebook.getId());
        assertThat(saved).usingRecursiveComparison().isEqualTo(modified);
        assertThat(notebookClient.getNotebookRevisions(notebook.getId()))
                .hasSize(2)
                .last().satisfies(revision -> {
                    assertThat(revision.getRevision()).isEqualTo(2);
                    assertThat(revision.getDate()).isEqualTo(modified.getModifiedAt());
                    assertThat(revision.getUser()).isEqualTo(JOHN_USER_REF);
                    assertThat(revision.getSummary()).matches("Edit: name=.+, description");
                });
    }

    @Test
    void testEditNotebookNameUpdatesExperimentNames() {
        NotebookDetailsDTO notebook = createNotebook(project.getId(), "notebook description");
        String oldNotebookName = notebook.getName();

        ExperimentDetailsDTO experiment1 = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        ExperimentDetailsDTO experiment2 = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        ExperimentDetailsDTO experiment3 = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));

        assertThat(experiment1.getName()).startsWith(oldNotebookName + "-");
        assertThat(experiment2.getName()).startsWith(oldNotebookName + "-");
        assertThat(experiment3.getName()).startsWith(oldNotebookName + "-");

        String exp1Number = experiment1.getName().substring(experiment1.getName().lastIndexOf('-') + 1);
        String exp2Number = experiment2.getName().substring(experiment2.getName().lastIndexOf('-') + 1);
        String exp3Number = experiment3.getName().substring(experiment3.getName().lastIndexOf('-') + 1);

        String newNotebookName = notebookClient.getNextNotebookNumber();
        NotebookDetailsDTO modifiedNotebook = notebookClient.editNotebook(notebook.getId(), new NotebookEditRequest(JsonNullable.of(newNotebookName), JsonNullable.undefined()));

        assertThat(modifiedNotebook.getName()).isEqualTo(newNotebookName);

        ExperimentDetailsDTO updatedExp1 = experimentClient.getExperiment(experiment1.getId());
        ExperimentDetailsDTO updatedExp2 = experimentClient.getExperiment(experiment2.getId());
        ExperimentDetailsDTO updatedExp3 = experimentClient.getExperiment(experiment3.getId());

        assertThat(updatedExp1.getName()).isEqualTo(newNotebookName + "-" + exp1Number);
        assertThat(updatedExp2.getName()).isEqualTo(newNotebookName + "-" + exp2Number);
        assertThat(updatedExp3.getName()).isEqualTo(newNotebookName + "-" + exp3Number);

        List<RevisionSummaryDTO> exp1Revisions = experimentClient.getExperimentRevisions(experiment1.getId(), null);
        assertThat(exp1Revisions).hasSizeGreaterThan(1);
        assertThat(exp1Revisions).last().satisfies(revision -> {
            assertThat(revision.getSummary()).contains("Experiment name updated");
            assertThat(revision.getSummary()).contains(oldNotebookName);
            assertThat(revision.getSummary()).contains(newNotebookName);
        });
    }

    @Test
    void testCreateAttachment() {
        NotebookDetailsDTO notebook = createNotebook(project.getId());
        List<AttachmentDTO> attachments = notebookClient.createNotebookAttachment(notebook.getId(), "attachment.txt", "content".getBytes());
        assertThat(attachments).singleElement().satisfies(a -> {
            assertThat(a.getId()).isNotNull();
            assertThat(a.getName()).isEqualTo("attachment.txt");
            assertThat(a.getCreatedBy().getDisplayName()).isEqualTo(JOHN_DISPLAY_NAME);
            assertThat(a.getCreatedAt()).isNotNull();
            assertThat(a.getModifiedBy().getDisplayName()).isEqualTo(JOHN_DISPLAY_NAME);
            assertThat(a.getModifiedAt()).isNotNull();
        });
        assertThat(notebookClient.getNotebookRevisions(notebook.getId()))
                .last().satisfies(revision -> {
                    assertThat(revision.getSummary()).isEqualTo("Created attachment: attachment.txt, 7 bytes");
                });
    }

    @Test
    void testDownloadAttachment() throws Exception {
        NotebookDetailsDTO notebook = createNotebook(project.getId());
        List<AttachmentDTO> attachments = notebookClient.createNotebookAttachment(notebook.getId(), "attachment.txt", "content".getBytes());
        try (Response response = notebookClient.downloadNotebookAttachment(notebook.getId(), attachments.getFirst().getId())) {
            assertThat(extractFilename(response.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION))).isEqualTo("attachment.txt");
            assertThat((byte[]) response.getEntity()).asString().isEqualTo("content");
        }
    }

    @Test
    void testDeleteAttachment() {
        NotebookDetailsDTO notebook = createNotebook(project.getId());
        List<AttachmentDTO> attachments = notebookClient.createNotebookAttachment(notebook.getId(), "attachment.txt", "content".getBytes());
        notebookClient.deleteNotebookAttachment(notebook.getId(), attachments.getFirst().getId());
        notebook = notebookClient.getNotebook(notebook.getId());
        assertThat(notebook.getAttachments()).isEmpty();
        assertThat(notebookClient.getNotebookRevisions(notebook.getId()))
                .last().satisfies(revision -> {
                    assertThat(revision.getSummary()).isEqualTo("Deleted attachment: attachment.txt");
                });
    }

    @Test
    void testQuickSearch() {
        NotebookDetailsDTO notebook = createNotebook(project.getId(), "QS1 QSOld");
        String name1 = notebook.getName();
        NotebookDetailsDTO notebook2 = createNotebook(project.getId(), "QS1 QS2 quickSearchCommon");
        String name2 = notebook2.getName();

        Page<NotebookDTO> result1 = notebookClient.getProjectNotebooks(project.getId(), name1, null, null, Paging.DEFAULT);
        assertThat(result1.getItems()).map(NotebookDTO::getName).containsOnly(name1);

        Page<NotebookDTO> result2 = notebookClient.getProjectNotebooks(project.getId(), "qs1", null, null, Paging.DEFAULT);
        assertThat(result2.getItems()).map(NotebookDTO::getName).containsExactlyInAnyOrder(name1, name2);

        notebookClient.editNotebook(notebook.getId(), new NotebookEditRequest(JsonNullable.undefined(), JsonNullable.of("QS1 QSNew")));
        Page<NotebookDTO> result3 = notebookClient.getProjectNotebooks(project.getId(), "QSOld", null, null, Paging.DEFAULT);
        assertThat(result3.getItems()).isEmpty();

        Page<NotebookDTO> result4 = notebookClient.getProjectNotebooks(project.getId(), "QSNew", null, null, Paging.DEFAULT);
        assertThat(result4.getItems()).map(NotebookDTO::getName).containsExactly(name1);

        Page<NotebookDTO> result5 = notebookClient.getProjectNotebooks(project.getId(), name2.substring(4), null, null, Paging.DEFAULT);
        assertThat(result5.getItems()).map(NotebookDTO::getName).containsExactly(name2);
    }

    @Test
    void testUpdateAccess() {
        NotebookDetailsDTO notebook = createNotebook(project.getId());
        notebookClient.updateNotebookAccess(notebook.getId(), AccessForm.of(MAGGIE_USERNAME, AccessLevel.EDIT));
        assertThat(notebookClient.getNotebookRevisions(notebook.getId()))
                .hasSize(2)
                .last().satisfies(revision -> {
                    assertThat(revision.getRevision()).isEqualTo(2);
                    assertThat(revision.getUser()).isEqualTo(JOHN_USER_REF);
                    assertThat(revision.getSummary()).isEqualTo("Edited Team: granted maggie EDIT access");
                });
        notebookClient.updateNotebookAccess(notebook.getId(), AccessForm.of(MAGGIE_USERNAME, AccessLevel.NONE));
        assertThat(notebookClient.getNotebookRevisions(notebook.getId()))
                .hasSize(3)
                .last().satisfies(revision -> {
                    assertThat(revision.getSummary()).isEqualTo("Edited Team: removed maggie");
                });
    }

    @Nested
    @TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class TestNestedAccess {

        ProjectDetailsDTO project;
        NotebookDetailsDTO notebook;
        ExperimentDetailsDTO experiment;

        @BeforeEach
        void setUp(TestInfo testInfo) {
            withUser(JOHN_USERNAME, () -> {
                project = projectClient.createProject(new ProjectRequest(testInfo.getTestMethod().get().getName()));
                notebook = createNotebook(project.getId());
                notebookClient.updateNotebookAccess(notebook.getId(), AccessForm.of(BART_USERNAME, AccessLevel.ADMIN));
                experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
                experimentClient.updateExperimentAccess(experiment.getId(), AccessForm.of(BART_USERNAME, AccessLevel.VIEW));
                experimentClient.updateExperimentAccess(experiment.getId(), AccessForm.of(LISA_USERNAME, AccessLevel.VIEW));
            });
        }

        @Test
        void testRemoveAccess() {
            List<ACLEntryDTO> notebookAccess = notebookClient.updateNotebookAccess(notebook.getId(), AccessForm.of(BART_USERNAME, AccessLevel.NONE));
            assertThatACL(notebookAccess).containsOnly(
                    JOHN_DISPLAY_NAME, AccessLevel.AUTHOR, false,
                    BART_DISPLAY_NAME, AccessLevel.IMPLICIT_VIEW, false,
                    LISA_DISPLAY_NAME, AccessLevel.IMPLICIT_VIEW, false
            );
        }

        @Test
        void testRemoveAccessIncludeNested() {
            List<ACLEntryDTO> projectAccess = notebookClient.updateNotebookAccess(notebook.getId(), AccessForm.of(BART_USERNAME, AccessLevel.NONE, true));
            assertThatACL(projectAccess).containsOnly(
                    JOHN_DISPLAY_NAME, AccessLevel.AUTHOR, false,
                    LISA_DISPLAY_NAME, AccessLevel.IMPLICIT_VIEW, false
            );
            projectAccess = notebookClient.updateNotebookAccess(notebook.getId(), AccessForm.of(LISA_USERNAME, AccessLevel.NONE, true));
            assertThatACL(projectAccess).containsOnly(
                    JOHN_DISPLAY_NAME, AccessLevel.AUTHOR, false
            );
        }
    }
}

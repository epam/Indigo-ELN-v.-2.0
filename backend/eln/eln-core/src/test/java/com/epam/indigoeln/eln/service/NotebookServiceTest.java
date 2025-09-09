package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.test.ResponseWithHeaders;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.ws.rs.core.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.epam.indigoeln.test.ClientCallAssert.assertThatClientCall;
import static org.assertj.core.api.Assertions.assertThat;


@QuarkusTest
@JwtSecurity
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
                .isBadRequest("Notebook name must be 8 digits");
    }

    @Test
    void testDuplicateNames() {
        String name = nextNotebookName();
        notebookClient.createNotebook(project.getId(), new NotebookRequest(name));
        assertThatClientCall(() -> notebookClient.createNotebook(project.getId(), new NotebookRequest(name)))
                .isBadRequest("Notebook with name '.+' already exists");
    }

    @Test
    void testCreateNotebook() {
        String name = nextNotebookName();
        NotebookDetailsDTO notebook = notebookClient.createNotebook(project.getId(), new NotebookRequest(name));
        assertThat(notebook.getId()).isNotNull();
        assertThat(notebook.getName()).isEqualTo(name);
        assertThat(notebook.getCreatedBy().getDisplayName()).isEqualTo(JOHN_DISPLAY_NAME);
        assertThat(notebook.getCreatedAt()).isNotNull();
        assertThat(notebook.getModifiedBy().getDisplayName()).isEqualTo(JOHN_DISPLAY_NAME);
        assertThat(notebook.getModifiedAt()).isNotNull();
    }

    @Test
    void testGetNotebook() {
        NotebookDetailsDTO createdNotebook = notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
        NotebookDetailsDTO loadedNotebook = notebookClient.getNotebook(createdNotebook.getId());
        assertThat(loadedNotebook).usingRecursiveComparison().isEqualTo(createdNotebook);
    }

    @Test
    void testGetNotebooks() {
        String name = nextNotebookName();
        NotebookDetailsDTO createdNotebook = notebookClient.createNotebook(project.getId(), new NotebookRequest(name));
        Page<NotebookDTO> notebooks = notebookClient.getProjectNotebooks(project.getId(), null, null, null, Paging.DEFAULT);
        assertThat(notebooks.getItems()).hasSize(1).first().satisfies(notebook -> {
            assertThat(notebook.getId()).isNotNull();
            assertThat(notebook.getName()).isEqualTo(name);
            assertThat(notebook.getCreatedBy().getDisplayName()).isEqualTo(JOHN_DISPLAY_NAME);
            assertThat(notebook.getCreatedAt()).isNotNull();
            assertThat(notebook.getModifiedBy().getDisplayName()).isEqualTo(JOHN_DISPLAY_NAME);
            assertThat(notebook.getModifiedAt()).isNotNull();
            assertThat(notebook.getExperimentCount()).isEmpty();
        });
    }

    @Test
    void testGetNotebooksSortedByEarliest() {
        notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
        notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
        notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));

        Page<NotebookDTO> notebooks = notebookClient.getProjectNotebooks(project.getId(), null, SortOrder.EARLIEST, null, Paging.DEFAULT);

        assertThat(notebooks.getItems())
                .isSortedAccordingTo(Comparator.comparing(NotebookDTO::getModifiedAt));
    }

    @Test
    void testGetNotebooksSortedByLatest() {
        notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
        notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
        notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));

        Page<NotebookDTO> notebooks = notebookClient.getProjectNotebooks(project.getId(), null, SortOrder.LATEST, null, Paging.DEFAULT);

        assertThat(notebooks.getItems())
                .isSortedAccordingTo(Comparator.comparing(NotebookDTO::getModifiedAt).reversed());
    }

    @Test
    void testGetNotebooksCreatedByMe() {
        withUser(ELNBaseTest.JOHN_USERNAME, () -> {
            notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
            notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
        });

        withUser(BART_USERNAME, () -> {
            notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
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
    void testEditNotebook() {
        NotebookDetailsDTO notebook = notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName(), "d"));
        NotebookDetailsDTO notModified = notebookClient.editNotebook(notebook.getId(), new NotebookEditRequest(null, null));
        assertThat(notModified).usingRecursiveComparison(COMPARE_WITHOUT_MODIFIED_AT).isEqualTo(notebook);
        String newName = nextNotebookName();
        NotebookDetailsDTO modified = notebookClient.editNotebook(notebook.getId(), new NotebookEditRequest(Optional.of(newName), Optional.of("d2")));
        assertThat(modified.getName()).isEqualTo(newName);
        assertThat(modified.getDescription()).isEqualTo("d2");
        NotebookDetailsDTO saved = notebookClient.getNotebook(notebook.getId());
        assertThat(saved).usingRecursiveComparison().isEqualTo(modified);
    }

    @Test
    void testCreateAttachment(@TempDir Path tempDir) {
        NotebookDetailsDTO notebook = notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
        List<AttachmentDTO> attachments = notebookClient.createNotebookAttachment(notebook.getId(), "attachment.txt", tempDir, "content".getBytes());
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
        NotebookDetailsDTO notebook = notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
        List<AttachmentDTO> attachments = notebookClient.createNotebookAttachment(notebook.getId(), "attachment.txt", tempDir, "content".getBytes());
        ResponseWithHeaders response = notebookClient.downloadNotebookAttachmentClient(notebook.getId(), attachments.getFirst().getId());
        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION)).containsExactly("attachment; filename=attachment.txt");
        assertThat(response.getContent()).hasContent("content");
    }

    @Test
    void testDeleteAttachment(@TempDir Path tempDir) {
        NotebookDetailsDTO notebook = notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
        List<AttachmentDTO> attachments = notebookClient.createNotebookAttachment(notebook.getId(), "attachment.txt", tempDir, "content".getBytes());
        notebookClient.deleteNotebookAttachment(notebook.getId(), attachments.getFirst().getId());
        notebook = notebookClient.getNotebook(notebook.getId());
        assertThat(notebook.getAttachments()).isEmpty();
    }

    @Test
    void testQuickSearch() {
        String name1 = nextNotebookName(), name2 = nextNotebookName();
        NotebookDetailsDTO notebook = notebookClient.createNotebook(project.getId(), new NotebookRequest(name1, "QS1 QSOld"));
        String p1 = notebook.getName();
        String p2 = notebookClient.createNotebook(project.getId(), new NotebookRequest(name2, "QS1 QS2 quickSearchCommon")).getName();

        Page<NotebookDTO> result1 = notebookClient.getProjectNotebooks(project.getId(), name1, null, null, Paging.DEFAULT);
        assertThat(result1.getItems()).map(NotebookDTO::getName).containsOnly(p1);

        Page<NotebookDTO> result2 = notebookClient.getProjectNotebooks(project.getId(), "qs1", null, null, Paging.DEFAULT);
        assertThat(result2.getItems()).map(NotebookDTO::getName).containsExactlyInAnyOrder(p1, p2);

        notebookClient.editNotebook(notebook.getId(), new NotebookEditRequest(null, Optional.of("QS1 QSNew")));
        Page<NotebookDTO> result3 = notebookClient.getProjectNotebooks(project.getId(), "QSOld", null, null, Paging.DEFAULT);
        assertThat(result3.getItems()).isEmpty();

        Page<NotebookDTO> result4 = notebookClient.getProjectNotebooks(project.getId(), "QSNew", null, null, Paging.DEFAULT);
        assertThat(result4.getItems()).map(NotebookDTO::getName).containsExactly(p1);
    }
}

package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.BaseTest;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.util.ResponseWithHeaders;
import com.epam.indigoeln.eln.util.TestHelper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.ws.rs.core.HttpHeaders;
import org.junit.jupiter.api.BeforeAll;
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
class NotebookServiceTest extends BaseTest {

    ProjectDetailsDTO project;

    @BeforeEach
    void setUp() {
        project = projectsClient.createProject(new ProjectRequest("NotebookServiceTest" + UUID.randomUUID()));
    }

    @Test
    void testCreateNotebookValidation() {
        assertThatClientCall(() -> notebooksClient.createNotebook(project.getId(), new NotebookRequest(null)))
                .isBadRequest("must not be empty");
    }

    @Test
    void testNameFormatValidation() {
        assertThatClientCall(() -> notebooksClient.createNotebook(project.getId(), new NotebookRequest("not digits")))
                .isBadRequest("Notebook name must be 8 digits");
    }

    @Test
    void testDuplicateNames() {
        String name = nextNotebookName();
        notebooksClient.createNotebook(project.getId(), new NotebookRequest(name));
        assertThatClientCall(() -> notebooksClient.createNotebook(project.getId(), new NotebookRequest(name)))
                .isBadRequest("Notebook with name '.+' already exists");
    }

    @Test
    void testCreateNotebook() {
        String name = nextNotebookName();
        NotebookDetailsDTO notebook = notebooksClient.createNotebook(project.getId(), new NotebookRequest(name));
        assertThat(notebook.getId()).isNotNull();
        assertThat(notebook.getName()).isEqualTo(name);
        assertThat(notebook.getCreatedBy().getDisplayName()).isEqualTo(TestHelper.JOHN_DISPLAY_NAME);
        assertThat(notebook.getCreatedAt()).isNotNull();
        assertThat(notebook.getModifiedBy().getDisplayName()).isEqualTo(TestHelper.JOHN_DISPLAY_NAME);
        assertThat(notebook.getModifiedAt()).isNotNull();
    }
    
    @Test
    void testGetNotebook() {
        NotebookDetailsDTO createdNotebook = notebooksClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
        NotebookDetailsDTO loadedNotebook = notebooksClient.getNotebook(createdNotebook.getId());
        assertThat(loadedNotebook).usingRecursiveComparison().isEqualTo(createdNotebook);
    }

    @Test
    void testGetNotebooks() {
        String name = nextNotebookName();
        NotebookDetailsDTO createdNotebook = notebooksClient.createNotebook(project.getId(), new NotebookRequest(name));
        Page<NotebookDTO> notebooks = notebooksClient.getProjectNotebooks(project.getId(), null, Paging.DEFAULT);
        assertThat(notebooks.getItems()).hasSize(1).first().satisfies(notebook -> {
            assertThat(notebook.getId()).isNotNull();
            assertThat(notebook.getName()).isEqualTo(name);
            assertThat(notebook.getCreatedBy().getDisplayName()).isEqualTo(TestHelper.JOHN_DISPLAY_NAME);
            assertThat(notebook.getCreatedAt()).isNotNull();
            assertThat(notebook.getModifiedBy().getDisplayName()).isEqualTo(TestHelper.JOHN_DISPLAY_NAME);
            assertThat(notebook.getModifiedAt()).isNotNull();
            assertThat(notebook.getExperimentCount()).isEmpty();
        });
    }
    
    @Test
    void testEditNotebook() {
        NotebookDetailsDTO notebook = notebooksClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName(), "d"));
        NotebookDetailsDTO notModified = notebooksClient.editNotebook(notebook.getId(), new NotebookEditRequest(null, null));
        assertThat(notModified).usingRecursiveComparison(TestHelper.COMPARE_WITHOUT_MODIFIED_AT).isEqualTo(notebook);
        String newName = nextNotebookName();
        NotebookDetailsDTO modified = notebooksClient.editNotebook(notebook.getId(), new NotebookEditRequest(Optional.of(newName), Optional.of("d2")));
        assertThat(modified.getName()).isEqualTo(newName);
        assertThat(modified.getDescription()).isEqualTo("d2");
        NotebookDetailsDTO saved = notebooksClient.getNotebook(notebook.getId());
        assertThat(saved).usingRecursiveComparison().isEqualTo(modified);
    }

    @Test
    void testCreateAttachment(@TempDir Path tempDir) {
        NotebookDetailsDTO notebook = notebooksClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
        List<AttachmentDTO> attachments = notebooksClient.createNotebookAttachment(notebook.getId(), "attachment.txt", tempDir, "content".getBytes());
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
        NotebookDetailsDTO notebook = notebooksClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
        List<AttachmentDTO> attachments = notebooksClient.createNotebookAttachment(notebook.getId(), "attachment.txt", tempDir, "content".getBytes());
        ResponseWithHeaders response = notebooksClient.downloadNotebookAttachmentClient(notebook.getId(), attachments.getFirst().getId());
        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION)).containsExactly("attachment; filename=attachment.txt");
        assertThat(response.getContent()).hasContent("content");
    }

    @Test
    void testDeleteAttachment(@TempDir Path tempDir) {
        NotebookDetailsDTO notebook = notebooksClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
        List<AttachmentDTO> attachments = notebooksClient.createNotebookAttachment(notebook.getId(), "attachment.txt", tempDir, "content".getBytes());
        notebooksClient.deleteNotebookAttachment(notebook.getId(), attachments.getFirst().getId());
        notebook = notebooksClient.getNotebook(notebook.getId());
        assertThat(notebook.getAttachments()).isEmpty();
    }

    @Test
    void testQuickSearch() {
        String name1 = nextNotebookName(), name2 = nextNotebookName();
        NotebookDetailsDTO notebook = notebooksClient.createNotebook(project.getId(), new NotebookRequest(name1, "QS1 QSOld"));
        String p1 = notebook.getName();
        String p2 = notebooksClient.createNotebook(project.getId(), new NotebookRequest(name2, "QS1 QS2 quickSearchCommon")).getName();

        Page<NotebookDTO> result1 = notebooksClient.getProjectNotebooks(project.getId(), name1, Paging.DEFAULT);
        assertThat(result1.getItems()).map(NotebookDTO::getName).containsOnly(p1);

        Page<NotebookDTO> result2 = notebooksClient.getProjectNotebooks(project.getId(), "qs1", Paging.DEFAULT);
        assertThat(result2.getItems()).map(NotebookDTO::getName).containsExactlyInAnyOrder(p1, p2);

        notebooksClient.editNotebook(notebook.getId(), new NotebookEditRequest(null, Optional.of("QS1 QSNew")));
        Page<NotebookDTO> result3 = notebooksClient.getProjectNotebooks(project.getId(), "QSOld", Paging.DEFAULT);
        assertThat(result3.getItems()).isEmpty();

        Page<NotebookDTO> result4 = notebooksClient.getProjectNotebooks(project.getId(), "QSNew", Paging.DEFAULT);
        assertThat(result4.getItems()).map(NotebookDTO::getName).containsExactly(p1);
    }
}

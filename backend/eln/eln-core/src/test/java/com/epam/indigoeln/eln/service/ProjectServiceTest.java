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
import org.junit.jupiter.api.io.TempDir;
import org.openapitools.jackson.nullable.JsonNullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static com.epam.indigoeln.common.util.ContentDispositionUtil.extractFilename;
import static com.epam.indigoeln.eln.model.ApplicationPermission.*;
import static com.epam.indigoeln.eln.test.ACLListAssert.assertThatACL;
import static com.epam.indigoeln.test.ClientCallAssert.assertThatClientCall;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;


@QuarkusTest
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
class ProjectServiceTest extends ELNBaseTest {

    @BeforeAll
    void tearDownAll() {
        dictionaryClient.getDictionary(BuiltInDictionary.PROJECT_KEYWORD).forEach(item -> {
            dictionaryClient.removeDictionaryItem(BuiltInDictionary.PROJECT_KEYWORD, item.getId());
        });
    }

    @Test
    @Order(-100)
    void testCounters() {
        TotalCounts expected = new TotalCounts(0, 0, 0, Map.of());
        assertThat(miscClient.getTotalCounts()).isEqualTo(expected);

        ProjectDetailsDTO project = projectClient.createProject(new ProjectRequest("testCounters"));
        assertThat(project.getNotebookCount()).isZero();
        assertThat(project.getExperimentCount()).isZero();
        assertThat(project.getExperimentCountByStatus()).isEmpty();
        expected.setProjects(1);
        assertThat(miscClient.getTotalCounts()).isEqualTo(expected);

        NotebookDetailsDTO notebook = notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
        assertThat(notebook.getExperimentCount()).isZero();
        assertThat(notebook.getExperimentCountByStatus()).isEmpty();
        project = projectClient.getProject(project.getId());
        assertThat(project.getNotebookCount()).isOne();
        assertThat(project.getExperimentCount()).isZero();
        assertThat(project.getExperimentCountByStatus()).isEmpty();
        expected.setNotebooks(1);
        assertThat(miscClient.getTotalCounts()).isEqualTo(expected);

        ExperimentDetailsDTO experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        notebook = notebookClient.getNotebook(notebook.getId());
        assertThat(notebook.getExperimentCount()).isOne();
        assertThat(notebook.getExperimentCountByStatus()).containsExactly(entry(ExperimentStatus.OPEN, 1));
        project = projectClient.getProject(project.getId());
        assertThat(project.getNotebookCount()).isOne();
        assertThat(project.getExperimentCount()).isOne();
        assertThat(project.getExperimentCountByStatus()).containsExactly(entry(ExperimentStatus.OPEN, 1));
        expected.setExperiments(1);
        expected.setExperimentsByStatus(Map.of(ExperimentStatus.OPEN, 1));
        assertThat(miscClient.getTotalCounts()).isEqualTo(expected);
    }

    @Test
    void testCreateProjectValidation() {
        assertThatClientCall(() -> projectClient.createProject(new ProjectRequest(null, List.of(), null, null)))
                .isBadRequest("Project Name is required");
    }

    @Test
    void testCreateProject() {
        ProjectDetailsDTO project = projectClient.createProject(new ProjectRequest("testCreateProject", List.of("keyword1", "keyword2"), "literature", "description"));
        assertThat(project.getId()).isNotNull();
        assertThat(project.getName()).isEqualTo("testCreateProject");
        assertThat(project.getCreatedBy().getDisplayName()).isEqualTo(JOHN_DISPLAY_NAME);
        assertThat(project.getCreatedAt()).isNotNull();
        assertThat(project.getModifiedBy().getDisplayName()).isEqualTo(JOHN_DISPLAY_NAME);
        assertThat(project.getModifiedAt()).isNotNull();
        assertThat(project.getKeywords()).containsExactly("keyword1", "keyword2");
        assertThat(project.getLiterature()).isEqualTo("literature");
        assertThat(project.getDescription()).isEqualTo("description");
        assertThat(project.getNotebookCount()).isEqualTo(0);
        assertThat(project.getExperimentCount()).isZero();
        assertThat(project.getExperimentCountByStatus()).isEmpty();
        assertThat(project.getAttachments()).isEmpty();
        assertThat(project.getCurrentPermissions()).containsExactlyInAnyOrder(VIEW_PROJECTS, EDIT_PROJECTS, MANAGE_PROJECT_ACCESS, DELETE_PROJECTS);
        assertThatACL(project.getAcl()).containsOnly(JOHN_DISPLAY_NAME, AccessLevel.AUTHOR, false);
        assertThat(project.getRevision()).isOne();
        assertThat(projectClient.getProjectRevisions(project.getId()))
                .hasSize(1)
                .first().satisfies(revision -> {
                    assertThat(revision.getRevision()).isOne();
                    assertThat(revision.getDate()).isEqualTo(project.getCreatedAt());
                    assertThat(revision.getUser()).isEqualTo(JOHN_USER_REF);
                    assertThat(revision.getSummary()).isEqualTo("Create project");
                });
    }

    @Test
    void testCreateProjectNameTooLong() {
        String longName = "x".repeat(257);

        assertThatClientCall(() ->
                projectClient.createProject(new ProjectRequest(longName))
        ).isBadRequest("must be at most 256 characters");
    }

    @Test
    void testRenameProjectNameTooLong() {
        ProjectDetailsDTO project =
                projectClient.createProject(new ProjectRequest("testRenameProjectName"));

        String longName = "x".repeat(257);

        assertThatClientCall(() ->
                projectClient.editProject(
                        project.getId(),
                        new ProjectEditRequest().withName(JsonNullable.of(longName))
                )
        ).isBadRequest("must be at most 256 characters");
    }

    @Test
    void testRenameProjectNameIsEmpty() {
        ProjectDetailsDTO project =
                projectClient.createProject(new ProjectRequest("testRenameProjectName"));

        assertThatClientCall(() ->
                projectClient.editProject(
                        project.getId(),
                        new ProjectEditRequest().withName(JsonNullable.of(""))
                )
        ).isBadRequest("Project Name is required");
    }

    @Test
    void testDuplicateNames() {
        projectClient.createProject(new ProjectRequest("testDuplicateNames"));
        assertThatClientCall(() -> projectClient.createProject(new ProjectRequest("testDuplicateNames")))
                .isBadRequest("Unique name is required");
    }

    @Test
    void testRenameDuplicateNames() {
        projectClient.createProject(new ProjectRequest("testRenameDuplicateNames"));
        ProjectDetailsDTO project2 = projectClient.createProject(new ProjectRequest("testRenameDuplicateNames2"));
        assertThatClientCall(() -> projectClient.editProject(project2.getId(), new ProjectEditRequest().withName(JsonNullable.of("testRenameDuplicateNames"))))
                .isBadRequest("Unique name is required");
    }

    @Test
    void testCheckProjectNameExistenceEndpointSuccessWhenExists() {
        String name = "testCheckProjectNameExistenceEndpointSuccessWhenExists";
        projectClient.createProject(new ProjectRequest(name));

        assertThatClientCall(() -> projectClient.checkProjectNameExistence(name))
                .isSuccessfulWithResult(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getExists()).isTrue();
                });
    }

    @Test
    void testCheckProjectNameExistenceEndpointSuccessWhenNotExists() {
        String name = "testCheckProjectNameExistenceEndpointSuccessWhenNotExists";

        assertThatClientCall(() -> projectClient.checkProjectNameExistence(name))
                .isSuccessfulWithResult(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getExists()).isFalse();
                });
    }

    @Test
    void testCheckProjectNameExistenceEndpointValidationEmptyName() {
        assertThatClientCall(() -> projectClient.checkProjectNameExistence(""))
                .isBadRequest("must not be empty");
    }

    @Test
    void testCheckProjectNameExistenceWhenExists() {
        String name = "testCheckProjectNameExistenceWhenExists";
        projectClient.createProject(new ProjectRequest(name));

        assertThatClientCall(() -> projectClient.checkProjectNameExistence(name))
                .isSuccessfulWithResult(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getExists()).isTrue();
                });
    }

    @Test
    void testCheckProjectNameExistenceWhenNotExists() {
        String name = "testCheckProjectNameExistenceWhenNotExists";

        assertThatClientCall(() -> projectClient.checkProjectNameExistence(name))
                .isSuccessfulWithResult(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getExists()).isFalse();
                });
    }

    @Test
    void testCheckProjectNameExistenceWithMultipleProjects() {
        String name1 = "testCheckProjectNameExistence1";
        String name2 = "testCheckProjectNameExistence2";

        projectClient.createProject(new ProjectRequest(name1));
        projectClient.createProject(new ProjectRequest(name2));

        assertThatClientCall(() -> projectClient.checkProjectNameExistence(name1))
                .isSuccessfulWithResult(result -> assertThat(result.getExists()).isTrue());

        assertThatClientCall(() -> projectClient.checkProjectNameExistence(name2))
                .isSuccessfulWithResult(result -> assertThat(result.getExists()).isTrue());

        assertThatClientCall(() -> projectClient.checkProjectNameExistence("nonexistentProject"))
                .isSuccessfulWithResult(result -> assertThat(result.getExists()).isFalse());
    }

    @Test
    void testCheckProjectNameExistenceWithEmptyName() {
        assertThatClientCall(() -> projectClient.checkProjectNameExistence(""))
                .isBadRequest("must not be empty");
    }

    @Test
    void testCheckProjectNameExistenceWithNullName() {
        assertThatClientCall(() -> projectClient.checkProjectNameExistence(null))
                .isBadRequest("must not be empty");
    }

    @Test
    void testGetProject() {
        ProjectDetailsDTO createdProject = projectClient.createProject(new ProjectRequest("testGetProject", List.of("keyword1", "keyword2"), "literature", "description"));
        ProjectDetailsDTO loadedProject = projectClient.getProject(createdProject.getId());
        assertThat(loadedProject).usingRecursiveComparison().isEqualTo(createdProject);
    }

    @Test
    void testGetProjects() {
        projectClient.createProject(new ProjectRequest("testGetProjects", List.of("keyword1", "keyword2"), "literature", "description"));
        Page<ProjectDTO> projects = projectClient.getProjects(null, null, null, Paging.DEFAULT);
        assertThat(projects.getItems()).first().satisfies(project -> {
            assertThat(project.getId()).isNotNull();
            assertThat(project.getName()).isEqualTo("testGetProjects");
            assertThat(project.getCreatedBy().getDisplayName()).isEqualTo(JOHN_DISPLAY_NAME);
            assertThat(project.getCreatedAt()).isNotNull();
            assertThat(project.getModifiedBy().getDisplayName()).isEqualTo(JOHN_DISPLAY_NAME);
            assertThat(project.getModifiedAt()).isNotNull();
            assertThat(project.getNotebookCount()).isEqualTo(0);
            assertThat(project.getExperimentCount()).isZero();
            assertThat(project.getExperimentCountByStatus()).isEmpty();
        });
    }

    @Test
    void testGetProjectsPagination() {
        cleanupDatabase();

        for (int i = 1; i <= 3; i++) {
            projectClient.createProject(new ProjectRequest("testGetProjectsPagination" + i));
        }

        Paging paging1 = new Paging(0, 2);
        Page<ProjectDTO> page0 = projectClient.getProjects(null, null, null, paging1);
        assertThat(page0.getTotalItems()).isEqualTo(3);
        assertThat(page0.getTotalPages()).isEqualTo(2);
        assertThat(page0.getItems()).hasSize(2);
        assertThat(page0.getItems()).extracting(ProjectDTO::getName).containsExactly("testGetProjectsPagination3", "testGetProjectsPagination2");

        Paging paging = new Paging(1, 2);
        Page<ProjectDTO> page1 = projectClient.getProjects(null, null, null, paging);
        assertThat(page1.getTotalItems()).isEqualTo(3);
        assertThat(page1.getTotalPages()).isEqualTo(2);
        assertThat(page1.getItems().size()).isEqualTo(1);
        assertThat(page1.getItems()).extracting(ProjectDTO::getName).containsExactly("testGetProjectsPagination1");
    }

    @Test
    void testGetProjectsSortByEarliest() {
        cleanupDatabase();

        projectClient.createProject(new ProjectRequest("Project1"));
        projectClient.createProject(new ProjectRequest("Project2"));
        projectClient.createProject(new ProjectRequest("Project3"));

        Page<ProjectDTO> projects = projectClient.getProjects(null, SortOrder.EARLIEST, null, Paging.DEFAULT);

        assertThat(projects.getItems())
                .extracting(ProjectDTO::getName)
                .containsExactly("Project1", "Project2", "Project3");
    }

    @Test
    void testGetProjectsSortByLatest() {
        cleanupDatabase();

        projectClient.createProject(new ProjectRequest("Project1"));
        projectClient.createProject(new ProjectRequest("Project2"));
        projectClient.createProject(new ProjectRequest("Project3"));

        Page<ProjectDTO> projects = projectClient.getProjects(null, SortOrder.LATEST, null, Paging.DEFAULT);

        assertThat(projects.getItems())
                .extracting(ProjectDTO::getName)
                .containsExactly("Project3", "Project2", "Project1");
    }

    @Test
    void testGetProjectsCreatedByMe() {
        cleanupDatabase();

        withUser(ELNBaseTest.JOHN_USERNAME, () -> {
            projectClient.createProject(new ProjectRequest("MyProject1"));
            projectClient.createProject(new ProjectRequest("MyProject2"));
        });

        withUser(BART_USERNAME, () -> {
            projectClient.createProject(new ProjectRequest("OtherUserProject"));
        });

        withUser(ELNBaseTest.JOHN_USERNAME, () -> {
            Page<ProjectDTO> projects = projectClient.getProjects(null, null, true, Paging.DEFAULT);

            assertThat(projects.getItems())
                    .extracting(ProjectDTO::getName)
                    .containsExactlyInAnyOrder("MyProject1", "MyProject2");

            assertThat(projects.getItems())
                    .extracting(ProjectDTO::getName)
                    .doesNotContain("OtherUserProject");
        });
    }

    @Test
    void testEditProjectNoChanges() {
        ProjectDetailsDTO project = projectClient.createProject(new ProjectRequest("testEditProject", List.of("k1", "k2"), "l", "d"));
        assertThatClientCall(() -> projectClient.editProject(project.getId(), new ProjectEditRequest(JsonNullable.undefined(), JsonNullable.undefined(), JsonNullable.undefined(), JsonNullable.undefined())))
                .isBadRequest("Nothing to update");
    }

    @Test
    void testEditProject() {
        ProjectDetailsDTO project = projectClient.createProject(new ProjectRequest("testEditProject", List.of("k1", "k2"), "l", "d"));
        ProjectDetailsDTO modified = projectClient.editProject(project.getId(), new ProjectEditRequest(JsonNullable.of("testEditProject_new"), JsonNullable.of(List.of("k2", "k3")), JsonNullable.of("l2"), JsonNullable.of("d2")));
        assertThat(modified.getName()).isEqualTo("testEditProject_new");
        assertThat(modified.getKeywords()).containsExactly("k2", "k3");
        assertThat(modified.getLiterature()).isEqualTo("l2");
        assertThat(modified.getDescription()).isEqualTo("d2");
        ProjectDetailsDTO saved = projectClient.getProject(project.getId());
        assertThat(saved).usingRecursiveComparison().isEqualTo(modified);
        assertThat(projectClient.getProjectRevisions(project.getId()))
                .hasSize(2)
                .last().satisfies(revision -> {
                    assertThat(revision.getRevision()).isEqualTo(2);
                    assertThat(revision.getDate()).isEqualTo(modified.getModifiedAt());
                    assertThat(revision.getUser()).isEqualTo(JOHN_USER_REF);
                    assertThat(revision.getSummary()).matches("Edit: multiple attributes");
                });
    }

    @Test
    void testCounts() {
        UUID projectId = projectClient.createProject(new ProjectRequest("testCounts")).getId();
        UUID notebook1Id = notebookClient.createNotebook(projectId, new NotebookRequest(nextNotebookName())).getId();
        UUID notebook2Id = notebookClient.createNotebook(projectId, new NotebookRequest(nextNotebookName())).getId();
        experimentClient.createExperiment(notebook1Id, new ExperimentRequest(emptyTemplateID));
        experimentClient.createExperiment(notebook1Id, new ExperimentRequest(emptyTemplateID));
        experimentClient.createExperiment(notebook2Id, new ExperimentRequest(emptyTemplateID));

        TotalCounts totalCounts = miscClient.getTotalCounts();
        assertThat(totalCounts.getProjects()).isGreaterThanOrEqualTo(1);
        assertThat(totalCounts.getNotebooks()).isGreaterThanOrEqualTo(2);
        assertThat(totalCounts.getExperiments()).isGreaterThanOrEqualTo(3);
        assertThat(totalCounts.getExperimentsByStatus().get(ExperimentStatus.OPEN)).isGreaterThanOrEqualTo(3);

        Paging paging = new Paging(0, 1);
        Page<ProjectDTO> projects = projectClient.getProjects(null, null, null, paging);
        assertThat(projects.getItems()).filteredOn(p -> p.getId().equals(projectId)).singleElement().satisfies(p -> {
            assertThat(p.getNotebookCount()).isEqualTo(2);
            assertThat(p.getExperimentCount()).isEqualTo(3);
            assertThat(p.getExperimentCountByStatus()).contains(entry(ExperimentStatus.OPEN, 3));
        });
        ProjectDetailsDTO project = projectClient.getProject(projectId);
        assertThat(project.getNotebookCount()).isEqualTo(2);
        assertThat(project.getExperimentCount()).isEqualTo(3);
        assertThat(project.getExperimentCountByStatus()).contains(entry(ExperimentStatus.OPEN, 3));

        Page<NotebookDTO> notebooks = notebookClient.getProjectNotebooks(projectId, null, null, null, Paging.DEFAULT);
        assertThat(notebooks.getItems()).filteredOn(n -> n.getId().equals(notebook1Id)).singleElement().satisfies(n -> {
            assertThat(n.getExperimentCount()).isEqualTo(2);
            assertThat(n.getExperimentCountByStatus()).contains(entry(ExperimentStatus.OPEN, 2));
        });
        assertThat(notebooks.getItems()).filteredOn(n -> n.getId().equals(notebook2Id)).singleElement().satisfies(n -> {
            assertThat(n.getExperimentCount()).isOne();
            assertThat(n.getExperimentCountByStatus()).contains(entry(ExperimentStatus.OPEN, 1));
        });
        NotebookDetailsDTO notebook1 = notebookClient.getNotebook(notebook1Id);
        assertThat(notebook1.getExperimentCount()).isEqualTo(2);
        assertThat(notebook1.getExperimentCountByStatus()).contains(entry(ExperimentStatus.OPEN, 2));
        NotebookDetailsDTO notebook2 = notebookClient.getNotebook(notebook2Id);
        assertThat(notebook2.getExperimentCount()).isOne();
        assertThat(notebook2.getExperimentCountByStatus()).contains(entry(ExperimentStatus.OPEN, 1));
    }

    @Test
    void testCreateAttachment(@TempDir Path tempDir) {
        ProjectDetailsDTO project = projectClient.createProject(new ProjectRequest("testCreateAttachment"));
        List<AttachmentDTO> attachments = projectClient.createProjectAttachment(project.getId(), "attachment.txt", "content".getBytes());
        assertThat(attachments).singleElement().satisfies(a -> {
            assertThat(a.getId()).isNotNull();
            assertThat(a.getName()).isEqualTo("attachment.txt");
            assertThat(a.getCreatedBy().getDisplayName()).isEqualTo(JOHN_DISPLAY_NAME);
            assertThat(a.getCreatedAt()).isNotNull();
            assertThat(a.getModifiedBy().getDisplayName()).isEqualTo(JOHN_DISPLAY_NAME);
            assertThat(a.getModifiedAt()).isNotNull();
        });
        assertThat(projectClient.getProjectRevisions(project.getId()))
                .last().satisfies(revision -> {
                    assertThat(revision.getSummary()).isEqualTo("Created attachment: attachment.txt, 7 bytes");
                });
    }

    @Test
    void testDownloadAttachment(@TempDir Path tempDir) throws Exception {
        ProjectDetailsDTO project = projectClient.createProject(new ProjectRequest("testDownloadAttachment"));
        List<AttachmentDTO> attachments = projectClient.createProjectAttachment(project.getId(), "attachment.txt", "content".getBytes());
        try (Response response = projectClient.downloadProjectAttachment(project.getId(), attachments.getFirst().getId())) {
            assertThat(extractFilename(response.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION))).isEqualTo("attachment.txt");
            assertThat((byte[]) response.getEntity()).asString().isEqualTo("content");
        }
    }

    @Test
    void testDeleteAttachment(@TempDir Path tempDir) {
        ProjectDetailsDTO project = projectClient.createProject(new ProjectRequest("testDeleteAttachment"));
        List<AttachmentDTO> attachments = projectClient.createProjectAttachment(project.getId(), "attachment.txt", "content".getBytes());
        projectClient.deleteProjectAttachment(project.getId(), attachments.getFirst().getId());
        project = projectClient.getProject(project.getId());
        assertThat(project.getAttachments()).isEmpty();
        assertThat(projectClient.getProjectRevisions(project.getId()))
                .last().satisfies(revision -> {
                    assertThat(revision.getSummary()).isEqualTo("Deleted attachment: attachment.txt");
                });
    }

    @Test
    void testUploadLargeAttachment(@TempDir Path tempDir) throws Exception {
        ProjectDetailsDTO project = projectClient.createProject(new ProjectRequest("testUploadLargeAttachment"));

        // Use 7 MB file to stay safely below AWS API Gateway limit
        int fileSizeInBytes = 7 * 1024 * 1024; // 7 MB
        byte[] largeContent = new byte[fileSizeInBytes];
        new Random().nextBytes(largeContent);

        String fileName = "large_test_file_7MB.pptx";
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, largeContent);

        long startTime = System.currentTimeMillis();

        List<AttachmentDTO> attachments = projectClient.createProjectAttachment(
                project.getId(),
                fileName,
                largeContent
        );

        long elapsedTime = System.currentTimeMillis() - startTime;
        assertThat(attachments).isNotEmpty();
    }

    @Test
    void testUploadAttachmentToInvalidProject() {
        UUID missingProjectId = UUID.randomUUID();

        assertThatClientCall(() ->
                projectClient.createProjectAttachment(missingProjectId, "file.txt", "content".getBytes())
        ).isNotFound("PROJECT " + missingProjectId + " not found");
    }

    @Test
    void testSuggestKeywords() {
        projectClient.createProject(new ProjectRequest("testSuggestKeywords", List.of("k1", "K2", "k3", "keyword1", "Keyword2"), null, null));
        List<DictionaryItemRef> all = dictionaryClient.suggestDictionaryItems(BuiltInDictionary.PROJECT_KEYWORD, "");
        assertThat(all).map(DictionaryItemRef::getName).contains("k1", "k2", "k3");
        List<DictionaryItemRef> filtered = dictionaryClient.suggestDictionaryItems(BuiltInDictionary.PROJECT_KEYWORD, "ke");
        assertThat(filtered).map(DictionaryItemRef::getName).containsExactly("keyword1", "keyword2", "Keyword2");
    }

    @Test
    void testQuickSearch() {
        ProjectDetailsDTO project = projectClient.createProject(new ProjectRequest("quickSearchA", List.of(), null, "QS1 QS2 QSOld"));
        String p1 = project.getName();
        String p2 = projectClient.createProject(new ProjectRequest("quickSearchB", List.of(), null, "QS1 QS3 quickSearchCommon")).getName();
        String p3 = projectClient.createProject(new ProjectRequest("quickSearchC quickSearchCommon", List.of("QSKeyword"), "QSLiterature", "QS2 QS3")).getName();

        Page<ProjectDTO> result1 = projectClient.getProjects("quickSearchA", null, null, Paging.DEFAULT);
        assertThat(result1.getItems()).map(ProjectDTO::getName).containsOnly(p1);

        Page<ProjectDTO> result2 = projectClient.getProjects("qs1", null, null, Paging.DEFAULT);
        assertThat(result2.getItems()).map(ProjectDTO::getName).containsExactlyInAnyOrder(p1, p2);

        Page<ProjectDTO> result3 = projectClient.getProjects("quickSearchCommon", null, null, Paging.DEFAULT);
        assertThat(result3.getItems()).map(ProjectDTO::getName).containsExactlyInAnyOrder(p2, p3);

        // TODO keywords doesn't get reflected in the index because keywords are in a separate table and update is not triggered
//        Page<ProjectDTO> result4 = projectsClient.getProjects("QSKeyword", Paging.DEFAULT);
//        assertThat(result4.getItems()).map(ProjectDTO::getName).containsOnly(p3);

        Page<ProjectDTO> result5 = projectClient.getProjects("QSLiterature", null, null, Paging.DEFAULT);
        assertThat(result5.getItems()).map(ProjectDTO::getName).containsOnly(p3);

        projectClient.editProject(project.getId(), new ProjectEditRequest(JsonNullable.undefined(), JsonNullable.undefined(), JsonNullable.undefined(), JsonNullable.of("QS1 QS2 QSNew")));
        Page<ProjectDTO> result6 = projectClient.getProjects("QSOld", null, null, Paging.DEFAULT);
        assertThat(result6.getItems()).isEmpty();

        Page<ProjectDTO> result7 = projectClient.getProjects("QSNew", null, null, Paging.DEFAULT);
        assertThat(result7.getItems()).map(ProjectDTO::getName).containsExactly(p1);

        Page<ProjectDTO> result8 = projectClient.getProjects("archC", null, null, Paging.DEFAULT);
        assertThat(result8.getItems()).map(ProjectDTO::getName).containsExactly(p3);
    }

    @Test
    void testAdminCanUpdateAccessForUserCreatedProject() {
        ProjectDetailsDTO project = projectClient.createProject(new ProjectRequest("testAdminCanUpdateAccessForUserCreatedProject"));
        withUser(ADMIN_USERNAME, () -> {
            projectClient.updateProjectAccess(project.getId(), AccessForm.of(BART_USERNAME, AccessLevel.EDIT));
        });
    }

    @Test
    void testUpdateAccess() {
        ProjectDetailsDTO project = projectClient.createProject(new ProjectRequest("testUpdateAccess"));
        NotebookDetailsDTO notebook = notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
        ExperimentDetailsDTO experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        projectClient.updateProjectAccess(project.getId(), AccessForm.of(MAGGIE_USERNAME, AccessLevel.EDIT));
        assertThat(projectClient.getProjectRevisions(project.getId()))
                .hasSize(2)
                .last().satisfies(revision -> {
                    assertThat(revision.getRevision()).isEqualTo(2);
                    assertThat(revision.getUser()).isEqualTo(JOHN_USER_REF);
                    assertThat(revision.getSummary()).isEqualTo("Edited Team: granted maggie EDIT access");
                });
        assertThat(notebookClient.getNotebookRevisions(notebook.getId()))
                .last().satisfies(revision -> {
                    assertThat(revision.getSummary()).isEqualTo("Access updated because of the changes in project testUpdateAccess");
                });
        assertThat(experimentClient.getExperimentRevisions(experiment.getId(), true))
                .last().satisfies(revision -> {
                    assertThat(revision.getSummary()).isEqualTo("Access updated because of the changes in project testUpdateAccess");
                });

        projectClient.updateProjectAccess(project.getId(), AccessForm.of(MAGGIE_USERNAME, AccessLevel.NONE));
        assertThat(projectClient.getProjectRevisions(project.getId()))
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
                projectClient.updateProjectAccess(project.getId(), AccessForm.of(BART_USERNAME, AccessLevel.EDIT));
                notebook = notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
                notebookClient.updateNotebookAccess(notebook.getId(), AccessForm.of(BART_USERNAME, AccessLevel.ADMIN));
                experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
                experimentClient.updateExperimentAccess(experiment.getId(), AccessForm.of(LISA_USERNAME, AccessLevel.VIEW));
            });
        }

        @Test
        void testGetNestedAccess() {
            assertThat(projectClient.getNestedProjectAccess(project.getId()))
                    .containsExactly(
                            new NestedACLEntryDTO(ELNEntityType.NOTEBOOK, notebook.getId(), notebook.getName(), BART_DISPLAY_NAME, AccessLevel.ADMIN),
                            new NestedACLEntryDTO(ELNEntityType.EXPERIMENT, experiment.getId(), experiment.getName(), LISA_DISPLAY_NAME, AccessLevel.VIEW)
                    );
        }

        @Test
        void testRemoveAccess() {
            List<ACLEntryDTO> projectAccess = projectClient.updateProjectAccess(project.getId(), AccessForm.of(BART_USERNAME, AccessLevel.NONE));
            assertThatACL(projectAccess).containsOnly(
                    JOHN_DISPLAY_NAME, AccessLevel.AUTHOR, false,
                    BART_DISPLAY_NAME, AccessLevel.IMPLICIT_VIEW, false,
                    LISA_DISPLAY_NAME, AccessLevel.IMPLICIT_VIEW, false
            );
        }

        @Test
        void testRemoveAccessIncludeNested() {
            List<ACLEntryDTO> projectAccess = projectClient.updateProjectAccess(project.getId(), AccessForm.of(LISA_USERNAME, AccessLevel.NONE, true));
            assertThatACL(projectAccess).containsOnly(
                    JOHN_DISPLAY_NAME, AccessLevel.AUTHOR, false,
                    BART_DISPLAY_NAME, AccessLevel.EDIT, false
            );
            projectAccess = projectClient.updateProjectAccess(project.getId(), AccessForm.of(BART_USERNAME, AccessLevel.NONE, true));
            assertThatACL(projectAccess).containsOnly(
                    JOHN_DISPLAY_NAME, AccessLevel.AUTHOR, false
            );
        }
    }
}

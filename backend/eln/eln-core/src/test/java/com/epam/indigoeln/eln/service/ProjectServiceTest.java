package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.model.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.epam.indigoeln.eln.model.ApplicationPermission.*;
import static com.epam.indigoeln.eln.test.ACLListAssert.assertThatACL;
import static com.epam.indigoeln.test.ClientCallAssert.assertThatClientCall;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;


@QuarkusTest
@JwtSecurity
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
                .isBadRequest("must not be empty");
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
    }

    @Test
    void testDuplicateNames() {
        projectClient.createProject(new ProjectRequest("testDuplicateNames"));
        assertThatClientCall(() -> projectClient.createProject(new ProjectRequest("testDuplicateNames")))
                .isBadRequest("Project with name 'testDuplicateNames' already exists");
    }

    @Test
    void testRenameDuplicateNames() {
        projectClient.createProject(new ProjectRequest("testRenameDuplicateNames"));
        ProjectDetailsDTO project2 = projectClient.createProject(new ProjectRequest("testRenameDuplicateNames2"));
        assertThatClientCall(() -> projectClient.editProject(project2.getId(), new ProjectEditRequest().withName(Optional.of("testRenameDuplicateNames"))))
                .isBadRequest("Project with name 'testRenameDuplicateNames' already exists");
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
    void testEditProject() {
        ProjectDetailsDTO project = projectClient.createProject(new ProjectRequest("testEditProject", List.of("k1", "k2"), "l", "d"));
        ProjectDetailsDTO notModified = projectClient.editProject(project.getId(), new ProjectEditRequest(null, null, null, null));
        assertThat(notModified).usingRecursiveComparison(COMPARE_WITHOUT_MODIFIED_AT).isEqualTo(project);
        ProjectDetailsDTO modified = projectClient.editProject(project.getId(), new ProjectEditRequest(Optional.of("testEditProject_new"), Optional.of(List.of("k2", "k3")), Optional.of("l2"), Optional.of("d2")));
        assertThat(modified.getName()).isEqualTo("testEditProject_new");
        assertThat(modified.getKeywords()).containsExactly("k2", "k3");
        assertThat(modified.getLiterature()).isEqualTo("l2");
        assertThat(modified.getDescription()).isEqualTo("d2");
        ProjectDetailsDTO saved = projectClient.getProject(project.getId());
        assertThat(saved).usingRecursiveComparison().isEqualTo(modified);
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
        List<AttachmentDTO> attachments = projectClient.createProjectAttachment(project.getId(), "attachment.txt", tempDir, "content".getBytes());
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
        ProjectDetailsDTO project = projectClient.createProject(new ProjectRequest("testDownloadAttachment"));
        List<AttachmentDTO> attachments = projectClient.createProjectAttachment(project.getId(), "attachment.txt", tempDir, "content".getBytes());
        Response response = projectClient.downloadProjectAttachmentClient(project.getId(), attachments.getFirst().getId());
        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION)).containsExactly("attachment; filename=attachment.txt");
        assertThat((byte[]) response.getEntity()).asString().isEqualTo("content");
    }

    @Test
    void testDeleteAttachment(@TempDir Path tempDir) {
        ProjectDetailsDTO project = projectClient.createProject(new ProjectRequest("testDeleteAttachment"));
        List<AttachmentDTO> attachments = projectClient.createProjectAttachment(project.getId(), "attachment.txt", tempDir, "content".getBytes());
        projectClient.deleteProjectAttachment(project.getId(), attachments.getFirst().getId());
        project = projectClient.getProject(project.getId());
        assertThat(project.getAttachments()).isEmpty();
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

        projectClient.editProject(project.getId(), new ProjectEditRequest(null, null, null, Optional.of("QS1 QS2 QSNew")));
        Page<ProjectDTO> result6 = projectClient.getProjects("QSOld", null, null, Paging.DEFAULT);
        assertThat(result6.getItems()).isEmpty();

        Page<ProjectDTO> result7 = projectClient.getProjects("QSNew", null, null, Paging.DEFAULT);
        assertThat(result7.getItems()).map(ProjectDTO::getName).containsExactly(p1);
    }

    @Nested
    @JwtSecurity
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
                projectClient.updateProjectAccess(project.getId(), AccessForm.of(bartUserID, AccessLevel.EDIT));
                notebook = notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
                notebookClient.updateNotebookAccess(notebook.getId(), AccessForm.of(bartUserID, AccessLevel.ADMIN));
                experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
                experimentClient.updateExperimentAccess(experiment.getId(), AccessForm.of(lisaUserID, AccessLevel.VIEW));
            });
        }

        @Test
        void testGetNestedAccess() {
            assertThat(projectClient.getNestedProjectAccess(project.getId()))
                    .containsExactly(
                            new NestedACLEntryDTO(EntityType.NOTEBOOK, notebook.getId(), notebook.getName(), bartUserID, BART_DISPLAY_NAME, AccessLevel.ADMIN),
                            new NestedACLEntryDTO(EntityType.EXPERIMENT, experiment.getId(), experiment.getName(), lisaUserID, LISA_DISPLAY_NAME, AccessLevel.VIEW)
                    );
        }

        @Test
        void testRemoveAccess() {
            List<ACLDetailsEntryDTO> projectAccess = projectClient.updateProjectAccess(project.getId(), AccessForm.of(bartUserID, AccessLevel.NONE));
            assertThatACL(projectAccess).containsOnly(
                    JOHN_DISPLAY_NAME, AccessLevel.AUTHOR, false,
                    BART_DISPLAY_NAME, AccessLevel.IMPLICIT_VIEW, false,
                    LISA_DISPLAY_NAME, AccessLevel.IMPLICIT_VIEW, false
            );
        }

        @Test
        void testRemoveAccessIncludeNested() {
            List<ACLDetailsEntryDTO> projectAccess = projectClient.updateProjectAccess(project.getId(), AccessForm.of(lisaUserID, AccessLevel.NONE, true));
            assertThatACL(projectAccess).containsOnly(
                    JOHN_DISPLAY_NAME, AccessLevel.AUTHOR, false,
                    BART_DISPLAY_NAME, AccessLevel.EDIT, false
            );
            projectAccess = projectClient.updateProjectAccess(project.getId(), AccessForm.of(bartUserID, AccessLevel.NONE, true));
            assertThatACL(projectAccess).containsOnly(
                    JOHN_DISPLAY_NAME, AccessLevel.AUTHOR, false
            );
        }
    }
}

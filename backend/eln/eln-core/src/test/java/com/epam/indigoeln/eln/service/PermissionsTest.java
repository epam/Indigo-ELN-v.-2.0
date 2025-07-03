package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.exception.AccessDeniedException;
import com.epam.indigoeln.eln.BaseTest;
import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.repository.NotebookRepository;
import com.epam.indigoeln.eln.repository.ProjectRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import one.util.streamex.StreamEx;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.epam.indigoeln.eln.model.AccessLevel.*;
import static com.epam.indigoeln.eln.service.CustomAssertions.assertThatACL;
import static com.epam.indigoeln.eln.service.CustomAssertions.assertThatClientCall;
import static com.epam.indigoeln.eln.util.TestHelper.*;
import static org.assertj.core.api.Assertions.*;


@QuarkusTest
@JwtSecurity
@TestSecurity(user = WILLOW_USERNAME)
class PermissionsTest extends BaseTest {

    static Paging PAGING = new Paging(0, 100);

    @Inject
    ProjectRepository projectRepository;
    @Inject
    NotebookRepository notebookRepository;
    @Inject
    ExperimentRepository experimentRepository;
    @Inject
    ACLService aclService;

    TemplateDetailsDTO template;
    List<TestRow> rows;

    List<TemplateComponent> components = List.of(new TemplateComponent.Attachments());

    @BeforeAll
    @Transactional
    void setupAll() {
        testHelper.cleanupDatabase();
        testHelper.createTestUsers();

        rows = new BufferedReader(new InputStreamReader(PermissionsTest.class.getResourceAsStream("permissions.csv")))
                .lines()
                .skip(1)
                .map(line -> line.split(","))
                .map(line -> new TestRow(
                        Integer.parseInt(line[0]),
                        AccessLevel.valueOf(line[1]),
                        AccessLevel.valueOf(line[2]),
                        AccessLevel.valueOf(line[3]),
                        AccessLevel.valueOf(line[4]),
                        AccessLevel.valueOf(line[5]),
                        AccessLevel.valueOf(line[6])
                ))
                .toList();
    }

    @Test
    @Order(-100)
    @TestSecurity(user = JOHN_USERNAME)
    void insertTestData(@TempDir Path tempDir) {
        template = templatesClient.createTemplate(new TemplateRequest("template", components));
        for (TestRow row : rows) {
            row.projectId = projectsClient.createProject(new ProjectRequest("project" + row.testId)).getId();
            projectsClient.createProjectAttachment(row.projectId, "attachment.txt", tempDir, new byte[0]);
            if (row.project != NONE) {
                projectsClient.updateProjectAccess(row.projectId, AccessForm.of(testHelper.getWillowUserID(), row.project));
            }
            row.projectDetails = projectsClient.getProject(row.projectId);
            row.notebookId = notebooksClient.createNotebook(row.projectId, new NotebookRequest(nextNotebookName())).getId();
            notebooksClient.createNotebookAttachment(row.notebookId, "attachment.txt", tempDir, new byte[0]);
            if (row.notebook != NONE) {
                notebooksClient.updateNotebookAccess(row.notebookId, AccessForm.of(testHelper.getWillowUserID(), row.notebook));
            }
            row.notebookDetails = notebooksClient.getNotebook(row.notebookId);
            row.experimentId = experimentsClient.createExperiment(row.notebookId, new ExperimentRequest(getEmptyTemplateID())).getId();
            experimentsClient.createExperimentAttachment(row.experimentId, "attachment.txt", tempDir, new byte[0]);
            if (row.experiment != NONE) {
                experimentsClient.updateExperimentAccess(row.experimentId, AccessForm.of(testHelper.getWillowUserID(), row.experiment));
            }
            row.experimentDetails = experimentsClient.getExperiment(row.experimentId);
        }
    }

    @Test
    @Order(-90)
    @DataAccess
    @Transactional
    @TestSecurity(user = WILLOW_USERNAME)
    void testCalculateAccessLevel() {
        for (TestRow row : rows) {
            AccessLevel projectLevel = NONE, notebookLevel = NONE, experimentLevel = NONE;
            if (row.effectiveProject != NONE) {
                ProjectEntity project = projectRepository.get(row.projectId);
                projectLevel = project.getCurrentAccess();
            } else {
                assertThatThrownBy(() -> projectRepository.get(row.projectId))
                        .as(row.toString())
                        .isInstanceOf(AccessDeniedException.class);
            }
            if (row.effectiveNotebook != NONE) {
                NotebookEntity notebook = notebookRepository.get(row.notebookId);
                notebookLevel = notebook.getCurrentAccess();
            } else {
                assertThatThrownBy(() -> notebookRepository.get(row.notebookId))
                        .as(row.toString())
                        .isInstanceOf(AccessDeniedException.class);
            }
            if (row.effectiveExperiment != NONE) {
                ExperimentEntity experiment = experimentRepository.get(row.experimentId);
                experimentLevel = experiment.getCurrentAccess();
            } else {
                assertThatThrownBy(() -> experimentRepository.get(row.experimentId))
                        .as(row.toString())
                        .isInstanceOf(AccessDeniedException.class);
            }
            assertThat(tuple(projectLevel, notebookLevel, experimentLevel))
                    .as(row.toString())
                    .isEqualTo(tuple(row.effectiveProject, row.effectiveNotebook, row.effectiveExperiment));
        }
    }

    @Test
    void testListProjects() {
        Page<ProjectDTO> projects = projectsClient.getProjects(null, PAGING);
        Set<String> expected = StreamEx.of(rows)
                .filter(r -> r.effectiveProject != NONE)
                .map(r -> r.projectDetails.getName())
                .toSet();
        assertThat(projects.getItems()).extracting(ProjectDTO::getName).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void testCreateProjectRejected() {
        assertThatClientCall(() -> projectsClient.createProject(new ProjectRequest("testCreateProjectRejected")))
                .isForbidden("Operation not permitted");
    }

    @Test
    void testCreateTemplateRejected() {
        assertThatClientCall(() -> templatesClient.createTemplate(new TemplateRequest("testCreateTemplateRejected", components)))
                .isForbidden("Operation not permitted");
    }

    @Test
    void testEditTemplateRejected() {
        assertThatClientCall(() -> templatesClient.editTemplate(template.getId(), new TemplateEditRequest()))
                .isForbidden("Operation not permitted");
    }

    @Test
    @JwtSecurity
    @TestSecurity(user = LISA_USERNAME)
    void testEditTemplateAllowed() {
        assertThatClientCall(() -> templatesClient.editTemplate(template.getId(), new TemplateEditRequest()))
                .isSuccessful();
    }

    @Test
    void testGetProject() {
        for (TestRow row : rows) {
            assertThatClientCall(() -> projectsClient.getProject(row.projectId))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveProject.isSufficientFor(IMPLICIT_VIEW), "not found or not accessible");
        }
    }

    @Test
    void testEditProject() {
        for (TestRow row : rows) {
            assertThatClientCall(() -> projectsClient.editProject(row.projectId, new ProjectEditRequest()))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveProject.isSufficientFor(EDIT), "(Operation not permitted)|(not found or not accessible)");
        }
    }

    @Test
    void testProjectAttachments(@TempDir Path tempDir) {
        for (TestRow row : rows) {
            assertThatClientCall(() -> projectsClient.createProjectAttachment(row.projectId, "a", tempDir, new byte[0]))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveProject.isSufficientFor(EDIT), "(Operation not permitted)|(not found or not accessible)");
            assertThatClientCall(() -> projectsClient.downloadProjectAttachmentClient(row.projectId, row.projectDetails.getAttachments().getFirst().getId()))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveProject.isSufficientFor(IMPLICIT_VIEW), "(Operation not permitted)|(not found or not accessible)");
            assertThatClientCall(() -> projectsClient.deleteProjectAttachment(row.projectId, row.projectDetails.getAttachments().getFirst().getId()))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveProject.isSufficientFor(EDIT), "(Operation not permitted)|(not found or not accessible)");
        }
    }

    @Test
    void testUpdateProjectAccess() {
        for (TestRow row : rows) {
            if (row.project != NONE) {
                assertThatClientCall(() -> projectsClient.updateProjectAccess(row.projectId, AccessForm.of(testHelper.getWillowUserID(), row.project)))
                        .isAllowedIf(row.effectiveProject.isSufficientFor(ADMIN), "Operation not permitted");
            }
        }
    }

    @Test
    void testListNotebooks() {
        for (TestRow row : rows) {
            Page<NotebookDTO> notebooks = notebooksClient.getProjectNotebooks(row.projectId, null, PAGING);
            if (row.effectiveNotebook != NONE) {
                assertThat(notebooks.getItems()).extracting(NotebookDTO::getName).containsExactly(row.notebookDetails.getName());
            } else {
                assertThat(notebooks.getItems()).isEmpty();
            }
        }
    }

    // TODO assuming EDIT is required to create children
    @Test
    void testCreateNotebook() {
        for (TestRow row : rows) {
            assertThatClientCall(() -> notebooksClient.createNotebook(row.projectId, new NotebookRequest(nextNotebookName())))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveProject.isSufficientFor(EDIT), "(Operation not permitted)|(not found or not accessible)");
        }
    }
    
    @Test
    void testGetNotebook() {
        for (TestRow row : rows) {
            assertThatClientCall(() -> notebooksClient.getNotebook(row.notebookId))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveNotebook.isSufficientFor(IMPLICIT_VIEW), "not found or not accessible");
        }
    }

    @Test
    void testEditNotebook() {
        for (TestRow row : rows) {
            assertThatClientCall(() -> notebooksClient.editNotebook(row.notebookId, new NotebookEditRequest()))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveNotebook.isSufficientFor(EDIT), "(Operation not permitted)|(not found or not accessible)");
        }
    }

    @Test
    void testNotebookAttachments(@TempDir Path tempDir) {
        for (TestRow row : rows) {
            assertThatClientCall(() -> notebooksClient.createNotebookAttachment(row.notebookId, "a", tempDir, new byte[0]))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveNotebook.isSufficientFor(EDIT), "(Operation not permitted)|(not found or not accessible)");
            assertThatClientCall(() -> notebooksClient.downloadNotebookAttachmentClient(row.notebookId, row.notebookDetails.getAttachments().getFirst().getId()))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveNotebook.isSufficientFor(IMPLICIT_VIEW), "(Operation not permitted)|(not found or not accessible)");
            assertThatClientCall(() -> notebooksClient.deleteNotebookAttachment(row.notebookId, row.notebookDetails.getAttachments().getFirst().getId()))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveNotebook.isSufficientFor(EDIT), "(Operation not permitted)|(not found or not accessible)");
        }
    }

    @Test
    void testUpdateNotebookAccess() {
        for (TestRow row : rows) {
            if (row.notebook != NONE) {
                assertThatClientCall(() -> notebooksClient.updateNotebookAccess(row.notebookId, AccessForm.of(testHelper.getWillowUserID(), row.notebook)))
                        .isAllowedIf(row.effectiveNotebook.isSufficientFor(ADMIN), "Operation not permitted");
            }
        }
    }

    @Test
    void testCreateExperiment() {
        for (TestRow row : rows) {
            assertThatClientCall(() -> experimentsClient.createExperiment(row.notebookId, new ExperimentRequest(getEmptyTemplateID())))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveNotebook.isSufficientFor(EDIT), "(Operation not permitted)|(not found or not accessible)");
        }
    }

    @Test
    void testListExperiments() {
        for (TestRow row : rows) {
            Page<ExperimentDTO> experiments = experimentsClient.getNotebookExperiments(row.notebookId, PAGING);
            if (row.effectiveExperiment != NONE) {
                assertThat(experiments.getItems()).extracting(ExperimentDTO::getName).containsExactly(row.experimentDetails.getName());
            } else {
                assertThat(experiments.getItems()).isEmpty();
            }
        }
    }

    @Test
    void testGetExperiment() {
        for (TestRow row : rows) {
            assertThatClientCall(() -> experimentsClient.getExperiment(row.experimentId))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveExperiment.isSufficientFor(IMPLICIT_VIEW), "not found or not accessible");
        }
    }

    @Test
    void testEditExperiment() {
        for (TestRow row : rows) {
            assertThatClientCall(() -> experimentsClient.editExperiment(row.experimentId, new ExperimentEditRequest()))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveExperiment.isSufficientFor(EDIT), "(Operation not permitted)|(not found or not accessible)");
        }
    }

    @Test
    void testExperimentAttachments(@TempDir Path tempDir) {
        for (TestRow row : rows) {
            assertThatClientCall(() -> experimentsClient.createExperimentAttachment(row.experimentId, "a", tempDir, new byte[0]))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveExperiment.isSufficientFor(EDIT), "(Operation not permitted)|(not found or not accessible)");
            assertThatClientCall(() -> experimentsClient.downloadExperimentAttachmentClient(row.experimentId, row.experimentDetails.getAttachments().getFirst().getId()))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveExperiment.isSufficientFor(IMPLICIT_VIEW), "(Operation not permitted)|(not found or not accessible)");
            assertThatClientCall(() -> experimentsClient.deleteExperimentAttachment(row.experimentId, row.experimentDetails.getAttachments().getFirst().getId()))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveExperiment.isSufficientFor(EDIT), "(Operation not permitted)|(not found or not accessible)");
        }
    }

    @Test
    void testMarkExperiment() {
        for (TestRow row : rows) {
            assertThatClientCall(() -> experimentsClient.markExperiment(row.experimentId))
                    .isAllowedIf(row.effectiveExperiment.isSufficientFor(VIEW), "not found or not accessible");
        }
    }

    @Test
    void testUpdateExperimentAccess() {
        for (TestRow row : rows) {
            if (row.experiment != NONE) {
                assertThatClientCall(() -> experimentsClient.updateExperimentAccess(row.experimentId, AccessForm.of(testHelper.getWillowUserID(), row.experiment)))
                        .isAllowedIf(row.effectiveExperiment.isSufficientFor(ADMIN), "Operation not permitted");
            }
        }
    }

    @Test
    @TestSecurity(user = BART_USERNAME)
    void testContentEditorCanSeeEverything() {
        assertThat(projectsClient.getProjects(null, PAGING).getTotalItems()).isEqualTo(rows.size());
        for (TestRow row : rows) {
            assertThatClientCall(() -> projectsClient.getProject(row.projectId))
                    .isSuccessful();
            assertThatClientCall(() -> notebooksClient.getProjectNotebooks(row.projectId, null, PAGING))
                    .isSuccessfulWithResult(p -> assertThat(p.getItems()).hasSize(1));
            assertThatClientCall(() -> notebooksClient.getNotebook(row.notebookId))
                    .isSuccessful();
            assertThatClientCall(() -> experimentsClient.getNotebookExperiments(row.notebookId, PAGING))
                    .isSuccessfulWithResult(p -> assertThat(p.getItems()).hasSize(1));
            assertThatClientCall(() -> experimentsClient.getExperiment(row.experimentId))
                    .isSuccessful();
        }
    }

    @Test
    @DataAccess
    @Transactional
    @TestSecurity(user = BART_USERNAME)
    void testContentEditorHasEditPermissions() {
        for (TestRow row : rows) {
            ProjectEntity project = projectRepository.get(row.projectId);
            for (AccessOperation operation : EnumSet.of(AccessOperation.VIEW, AccessOperation.EDIT, AccessOperation.CREATE_NOTEBOOK)) {
                aclService.ensureAccess(project, operation);
            }
            NotebookEntity notebook = notebookRepository.get(row.notebookId);
            for (AccessOperation operation : EnumSet.of(AccessOperation.VIEW, AccessOperation.EDIT, AccessOperation.CREATE_EXPERIMENT)) {
                aclService.ensureAccess(notebook, operation);
            }
            ExperimentEntity experiment = experimentRepository.get(row.experimentId);
            for (AccessOperation operation : EnumSet.of(AccessOperation.VIEW, AccessOperation.EDIT)) {
                aclService.ensureAccess(experiment, operation);
            }
        }
    }

    @Test
    @DataAccess
    @Transactional
    @TestSecurity(user = JOHN_USERNAME)
    void testAdminCanManageDictionaries() {
        miscClient.updateDictionary(Dictionary.THERAPEUTIC_AREA, List.of(new DictionaryItemRequest(null, "A", null, false)));
    }

    @Test
    @DataAccess
    @Transactional
    @TestSecurity(user = BART_USERNAME)
    void testNotAdminCannotManageDictionaries() {
        assertThatClientCall(() -> miscClient.updateDictionary(Dictionary.THERAPEUTIC_AREA, List.of()))
                .isForbidden("Operation not permitted");
    }

    @Nested
    @JwtSecurity
    @TestSecurity(user = JOHN_USERNAME)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class NestedPermissionsTest {

        ProjectDetailsDTO project;
        NotebookDetailsDTO notebook;
        ExperimentDetailsDTO experiment;
        NotebookDetailsDTO notebook2;
        ExperimentDetailsDTO experiment2;

        String secondNotebookName;

        @Test
        @Order(0)
        void testCreate() {
            project = projectsClient.createProject(new ProjectRequest("testExperimentAccess"));
            notebook = notebooksClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
            experiment = experimentsClient.createExperiment(notebook.getId(), new ExperimentRequest(getEmptyTemplateID()));
        }

        @Test
        @Order(100)
        void testAuthorHasAccessByDefault() {
            assertThatACL(project.getAcl()).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false);
            assertThatACL(notebook.getAcl()).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false);
            assertThatACL(experiment.getAcl()).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false);
        }

        @Test
        @Order(101)
        void testCannotAssignAuthorPermission() {
            assertThatClientCall(() -> projectsClient.updateProjectAccess(project.getId(), AccessForm.of(testHelper.getWillowUserID(), AUTHOR)))
                    .isBadRequest("Cannot assign AUTHOR permission to anyone else");
        }

        @Test
        @Order(101)
        void testCannotRemoveAuthorPermission() {
            assertThatClientCall(() -> projectsClient.updateProjectAccess(project.getId(), AccessForm.of(testHelper.getJohnUserID(), VIEW)))
                    .isBadRequest("AUTHOR permission cannot be removed");
        }

        @Test
        @Order(200)
        void testAddUser() {
            List<ACLEntryDTO> acl = projectsClient.updateProjectAccess(project.getId(), AccessForm.of(testHelper.getWillowUserID(), VIEW));
            assertThatACL(acl).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, WILLOW_DISPLAY_NAME, VIEW, false);
            acl = notebooksClient.updateNotebookAccess(notebook.getId(), AccessForm.of(testHelper.getWillowUserID(), VIEW));
            assertThatACL(acl).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, WILLOW_DISPLAY_NAME, VIEW, false);
            acl = experimentsClient.updateExperimentAccess(experiment.getId(), AccessForm.of(testHelper.getWillowUserID(), VIEW));
            assertThatACL(acl).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, WILLOW_DISPLAY_NAME, VIEW, false);
        }

        @Test
        @Order(201)
        void testRemoveUser() {
            List<ACLEntryDTO> acl = projectsClient.updateProjectAccess(project.getId(), AccessForm.of(testHelper.getWillowUserID(), NONE));
            assertThatACL(acl).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false);
            acl = notebooksClient.updateNotebookAccess(notebook.getId(), AccessForm.of(testHelper.getWillowUserID(), NONE));
            assertThatACL(acl).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false);
            acl = experimentsClient.updateExperimentAccess(experiment.getId(), AccessForm.of(testHelper.getWillowUserID(), NONE));
            assertThatACL(acl).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false);
        }

        @Test
        @Order(300)
        void testInheritedPermissionsAreListedInACL() {
            projectsClient.updateProjectAccess(project.getId(), AccessForm.of(testHelper.getWillowUserID(), EDIT));
            assertThatACL(projectsClient.getProject(project.getId()).getAcl()).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, WILLOW_DISPLAY_NAME, EDIT, false);
            assertThatACL(notebooksClient.getNotebook(notebook.getId()).getAcl()).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, WILLOW_DISPLAY_NAME, EDIT, true);
            assertThatACL(experimentsClient.getExperiment(experiment.getId()).getAcl()).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, WILLOW_DISPLAY_NAME, EDIT, true);
            notebooksClient.updateNotebookAccess(notebook.getId(), AccessForm.of(testHelper.getWillowUserID(), ADMIN));
            assertThatACL(notebooksClient.getNotebook(notebook.getId()).getAcl()).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, WILLOW_DISPLAY_NAME, ADMIN, false);
            assertThatACL(experimentsClient.getExperiment(experiment.getId()).getAcl()).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, WILLOW_DISPLAY_NAME, ADMIN, true);
        }

        @Test
        @Order(400)
        void testCreateSecondExperiment() {
            projectsClient.updateProjectAccess(project.getId(), AccessForm.of(testHelper.getWillowUserID(), NONE));
            notebooksClient.updateNotebookAccess(notebook.getId(), AccessForm.of(testHelper.getWillowUserID(), NONE));
            secondNotebookName = nextNotebookName();
            notebook2 = notebooksClient.createNotebook(project.getId(), new NotebookRequest(secondNotebookName));
            experiment2 = experimentsClient.createExperiment(notebook2.getId(), new ExperimentRequest(getEmptyTemplateID()));
            experimentsClient.updateExperimentAccess(experiment2.getId(), AccessForm.of(testHelper.getWillowUserID(), EDIT));
        }

        @Test
        @Order(401)
        @JwtSecurity
        @TestSecurity(user = WILLOW_USERNAME)
        void testImplicitViewDoesntListSiblingEntities() {
            Page<NotebookDTO> notebooks = notebooksClient.getProjectNotebooks(project.getId(), null, PAGING);
            assertThat(notebooks.getItems()).extracting(NotebookDTO::getName).containsOnly(secondNotebookName);
            Page<ExperimentDTO> experiments = experimentsClient.getNotebookExperiments(notebook2.getId(), PAGING);
            assertThat(experiments.getItems()).extracting(ExperimentDTO::getName).containsOnly(experiment2.getName());
        }

        @Test
        @Order(500)
        void testACLInListLimitedTo3() {
            projectsClient.updateProjectAccess(project.getId(), AccessForm.of(testHelper.getWillowUserID(), EDIT));
            projectsClient.updateProjectAccess(project.getId(), AccessForm.of(testHelper.getBartUserID(), EDIT));
            List<ACLEntryDTO> acl = projectsClient.updateProjectAccess(project.getId(), AccessForm.of(testHelper.getLisaUserID(), EDIT));
            assertThatACL(acl).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, BART_DISPLAY_NAME, EDIT, false, LISA_DISPLAY_NAME, EDIT, false, WILLOW_DISPLAY_NAME, EDIT, false);
            Paging paging = new Paging(0, 1);
            ProjectDTO projectDTO = projectsClient.getProjects(null, paging).getItems().getFirst();
            assertThat(projectDTO.getId()).isEqualTo(project.getId());
            assertThatACL(projectDTO.getAcl()).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, BART_DISPLAY_NAME, EDIT, false, LISA_DISPLAY_NAME, EDIT, false);
            assertThat(projectDTO.getAclCount()).isEqualTo(4);

            notebooksClient.updateNotebookAccess(notebook2.getId(), AccessForm.of(testHelper.getWillowUserID(), EDIT));
            notebooksClient.updateNotebookAccess(notebook2.getId(), AccessForm.of(testHelper.getBartUserID(), EDIT));
            acl = notebooksClient.updateNotebookAccess(notebook2.getId(), AccessForm.of(testHelper.getLisaUserID(), EDIT));
            assertThatACL(acl).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, BART_DISPLAY_NAME, EDIT, false, LISA_DISPLAY_NAME, EDIT, false, WILLOW_DISPLAY_NAME, EDIT, false);
            NotebookDTO notebookDTO = notebooksClient.getProjectNotebooks(project.getId(), null, PAGING).getItems().getFirst();
            assertThat(notebookDTO.getId()).isEqualTo(notebook2.getId());
            assertThatACL(notebookDTO.getAcl()).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, BART_DISPLAY_NAME, EDIT, false, LISA_DISPLAY_NAME, EDIT, false);
            assertThat(notebookDTO.getAclCount()).isEqualTo(4);
            
            experimentsClient.updateExperimentAccess(experiment2.getId(), AccessForm.of(testHelper.getWillowUserID(), EDIT));
            experimentsClient.updateExperimentAccess(experiment2.getId(), AccessForm.of(testHelper.getBartUserID(), EDIT));
            acl = experimentsClient.updateExperimentAccess(experiment2.getId(), AccessForm.of(testHelper.getLisaUserID(), EDIT));
            assertThatACL(acl).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, BART_DISPLAY_NAME, EDIT, false, LISA_DISPLAY_NAME, EDIT, false, WILLOW_DISPLAY_NAME, EDIT, false);
            ExperimentDTO experimentDTO = experimentsClient.getNotebookExperiments(notebook2.getId(), PAGING).getItems().getFirst();
            assertThat(experimentDTO.getId()).isEqualTo(experiment2.getId());
            assertThatACL(experimentDTO.getAcl()).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, BART_DISPLAY_NAME, EDIT, false, LISA_DISPLAY_NAME, EDIT, false);
            assertThat(experimentDTO.getAclCount()).isEqualTo(4);
        }
    }

    @ToString(exclude = {"projectDetails", "notebookDetails", "experimentDetails"})
    @RequiredArgsConstructor
    private static class TestRow {
        final int testId;
        final AccessLevel project;
        final AccessLevel notebook;
        final AccessLevel experiment;
        final AccessLevel effectiveProject;
        final AccessLevel effectiveNotebook;
        final AccessLevel effectiveExperiment;
        UUID projectId;
        UUID notebookId;
        UUID experimentId;
        ProjectDetailsDTO projectDetails;
        NotebookDetailsDTO notebookDetails;
        ExperimentDetailsDTO experimentDetails;
    }
}

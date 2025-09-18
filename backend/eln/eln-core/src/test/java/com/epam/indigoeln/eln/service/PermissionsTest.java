package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.exception.AccessDeniedException;
import com.epam.indigoeln.eln.ELNBaseTest;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

import static com.epam.indigoeln.common.util.ModelUtil.loadResourceAsStream;
import static com.epam.indigoeln.eln.model.AccessLevel.*;
import static com.epam.indigoeln.eln.test.ACLListAssert.assertThatACL;
import static com.epam.indigoeln.test.ClientCallAssert.assertThatClientCall;
import static org.assertj.core.api.Assertions.*;


@QuarkusTest
@JwtSecurity
@TestSecurity(user = ELNBaseTest.WILLOW_USERNAME)
class PermissionsTest extends ELNBaseTest {

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
    List<TemplateTab> templateTabs = List.of(new TemplateTab("tabName", components));

    @BeforeAll
    @Transactional
    void setupAll() {
        cleanupDatabase();

        rows = new BufferedReader(new InputStreamReader(loadResourceAsStream(getClass(), "/com/epam/indigoeln/eln/service/permissions.csv")))
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
    @TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
    void insertTestData(@TempDir Path tempDir) {
        template = templateClient.createTemplate(new TemplateRequest("template", templateTabs));
        for (TestRow row : rows) {
            row.projectId = projectClient.createProject(new ProjectRequest("project" + row.testId)).getId();
            projectClient.createProjectAttachment(row.projectId, "attachment.txt", tempDir, new byte[0]);
            if (row.project != NONE) {
                projectClient.updateProjectAccess(row.projectId, AccessForm.of(willowUserID, row.project));
            }
            row.projectDetails = projectClient.getProject(row.projectId);
            row.notebookId = notebookClient.createNotebook(row.projectId, new NotebookRequest(nextNotebookName())).getId();
            notebookClient.createNotebookAttachment(row.notebookId, "attachment.txt", tempDir, new byte[0]);
            if (row.notebook != NONE) {
                notebookClient.updateNotebookAccess(row.notebookId, AccessForm.of(willowUserID, row.notebook));
            }
            row.notebookDetails = notebookClient.getNotebook(row.notebookId);
            row.experimentId = experimentClient.createExperiment(row.notebookId, new ExperimentRequest(emptyTemplateID)).getId();
            experimentClient.createExperimentAttachment(row.experimentId, "attachment.txt", tempDir, new byte[0]);
            if (row.experiment != NONE) {
                experimentClient.updateExperimentAccess(row.experimentId, AccessForm.of(willowUserID, row.experiment));
            }
            row.experimentDetails = experimentClient.getExperiment(row.experimentId);
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
        Page<ProjectDTO> projects = projectClient.getProjects(null, null, null, PAGING);
        Set<String> expected = StreamEx.of(rows)
                .filter(r -> r.effectiveProject != NONE)
                .map(r -> r.projectDetails.getName())
                .toSet();
        assertThat(projects.getItems()).extracting(ProjectDTO::getName).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void testCreateProjectRejected() {
        assertThatClientCall(() -> projectClient.createProject(new ProjectRequest("testCreateProjectRejected")))
                .isForbidden("Operation not permitted");
    }

    @Test
    void testCreateTemplateRejected() {
        assertThatClientCall(() -> templateClient.createTemplate(new TemplateRequest("testCreateTemplateRejected", templateTabs)))
                .isForbidden("Operation not permitted");
    }

    @Test
    void testEditTemplateRejected() {
        assertThatClientCall(() -> templateClient.editTemplate(template.getId(), new TemplateEditRequest()))
                .isForbidden("Operation not permitted");
    }

    @Test
    @JwtSecurity
    @TestSecurity(user = LISA_USERNAME)
    void testEditTemplateAllowed() {
        assertThatClientCall(() -> templateClient.editTemplate(template.getId(), new TemplateEditRequest()))
                .isSuccessful();
    }

    @Test
    void testGetProject() {
        for (TestRow row : rows) {
            assertThatClientCall(() -> projectClient.getProject(row.projectId))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveProject.isSufficientFor(IMPLICIT_VIEW), "not found or not accessible");
        }
    }

    @Test
    void testEditProject() {
        for (TestRow row : rows) {
            assertThatClientCall(() -> projectClient.editProject(row.projectId, new ProjectEditRequest()))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveProject.isSufficientFor(EDIT), "(Operation not permitted)|(not found or not accessible)");
        }
    }

    @Test
    void testProjectAttachments(@TempDir Path tempDir) {
        for (TestRow row : rows) {
            assertThatClientCall(() -> projectClient.createProjectAttachment(row.projectId, "a", tempDir, new byte[0]))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveProject.isSufficientFor(EDIT), "(Operation not permitted)|(not found or not accessible)");
            assertThatClientCall(() -> projectClient.downloadProjectAttachmentClient(row.projectId, row.projectDetails.getAttachments().getFirst().getId()))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveProject.isSufficientFor(IMPLICIT_VIEW), "(Operation not permitted)|(not found or not accessible)");
            assertThatClientCall(() -> projectClient.deleteProjectAttachment(row.projectId, row.projectDetails.getAttachments().getFirst().getId()))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveProject.isSufficientFor(EDIT), "(Operation not permitted)|(not found or not accessible)");
        }
    }

    @Test
    void testUpdateProjectAccess() {
        for (TestRow row : rows) {
            if (row.project != NONE) {
                assertThatClientCall(() -> projectClient.updateProjectAccess(row.projectId, AccessForm.of(willowUserID, row.project)))
                        .isAllowedIf(row.effectiveProject.isSufficientFor(ADMIN), "Operation not permitted");
            }
        }
    }

    @Test
    void testListNotebooks() {
        for (TestRow row : rows) {
            Page<NotebookDTO> notebooks = notebookClient.getProjectNotebooks(row.projectId, null, null, null, PAGING);
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
            assertThatClientCall(() -> notebookClient.createNotebook(row.projectId, new NotebookRequest(nextNotebookName())))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveProject.isSufficientFor(EDIT), "(Operation not permitted)|(not found or not accessible)");
        }
    }

    @Test
    void testGetNotebook() {
        for (TestRow row : rows) {
            assertThatClientCall(() -> notebookClient.getNotebook(row.notebookId))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveNotebook.isSufficientFor(IMPLICIT_VIEW), "not found or not accessible");
        }
    }

    @Test
    void testEditNotebook() {
        for (TestRow row : rows) {
            assertThatClientCall(() -> notebookClient.editNotebook(row.notebookId, new NotebookEditRequest()))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveNotebook.isSufficientFor(EDIT), "(Operation not permitted)|(not found or not accessible)");
        }
    }

    @Test
    void testNotebookAttachments(@TempDir Path tempDir) {
        for (TestRow row : rows) {
            assertThatClientCall(() -> notebookClient.createNotebookAttachment(row.notebookId, "a", tempDir, new byte[0]))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveNotebook.isSufficientFor(EDIT), "(Operation not permitted)|(not found or not accessible)");
            assertThatClientCall(() -> notebookClient.downloadNotebookAttachmentClient(row.notebookId, row.notebookDetails.getAttachments().getFirst().getId()))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveNotebook.isSufficientFor(IMPLICIT_VIEW), "(Operation not permitted)|(not found or not accessible)");
            assertThatClientCall(() -> notebookClient.deleteNotebookAttachment(row.notebookId, row.notebookDetails.getAttachments().getFirst().getId()))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveNotebook.isSufficientFor(EDIT), "(Operation not permitted)|(not found or not accessible)");
        }
    }

    @Test
    void testUpdateNotebookAccess() {
        for (TestRow row : rows) {
            if (row.notebook != NONE) {
                assertThatClientCall(() -> notebookClient.updateNotebookAccess(row.notebookId, AccessForm.of(willowUserID, row.notebook)))
                        .isAllowedIf(row.effectiveNotebook.isSufficientFor(ADMIN), "Operation not permitted");
            }
        }
    }

    @Test
    void testCreateExperiment() {
        for (TestRow row : rows) {
            assertThatClientCall(() -> experimentClient.createExperiment(row.notebookId, new ExperimentRequest(emptyTemplateID)))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveNotebook.isSufficientFor(EDIT), "(Operation not permitted)|(not found or not accessible)");
        }
    }

    @Test
    void testListExperiments() {
        for (TestRow row : rows) {
            Page<ExperimentDTO> experiments = experimentClient.getNotebookExperiments(row.notebookId, null, null, PAGING);
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
            assertThatClientCall(() -> experimentClient.getExperiment(row.experimentId))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveExperiment.isSufficientFor(IMPLICIT_VIEW), "not found or not accessible");
        }
    }

    @Test
    void testEditExperiment() {
        for (TestRow row : rows) {
            assertThatClientCall(() -> experimentClient.editExperiment(row.experimentId, new ExperimentEditRequest()))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveExperiment.isSufficientFor(EDIT), "(Operation not permitted)|(not found or not accessible)");
        }
    }

    @Test
    void testExperimentAttachments(@TempDir Path tempDir) {
        for (TestRow row : rows) {
            assertThatClientCall(() -> experimentClient.createExperimentAttachment(row.experimentId, "a", tempDir, "content".getBytes(StandardCharsets.UTF_8)))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveExperiment.isSufficientFor(EDIT), "(Operation not permitted)|(not found or not accessible)");
            assertThatClientCall(() -> experimentClient.downloadExperimentAttachmentClient(row.experimentId, row.experimentDetails.getAttachments().getFirst().getId()))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveExperiment.isSufficientFor(IMPLICIT_VIEW), "(Operation not permitted)|(not found or not accessible)");
            assertThatClientCall(() -> experimentClient.deleteExperimentAttachment(row.experimentId, row.experimentDetails.getAttachments().getFirst().getId()))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveExperiment.isSufficientFor(EDIT), "(Operation not permitted)|(not found or not accessible)");
        }
    }

    @Test
    void testMarkExperiment() {
        for (TestRow row : rows) {
            assertThatClientCall(() -> experimentClient.markExperiment(row.experimentId))
                    .isAllowedIf(row.effectiveExperiment.isSufficientFor(VIEW), "not found or not accessible");
        }
    }

    @Test
    void testUpdateExperimentAccess() {
        for (TestRow row : rows) {
            if (row.experiment != NONE) {
                assertThatClientCall(() -> experimentClient.updateExperimentAccess(row.experimentId, AccessForm.of(willowUserID, row.experiment)))
                        .isAllowedIf(row.effectiveExperiment.isSufficientFor(ADMIN), "Operation not permitted");
            }
        }
    }

    @Test
    @TestSecurity(user = BART_USERNAME)
    void testContentEditorCanSeeEverything() {
        assertThat(projectClient.getProjects(null, null, null, PAGING).getTotalItems()).isEqualTo(rows.size());
        for (TestRow row : rows) {
            assertThatClientCall(() -> projectClient.getProject(row.projectId))
                    .isSuccessful();
            assertThatClientCall(() -> notebookClient.getProjectNotebooks(row.projectId, null, null, null, PAGING))
                    .isSuccessfulWithResult(p -> assertThat(p.getItems()).hasSize(1));
            assertThatClientCall(() -> notebookClient.getNotebook(row.notebookId))
                    .isSuccessful();
            assertThatClientCall(() -> experimentClient.getNotebookExperiments(row.notebookId, null, null, PAGING))
                    .isSuccessfulWithResult(p -> assertThat(p.getItems()).hasSize(1));
            assertThatClientCall(() -> experimentClient.getExperiment(row.experimentId))
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
            for (ApplicationPermission operation : EnumSet.of(ApplicationPermission.VIEW_PROJECTS, ApplicationPermission.EDIT_PROJECTS, ApplicationPermission.CREATE_NOTEBOOKS)) {
                aclService.ensureAccess(project, operation);
            }
            NotebookEntity notebook = notebookRepository.get(row.notebookId);
            for (ApplicationPermission operation : EnumSet.of(ApplicationPermission.VIEW_NOTEBOOKS, ApplicationPermission.EDIT_NOTEBOOKS, ApplicationPermission.CREATE_EXPERIMENTS)) {
                aclService.ensureAccess(notebook, operation);
            }
            ExperimentEntity experiment = experimentRepository.get(row.experimentId);
            for (ApplicationPermission operation : EnumSet.of(ApplicationPermission.VIEW_EXPERIMENTS, ApplicationPermission.EDIT_EXPERIMENTS)) {
                aclService.ensureAccess(experiment, operation);
            }
        }
    }

    @Test
    @DataAccess
    @Transactional
    @TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
    void testAdminCanManageDictionaries() {
        List<DictionaryItemDTO> items = dictionaryClient.addDictionaryItem(BuiltInDictionary.TEST, new DictionaryItemRequest("A", "Adescription"));
        dictionaryClient.updateDictionaryItem(BuiltInDictionary.TEST, items.getFirst().getId(), new DictionaryItemEditRequest(
                Optional.of("Anew"),
                Optional.of("AdescriptionNew"),
                Optional.of(1),
                Optional.of(false)
        ));
        dictionaryClient.removeDictionaryItem(BuiltInDictionary.TEST, items.getFirst().getId());
    }

    @Test
    @DataAccess
    @Transactional
    @TestSecurity(user = BART_USERNAME)
    void testNotAdminCannotManageDictionaries() {
        assertThatClientCall(() -> dictionaryClient.addDictionaryItem(BuiltInDictionary.TEST, new DictionaryItemRequest("A", "Adescription")))
                .isForbidden("Operation not permitted");
        assertThatClientCall(() -> dictionaryClient.updateDictionaryItem(BuiltInDictionary.TEST, UUID.randomUUID(), new DictionaryItemEditRequest(null, null, null, null)))
                .isForbidden("Operation not permitted");
        assertThatClientCall(() -> dictionaryClient.removeDictionaryItem(BuiltInDictionary.TEST, UUID.randomUUID()))
                .isForbidden("Operation not permitted");
    }

    @Nested
    @JwtSecurity
    @TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
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
            project = projectClient.createProject(new ProjectRequest("testExperimentAccess"));
            notebook = notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
            experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
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
            assertThatClientCall(() -> projectClient.updateProjectAccess(project.getId(), AccessForm.of(willowUserID, AUTHOR)))
                    .isBadRequest("Cannot assign AUTHOR permission to anyone else");
        }

        @Test
        @Order(101)
        void testCannotRemoveAuthorPermission() {
            assertThatClientCall(() -> projectClient.updateProjectAccess(project.getId(), AccessForm.of(johnUserID, VIEW)))
                    .isBadRequest("AUTHOR permission cannot be removed");
        }

        @Test
        @Order(200)
        void testAddUser() {
            List<ACLDetailsEntryDTO> acl = projectClient.updateProjectAccess(project.getId(), AccessForm.of(willowUserID, VIEW));
            assertThatACL(acl).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, WILLOW_DISPLAY_NAME, VIEW, false);
            acl = notebookClient.updateNotebookAccess(notebook.getId(), AccessForm.of(willowUserID, VIEW));
            assertThatACL(acl).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, WILLOW_DISPLAY_NAME, VIEW, false);
            acl = experimentClient.updateExperimentAccess(experiment.getId(), AccessForm.of(willowUserID, VIEW));
            assertThatACL(acl).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, WILLOW_DISPLAY_NAME, VIEW, false);
        }

        @Test
        @Order(201)
        void testRemoveUser() {
            List<ACLDetailsEntryDTO> acl = projectClient.updateProjectAccess(project.getId(), AccessForm.of(willowUserID, NONE));
            assertThatACL(acl).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false);
            acl = notebookClient.updateNotebookAccess(notebook.getId(), AccessForm.of(willowUserID, NONE));
            assertThatACL(acl).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false);
            acl = experimentClient.updateExperimentAccess(experiment.getId(), AccessForm.of(willowUserID, NONE));
            assertThatACL(acl).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false);
        }

        @Test
        @Order(300)
        void testInheritedPermissionsAreListedInACL() {
            projectClient.updateProjectAccess(project.getId(), AccessForm.of(willowUserID, EDIT));
            assertThatACL(projectClient.getProject(project.getId()).getAcl()).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, WILLOW_DISPLAY_NAME, EDIT, false);
            assertThatACL(notebookClient.getNotebook(notebook.getId()).getAcl()).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, WILLOW_DISPLAY_NAME, EDIT, true);
            assertThatACL(experimentClient.getExperiment(experiment.getId()).getAcl()).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, WILLOW_DISPLAY_NAME, EDIT, true);
            notebookClient.updateNotebookAccess(notebook.getId(), AccessForm.of(willowUserID, ADMIN));
            assertThatACL(notebookClient.getNotebook(notebook.getId()).getAcl()).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, WILLOW_DISPLAY_NAME, ADMIN, false);
            assertThatACL(experimentClient.getExperiment(experiment.getId()).getAcl()).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, WILLOW_DISPLAY_NAME, ADMIN, true);
        }

        @Test
        @Order(400)
        void testCreateSecondExperiment() {
            projectClient.updateProjectAccess(project.getId(), AccessForm.of(willowUserID, NONE));
            notebookClient.updateNotebookAccess(notebook.getId(), AccessForm.of(willowUserID, NONE));
            secondNotebookName = nextNotebookName();
            notebook2 = notebookClient.createNotebook(project.getId(), new NotebookRequest(secondNotebookName));
            experiment2 = experimentClient.createExperiment(notebook2.getId(), new ExperimentRequest(emptyTemplateID));
            experimentClient.updateExperimentAccess(experiment2.getId(), AccessForm.of(willowUserID, EDIT));
        }

        @Test
        @Order(401)
        @JwtSecurity
        @TestSecurity(user = WILLOW_USERNAME)
        void testImplicitViewDoesntListSiblingEntities() {
            Page<NotebookDTO> notebooks = notebookClient.getProjectNotebooks(project.getId(), null, null, null, PAGING);
            assertThat(notebooks.getItems()).extracting(NotebookDTO::getName).containsOnly(secondNotebookName);
            Page<ExperimentDTO> experiments = experimentClient.getNotebookExperiments(notebook2.getId(), null, null, PAGING);
            assertThat(experiments.getItems()).extracting(ExperimentDTO::getName).containsOnly(experiment2.getName());
        }

        @Test
        @Order(500)
        void testACLInListLimitedTo3() {
            projectClient.updateProjectAccess(project.getId(), AccessForm.of(willowUserID, EDIT));
            projectClient.updateProjectAccess(project.getId(), AccessForm.of(bartUserID, EDIT));
            List<ACLDetailsEntryDTO> acl = projectClient.updateProjectAccess(project.getId(), AccessForm.of(lisaUserID, EDIT));
            assertThatACL(acl).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, BART_DISPLAY_NAME, EDIT, false, LISA_DISPLAY_NAME, EDIT, false, WILLOW_DISPLAY_NAME, EDIT, false);
            Paging paging = new Paging(0, 1);
            ProjectDTO projectDTO = projectClient.getProjects(null, null, null, paging).getItems().getFirst();
            assertThat(projectDTO.getId()).isEqualTo(project.getId());
            assertThatACL(projectDTO.getAcl()).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, BART_DISPLAY_NAME, EDIT, false, LISA_DISPLAY_NAME, EDIT, false);
            assertThat(projectDTO.getAclCount()).isEqualTo(4);

            notebookClient.updateNotebookAccess(notebook2.getId(), AccessForm.of(willowUserID, EDIT));
            notebookClient.updateNotebookAccess(notebook2.getId(), AccessForm.of(bartUserID, EDIT));
            acl = notebookClient.updateNotebookAccess(notebook2.getId(), AccessForm.of(lisaUserID, EDIT));
            assertThatACL(acl).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, BART_DISPLAY_NAME, EDIT, false, LISA_DISPLAY_NAME, EDIT, false, WILLOW_DISPLAY_NAME, EDIT, false);
            NotebookDTO notebookDTO = notebookClient.getProjectNotebooks(project.getId(), null, null, null, PAGING).getItems().getFirst();
            assertThat(notebookDTO.getId()).isEqualTo(notebook2.getId());
            assertThatACL(notebookDTO.getAcl()).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, BART_DISPLAY_NAME, EDIT, false, LISA_DISPLAY_NAME, EDIT, false);
            assertThat(notebookDTO.getAclCount()).isEqualTo(4);

            experimentClient.updateExperimentAccess(experiment2.getId(), AccessForm.of(willowUserID, EDIT));
            experimentClient.updateExperimentAccess(experiment2.getId(), AccessForm.of(bartUserID, EDIT));
            acl = experimentClient.updateExperimentAccess(experiment2.getId(), AccessForm.of(lisaUserID, EDIT));
            assertThatACL(acl).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, BART_DISPLAY_NAME, EDIT, false, LISA_DISPLAY_NAME, EDIT, false, WILLOW_DISPLAY_NAME, EDIT, false);
            ExperimentDTO experimentDTO = experimentClient.getNotebookExperiments(notebook2.getId(), null, null, PAGING).getItems().getFirst();
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

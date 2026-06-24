package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
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
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.assertj.core.util.Throwables;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.openapitools.jackson.nullable.JsonNullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

import static com.epam.indigoeln.common.util.ModelUtil.loadResourceAsStream;
import static com.epam.indigoeln.eln.model.AccessLevel.*;
import static com.epam.indigoeln.eln.test.ACLListAssert.assertThatACL;
import static com.epam.indigoeln.test.ClientCallAssert.assertThatClientCall;
import static org.assertj.core.api.Assertions.*;


@Slf4j
@QuarkusTest
@TestSecurity(user = ELNBaseTest.WILLOW_USERNAME)
class PermissionsTest extends ELNBaseTest {

    static final Paging PAGING = new Paging(0, 100);

    static final List<TestRow> rows;

    @Inject
    ProjectRepository projectRepository;
    @Inject
    NotebookRepository notebookRepository;
    @Inject
    ExperimentRepository experimentRepository;
    @Inject
    ACLService aclService;

    TemplateDetailsDTO template;
    TherapeuticAreaRef therapeuticArea;

    List<TemplateTab> templateTabs = List.of(new TemplateTab("tabName", List.of(
            new TemplateComponent.Attachments()))
    );

    static {
        rows = new BufferedReader(new InputStreamReader(loadResourceAsStream(PermissionsTest.class, "/com/epam/indigoeln/eln/service/permissions.csv")))
                .lines()
                .skip(1)
                .map(line -> line.split(","))
                .map(line -> new TestRow(
                        Integer.parseInt(line[0]),
                        valueOf(line[1]),
                        valueOf(line[2]),
                        valueOf(line[3]),
                        valueOf(line[4]),
                        valueOf(line[5]),
                        valueOf(line[6])
                ))
                .toList();
    }

    @BeforeAll
    void setupAll(@TempDir Path tempDir) {
        cleanupDatabase();
        withUser(JOHN_USERNAME, () -> {
            template = templateClient.createTemplate(new TemplateRequest("PermissionsTest", templateTabs));
            therapeuticArea = dictionaryClient.getFirst(BuiltInDictionary.THERAPEUTIC_AREA);
            iterateRowsParallel(row -> {
                row.projectId = projectClient.createProject(new ProjectRequest("project" + row.testId)).getId();
                projectClient.createProjectAttachment(row.projectId, "attachment.txt", new byte[0]);
                if (row.project != NONE) {
                    projectClient.updateProjectAccess(row.projectId, AccessForm.of(WILLOW_USERNAME, row.project));
                }
                row.projectDetails = projectClient.getProject(row.projectId);
                row.notebookId = notebookClient.createNotebook(row.projectId, new NotebookRequest(nextNotebookName())).getId();
                notebookClient.createNotebookAttachment(row.notebookId, "attachment.txt", new byte[0]);
                if (row.notebook != NONE) {
                    notebookClient.updateNotebookAccess(row.notebookId, AccessForm.of(WILLOW_USERNAME, row.notebook));
                }
                row.notebookDetails = notebookClient.getNotebook(row.notebookId);
                row.experimentId = experimentClient.createExperiment(row.notebookId, new ExperimentRequest(emptyTemplateID)).getId();
                experimentClient.createExperimentAttachment(row.experimentId, "attachment.txt", new byte[0]);
                if (row.experiment != NONE) {
                    experimentClient.updateExperimentAccess(row.experimentId, AccessForm.of(WILLOW_USERNAME, row.experiment));
                }
                row.experimentDetails = experimentClient.getExperiment(row.experimentId);
            });
        });
    }

    @AfterAll
    void tearDownAll() {
        withUser(ADMIN_USERNAME, () -> {
            //noinspection ConstantValue
            if (template != null) {
                templateClient.deleteTemplate(template.getId());
            }
        });
    }

    @Test
    @Order(-90)
    @DataAccess
    @Transactional
    @TestSecurity(user = WILLOW_USERNAME)
    void testCalculateAccessLevel() {
        iterateRows(row -> {
            ProjectEntity project = projectRepository.get(row.projectId);
            AccessLevel projectLevel = project.getCalculatedInfo() != null ? project.getCalculatedInfo().getCurrentAccess() : NONE;
            NotebookEntity notebook = notebookRepository.get(row.notebookId);
            AccessLevel notebookLevel = notebook.getCalculatedInfo() != null ? notebook.getCalculatedInfo().getCurrentAccess() : NONE;
            ExperimentEntity experiment = experimentRepository.get(row.experimentId);
            AccessLevel experimentLevel = experiment.getCalculatedInfo() != null ? experiment.getCalculatedInfo().getCurrentAccess() : NONE;
            assertThat(tuple(projectLevel, notebookLevel, experimentLevel))
                    .as(row.toString())
                    .isEqualTo(tuple(row.effectiveProject, row.effectiveNotebook, row.effectiveExperiment));
        });
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
    @TestSecurity(user = LISA_USERNAME)
    void testEditTemplateAllowed() {
        assertThatClientCall(() -> templateClient.editTemplate(template.getId(), new TemplateEditRequest()))
                .isSuccessful();
    }

    @Test
    void testGetProject() {
        iterateRowsParallel(row -> {
            assertThatClientCall(() -> projectClient.getProject(row.projectId))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveProject.isSufficientFor(IMPLICIT_VIEW), "Operation not permitted");
        });
    }

    @Test
    void testEditProject() {
        iterateRowsParallel(row -> {
            assertThatClientCall(() -> projectClient.editProject(row.projectId, new ProjectEditRequest().withDescription(JsonNullable.of("updated"))))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveProject.isSufficientFor(EDIT), "Operation not permitted");
        });
    }

    @Test
    void testProjectAttachments(@TempDir Path tempDir) {
        iterateRowsParallel(row -> {
            assertThatClientCall(() -> projectClient.createProjectAttachment(row.projectId, "a", new byte[0]))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveProject.isSufficientFor(EDIT), "Operation not permitted");
            assertThatClientCall(() -> projectClient.downloadProjectAttachment(row.projectId, row.projectDetails.getAttachments().getFirst().getId()))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveProject.isSufficientFor(IMPLICIT_VIEW), "Operation not permitted");
            assertThatClientCall(() -> projectClient.deleteProjectAttachment(row.projectId, row.projectDetails.getAttachments().getFirst().getId()))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveProject.isSufficientFor(EDIT), "Operation not permitted");
        });
    }

    @Test
    void testUpdateProjectAccess() {
        iterateRowsParallel(row -> {
            if (row.project != NONE) {
                assertThatClientCall(() -> projectClient.updateProjectAccess(row.projectId, AccessForm.of(WILLOW_USERNAME, row.project)))
                        .isAllowedIf(row.effectiveProject.isSufficientFor(ADMIN), "Operation not permitted");
            }
        });
    }

    @Test
    void testListNotebooks() {
        iterateRowsParallel(row -> {
            Page<NotebookDTO> notebooks = notebookClient.getProjectNotebooks(row.projectId, null, null, null, PAGING);
            if (row.effectiveNotebook != NONE) {
                assertThat(notebooks.getItems()).extracting(NotebookDTO::getName).containsExactly(row.notebookDetails.getName());
            } else {
                assertThat(notebooks.getItems()).isEmpty();
            }
        });
    }

    // TODO assuming EDIT is required to create children
    @Test
    void testCreateNotebook() {
        iterateRowsParallel(row -> {
            assertThatClientCall(() -> notebookClient.createNotebook(row.projectId, new NotebookRequest(nextNotebookName())))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveProject.isSufficientFor(EDIT), "Operation not permitted");
        });
    }

    @Test
    void testGetNotebook() {
        iterateRowsParallel(row -> {
            assertThatClientCall(() -> notebookClient.getNotebook(row.notebookId))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveNotebook.isSufficientFor(IMPLICIT_VIEW), "Operation not permitted");
        });
    }

    @Test
    void testEditNotebook() {
        iterateRowsParallel(row -> {
            assertThatClientCall(() -> notebookClient.editNotebook(row.notebookId, new NotebookEditRequest().withDescription(JsonNullable.of("updated"))))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveNotebook.isSufficientFor(EDIT), "Operation not permitted");
        });
    }

    @Test
    void testNotebookAttachments(@TempDir Path tempDir) {
        iterateRowsParallel(row -> {
            assertThatClientCall(() -> notebookClient.createNotebookAttachment(row.notebookId, "a", new byte[0]))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveNotebook.isSufficientFor(EDIT), "Operation not permitted");
            assertThatClientCall(() -> notebookClient.downloadNotebookAttachment(row.notebookId, row.notebookDetails.getAttachments().getFirst().getId()))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveNotebook.isSufficientFor(IMPLICIT_VIEW), "Operation not permitted");
            assertThatClientCall(() -> notebookClient.deleteNotebookAttachment(row.notebookId, row.notebookDetails.getAttachments().getFirst().getId()))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveNotebook.isSufficientFor(EDIT), "Operation not permitted");
        });
    }

    @Test
    void testUpdateNotebookAccess() {
        iterateRowsParallel(row -> {
            if (row.notebook != NONE) {
                assertThatClientCall(() -> notebookClient.updateNotebookAccess(row.notebookId, AccessForm.of(WILLOW_USERNAME, row.notebook)))
                        .isAllowedIf(row.effectiveNotebook.isSufficientFor(ADMIN), "Operation not permitted");
            }
        });
    }

    @Test
    void testCreateExperiment() {
        iterateRowsParallel(row -> {
            assertThatClientCall(() -> experimentClient.createExperiment(row.notebookId, new ExperimentRequest(emptyTemplateID)))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveNotebook.isSufficientFor(EDIT), "Operation not permitted");
        });
    }

    @Test
    void testListExperiments() {
        iterateRowsParallel(row -> {
            Page<ExperimentDTO> experiments = experimentClient.getNotebookExperiments(row.notebookId, null, null, null, PAGING);
            if (row.effectiveExperiment != NONE) {
                assertThat(experiments.getItems()).extracting(ExperimentDTO::getName).containsExactly(row.experimentDetails.getName());
            } else {
                assertThat(experiments.getItems()).isEmpty();
            }
        });
    }

    @Test
    void testGetExperiment() {
        iterateRowsParallel(row -> {
            assertThatClientCall(() -> experimentClient.getExperiment(row.experimentId))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveExperiment.isSufficientFor(IMPLICIT_VIEW), "Operation not permitted");
        });
    }

    @Test
    void testEditExperiment() {
        iterateRowsParallel(row -> {
            assertThatClientCall(() -> {
                experimentClient.editExperiment(row.experimentId, new ExperimentEditRequest().withTherapeuticArea(JsonNullable.of(therapeuticArea)));
            })
                    .as(row.toString())
                    .isAllowedIf(row.effectiveExperiment.isSufficientFor(EDIT), "Operation not permitted");
        });
    }

    @Test
    void testExperimentAttachments(@TempDir Path tempDir) {
        iterateRowsParallel(row -> {
            assertThatClientCall(() -> experimentClient.createExperimentAttachment(row.experimentId, "a", "content".getBytes(StandardCharsets.UTF_8)))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveExperiment.isSufficientFor(EDIT), "Operation not permitted");
            assertThatClientCall(() -> experimentClient.downloadExperimentAttachment(row.experimentId, row.experimentDetails.getAttachments().getFirst().getId()))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveExperiment.isSufficientFor(IMPLICIT_VIEW), "Operation not permitted");
            assertThatClientCall(() -> experimentClient.deleteExperimentAttachment(row.experimentId, row.experimentDetails.getAttachments().getFirst().getId()))
                    .as(row.toString())
                    .isAllowedIf(row.effectiveExperiment.isSufficientFor(EDIT), "Operation not permitted");
        });
    }

    @Test
    void testMarkExperiment() {
        iterateRowsParallel(row -> {
            assertThatClientCall(() -> experimentClient.markExperiment(row.experimentId))
                    .isAllowedIf(row.effectiveExperiment.isSufficientFor(VIEW), "Operation not permitted");
        });
    }

    @Test
    void testUpdateExperimentAccess() {
        iterateRowsParallel(row -> {
            if (row.experiment != NONE) {
                assertThatClientCall(() -> experimentClient.updateExperimentAccess(row.experimentId, AccessForm.of(WILLOW_USERNAME, row.experiment)))
                        .isAllowedIf(row.effectiveExperiment.isSufficientFor(ADMIN), "Operation not permitted");
            }
        });
    }

    @Test
    @TestSecurity(user = BART_USERNAME)
    void testContentEditorCanSeeEverything() {
        assertThat(projectClient.getProjects(null, null, null, PAGING).getTotalItems()).isEqualTo(rows.size());
        iterateRowsParallel(row -> {
            assertThatClientCall(() -> projectClient.getProject(row.projectId))
                    .isSuccessful();
            assertThatClientCall(() -> notebookClient.getProjectNotebooks(row.projectId, null, null, null, PAGING))
                    .isSuccessfulWithResult(p -> assertThat(p.getItems()).hasSize(1));
            assertThatClientCall(() -> notebookClient.getNotebook(row.notebookId))
                    .isSuccessful();
            assertThatClientCall(() -> experimentClient.getNotebookExperiments(row.notebookId, null, null, null, PAGING))
                    .isSuccessfulWithResult(p -> assertThat(p.getItems()).hasSize(1));
            assertThatClientCall(() -> experimentClient.getExperiment(row.experimentId))
                    .isSuccessful();
        });
    }

    @Test
    @DataAccess
    @Transactional
    @TestSecurity(user = BART_USERNAME)
    void testContentEditorHasEditPermissions() {
        iterateRows(row -> {
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
        });
    }

    @Test
    @DataAccess
    @Transactional
    @TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
    void testAdminCanManageDictionaries() {
        DictionaryDTO dictionary = dictionaryClient.createDictionary(new DictionaryRequest("TEST", "Test", false, null));
        try {
            List<DictionaryItemDTO> items = dictionaryClient.addDictionaryItem(dictionary.getId().toString(), new DictionaryItemRequest("A", "Adescription"));
            dictionaryClient.updateDictionaryItem(dictionary.getId().toString(), items.getFirst().getId(), new DictionaryItemEditRequest(
                    JsonNullable.of("Anew"),
                    JsonNullable.of("AdescriptionNew"),
                    JsonNullable.of(1),
                    JsonNullable.of(false)
            ));
            dictionaryClient.removeDictionaryItem(dictionary.getId().toString(), items.getFirst().getId());
        } finally {
            dictionaryClient.removeDictionary(dictionary.getId().toString());
        }
    }

    @Test
    @DataAccess
    @Transactional
    @TestSecurity(user = BART_USERNAME)
    void testNotAdminCannotManageDictionaries() {
        assertThatClientCall(() -> dictionaryClient.createDictionary(new DictionaryRequest("TEST", "Test", false, null)))
                .isForbidden("Operation not permitted");
        assertThatClientCall(() -> dictionaryClient.updateDictionary(BuiltInDictionary.SAMPLE_SOURCE.name(), new DictionaryEditRequest(JsonNullable.of("TEST1"), JsonNullable.undefined(), JsonNullable.undefined(), JsonNullable.undefined())))
                .isForbidden("Operation not permitted");
        assertThatClientCall(() -> dictionaryClient.removeDictionary(BuiltInDictionary.SAMPLE_SOURCE.name()))
                .isForbidden("Operation not permitted");

        assertThatClientCall(() -> dictionaryClient.addDictionaryItem(BuiltInDictionary.THERAPEUTIC_AREA, new DictionaryItemRequest("A", "Adescription")))
                .isForbidden("Operation not permitted");
        assertThatClientCall(() -> dictionaryClient.updateDictionaryItem(BuiltInDictionary.THERAPEUTIC_AREA, UUID.randomUUID(), new DictionaryItemEditRequest(JsonNullable.undefined(), JsonNullable.undefined(), JsonNullable.undefined(), JsonNullable.undefined())))
                .isForbidden("Operation not permitted");
        assertThatClientCall(() -> dictionaryClient.removeDictionaryItem(BuiltInDictionary.THERAPEUTIC_AREA, UUID.randomUUID()))
                .isForbidden("Operation not permitted"); // TODO
    }

    private void iterateRows(Consumer<TestRow> block) {
        for (TestRow row : rows) {
            log.info("Row: {}", row);
            block.accept(row);
        }
    }

    private void iterateRowsParallel(Consumer<TestRow> block) {
        Map<TestRow, ? extends Exception> failures = StreamEx.of(rows).parallel()
                .mapToEntryPartial(row -> {
                    try {
                        block.accept(row);
                        return Optional.empty();
                    } catch (Exception e) {
                        return Optional.of(e);
                    }
                })
                .toMap();
        if (!failures.isEmpty()) {
            fail(failures.size() + " test cases failed:\n" + StreamEx.ofKeys(failures).joining("\n") + "\nerrors: \n" + StreamEx.of(Throwables.describeErrors(List.copyOf(failures.values()))).joining("\n"));
        }
    }

    @Nested
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
            assertThatClientCall(() -> projectClient.updateProjectAccess(project.getId(), AccessForm.of(WILLOW_USERNAME, AUTHOR)))
                    .isBadRequest("Cannot assign AUTHOR permission to anyone else");
        }

        @Test
        @Order(101)
        void testCannotRemoveAuthorPermission() {
            assertThatClientCall(() -> projectClient.updateProjectAccess(project.getId(), AccessForm.of(JOHN_USERNAME, VIEW)))
                    .isBadRequest("AUTHOR permission cannot be removed");
        }

        @Test
        @Order(200)
        void testAddUser() {
            List<ACLEntryDTO> acl = projectClient.updateProjectAccess(project.getId(), AccessForm.of(WILLOW_USERNAME, VIEW));
            assertThatACL(acl).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, WILLOW_DISPLAY_NAME, VIEW, false);
            acl = notebookClient.updateNotebookAccess(notebook.getId(), AccessForm.of(WILLOW_USERNAME, VIEW));
            assertThatACL(acl).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, WILLOW_DISPLAY_NAME, VIEW, false);
            acl = experimentClient.updateExperimentAccess(experiment.getId(), AccessForm.of(WILLOW_USERNAME, VIEW));
            assertThatACL(acl).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, WILLOW_DISPLAY_NAME, VIEW, false);
        }

        @Test
        @Order(201)
        void testRemoveUser() {
            List<ACLEntryDTO> acl = projectClient.updateProjectAccess(project.getId(), AccessForm.of(WILLOW_USERNAME, NONE));
            assertThatACL(acl).containsOnly(
                    JOHN_DISPLAY_NAME, AUTHOR, false,
                    WILLOW_DISPLAY_NAME, IMPLICIT_VIEW, false
            );
            acl = notebookClient.updateNotebookAccess(notebook.getId(), AccessForm.of(WILLOW_USERNAME, NONE));
            assertThatACL(acl).containsOnly(
                    JOHN_DISPLAY_NAME, AUTHOR, false,
                    WILLOW_DISPLAY_NAME, IMPLICIT_VIEW, false
            );
            acl = experimentClient.updateExperimentAccess(experiment.getId(), AccessForm.of(WILLOW_USERNAME, NONE));
            assertThatACL(acl).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false);
        }

        @Test
        @Order(300)
        void testInheritedPermissionsAreListedInACL() {
            projectClient.updateProjectAccess(project.getId(), AccessForm.of(WILLOW_USERNAME, EDIT));
            assertThatACL(projectClient.getProject(project.getId()).getAcl()).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, WILLOW_DISPLAY_NAME, EDIT, false);
            assertThatACL(notebookClient.getNotebook(notebook.getId()).getAcl()).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, WILLOW_DISPLAY_NAME, EDIT, true);
            assertThatACL(experimentClient.getExperiment(experiment.getId()).getAcl()).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, WILLOW_DISPLAY_NAME, EDIT, true);
            notebookClient.updateNotebookAccess(notebook.getId(), AccessForm.of(WILLOW_USERNAME, ADMIN));
            assertThatACL(notebookClient.getNotebook(notebook.getId()).getAcl()).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, WILLOW_DISPLAY_NAME, ADMIN, false);
            assertThatACL(experimentClient.getExperiment(experiment.getId()).getAcl()).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, WILLOW_DISPLAY_NAME, ADMIN, true);
        }

        @Test
        @Order(400)
        void testCreateSecondExperiment() {
            projectClient.updateProjectAccess(project.getId(), AccessForm.of(WILLOW_USERNAME, NONE));
            notebookClient.updateNotebookAccess(notebook.getId(), AccessForm.of(WILLOW_USERNAME, NONE));
            secondNotebookName = nextNotebookName();
            notebook2 = notebookClient.createNotebook(project.getId(), new NotebookRequest(secondNotebookName));
            experiment2 = experimentClient.createExperiment(notebook2.getId(), new ExperimentRequest(emptyTemplateID));
            experimentClient.updateExperimentAccess(experiment2.getId(), AccessForm.of(WILLOW_USERNAME, EDIT));
        }

        @Test
        @Order(401)
        @TestSecurity(user = WILLOW_USERNAME)
        void testImplicitViewDoesntListSiblingEntities() {
            Page<NotebookDTO> notebooks = notebookClient.getProjectNotebooks(project.getId(), null, null, null, PAGING);
            assertThat(notebooks.getItems()).extracting(NotebookDTO::getName).containsOnly(secondNotebookName);
            Page<ExperimentDTO> experiments = experimentClient.getNotebookExperiments(notebook2.getId(), null, null, null, PAGING);
            assertThat(experiments.getItems()).extracting(ExperimentDTO::getName).containsOnly(experiment2.getName());
        }

        @Test
        @Order(500)
        void testACLInListLimitedTo3() {
            projectClient.updateProjectAccess(project.getId(), AccessForm.of(WILLOW_USERNAME, EDIT));
            projectClient.updateProjectAccess(project.getId(), AccessForm.of(BART_USERNAME, EDIT));
            List<ACLEntryDTO> acl = projectClient.updateProjectAccess(project.getId(), AccessForm.of(LISA_USERNAME, EDIT));
            assertThatACL(acl).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, BART_DISPLAY_NAME, EDIT, false, LISA_DISPLAY_NAME, EDIT, false, WILLOW_DISPLAY_NAME, EDIT, false);
            Paging paging = new Paging(0, 1);
            ProjectDTO projectDTO = projectClient.getProjects(project.getName(), null, null, paging).getItems().getFirst();
            assertThat(projectDTO.getId()).isEqualTo(project.getId());
            assertThatACL(projectDTO.getAcl()).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, BART_DISPLAY_NAME, EDIT, false, LISA_DISPLAY_NAME, EDIT, false);
            assertThat(projectDTO.getAclCount()).isEqualTo(4);

            notebookClient.updateNotebookAccess(notebook2.getId(), AccessForm.of(WILLOW_USERNAME, EDIT));
            notebookClient.updateNotebookAccess(notebook2.getId(), AccessForm.of(BART_USERNAME, EDIT));
            acl = notebookClient.updateNotebookAccess(notebook2.getId(), AccessForm.of(LISA_USERNAME, EDIT));
            assertThatACL(acl).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, BART_DISPLAY_NAME, EDIT, false, LISA_DISPLAY_NAME, EDIT, false, WILLOW_DISPLAY_NAME, EDIT, false);
            NotebookDTO notebookDTO = notebookClient.getProjectNotebooks(project.getId(), notebook2.getName(), null, null, PAGING).getItems().getFirst();
            assertThat(notebookDTO.getId()).isEqualTo(notebook2.getId());
            assertThatACL(notebookDTO.getAcl()).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, BART_DISPLAY_NAME, EDIT, false, LISA_DISPLAY_NAME, EDIT, false);
            assertThat(notebookDTO.getAclCount()).isEqualTo(4);

            experimentClient.updateExperimentAccess(experiment2.getId(), AccessForm.of(WILLOW_USERNAME, EDIT));
            experimentClient.updateExperimentAccess(experiment2.getId(), AccessForm.of(BART_USERNAME, EDIT));
            acl = experimentClient.updateExperimentAccess(experiment2.getId(), AccessForm.of(LISA_USERNAME, EDIT));
            assertThatACL(acl).containsOnly(JOHN_DISPLAY_NAME, AUTHOR, false, BART_DISPLAY_NAME, EDIT, false, LISA_DISPLAY_NAME, EDIT, false, WILLOW_DISPLAY_NAME, EDIT, false);
            ExperimentDTO experimentDTO = experimentClient.getNotebookExperiments(notebook2.getId(), null, null, null, PAGING).getItems().getFirst();
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

package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.ExperimentRevisionEntity;
import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.util.ExperimentDetailsReportBuilder;
import com.epam.indigoeln.eln.util.SearchVectorUpdater;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.epam.indigoeln.eln.model.ApplicationPermission.CREATE_PROJECTS;
import static com.epam.indigoeln.eln.model.ApplicationPermission.VIEW_EXPERIMENTS;

// no @Transactional
@Slf4j
@ApplicationScoped
@SuppressWarnings("SqlWithoutWhere")
public class SupportService {

    private static final String ATTACHMENT_LOG = "\tattachment ";
    private static final String CONTENT = "content";
    private static final String ATTACHMENT = "attachment";

    @PersistenceContext
    EntityManager em;
    @Inject
    Flyway flyway;
    @Inject
    ACLService aclService;
    @Inject
    DictionaryService dictionaryService;
    @Inject
    TemplateService templateService;
    @Inject
    ProjectService projectService;
    @Inject
    NotebookService notebookService;
    @Inject
    ExperimentService experimentService;
    @Inject
    CompoundService compoundService;
    @Inject
    ExperimentRepository experimentRepository;
    @Inject
    AttachmentService attachmentService;
    @Inject
    ExperimentDetailsReportBuilder experimentDetailsReportBuilder;
    @Inject
    SearchVectorUpdater searchVectorUpdater;

    private final Random random = new Random();

    public Map<String, String> migrate() {
        aclService.ensureTopLevelAccess(ApplicationPermission.MANAGE_USERS);
        MigrateResult result = flyway.migrate();
        return Map.of(
                "migrations executed", Integer.toString(result.migrationsExecuted),
                "total time", Long.toString(result.getTotalMigrationTime())
        );
    }

    @Transactional
    public Map<String, String> reindexSearchVectors() {
        aclService.ensureTopLevelAccess(ApplicationPermission.MANAGE_USERS);

        List<ProjectEntity> projects = em.createQuery(
                "SELECT DISTINCT p FROM ProjectEntity p JOIN FETCH p.createdBy LEFT JOIN FETCH p.keywords",
                ProjectEntity.class).getResultList();
        projects.forEach(project -> projectService.updateSearchVector(project, projectService.collectSearchFields(project)));

        List<NotebookEntity> notebooks = em.createQuery(
                "SELECT n FROM NotebookEntity n JOIN FETCH n.createdBy",
                NotebookEntity.class).getResultList();
        notebooks.forEach(notebook -> notebookService.updateSearchVector(notebook, notebookService.collectSearchFields(notebook)));

        List<ExperimentEntity> experiments = em.createQuery(
                "SELECT e FROM ExperimentEntity e JOIN FETCH e.createdBy",
                ExperimentEntity.class).getResultList();
        experiments.forEach(experiment -> experimentService.updateSearchVector(experiment, experimentService.collectSearchFields(experiment)));

        List<SampleEntity> samples = em.createQuery(
                "SELECT s FROM SampleEntity s JOIN FETCH s.compound",
                SampleEntity.class).getResultList();
        samples.forEach(compoundService::updateSearchVector);

        return Map.of(
                "projects", String.valueOf(projects.size()),
                "notebooks", String.valueOf(notebooks.size()),
                "experiments", String.valueOf(experiments.size()),
                "samples", String.valueOf(samples.size())
        );
    }

    @Transactional
    public Map<String, String> insertTestData() {
        aclService.ensureTopLevelAccess(CREATE_PROJECTS);
        List<TherapeuticAreaRef> therapeuticAreas = dictionaryService.getDictionary(BuiltInDictionary.THERAPEUTIC_AREA.name(), false);
        List<ProjectCodeRef> projectCodes = dictionaryService.getDictionary(BuiltInDictionary.PROJECT_CODE.name(), false);
        TemplateDTO template = templateService.getByName("Default");
        int lastUsedNotebookNumber = 0;
        int projectCount = 0, notebookCount = 0, experimentCount = 0, attachmentCount = 0;
        for (int projectNo = 1; projectNo <= random.nextInt(4, 6); projectNo++) {
            log.debug("project {}", projectNo);
            List<String> keywords = IntStream.range(0, random.nextInt(4)).mapToObj(i -> "keyword" + i).toList();
            ProjectDetailsDTO project = projectService.createProject(new ProjectRequest("Test Project " + projectNo, keywords, "literature", "description"));
            projectCount++;
            for (int attachmentNo = 1; attachmentNo <= random.nextInt(0, 2); attachmentNo++) {
                log.debug("project {} attachment {}", projectNo, attachmentNo);
                attachmentService.createProjectAttachment(project.getId(), "attachment" + attachmentNo + ".txt", "content".getBytes(), true);
                attachmentCount++;
            }
            for (int notebookNo = 1; notebookNo <= random.nextInt(1, 4); notebookNo++) {
                log.debug("project {} notebook {}", projectNo, notebookNo);
                NotebookDetailsDTO notebook = notebookService.createNotebook(project.getId(), new NotebookRequest("%08d".formatted(++lastUsedNotebookNumber), "description"));
                notebookCount++;
                for (int attachmentNo = 1; attachmentNo <= random.nextInt(0, 4); attachmentNo++) {
                    log.debug("project {} notebook {} attachment {}", projectNo, notebookNo, attachmentNo);
                    attachmentService.createNotebookAttachment(notebook.getId(), "attachment" + attachmentNo + ".txt", "content".getBytes(), true);
                    attachmentCount++;
                }
                for (int experimentNo = 1; experimentNo <= random.nextInt(1, 12); experimentNo++) {
                    log.debug("project {} notebook {} experiment {}", projectNo, notebookNo, experimentNo);
                    ExperimentDetailsDTO experiment = experimentService.createExperiment(notebook.getId(), new ExperimentRequest(template.getId()
                            , "image"
                            , randomOrNone(therapeuticAreas)
                            , randomOrNone(projectCodes)
                    ));
                    experimentCount++;
                    for (int attachmentNo = 1; attachmentNo <= random.nextInt(0, 4); attachmentNo++) {
                        log.debug("project {} notebook {} experiment {} attachment {}", projectNo, notebookNo, experimentNo, attachmentNo);
                        attachmentService.createExperimentAttachment(experiment.getId(), "attachment" + attachmentNo + ".txt", "content".getBytes(), true);
                        attachmentCount++;
                    }
                }
            }
        }
        return Map.of(
                "projects", "" + projectCount,
                "notebooks", "" + notebookCount,
                "experiments", "" + experimentCount,
                "attachments", "" + attachmentCount
        );
    }

    private <T> @Nullable T randomOrNone(List<T> list) {
        int no = random.nextInt(-1, list.size());
        return no == -1 ? null : list.get(no);
    }

    @DataAccess
    @Transactional
    public Pair<String, byte[]> generateExperimentDetailsReport(UUID experimentID) {
        ExperimentEntity experiment = experimentRepository.load(experimentID);
        aclService.ensureAccess(experiment, VIEW_EXPERIMENTS);
        List<ExperimentRevisionEntity> revisions = experimentRepository.getRevisions(experiment, false);
        byte[] bytes = experimentDetailsReportBuilder.build(experiment, revisions);
        return Pair.of("experiment-" + experiment.getName() + ".html", bytes);
    }
}

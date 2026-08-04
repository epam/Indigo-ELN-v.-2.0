package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.ExperimentRevisionEntity;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.util.ExperimentDetailsReportBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.epam.indigoeln.eln.model.ApplicationPermission.VIEW_EXPERIMENTS;

// no @Transactional
@ApplicationScoped
@SuppressWarnings("SqlWithoutWhere")
public class SupportService {

    private static final String ATTACHMENT_LOG = "\tattachment ";
    private static final String CONTENT = "content";
    private static final String ATTACHMENT = "attachment";

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
    ExperimentRepository experimentRepository;
    @Inject
    AttachmentService attachmentService;
    @Inject
    ExperimentDetailsReportBuilder experimentDetailsReportBuilder;

    private final Random random = new Random();

    public Map<String, String> migrate() {
        MigrateResult result = flyway.migrate();
        return Map.of(
                "migrations executed", Integer.toString(result.migrationsExecuted),
                "total time", Long.toString(result.getTotalMigrationTime())
        );
    }

    @Transactional
    public Map<String, String> insertTestData() {
        List<TherapeuticAreaRef> therapeuticAreas = dictionaryService.getDictionary(BuiltInDictionary.THERAPEUTIC_AREA.name(), false);
        List<ProjectCodeRef> projectCodes = dictionaryService.getDictionary(BuiltInDictionary.PROJECT_CODE.name(), false);
        TemplateDTO template = templateService.getByName("Default");
        int lastUsedNotebookNumber = 0;
        int projectCount = 0, notebookCount = 0, experimentCount = 0, attachmentCount = 0;
        for (int projectNo = 1; projectNo <= random.nextInt(4, 6); projectNo++) {
            System.out.println("project " + projectNo);
            List<String> keywords = IntStream.range(0, random.nextInt(4)).mapToObj(i -> "keyword" + i).toList();
            ProjectDetailsDTO project = projectService.createProject(new ProjectRequest("Test Project " + projectNo, keywords, "literature", "description"));
            projectCount++;
            for (int attachmentNo = 1; attachmentNo <= random.nextInt(0, 2); attachmentNo++) {
                System.out.println(ATTACHMENT_LOG + attachmentNo);
                attachmentService.createProjectAttachment(project.getId(), ATTACHMENT + attachmentNo + ".txt", CONTENT.getBytes(), true);
                attachmentCount++;
            }
            for (int notebookNo = 1; notebookNo <= random.nextInt(1, 4); notebookNo++) {
                System.out.println("\tnotebook " + notebookNo);
                NotebookDetailsDTO notebook = notebookService.createNotebook(project.getId(), new NotebookRequest("%08d".formatted(++lastUsedNotebookNumber), "description"));
                notebookCount++;
                for (int attachmentNo = 1; attachmentNo <= random.nextInt(0, 4); attachmentNo++) {
                    System.out.println(ATTACHMENT_LOG + attachmentNo);
                    attachmentService.createNotebookAttachment(notebook.getId(), ATTACHMENT + attachmentNo + ".txt", CONTENT.getBytes(), true);
                    attachmentCount++;
                }
                for (int experimentNo = 1; experimentNo <= random.nextInt(1, 12); experimentNo++) {
                    System.out.println("\t\texperiment " + experimentNo);
                    ExperimentDetailsDTO experiment = experimentService.createExperiment(notebook.getId(), new ExperimentRequest(template.getId()
                            , "image"
                            , randomOrNone(therapeuticAreas)
                            , randomOrNone(projectCodes)
                    ));
                    experimentCount++;
                    for (int attachmentNo = 1; attachmentNo <= random.nextInt(0, 4); attachmentNo++) {
                        System.out.println(ATTACHMENT_LOG + attachmentNo);
                        attachmentService.createExperimentAttachment(experiment.getId(), ATTACHMENT + attachmentNo + ".txt", CONTENT.getBytes(), true);
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

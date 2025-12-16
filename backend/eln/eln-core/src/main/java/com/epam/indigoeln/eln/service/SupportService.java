package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.flywaydb.core.Flyway;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

// no @Transactional
@ApplicationScoped
@SuppressWarnings("SqlWithoutWhere")
public class SupportService {

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
    AttachmentService attachmentService;

    private final Random random = new Random();

    @Transactional
    public Map<String, String> insertTestData() {
        List<DictionaryItemRef> therapeuticAreas = dictionaryService.getDictionary(BuiltInDictionary.THERAPEUTIC_AREA.name());
        List<DictionaryItemRef> projectCodes = dictionaryService.getDictionary(BuiltInDictionary.PROJECT_CODE.name());
        TemplateDTO template = templateService.getByName("Default");
        int lastUsedNotebookNumber = 0;
        int projectCount = 0, notebookCount = 0, experimentCount = 0, attachmentCount = 0;
        for (int projectNo = 1; projectNo <= random.nextInt(4, 6); projectNo++) {
            System.out.println("project " + projectNo);
            List<String> keywords = IntStream.range(0, random.nextInt(4)).mapToObj(i -> "keyword" + i).toList();
            ProjectDetailsDTO project = projectService.createProject(new ProjectRequest("Test Project " + projectNo, keywords, "literature", "description"));
            projectCount++;
            for (int attachmentNo = 1; attachmentNo <= random.nextInt(0, 2); attachmentNo++) {
                System.out.println("\tattachment " + attachmentNo);
                attachmentService.createProjectAttachment(project.getId(), "attachment" + attachmentNo + ".txt", "content".getBytes());
                attachmentCount++;
            }
            for (int notebookNo = 1; notebookNo <= random.nextInt(1, 4); notebookNo++) {
                System.out.println("\tnotebook " + notebookNo);
                NotebookDetailsDTO notebook = notebookService.createNotebook(project.getId(), new NotebookRequest("%08d".formatted(++lastUsedNotebookNumber), "description"));
                notebookCount++;
                for (int attachmentNo = 1; attachmentNo <= random.nextInt(0, 4); attachmentNo++) {
                    System.out.println("\tattachment " + attachmentNo);
                    attachmentService.createNotebookAttachment(notebook.getId(), "attachment" + attachmentNo + ".txt", "content".getBytes());
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
                        System.out.println("\tattachment " + attachmentNo);
                        attachmentService.createExperimentAttachment(experiment.getId(), "attachment" + attachmentNo + ".txt", "content".getBytes());
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
}

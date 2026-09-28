package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.ExperimentRevisionEntity;
import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.mapper.SnapshotMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.util.ExperimentDetailsReportBuilder;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.NotebookSnapshot;
import com.epam.indigoeln.reaction.model.ProjectSnapshot;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.hibernate.jpa.AvailableHints;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.epam.indigoeln.eln.model.ApplicationPermission.CREATE_PROJECTS;
import static com.epam.indigoeln.eln.model.ApplicationPermission.VIEW_EXPERIMENTS;

// no @Transactional
@Slf4j
@ApplicationScoped
@SuppressWarnings("SqlWithoutWhere")
public class SupportService {

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
    GlobalSearchService globalSearchService;
    @Inject
    SnapshotMapper snapshotMapper;
    @Inject
    ExperimentDetailsReportBuilder experimentDetailsReportBuilder;
    @Inject
    UploadService uploadService;

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

        long experiments = doReindex(
                em.createQuery("FROM Experiment e ORDER BY e.id", ExperimentEntity.class),
                e -> {
                    ExperimentSnapshot snapshot = snapshotMapper.createSnapshot(e, false);
                    e.setSearchVector(globalSearchService.collectExperimentSearchVector(snapshot));
                    e.setSearchCompounds(globalSearchService.collectExperimentCompoundRefs(snapshot));
                    e.setSearchRxnfiles(globalSearchService.collectExperimentRxnfiles(snapshot));
                    e.setSearchBatches(globalSearchService.collectExperimentBatches(snapshot));
                });

        long projects = doReindex(
                em.createQuery("FROM Project p ORDER BY p.id", ProjectEntity.class),
                p -> {
                    ProjectSnapshot snapshot = snapshotMapper.createSnapshot(p);
                    p.setSearchVector(globalSearchService.collectProjectSearchVector(snapshot));
                });

        long notebooks = doReindex(
                em.createQuery("FROM Notebook n ORDER BY n.id", NotebookEntity.class),
                n -> {
                    NotebookSnapshot snapshot = snapshotMapper.createSnapshot(n);
                    n.setSearchVector(globalSearchService.collectNotebookSearchVector(snapshot));
                });

        long samples = doReindex(
                em.createQuery("FROM Sample s JOIN FETCH s.compound ORDER BY s.id", SampleEntity.class),
                s -> {
                    s.setSearchVector(globalSearchService.collectSampleSearchVector(s));
                });

        return Map.of(
                "projects", String.valueOf(projects),
                "notebooks", String.valueOf(notebooks),
                "experiments", String.valueOf(experiments),
                "samples", String.valueOf(samples));
    }

    private <T> long doReindex(TypedQuery<T> query, Consumer<T> processor) {
        try (Stream<T> stream = query
                .setHint(AvailableHints.HINT_FETCH_SIZE, 1000)
                .getResultStream()) {
            AtomicLong count = new AtomicLong(0);
            stream.forEach(item -> {
                processor.accept(item);
                em.flush();
                em.detach(item);
                count.incrementAndGet();
            });
            return count.get();
        }
    }

    @Transactional
    public Map<String, String> insertTestData() {
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
                Map<String, String> prepareData = attachmentService.prepareProjectAttachment(project.getId(), "attachment" + attachmentNo + ".txt", (long) "content".getBytes().length, true);
                String path = prepareData.get("url");
                String id = prepareData.get("id");
                String fileName = Arrays.stream(path.split("/")).toList().getLast();
                uploadService.uploadAttachment("attachment/" + fileName, "content".getBytes());
                attachmentService.completeProjectAttachment(project.getId(), UUID.fromString(id));
                attachmentCount++;
            }
            for (int notebookNo = 1; notebookNo <= random.nextInt(1, 4); notebookNo++) {
                log.debug("project {} notebook {}", projectNo, notebookNo);
                NotebookDetailsDTO notebook = notebookService.createNotebook(project.getId(), new NotebookRequest("%08d".formatted(++lastUsedNotebookNumber), "description"));
                notebookCount++;
                for (int attachmentNo = 1; attachmentNo <= random.nextInt(0, 4); attachmentNo++) {
                    log.debug("project {} notebook {} attachment {}", projectNo, notebookNo, attachmentNo);
                    Map<String, String> prepareData = attachmentService.prepareNotebookAttachment(notebook.getId(), "attachment" + attachmentNo + ".txt", (long) "content".getBytes().length, true);
                    String path = prepareData.get("url");
                    String id = prepareData.get("id");
                    String fileName = Arrays.stream(path.split("/")).toList().getLast();
                    uploadService.uploadAttachment("attachment/" + fileName, "content".getBytes());
                    attachmentService.completeNotebookAttachment(notebook.getId(), UUID.fromString(id));
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
                        Map<String, String> prepareData = attachmentService.prepareExperimentAttachment(experiment.getId(), "attachment" + attachmentNo + ".txt", (long) "content".getBytes().length, true);
                        String path = prepareData.get("url");
                        String id = prepareData.get("id");
                        String fileName = Arrays.stream(path.split("/")).toList().getLast();
                        uploadService.uploadAttachment("attachment/" + fileName, "content".getBytes());
                        attachmentService.completeExperimentAttachment(experiment.getId(), UUID.fromString(id));
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

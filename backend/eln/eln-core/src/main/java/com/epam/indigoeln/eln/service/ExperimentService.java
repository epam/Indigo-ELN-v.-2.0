package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.entity.TemplateEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.mapper.ExperimentMapper;
import com.epam.indigoeln.eln.mapper.ProjectMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.repository.NotebookRepository;
import com.epam.indigoeln.eln.repository.TemplateRepository;
import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.service.ExperimentModelService;
import com.epam.indigoeln.reports.api.ReportsAPI;
import com.epam.indigoeln.reports.api.ReportsClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.epam.indigoeln.common.util.ModelUtil.editProperty;
import static com.epam.indigoeln.eln.util.ModelUtil.updateDates;

@Slf4j
@DataAccess
@Transactional
@ApplicationScoped
public class ExperimentService {

    static final byte[] EMPTY_PICTURE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><svg xmlns=\"http://www.w3.org/2000/svg\" width=\"1\" height=\"1\"/>".getBytes(StandardCharsets.UTF_8);
    public static final Pattern FILENAME_REGEX = Pattern.compile("filename\\s*=\\s*\"?([^\";]+)\"?", Pattern.CASE_INSENSITIVE);

    @Inject
    NotebookRepository notebookRepository;
    @Inject
    ExperimentRepository experimentRepository;
    @Inject
    ACLService aclService;
    @Inject
    ExperimentMapper experimentMapper;
    @Inject
    UserService userService;
    @Inject
    DictionaryService dictionaryService;
    @Inject
    ExperimentModelService experimentModelService;
    @Inject
    TemplateRepository templateRepository;
    @Inject
    @RestClient
    ReportsClient reportsClient;
    @Inject
    ProjectMapper projectMapper;

    public ExperimentDetailsDTO createExperiment(UUID notebookId, ExperimentRequest request) {
        NotebookEntity notebook = notebookRepository.get(notebookId);
        aclService.ensureAccess(notebook, ApplicationPermission.CREATE_EXPERIMENTS);
        ExperimentEntity experiment = experimentMapper.requestToExperiment(request, ExperimentStatus.OPEN);
        TemplateEntity template = templateRepository.get(request.getTemplateID());
        experiment.setName(generateExperimentName(notebook));
        experiment.setTherapeuticArea(dictionaryService.lookup(BuiltInDictionary.THERAPEUTIC_AREA.name(), request.getTherapeuticArea()));
        experiment.setProjectCode(dictionaryService.lookup(BuiltInDictionary.PROJECT_CODE.name(), request.getProjectCode()));
        notebook.getProject().getExperiments().add(experiment);
        notebook.getExperiments().add(experiment);
        template.getExperiments().add(experiment);
        experiment.setProject(notebook.getProject());
        experiment.setNotebook(notebook);
        experiment.setTemplate(template);
        experiment.setModel(experimentModelService.createNewModel());
        updateDates(experiment, userService.getCurrentUser());
        aclService.initExperimentACL(experiment);
        experimentRepository.persist(experiment);
        experimentRepository.flushAndClear();
        return getExperiment(experiment.getId());
    }

    public Page<ExperimentDTO> getExperiments(@Nullable UUID projectId, @Nullable UUID notebookId, @Nullable SortOrder sort, @Nullable Boolean createdByMe, Paging paging) {
        UserEntity currentUser = null;

        if (Boolean.TRUE.equals(createdByMe)) {
            currentUser = userService.getCurrentUser();
        }

        return experimentRepository.findAll(projectId, notebookId, sort, currentUser, paging);
    }

    public List<ExperimentDTO> getMarkedExperiments() {
        return experimentRepository.findMarked();
    }

    public ExperimentDetailsDTO getExperiment(UUID experimentId) {
        return experimentRepository.load(experimentId);
    }

    public ExperimentDetailsDTO editExperiment(UUID experimentId, ExperimentEditRequest request) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, ApplicationPermission.EDIT_EXPERIMENTS);
        editProperty(request.getTherapeuticArea(), v -> {
            experiment.setTherapeuticArea(dictionaryService.lookup(BuiltInDictionary.THERAPEUTIC_AREA.name(), v));
        });
        editProperty(request.getProjectCode(), v -> {
            experiment.setProjectCode(dictionaryService.lookup(BuiltInDictionary.PROJECT_CODE.name(), v));
        });
        updateDates(experiment, userService.getCurrentUser());
        experimentRepository.flushAndClear();
        return getExperiment(experimentId);
    }

    public Boolean markExperiment(UUID experimentId, boolean isMarked) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, ApplicationPermission.VIEW_EXPERIMENTS);
        experimentRepository.markExperiment(experimentId, userService.getCurrentUser(), isMarked);
        return isMarked;
    }

    public List<ACLDetailsEntryDTO> updateExperimentAccess(UUID experimentId, List<AccessForm> form) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, ApplicationPermission.MANAGE_EXPERIMENT_ACCESS);
        for (AccessForm item : form) {
            UserEntity user = userService.getUserEntity(item.getUserID());
            aclService.updateExperimentACL(experiment.getNotebook().getProject(), experiment.getNotebook(), experiment, user, item.getLevel());
        }
        return experimentMapper.convertACLMap(experiment.getAclEntities());
    }

    public ExperimentModel getModel(UUID experimentId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, ApplicationPermission.VIEW_EXPERIMENTS);
        return experiment.getModel();
    }

    public ExperimentModel mutateModel(UUID experimentId, ExperimentModel model, Mutation mutation) {
        try {
            log.debug("Mutating model for experiment {} with mutation {}", experimentId, mutation);
            ExperimentEntity experiment = experimentRepository.get(experimentId);
            model = experimentModelService.applyMutation(experiment, model, mutation);
            experiment.setModel(model);
            return model;
        } catch (Throwable e) {
            log.error("Failed to mutate model for experiment {}: {}", experimentId, e.getMessage(), e);
            throw new RuntimeException("Failed to mutate model: " + e.getMessage(), e);
        }
    }

    public byte[] getExperimentPicture(UUID experimentId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, ApplicationPermission.VIEW_EXPERIMENTS);
        return experiment.getPicture() != null ? experiment.getPicture() : EMPTY_PICTURE;
    }

    public Response getReactionPicture(UUID experimentId, Anchor.Reaction reactionAnchor, @Nullable Integer version) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, ApplicationPermission.VIEW_EXPERIMENTS);
        Reaction reaction = getModel(experimentId).locate(reactionAnchor);
        CacheControl cacheControl = new CacheControl();
        if (version != null) {
            InvalidRequestException.validate(reaction.getRxnVersion() >= version, "Picture version " + version + " doesn't exist for reaction " + reactionAnchor);
            cacheControl.setMaxAge(3_600 * 24 * 30);
        }
        return Response.ok(experiment.getPicture() != null ? experiment.getPicture() : EMPTY_PICTURE, "image/svg+xml")
                .cacheControl(cacheControl)
                .build();
    }

    public Response printReport(UUID experimentId) {
        ExperimentReportContent content = printReport(experimentRepository.loadForReport(experimentId));
        return Response.ok(content.content)
                .header(HttpHeaders.CONTENT_DISPOSITION, content.contentDisposition)
                .header(HttpHeaders.CONTENT_TYPE, content.contentType)
                .build();
    }

    @SneakyThrows
    public ExperimentReportContent printReport(ExperimentEntity experiment) {
        experimentRepository.loadForReport(experiment.getId());
        ReportsAPI.ExperimentReportDataDTO data = new ReportsAPI.ExperimentReportDataDTO(
                projectMapper.entityToDTO(experiment.getProject()),
                experimentMapper.entityToDetailsDTO(experiment),
                experiment.getPicture() != null ? new String(experiment.getPicture(), StandardCharsets.UTF_8) : null,
                experiment.getModel()
        );
        try (Response response = reportsClient.generateExperimentReport(data)) {
            String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
            String contentDisposition = response.getHeaderString(HttpHeaders.CONTENT_DISPOSITION);
            String filename = "report.pdf";
            Matcher matcher = FILENAME_REGEX.matcher(contentDisposition);
            if (matcher.find()) {
                filename = matcher.group(1);
            }
            byte[] body = switch (response.getEntity()) {
                case byte[] bytes -> bytes;
                case InputStream is -> is.readAllBytes();
                default -> throw new IllegalStateException("Unexpected response entity type: " + response.getEntity().getClass());
            };
            return new ExperimentReportContent(body, contentDisposition, contentType, filename);
        }
    }

    private String generateExperimentName(NotebookEntity notebook) {
        String last = experimentRepository.getLastExperimentName(notebook);
        int lastNumber = last == null ? 0 : Integer.parseInt(last.substring(last.lastIndexOf('-') + 1));
        return "%s-%04d".formatted(notebook.getName(), lastNumber + 1);
    }

    public record ExperimentReportContent (
            byte[] content,
            String contentDisposition,
            String contentType,
            String filename
    ) {}
}

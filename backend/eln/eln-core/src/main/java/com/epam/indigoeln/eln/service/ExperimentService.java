package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.model.FindSamplesRequest;
import com.epam.indigoeln.compound.model.StructuralSearch;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.entity.UserInfo;
import com.epam.indigoeln.eln.mapper.ExperimentMapper;
import com.epam.indigoeln.eln.mapper.ProjectMapper;
import com.epam.indigoeln.eln.mapper.SnapshotMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.repository.NotebookRepository;
import com.epam.indigoeln.eln.repository.ProjectRepository;
import com.epam.indigoeln.eln.repository.TemplateRepository;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.epam.indigoeln.reaction.model.patch.ExperimentPatch;
import com.epam.indigoeln.reaction.service.ExperimentModelService;
import com.epam.indigoeln.reports.api.ReportsAPI;
import com.epam.indigoeln.reports.api.ReportsClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.epam.indigoeln.eln.model.ApplicationPermission.*;

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
    @Inject
    ObjectMapper objectMapper;
    @Inject
    CompoundService compoundService;
    @Inject
    ProjectRepository projectRepository;
    @Inject
    SnapshotMapper snapshotMapper;

    public ExperimentDetailsDTO createExperiment(UUID notebookId, ExperimentRequest request) {
        NotebookEntity notebook = notebookRepository.get(notebookId);
        aclService.ensureAccess(notebook, ApplicationPermission.CREATE_EXPERIMENTS);
        ExperimentEntity experiment = new ExperimentEntity();
        experiment.setCreatedBy(userService.getCurrentUserEntity());
        notebook.getProject().getExperiments().add(experiment);
        notebook.getExperiments().add(experiment);
        experiment.setProject(notebook.getProject());
        experiment.setNotebook(notebook);
        experiment.setLastUsedAnchor(0);
        experimentModelService.setModel(experiment, experimentModelService.createNewModel(experiment));
        experiment.setRevision(0);
        experiment.setStatus(ExperimentStatus.OPEN);
        experiment.setDeleted(false);
        experimentModelService.applyMutation(experiment, experimentMapper.requestToMutation(request));
        experimentRepository.flushAndRefresh(experiment);
        return getExperimentDetails(experiment);
    }

    public Page<ExperimentDTO> getExperiments(@Nullable UUID projectId, @Nullable UUID notebookId, @Nullable SortOrder sort, @Nullable Boolean createdByMe, Paging paging) {
        UserInfo currentUser = Boolean.TRUE.equals(createdByMe) ? userService.getCurrentUser() : null;
        boolean showAll = userService.getCurrentUser().getPermissions().contains(ApplicationPermission.VIEW_EXPERIMENTS);
        return experimentRepository.findAll(projectId, notebookId, sort, currentUser, paging, showAll);
    }

    public List<ExperimentDTO> getMarkedExperiments() {
        return experimentRepository.findMarked();
    }

    public ExperimentDetailsDTO getExperiment(UUID experimentId) {
        ExperimentEntity experiment = experimentRepository.load(experimentId);
        return getExperimentDetails(experiment);
    }

    public ExperimentSnapshot getExperimentSnapshot(UUID experimentId) {
        ExperimentEntity experiment = experimentRepository.load(experimentId);
        return snapshotMapper.createSnapshot(experiment, MutationContext.createFull(), () -> experimentModelService.getModel(experiment));
    }

    public ExperimentDetailsDTO getExperimentDetails(ExperimentEntity experiment) {
        Set<ApplicationPermission> currentPermissions = aclService.getCurrentPermissions(experiment.getCalculatedInfo() != null ? experiment.getCalculatedInfo().getCurrentAccess() : null);
        currentPermissions.retainAll(EnumSet.of(VIEW_EXPERIMENTS, EDIT_EXPERIMENTS, MANAGE_EXPERIMENT_ACCESS, DELETE_EXPERIMENTS, SUBMIT_EXPERIMENTS));
        return experimentMapper.entityToDetailsDTO(experiment, experimentModelService.getModel(experiment), currentPermissions);
    }

    public ExperimentDetailsDTO editExperiment(UUID experimentId, ExperimentEditRequest request) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, ApplicationPermission.EDIT_EXPERIMENTS);
        experimentModelService.applyMutation(experiment, experimentMapper.requestToMutation(request));
        experimentRepository.flushAndRefresh(experiment);
        return getExperimentDetails(experiment);
    }

    public Boolean markExperiment(UUID experimentId, boolean isMarked) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, ApplicationPermission.VIEW_EXPERIMENTS);
        experimentRepository.markExperiment(experimentId, userService.getCurrentUserEntity(), isMarked);
        return isMarked;
    }

    public List<ACLDetailsEntryDTO> updateExperimentAccess(UUID experimentId, List<AccessForm> form) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, ApplicationPermission.MANAGE_EXPERIMENT_ACCESS);
        Mutation mutation = new ExperimentMutation.EditExperimentAccess(form);
        experimentModelService.applyMutation(experiment, mutation);
        return experimentMapper.convertDetailsACLList(experiment.getFullACL());
    }

    public ExperimentModel mutateModel(UUID experimentId, Mutation mutation) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, EDIT_EXPERIMENTS);
        return experimentModelService.applyMutation(experiment, mutation).a();
    }

    public ExperimentPatch mutateModel2(UUID experimentId, Integer revision, Mutation mutation) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, EDIT_EXPERIMENTS);
        return experimentModelService.applyMutation(experiment, mutation).b();
    }

    public byte[] getExperimentPicture(UUID experimentId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, ApplicationPermission.VIEW_EXPERIMENTS);
        return experiment.getPicture() != null ? experiment.getPicture() : EMPTY_PICTURE;
    }

    public Response getReactionPicture(UUID experimentId, ReactionAnchor reactionAnchor, @Nullable Integer version) {
        // TODO generate on the fly from reaction rxnfile; shouldn't be heavyweight, because it will only be used when editing experiment, and most of the calls should be cached
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, ApplicationPermission.VIEW_EXPERIMENTS);
        Reaction reaction = experimentModelService.getModel(experiment).locate(reactionAnchor);
        CacheControl cacheControl = new CacheControl();
        if (version != null) {
            InvalidRequestException.validate(reaction.getRxnVersion() >= version, "Picture version " + version + " doesn't exist for reaction " + reactionAnchor);
            cacheControl.setMaxAge(3_600 * 24 * 30);
        }
        return Response.ok(experiment.getPicture() != null ? experiment.getPicture() : EMPTY_PICTURE, "image/svg+xml")
                .cacheControl(cacheControl)
                .build();
    }

    public Map<InputAnchor, @Nullable FindSamplesRequest> analyzeRXN(UUID experimentId, ReactionAnchor reactionAnchor) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, ApplicationPermission.VIEW_EXPERIMENTS);
        ExperimentModel model = experimentModelService.getModel(experiment);
        Reaction reaction = model.locate(reactionAnchor);
        return StreamEx.of(reaction.getInputs())
                .mapToEntry(ReactionInput::getAnchor, input -> {
                    if (input.getCompound().getCompoundID() != null) {
                        CompoundEntity compound = compoundService.getCompound(input.getCompound().getCompoundID());
                        return new FindSamplesRequest()
                                .withStructure(new StructuralSearch(StructuralSearch.Type.SUBSTRUCTURE, compound.getMolFile()));
                    }
                    return null;
                })
                .toCustomMap(LinkedHashMap::new);
    }

    public Response printReport(UUID experimentId) {
        ExperimentReportContent content = printReport(experimentRepository.load(experimentId));
        return Response.ok(content.content)
                .header(HttpHeaders.CONTENT_DISPOSITION, content.contentDisposition)
                .header(HttpHeaders.CONTENT_TYPE, content.contentType)
                .build();
    }

    @SneakyThrows
    public ExperimentReportContent printReport(ExperimentEntity experiment) {
        ReportsAPI.ExperimentReportDataDTO data = new ReportsAPI.ExperimentReportDataDTO(
                projectMapper.entityToDTO(experiment.getProject()),
                experimentMapper.entityToDetailsDTO(experiment, experimentModelService.getModel(experiment), Set.of()),
                experiment.getPicture() != null ? new String(experiment.getPicture(), StandardCharsets.UTF_8) : null
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

    public List<RevisionDetailsDTO<ExperimentPatch>> getExperimentRevisions(UUID experimentId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, ApplicationPermission.VIEW_EXPERIMENTS);
        return experimentMapper.revisionToDTOList(experiment.getRevisions());
    }

    public record ExperimentReportContent (
            byte[] content,
            String contentDisposition,
            String contentType,
            String filename
    ) {}
}

package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.config.TraceSegment;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.entity.TemplateEntity;
import com.epam.indigoeln.eln.entity.UserInfo;
import com.epam.indigoeln.eln.mapper.ExperimentMapper;
import com.epam.indigoeln.eln.mapper.ProjectMapper;
import com.epam.indigoeln.eln.mapper.SnapshotMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.repository.NotebookRepository;
import com.epam.indigoeln.eln.repository.TemplateRepository;
import com.epam.indigoeln.eln.util.PatchUtil;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.service.ExperimentModelService;
import com.epam.indigoeln.reaction.service.mutation.experiment.ExperimentMutationContext;
import com.epam.indigoeln.reports.api.ReportsAPI;
import com.epam.indigoeln.reports.api.ReportsClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.tuple.Triple;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.epam.indigoeln.common.exception.InvalidRequestException.validate;
import static com.epam.indigoeln.eln.model.ApplicationPermission.*;
import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
@DataAccess
@TraceSegment
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
    ExperimentModelService experimentModelService;
    @Inject
    TemplateRepository templateRepository;
    @Inject
    @RestClient
    ReportsClient reportsClient;
    @Inject
    ProjectMapper projectMapper;
    @Inject
    CompoundService compoundService;
    @Inject
    SnapshotMapper snapshotMapper;

    public ExperimentDetailsDTO createExperiment(UUID notebookId, ExperimentRequest request) {
        NotebookEntity notebook = notebookRepository.get(notebookId);
        ExperimentEntity experiment = new ExperimentEntity();
        notebook.getProject().getExperiments().add(experiment);
        notebook.getExperiments().add(experiment);
        experiment.setProject(notebook.getProject());
        experiment.setNotebook(notebook);
        TemplateEntity template = templateRepository.get(request.getTemplateID());
        template.getExperiments().add(experiment);
        experiment.setTemplate(template);
        experimentModelService.applyMutation(experiment, experimentMapper.requestToMutation(request));
        return getExperimentDetails(experiment);
    }

    public Page<ExperimentDTO> getExperiments(@Nullable UUID projectId, @Nullable UUID notebookId, @Nullable String search, @Nullable SortOrder sort, @Nullable Boolean createdByMe, Paging paging) {
        UserInfo currentUser = Boolean.TRUE.equals(createdByMe) ? userService.getCurrentUser() : null;
        boolean showAll = userService.getCurrentUser().getPermissions().contains(VIEW_EXPERIMENTS);
        return experimentRepository.findAll(projectId, notebookId, search, sort, currentUser, paging, showAll);
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
        return snapshotMapper.createSnapshot(experiment, true, true, experimentModelService.getModel(experiment));
    }

    public ExperimentDetailsDTO getExperimentDetails(ExperimentEntity experiment) {
        Set<ApplicationPermission> currentPermissions = aclService.getCurrentPermissions(experiment.getCalculatedInfo() != null ? experiment.getCalculatedInfo().getCurrentAccess() : null);
        currentPermissions.retainAll(EnumSet.of(VIEW_EXPERIMENTS, EDIT_EXPERIMENTS, MANAGE_EXPERIMENT_ACCESS, DELETE_EXPERIMENTS, SUBMIT_EXPERIMENTS));
        return experimentMapper.entityToDetailsDTO(experiment, experimentModelService.getModel(experiment), currentPermissions);
    }

    public ExperimentDetailsDTO editExperiment(UUID experimentId, ExperimentEditRequest request) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        experimentModelService.applyMutation(experiment, experimentMapper.requestToMutation(request));
        return getExperimentDetails(experiment);
    }

    public Boolean markExperiment(UUID experimentId, boolean isMarked) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, VIEW_EXPERIMENTS);
        experimentRepository.markExperiment(experimentId, userService.getCurrentUserEntity(), isMarked);
        return isMarked;
    }

    public List<ACLDetailsEntryDTO> updateExperimentAccess(UUID experimentId, List<AccessForm> form) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, MANAGE_EXPERIMENT_ACCESS);
        Mutation mutation = new ExperimentMutation.EditExperimentAccess(form);
        experimentModelService.applyMutation(experiment, mutation);
        return experimentMapper.convertDetailsACLList(experiment.getFullACL());
    }

    public ExperimentModel mutateModel(UUID experimentId, Mutation mutation) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, EDIT_EXPERIMENTS);
        return checkNotNull(experimentModelService.applyMutation(experiment, mutation).getLeft().getModel());
    }

    public JsonNode mutateModel2(UUID experimentId, Integer revision, Mutation mutation) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, EDIT_EXPERIMENTS);
        return experimentModelService.applyMutation(experiment, mutation).getMiddle();
    }

    public ExperimentSnapshot mutateModel3(UUID experimentId, Integer revision, Mutation mutation) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, EDIT_EXPERIMENTS);
        return experimentModelService.applyMutation(experiment, mutation).getLeft();
    }

    public MutationResponse mutateModel4(UUID experimentId, Integer revision, Mutation mutation) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, EDIT_EXPERIMENTS);
        Triple<ExperimentSnapshot, JsonNode, ExperimentMutationContext> triple = experimentModelService.applyMutation(experiment, mutation);
        MutationResponse response = triple.getRight().getResponse();
        response.setPatch(triple.getMiddle());
        return response;
    }

    public byte[] getExperimentPicture(UUID experimentId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, VIEW_EXPERIMENTS);
        return experiment.getPicture() != null ? experiment.getPicture() : EMPTY_PICTURE;
    }

    public byte[] getReactionPicture(UUID experimentId, ReactionAnchor reactionAnchor) {
        // TODO generate on the fly from reaction rxnfile; shouldn't be heavyweight, because it will only be used when editing experiment, and most of the calls should be cached
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, VIEW_EXPERIMENTS);
        Reaction reaction = experimentModelService.getModel(experiment).locate(reactionAnchor);
        return experiment.getPicture() != null ? experiment.getPicture() : EMPTY_PICTURE;
    }

    public Map<InputAnchor, String> analyzeRXN(UUID experimentId, ReactionAnchor reactionAnchor) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, VIEW_EXPERIMENTS);
        ExperimentModel model = experimentModelService.getModel(experiment);
        Reaction reaction = model.locate(reactionAnchor);
        return analyzeRXN(reaction);
    }

    public Map<InputAnchor, String> analyzeRXN(Reaction reaction) {
        return StreamEx.of(reaction.getInputs())
                .mapToEntry(ReactionInput::getAnchor, input -> {
                    if (input.getCompound() instanceof CompoundRef.Virtual) {
                        CompoundEntity compound = compoundService.getCompound(input.getCompound().getCompoundID());
                        return compound.getMolFile();
                    }
                    return null;
                })
                .filterValues(Objects::nonNull)
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

    public List<RevisionDetailsDTO> getExperimentRevisions(UUID experimentId, @Nullable UUID editSessionId, @Nullable Boolean reverseOrder) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, VIEW_EXPERIMENTS);
        return experimentMapper.revisionToDTOList(experimentRepository.getRevisions(experiment, editSessionId, MoreObjects.firstNonNull(reverseOrder, false)));
    }

    public List<ExperimentRevisionSummaryDTO> getExperimentRevisionsSummary(UUID experimentId) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, VIEW_EXPERIMENTS);
        experimentRepository.closeInactiveEditSessions(experiment, Duration.ofHours(1));
        return experimentRepository.getRevisionsSummary(experiment);
    }

    public JsonNode compareVersions(UUID experimentId, @Nullable Integer versionFrom, @Nullable Integer versionTo) {
        validate(!Objects.equals(versionFrom, versionTo), "Versions to compare must be different");
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, VIEW_EXPERIMENTS);
        return experimentModelService.createPatch(getSnapshotToCompare(experiment, versionFrom), getSnapshotToCompare(experiment, versionTo));
    }

    public String compareVersionsHTML(UUID experimentId, @org.jspecify.annotations.Nullable Integer versionFrom, @org.jspecify.annotations.Nullable Integer versionTo) {
        JsonNode patch = compareVersions(experimentId, versionFrom, versionTo);
        return PatchUtil.formatJSONDiff(patch);
    }

    private ExperimentSnapshot getSnapshotToCompare(ExperimentEntity experiment, @Nullable Integer version) {
        if (version != null) {
            return checkNotNull(experimentRepository.getVersion(experiment, version).getSnapshot());
        }
        ExperimentModel model = experimentModelService.getModel(experiment);
        return snapshotMapper.createSnapshot(experiment, true, true, model);
    }

    public List<ExperimentRef> suggestExperiments(String search) {
        return experimentRepository.suggest(search);
    }

    public record ExperimentReportContent (
            byte[] content,
            String contentDisposition,
            String contentType,
            String filename
    ) {}
}

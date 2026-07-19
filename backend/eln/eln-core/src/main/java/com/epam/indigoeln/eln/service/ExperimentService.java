package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.model.Page;
import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.model.SortOrder;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.config.DataAccess;
import com.epam.indigoeln.eln.entity.*;
import com.epam.indigoeln.eln.mapper.ExperimentMapper;
import com.epam.indigoeln.eln.mapper.ProjectMapper;
import com.epam.indigoeln.eln.mapper.SnapshotMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.repository.NotebookRepository;
import com.epam.indigoeln.eln.repository.TemplateRepository;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.indigowrapper.IndigoRendererAPI;
import com.epam.indigoeln.indigowrapper.IndigoSDFSaver;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.service.ExperimentModelService;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;
import com.epam.indigoeln.reaction.service.mutation.experiment.ExperimentMutationContext;
import com.epam.indigoeln.reports.api.ReportsAPI;
import com.epam.indigoeln.reports.api.ReportsClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.epam.indigoeln.common.exception.InvalidRequestException.validate;
import static com.epam.indigoeln.common.util.ContentDispositionUtil.extractFilename;
import static com.epam.indigoeln.common.util.ContentDispositionUtil.generateContentDisposition;
import static com.epam.indigoeln.eln.model.ApplicationPermission.*;
import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
@DataAccess
@Transactional
@ApplicationScoped
public class ExperimentService {

    static final byte[] EMPTY_PICTURE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><svg xmlns=\"http://www.w3.org/2000/svg\" width=\"1\" height=\"1\"/>".getBytes(StandardCharsets.UTF_8);

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
    @Inject
    IndigoAPI indigo;
    @Inject
    IndigoRendererAPI indigoRenderer;
    @Inject
    ObjectMapper objectMapper;

    public ExperimentDetailsDTO createExperiment(UUID notebookId, ExperimentRequest request) {
        NotebookEntity notebook = notebookRepository.get(notebookId);
        ExperimentEntity experiment = new ExperimentEntity();
        notebook.getProject().getExperiments().add(experiment);
        notebook.getExperiments().add(experiment);
        experiment.setProject(notebook.getProject());
        experiment.setNotebook(notebook);
        TemplateEntity template = templateRepository.get(request.getTemplateID());
        experiment.setTemplate(template);
        experiment.setModel(experimentModelService.createNewModel());
        experimentModelService.applyMutation(experiment, experimentMapper.requestToMutation(request));
        return getExperimentDetails(experiment);
    }

    public Page<ExperimentDTO> getExperiments(@Nullable UUID projectId, @Nullable UUID notebookId, @Nullable String search, @Nullable SortOrder sort, @Nullable Boolean createdByMe, Paging paging) {
        UserEntity currentUser = Boolean.TRUE.equals(createdByMe) ? userService.getCurrentUserEntity() : null;
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
        return snapshotMapper.createSnapshot(experiment, false);
    }

    public ExperimentDetailsDTO getExperimentDetails(ExperimentEntity experiment) {
        Set<ApplicationPermission> currentPermissions = aclService.getCurrentPermissions(experiment.getCurrentAccess());
        currentPermissions.retainAll(EnumSet.of(VIEW_EXPERIMENTS, EDIT_EXPERIMENTS, MANAGE_EXPERIMENT_ACCESS, DELETE_EXPERIMENTS, SUBMIT_EXPERIMENTS));
        return experimentMapper.entityToDetailsDTO(experiment, currentPermissions);
    }

    public ExperimentDetailsDTO editExperiment(UUID experimentId, ExperimentEditRequest request) {
        ExperimentEntity experiment = experimentRepository.loadAndLock(experimentId);
        experimentModelService.applyMutation(experiment, experimentMapper.requestToMutation(request));
        return getExperimentDetails(experiment);
    }

    public Boolean markExperiment(UUID experimentId, boolean isMarked) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, VIEW_EXPERIMENTS);
        experimentRepository.markExperiment(experimentId, userService.getCurrentUserEntity(), isMarked);
        return isMarked;
    }

    public List<ACLEntryDTO> updateExperimentAccess(UUID experimentId, List<AccessForm> form) {
        ExperimentEntity experiment = experimentRepository.loadAndLock(experimentId);
        ExperimentMutation mutation = new ExperimentMutation.EditExperimentAccess(form);
        experimentModelService.applyMutation(experiment, mutation);
        return experimentMapper.convertACLList(experiment.getFullACL());
    }

    @SneakyThrows
    public MutationResponse mutateModel(UUID experimentId, Integer revision, boolean verifyUndoRedo, ExperimentMutation mutation) {
        validate(mutation.isMutateMethodAllowed(), "Mutation is not allowed for generic mutate method");
        ExperimentEntity experiment = experimentRepository.loadAndLock(experimentId);
        aclService.ensureAccess(experiment, EDIT_EXPERIMENTS);
        MutationResult<ExperimentSnapshot, ExperimentMutationContext> result = experimentModelService.applyMutation(experiment, mutation);
        if (verifyUndoRedo) {
            MutationResult<ExperimentSnapshot, ExperimentMutationContext> undoResult = experimentModelService.applyMutation(experiment, new ExperimentMutation.Undo());
            undoResult.snapshotAfter().setRevision(result.snapshotBefore().getRevision());
            if (!undoResult.snapshotAfter().equals(result.snapshotBefore())) {
                throw new IllegalStateException("Experiment state after undo doesn't match the state before the initial mutation:\nBefore: %s\nAfter undo: %s\n".formatted(
                        objectMapper.writeValueAsString(result.snapshotBefore()), objectMapper.writeValueAsString(undoResult.snapshotAfter())
                ));
            }
            MutationResult<ExperimentSnapshot, ExperimentMutationContext> redoResult = experimentModelService.applyMutation(experiment, new ExperimentMutation.Redo());
            redoResult.snapshotAfter().setRevision(result.snapshotAfter().getRevision());
            if (!redoResult.snapshotAfter().equals(result.snapshotAfter())) {
                throw new IllegalStateException("Experiment state after redo doesn't match the state after initial mutation:\nAfter: %s\nAfter redo: %s\n".formatted(
                        objectMapper.writeValueAsString(result.snapshotAfter()), objectMapper.writeValueAsString(redoResult.snapshotAfter())
                ));
            }
        }
        MutationResponse response = result.context().getResponse();
        response.setPatch(result.patch());
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
        Reaction reaction = experiment.getModel().locate(reactionAnchor);
        checkNotNull(reaction); // reaction is not used for now, but will be used with multi-reaction experiments
        return experiment.getPicture() != null ? experiment.getPicture() : EMPTY_PICTURE;
    }

    public Map<InputAnchor, String> analyzeRXN(UUID experimentId, ReactionAnchor reactionAnchor) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, VIEW_EXPERIMENTS);
        Reaction reaction = experiment.getModel().locate(reactionAnchor);
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
                .nonNullValues()
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
                experimentMapper.entityToDetailsDTO(experiment, Set.of()),
                experiment.getPicture() != null ? new String(experiment.getPicture(), StandardCharsets.UTF_8) : null
        );
        try (Response response = reportsClient.generateExperimentReport(data)) {
            String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
            String contentDisposition = response.getHeaderString(HttpHeaders.CONTENT_DISPOSITION);
            String filename = MoreObjects.firstNonNull(extractFilename(contentDisposition), "report.pdf");
            byte[] body = switch (response.getEntity()) {
                case byte[] bytes -> bytes;
                case InputStream is -> is.readAllBytes();
                default -> throw new IllegalStateException("Unexpected response entity type: " + response.getEntity().getClass());
            };
            return new ExperimentReportContent(body, contentDisposition, contentType, filename);
        }
    }

    public List<RevisionSummaryDTO> getExperimentRevisions(UUID experimentId, @Nullable Boolean flatten) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, VIEW_EXPERIMENTS);
        List<ExperimentRevisionEntity> revisions = experimentRepository.getRevisions(experiment, false);
        if (Boolean.TRUE.equals(flatten)) {
            return StreamEx.of(revisions)
                    .map(experimentMapper::revisionToSummary)
                    .toList();
        }
        return StreamEx.of(revisions)
                .groupRuns((a, b) -> {
                    return a.getMutation().isApplicableToEditSession() && b.getMutation().isApplicableToEditSession() && a.getUser().getId().equals(b.getUser().getId());
                })
                .map(group -> {
                    return group.size() == 1
                            ? experimentMapper.revisionToSummary(group.getFirst())
                            : experimentMapper.revisionGroupToSummary(group);
                })
                .toList();
    }

    public String getRevisionDiff(UUID experimentId, int revision) {
        ExperimentEntity experiment = experimentRepository.get(experimentId);
        aclService.ensureAccess(experiment, VIEW_EXPERIMENTS);
        // rewind to get old state
        ExperimentSnapshot snapshot = snapshotMapper.createSnapshot(experiment, false);
        List<ExperimentRevisionEntity> range = experimentRepository.getRevisionRange(experiment, revision);
        ExperimentRevisionEntity targetRevision = range.getFirst();
        Preconditions.checkState(targetRevision.getRevision().equals(revision));
        JsonNode before = experimentModelService.rewindSnapshot(snapshot, range);
        return experimentModelService.formatDiff(before, targetRevision);
    }

    public List<ExperimentRef> suggestExperiments(String search) {
        return experimentRepository.suggest(search);
    }

    @SneakyThrows
    public MutationResponse importSDF(UUID experimentId, ReactionAnchor reactionAnchor, @NotNull FileUpload file) {
        ExperimentEntity experiment = experimentRepository.loadAndLock(experimentId);
        aclService.ensureAccess(experiment, EDIT_EXPERIMENTS);
        List<UUID> compoundIDs = compoundService.loadCompoundsFromFile(file.filePath(), false);
        MutationResult<ExperimentSnapshot, ExperimentMutationContext> result = experimentModelService.applyMutation(experiment, new ReactionMutation.ImportSDF(reactionAnchor, compoundIDs));
        MutationResponse response = result.context().getResponse();
        response.setPatch(result.patch());
        return response;
    }

    public record ExperimentReportContent (
            byte[] content,
            String contentDisposition,
            String contentType,
            String filename
    ) {}

    @Nullable
    private String getPropertySDFRepresentation(@Nullable Object property) {
        return switch (property) {
            case null -> null;
            case EnteredValue<?> ev when ev.isEmpty() -> null;
            case EnteredValue<?> ev -> ev.toUserFriendlyString(false, "");
            case Iterable<?> collection -> {
                yield StreamEx.of(collection.iterator())
                        .map(this::getPropertySDFRepresentation)
                        .nonNull()
                        .joining(System.lineSeparator());
            }
            default -> property.toString();
        };
    }

    private void setMoleculePropertyIfExists(IndigoMolecule molecule, @Nullable Object property, String propertyName) {
        String value = getPropertySDFRepresentation(property);
        if (value != null) {
            molecule.setProperty(propertyName, value);
        }
    }

    @SneakyThrows
    public Response exportSDF(UUID experimentId) {
        Path tempFilePath = Files.createTempFile("IndigoELN-export", ".sdf");

        try (IndigoSDFSaver saver = indigo.writeFile(tempFilePath.toString())) {
            ExperimentEntity experiment = experimentRepository.get(experimentId);
            aclService.ensureAccess(experiment, ApplicationPermission.VIEW_EXPERIMENTS);
            ExperimentModel model = experiment.getModel();

            for (Reaction reaction : model.getReactions()) {
                for (ReactionOutput output : reaction.getOutputs()) {
                    CompoundRef compoundRef = output.getCompound();

                    for (ReactionOutputSample sample: output.getSamples()) {
                        UUID moleculeId = sample.getRow().getCompound().getCompoundID();

                        if (moleculeId != null) {
                            CompoundEntity compound = compoundService.getCompound(moleculeId);
                            IndigoMolecule molecule = indigo.loadMolecule(compound.getMolFile());

                            molecule.setProperty("chemicalName", Objects.requireNonNullElse(compound.getChemicalName(), ""));

                            molecule.setProperty("shortNbkBatchNumber", sample.getShortNbkBatchNumber());
                            molecule.setProperty("outputName", output.getOutputName());

                            molecule.setProperty("type", output.getType().toString());
                            setMoleculePropertyIfExists(molecule, sample.getRegistrationStatus(), "registrationStatus");
                            setMoleculePropertyIfExists(molecule, sample.getActualWeight(), "actualWeight");
                            setMoleculePropertyIfExists(molecule, sample.getVolume(), "volume");
                            setMoleculePropertyIfExists(molecule, sample.getActualMol(), "actualMol");
                            setMoleculePropertyIfExists(molecule, sample.getMolarity(), "molarity");
                            setMoleculePropertyIfExists(molecule, sample.getYield(), "yield");
                            setMoleculePropertyIfExists(molecule, sample.getPurity(), "purity");

                            setMoleculePropertyIfExists(molecule, compoundRef.getMolWeight(), "molWeight");
                            setMoleculePropertyIfExists(molecule, sample.getSource(), "source");
                            setMoleculePropertyIfExists(molecule, compoundRef.getSaltCode(), "saltCode");
                            setMoleculePropertyIfExists(molecule, compoundRef.getStereoisomerCode(), "stereoisomerCode");
                            setMoleculePropertyIfExists(molecule, compoundRef.getSaltEQ(), "saltEQ");
                            setMoleculePropertyIfExists(molecule, output.getTheoWeight(), "theoWeight");
                            setMoleculePropertyIfExists(molecule, compoundRef.getCalculatedBatchMF(), "calculatedBatchMF");
                            setMoleculePropertyIfExists(molecule, sample.getStructureComment(), "structureComment");
                            setMoleculePropertyIfExists(molecule, sample.getSourceDetails(), "sourceDetails");
                            setMoleculePropertyIfExists(molecule, reaction.getPrecursorReactantIds(), "precursorReactantId");
                            setMoleculePropertyIfExists(molecule, sample.getStrCode(), "strCode");
                            setMoleculePropertyIfExists(molecule, output.getTheoMol(), "theoMol");
                            setMoleculePropertyIfExists(molecule, sample.getBatchComment(), "batchComment");

                            setMoleculePropertyIfExists(molecule, sample.getComponentState(), "componentState");
                            setMoleculePropertyIfExists(molecule, sample.getCompoundProtection(), "compoundProtection");
                            setMoleculePropertyIfExists(molecule, sample.getResidualSolvents(), "residualSolvents");
                            setMoleculePropertyIfExists(molecule, sample.getHealthHazards(), "healthHazards");
                            setMoleculePropertyIfExists(molecule, sample.getHandlingPrecautions(), "handlingPrecautions");

                            setMoleculePropertyIfExists(molecule, sample.getMeltingPoint(), "meltingPoint");
                            setMoleculePropertyIfExists(molecule, sample.getStorageInstructions(), "storageInstructions");
                            setMoleculePropertyIfExists(molecule, sample.getSolubilityInSolvents(), "solubilityInSolvents");
                            setMoleculePropertyIfExists(molecule, sample.getExternalSupplier(), "externalSupplier");

                            saver.sdfAppend(molecule);
                        }
                    }
                }
            }

            return Response.ok(Files.readAllBytes(tempFilePath))
                    .header(HttpHeaders.CONTENT_DISPOSITION, generateContentDisposition(false, experiment.getName() + ".sdf"))
                    .build();
        } finally {
            Files.deleteIfExists(tempFilePath);
        }
    }
}

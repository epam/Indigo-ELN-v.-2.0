package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.model.units.MeasurementUnit;
import com.epam.indigoeln.common.model.units.MolUnit;
import com.epam.indigoeln.common.model.units.MolarityUnit;
import com.epam.indigoeln.common.model.units.VolumeUnit;
import com.epam.indigoeln.common.model.units.WeightUnit;
import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.entity.ExperimentAttachment;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.ExperimentRevisionEntity;
import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.entity.UserInfo;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.model.ExperimentRef;
import com.epam.indigoeln.eln.model.ExperimentStatus;
import com.epam.indigoeln.eln.repository.ExperimentAttachmentRepository;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.repository.ProjectRepository;
import com.epam.indigoeln.eln.repository.UserRepository;
import com.epam.indigoeln.eln.service.AttachmentService;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.reaction.model.EnteredValue;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionInputSample;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.service.mutation.EntityMutationHelper;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.reaction.util.UnitUtils;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import one.util.streamex.StreamEx;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static com.epam.indigoeln.common.exception.InvalidRequestException.fail;
import static com.epam.indigoeln.common.exception.InvalidRequestException.validate;
import static com.epam.indigoeln.common.util.ModelUtil.editProperty;
import static com.epam.indigoeln.eln.util.ModelUtil.restoreAttachments;
import static com.google.common.base.Preconditions.checkNotNull;

@Dependent
@MutationHandlerFor(ExperimentMutation.CreateExperiment.class)
class CreateExperimentHandler extends AbstractExperimentMutationHandler<ExperimentMutation.CreateExperiment> {

    @Inject
    DictionaryService dictionaryService;

    @Override
    protected void doValidateAccess(ExperimentEntity experiment) {
        aclService.ensureAccess(experiment.getNotebook(), ApplicationPermission.CREATE_EXPERIMENTS);
    }

    @Override
    protected void doValidateStatus(ExperimentEntity entity) {
        // nothing
    }

    @Override
    public String doHandle(ExperimentEntity experiment, ExperimentMutation.CreateExperiment mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        experiment.setStatus(ExperimentStatus.OPEN);
        experiment.setDeleted(false);
        experiment.setTherapeuticArea(dictionaryService.lookup(mutation.therapeuticArea()));
        experiment.setProjectCode(dictionaryService.lookup(mutation.projectCode()));
        experiment.setDescription(mutation.description());

        experiment.setName(generateExperimentName(experiment.getNotebook()));
        experiment.setCreatedBy(userService.getCurrentUserEntity());
        experiment.setBatchCreator(experiment.getCreatedBy());
        aclService.initExperimentACL(experiment);
        return "Experiment created";
    }

    private String generateExperimentName(NotebookEntity notebook) {
        String last = experimentRepository.getLastExperimentName(notebook);
        int lastNumber = last == null ? 0 : Integer.parseInt(last.substring(last.lastIndexOf('-') + 1));
        return "%s-%04d".formatted(notebook.getName(), lastNumber + 1);
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.SetExperimentSignificantFigures.class)
class SetExperimentSignificantFiguresHandler extends ExperimentEditMutationHandlerBase<ExperimentMutation.SetExperimentSignificantFigures> {

    @Override
    public String doHandle(ExperimentEntity entity, ExperimentMutation.SetExperimentSignificantFigures mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        entity.getModel().setSignificantFigures(mutation.significantFigures());
        return formatSetterSummary("significant figures", mutation.significantFigures());
    }

    @Override
    public boolean isUndoable() {
        return true;
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.EditExperimentAttributes.class)
class EditExperimentAttributesHandler extends ExperimentEditMutationHandlerBase<ExperimentMutation.EditExperimentAttributes> {

    @Inject
    EntityMutationHelper entityMutationHelper;

    @Override
    public String doHandle(ExperimentEntity experiment, ExperimentMutation.EditExperimentAttributes mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        List<String> summaryList = new ArrayList<>();
        boolean updated = false;
        updated |= editProperty(mutation.title()
                , experiment::setTitle
                , summaryList, "title"
        );
        updated |= editProperty(mutation.therapeuticArea()
                , v -> {
                    DictionaryItemEntity value = dictionaryService.lookup(v);
                    experiment.setTherapeuticArea(value);
                }
                , summaryList
                , "therapeutic area"
        );
        updated |= editProperty(mutation.projectCode()
                , v -> {
                    DictionaryItemEntity value = dictionaryService.lookup(v);
                    experiment.setProjectCode(value);
                }
                , summaryList
                , "project code"
        );
        updated |= editProperty(mutation.description()
                , experiment::setDescription
                , summaryList
                , _ -> "description"
        );
        updated |= editProperty(mutation.literature()
                , experiment::setLiterature
                , summaryList
                , _ -> "literature"
        );
        updated |= editProperty(mutation.linkedExperiments()
                , v -> experiment.setLinkedExperiments(idsFromRefs(v))
                , summaryList
                , "linked experiments"
        );
        updated |= editProperty(mutation.continuedFrom()
                , v -> experiment.setContinuedFrom(idsFromRefs(v))
                , summaryList
                , "continued from"
        );
        updated |= editProperty(mutation.continuedTo()
                , v -> experiment.setContinuedTo(idsFromRefs(v))
                , summaryList
                , "continued to"
        );
        validate(updated, "Nothing to update");
        return entityMutationHelper.formatEditAttributesSummary(summaryList);
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public void doRestoreStateAfterUndo(ExperimentEntity experiment, ExperimentSnapshot snapshot, ExperimentMutation.EditExperimentAttributes mutation) {
        experiment.setTitle(snapshot.getTitle());
        experiment.setTherapeuticArea(dictionaryService.lookup(snapshot.getTherapeuticArea()));
        experiment.setProjectCode(dictionaryService.lookup(snapshot.getProjectCode()));
        experiment.setDescription(snapshot.getDescription());
        experiment.setLiterature(snapshot.getLiterature());
        if (mutation.linkedExperiments().isPresent()) {
            experiment.setLinkedExperiments(idsFromRefs(snapshot.getLinkedExperiments()));
        }
        if (mutation.continuedFrom().isPresent()) {
            experiment.setContinuedFrom(idsFromRefs(snapshot.getContinuedFrom()));
        }
        if (mutation.continuedTo().isPresent()) {
            experiment.setContinuedTo(idsFromRefs(snapshot.getContinuedTo()));
        }
    }

    private Set<UUID> idsFromRefs(Collection<ExperimentRef> refs) {
        Set<UUID> ids = StreamEx.of(refs).map(ExperimentRef::getId).toSet();
        validate(ids.isEmpty() || experimentRepository.resolveRefs(ids).size() == ids.size(), "One or more referenced experiments do not exist");
        return ids;
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.SetBatchCreator.class)
class SetBatchCreatorHandler extends ExperimentEditMutationHandlerBase<ExperimentMutation.SetBatchCreator> {

    @Inject
    UserRepository userRepository;

    @Override
    public String doHandle(ExperimentEntity entity, ExperimentMutation.SetBatchCreator mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        for (Reaction reaction : entity.getModel().getReactions()) {
            for (ReactionOutput row : reaction.getOutputs()) {
                if (row.hasSamplesWithRegistrationStarted()) {
                    fail("Cannot modify batch creator after at least one batch is submitted for registration");
                }
            }
        }
        UserInfo batchCreator = userService.getUserInfo(mutation.batchCreator());
        entity.setBatchCreator(userRepository.getReference(batchCreator.getId()));
        return formatSetterSummary("batch creator", batchCreator.getDisplayName());
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public void doRestoreStateAfterUndo(ExperimentEntity experiment, ExperimentSnapshot snapshot, ExperimentMutation.SetBatchCreator mutation) {
        UserInfo batchCreator = userService.getUserInfo(snapshot.getBatchCreator());
        experiment.setBatchCreator(userRepository.getReference(batchCreator.getId()));
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.EditExperimentAccess.class)
class EditExperimentAccessHandler extends AbstractExperimentMutationHandler<ExperimentMutation.EditExperimentAccess> {

    @Inject
    ProjectRepository projectRepository;
    @Inject
    EntityMutationHelper entityMutationHelper;

    @Override
    protected void doValidateAccess(ExperimentEntity experiment) {
        aclService.ensureAccess(experiment, ApplicationPermission.MANAGE_EXPERIMENT_ACCESS);
    }

    @Override
    protected void doValidateStatus(ExperimentEntity entity) {
        // nothing
    }

    @Override
    public String doHandle(ExperimentEntity experiment, ExperimentMutation.EditExperimentAccess mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        String summary = entityMutationHelper.formatEditAccessSummary(mutation.edits());
        projectRepository.lockProject(experiment.getProject());
        aclService.updateExperimentACL(experiment.getNotebook().getProject(), experiment.getNotebook(), experiment, mutation.edits());
        return summary;
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.CreateExperimentAttachment.class)
class CreateExperimentAttachmentHandler extends ExperimentEditMutationHandlerBase<ExperimentMutation.CreateExperimentAttachment> {

    @Inject
    ExperimentAttachmentRepository attachmentRepository;
    @Inject
    AttachmentService attachmentService;
    @Inject
    EntityMutationHelper entityMutationHelper;

    @Override
    public String doHandle(ExperimentEntity experiment, ExperimentMutation.CreateExperimentAttachment mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        ExperimentAttachment attachment = attachmentRepository.getReference(mutation.attachmentID());
        attachmentService.doAddAttachment(experiment, attachment);
        return entityMutationHelper.formatCreateAttachmentSummary(attachment);
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public void doRestoreStateAfterUndo(ExperimentEntity experiment, ExperimentSnapshot snapshot, ExperimentMutation.CreateExperimentAttachment mutation) {
        restoreAttachments(experiment, attachmentRepository.getReferences(checkNotNull(snapshot.getAttachments())));
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.DeleteExperimentAttachment.class)
class DeleteExperimentAttachmentHandler extends ExperimentEditMutationHandlerBase<ExperimentMutation.DeleteExperimentAttachment> {

    @Inject
    ExperimentAttachmentRepository attachmentRepository;

    @Override
    public String doHandle(ExperimentEntity experiment, ExperimentMutation.DeleteExperimentAttachment mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        ExperimentAttachment attachment = attachmentRepository.getReference(mutation.attachmentID());
        experiment.getAttachments().remove(attachment);
        attachment.setParent(null);
        attachment.setDeleted(true);
        return "Deleted attachment: " + attachment.getName();
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public void doRestoreStateAfterUndo(ExperimentEntity experiment, ExperimentSnapshot snapshot, ExperimentMutation.DeleteExperimentAttachment mutation) {
        restoreAttachments(experiment, attachmentRepository.getReferences(checkNotNull(snapshot.getAttachments())));
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.ExperimentAccessUpdated.class)
class ExperimentAccessUpdatedHandler extends AbstractExperimentMutationHandler<ExperimentMutation.ExperimentAccessUpdated> {

    @Override
    protected void doValidateAccess(ExperimentEntity experiment) {
        // nothing
    }

    @Override
    protected void doValidateStatus(ExperimentEntity experiment) {
        // nothing
    }

    @Override
    public String doHandle(ExperimentEntity experiment, ExperimentMutation.ExperimentAccessUpdated mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        aclService.recalculateACL(experiment);
        String reason = mutation.projectName() != null ? "project " + mutation.projectName() : "notebook " + mutation.notebookName();
        return "Access updated because of the changes in " + reason;
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.ExperimentNameUpdated.class)
class ExperimentNameUpdatedHandler extends AbstractExperimentMutationHandler<ExperimentMutation.ExperimentNameUpdated> {

    @Inject
    ExperimentRepository experimentRepository;

    @Override
    protected void doValidateAccess(ExperimentEntity experiment) {
        // nothing
    }

    @Override
    protected void doValidateStatus(ExperimentEntity experiment) {
        // nothing
    }

    @Override
    public String doHandle(ExperimentEntity experiment, ExperimentMutation.ExperimentNameUpdated mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        String oldName = experiment.getName();
        String newName = regenerateExperimentName(experiment, mutation.notebookName());
        experiment.setName(newName);
        return "Experiment name updated from " + oldName + " to " + newName + " due to notebook rename";
    }

    private String regenerateExperimentName(ExperimentEntity experiment, String notebookName) {
        String oldName = experiment.getName();
        int lastDash = oldName.lastIndexOf('-');
        String experimentNumber = oldName.substring(lastDash + 1);
        return "%s-%s".formatted(notebookName, experimentNumber);
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.Undo.class)
class ExperimentUndoHandler extends ExperimentEditMutationHandlerBase<ExperimentMutation.Undo> {

    @Inject
    ExperimentUndoHelper experimentUndoHelper;

    @Override
    public void doPrepare(ExperimentEntity entity, ExperimentMutation.Undo mutation, ExperimentMutationContext context) {
        experimentUndoHelper.doPrepare(entity, userService.getCurrentUserEntity(), false, context);
    }

    @Override
    public String doHandle(ExperimentEntity entity, ExperimentMutation.Undo mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        return experimentUndoHelper.doHandle(entity, snapshotBefore, context, false);
    }

    @Override
    protected ExperimentRevisionEntity doCreateRevision(ExperimentEntity experiment, ExperimentMutation.Undo mutation, String summary, Integer revisionNo, JsonNode patch, ExperimentMutationContext context, ExperimentSnapshot snapshotAfter) {
        ExperimentRevisionEntity revision = super.doCreateRevision(experiment, mutation, summary, revisionNo, patch, context, snapshotAfter);
        revision.setUndoFor(checkNotNull(context.getUndoInfo()).getRevision().getRevisionNo());
        return revision;
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.Redo.class)
class ExperimentRedoHandler extends ExperimentEditMutationHandlerBase<ExperimentMutation.Redo> {

    @Inject
    ExperimentUndoHelper experimentUndoHelper;

    @Override
    public void doPrepare(ExperimentEntity entity, ExperimentMutation.Redo mutation, ExperimentMutationContext context) {
        experimentUndoHelper.doPrepare(entity, userService.getCurrentUserEntity(), true, context);
    }

    @Override
    public String doHandle(ExperimentEntity entity, ExperimentMutation.Redo mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        return experimentUndoHelper.doHandle(entity, snapshotBefore, context, true);
    }

    @Override
    protected ExperimentRevisionEntity doCreateRevision(ExperimentEntity experiment, ExperimentMutation.Redo mutation, String summary, Integer revisionNo, JsonNode patch, ExperimentMutationContext context, ExperimentSnapshot snapshotAfter) {
        ExperimentRevisionEntity revision = super.doCreateRevision(experiment, mutation, summary, revisionNo, patch, context, snapshotAfter);
        revision.setRedoFor(checkNotNull(context.getUndoInfo()).getRevision().getRevisionNo());
        return revision;
    }
}

abstract class AbstractSetExperimentUnitHandler<T extends ExperimentMutation> extends ExperimentEditMutationHandlerBase<T> {

    @Override
    public boolean isUndoable() {
        return true;
    }

    protected <U extends MeasurementUnit> void convert(EnteredValue<U> value, U from, U to, Consumer<EnteredValue<U>> setter) {
        switch (value) {
            case EnteredValue.UserEntered<U> v -> setter.accept(EnteredValue.userEntered(convertValue(v, from, to).toPlainString(), to, v.getRevision()));
            case EnteredValue.Default<U> v -> setter.accept(EnteredValue.defaultValue(convertValue(v, from, to), to));
            default -> {} // calculated values get recalculated, the rest have no value
        }
    }

    private static <U extends MeasurementUnit> BigDecimal convertValue(EnteredValue<U> value, U from, U to) {
        return UnitUtils.convert(value.toBigDecimal(), from, to).stripTrailingZeros();
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.SetExperimentWeightUnit.class)
class SetExperimentWeightUnitHandler extends AbstractSetExperimentUnitHandler<ExperimentMutation.SetExperimentWeightUnit> {

    @Override
    public String doHandle(ExperimentEntity entity, ExperimentMutation.SetExperimentWeightUnit mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        WeightUnit from = entity.getModel().getUnitSource().getWeightUnit(), to = mutation.weightUnit();
        for (Reaction reaction : entity.getModel().getReactions()) {
            for (ReactionInput input : reaction.getInputs()) {
                convert(input.getWeight(), from, to, input::setWeight);
                for (ReactionInputSample sample : input.getSamples()) {
                    convert(sample.getWeight(), from, to, sample::setWeight);
                }
            }
            for (ReactionOutput output : reaction.getOutputs()) {
                for (ReactionOutputSample sample : output.getSamples()) {
                    convert(sample.getActualWeight(), from, to, sample::setActualWeight);
                }
            }
        }

        entity.getModel().setUnitSource(entity.getModel().getUnitSource().withWeightUnit(to));
        return formatSetterSummary("weight unit", to.getDisplayName());
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.SetExperimentVolumeUnit.class)
class SetExperimentVolumeUnitHandler extends AbstractSetExperimentUnitHandler<ExperimentMutation.SetExperimentVolumeUnit> {

    @Override
    public String doHandle(ExperimentEntity entity, ExperimentMutation.SetExperimentVolumeUnit mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        VolumeUnit from = entity.getModel().getUnitSource().getVolumeUnit(), to = mutation.volumeUnit();
        for (Reaction reaction : entity.getModel().getReactions()) {
            for (ReactionInput input : reaction.getInputs()) {
                convert(input.getVolume(), from, to, input::setVolume);
                for (ReactionInputSample sample : input.getSamples()) {
                    convert(sample.getVolume(), from, to, sample::setVolume);
                }
            }
            for (ReactionOutput output : reaction.getOutputs()) {
                for (ReactionOutputSample sample : output.getSamples()) {
                    convert(sample.getVolume(), from, to, sample::setVolume);
                }
            }
        }

        entity.getModel().setUnitSource(entity.getModel().getUnitSource().withVolumeUnit(mutation.volumeUnit()));
        return formatSetterSummary("volume unit", mutation.volumeUnit().getDisplayName());
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.SetExperimentMolUnit.class)
class SetExperimentMolUnitHandler extends AbstractSetExperimentUnitHandler<ExperimentMutation.SetExperimentMolUnit> {

    @Override
    public String doHandle(ExperimentEntity entity, ExperimentMutation.SetExperimentMolUnit mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        MolUnit from = entity.getModel().getUnitSource().getMolUnit(), to = mutation.molUnit();
        for (Reaction reaction : entity.getModel().getReactions()) {
            for (ReactionInput input : reaction.getInputs()) {
                convert(input.getMol(), from, to, input::setMol);
                for (ReactionInputSample sample : input.getSamples()) {
                    convert(sample.getMol(), from, to, sample::setMol);
                }
            }
            for (ReactionOutput output : reaction.getOutputs()) {
                for (ReactionOutputSample sample : output.getSamples()) {
                    convert(sample.getActualMol(), from, to, sample::setActualMol);
                }
            }
        }

        entity.getModel().setUnitSource(entity.getModel().getUnitSource().withMolUnit(mutation.molUnit()));
        return formatSetterSummary("mol unit", mutation.molUnit().getDisplayName());
    }
}

@Dependent
@MutationHandlerFor(ExperimentMutation.SetExperimentMolarityUnit.class)
class SetExperimentMolarityUnitHandler extends AbstractSetExperimentUnitHandler<ExperimentMutation.SetExperimentMolarityUnit> {

    @Override
    public String doHandle(ExperimentEntity entity, ExperimentMutation.SetExperimentMolarityUnit mutation, ExperimentMutationContext context, ExperimentSnapshot snapshotBefore) {
        MolarityUnit from = entity.getModel().getUnitSource().getMolarityUnit(), to = mutation.molarityUnit();
        for (Reaction reaction : entity.getModel().getReactions()) {
            for (ReactionInput input : reaction.getInputs()) {
                for (ReactionInputSample sample : input.getSamples()) {
                    convert(sample.getMolarity(), from, to, sample::setMolarity);
                }
            }
            for (ReactionOutput output : reaction.getOutputs()) {
                for (ReactionOutputSample sample : output.getSamples()) {
                    convert(sample.getMolarity(), from, to, sample::setMolarity);
                }
            }
        }
        entity.getModel().setUnitSource(entity.getModel().getUnitSource().withMolarityUnit(mutation.molarityUnit()));
        return formatSetterSummary("molarity unit", mutation.molarityUnit().getDisplayName());
    }
}

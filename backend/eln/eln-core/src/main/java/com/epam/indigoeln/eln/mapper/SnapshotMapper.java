package com.epam.indigoeln.eln.mapper;

import com.epam.indigoeln.eln.entity.*;
import com.epam.indigoeln.eln.model.ACLEntryDTO;
import com.epam.indigoeln.eln.model.AttachmentDTO;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.reaction.model.*;
import jakarta.inject.Inject;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueCheckStrategy =  NullValueCheckStrategy.ALWAYS)
public abstract class SnapshotMapper extends AbstractMapper {

    @Inject
    ExperimentRepository experimentRepository;

    @Mapping(target = "model", ignore = true)
    @Mapping(target = "acl", source = "fullACL")
    @Mapping(target = "templateId", source = "template.id")
    @Mapping(target = "linkedExperiments", ignore = true)
    @Mapping(target = "continuedFrom", ignore = true)
    @Mapping(target = "continuedTo", ignore = true)
    public abstract ExperimentSnapshot copyBasicFields(ExperimentEntity entity);

    @Mapping(target = "acl", source = "fullACL")
    public abstract ProjectSnapshot createSnapshot(ProjectEntity entity);

    @Mapping(target = "acl", source = "fullACL")
    public abstract NotebookSnapshot createSnapshot(NotebookEntity entity);

    protected abstract AttachmentDTO convertAttachment(AttachmentEntity entity);

    protected abstract Set<AttachmentDTO> convertAttachments(List<AttachmentEntity> attachments);

    protected abstract Set<ACLEntryDTO> convertACLs(ACLEntry[] aclEntries);

    public ExperimentSnapshot createSnapshot(ExperimentEntity experiment, boolean snapshotModel) {
        ExperimentSnapshot snapshot = copyBasicFields(experiment);
        snapshot.setModel(snapshotModel
                ? copyModel(experiment.getModel())
                : experiment.getModel()
        );
        ExperimentRepository.LinkedExperimentRefs refs = experimentRepository.resolveLinkedExperimentRefs(experiment);
        snapshot.setLinkedExperiments(Set.copyOf(refs.linkedExperiments()));
        snapshot.setContinuedFrom(Set.copyOf(refs.continuedFrom()));
        snapshot.setContinuedTo(Set.copyOf(refs.continuedTo()));
        return snapshot;
    }

    public ExperimentModel copyModel(ExperimentModel model) {
        ExperimentModel modelCopy = new ExperimentModel();
        doCopyModel(model, modelCopy);
        for (Reaction reaction : model.getReactions()) {
            Reaction reactionCopy = Reaction.create(modelCopy, reaction.getAnchor());
            copyReaction(reaction, reactionCopy);
            for (ReactionInput input : reaction.getInputs()) {
                ReactionInput inputCopy = ReactionInput.create(reactionCopy, input.getRole(), input.getAnchor(), copyCompoundRef(input.getCompound()));
                copyReactionInput(input, inputCopy);
                for (ReactionInputSample sample : input.getSamples()) {
                    ReactionInputSample sampleCopy = ReactionInputSample.create(inputCopy, sample.getAnchor());
                    copyReactionInputSample(sample, sampleCopy);
                }
            }
            for (ReactionOutput output : reaction.getOutputs()) {
                ReactionOutput outputCopy = ReactionOutput.create(reactionCopy, output.getType(), output.isIntended(), output.getOutputName(), output.getAnchor(), copyCompoundRef(output.getCompound()), output.getEq());
                copyReactionOutput(output, outputCopy);
                for (ReactionOutputSample sample : output.getSamples()) {
                    ReactionOutputSample sampleCopy = ReactionOutputSample.create(outputCopy, sample.getNbkBatchNumber(), sample.getAnchor(), sample.getPurity());
                    copyReactionOutputSample(sample, sampleCopy);
                }
            }
        }
        return modelCopy;
    }

    @Mapping(target = "reactions", ignore = true)
    protected abstract void doCopyModel(ExperimentModel model, @MappingTarget ExperimentModel copy);

    @Mapping(target = "inputs", ignore = true)
    @Mapping(target = "outputs", ignore = true)
    protected abstract void copyReaction(Reaction reaction, @MappingTarget Reaction copy);

    @Mapping(target = "role", ignore = true)
    @Mapping(target = "samples", ignore = true)
    protected abstract void copyReactionInput(ReactionInput row, @MappingTarget ReactionInput copy);

    @Mapping(target = "healthHazards", expression = "java(List.copyOf(sample.getHealthHazards()))")
    protected abstract void copyReactionInputSample(ReactionInputSample sample, @MappingTarget ReactionInputSample copy);

    @Mapping(target = "type", ignore = true)
    @Mapping(target = "intended", ignore = true)
    @Mapping(target = "outputName", ignore = true)
    @Mapping(target = "eq", ignore = true)
    @Mapping(target = "samples", ignore = true)
    protected abstract void copyReactionOutput(ReactionOutput row, @MappingTarget ReactionOutput copy);

    @Mapping(target = "purity", ignore = true)
    @Mapping(target = "healthHazards", expression = "java(List.copyOf(sample.getHealthHazards()))")
    @Mapping(target = "handlingPrecautions", expression = "java(List.copyOf(sample.getHandlingPrecautions()))")
    @Mapping(target = "storageInstructions", expression = "java(List.copyOf(sample.getStorageInstructions()))")
    @Mapping(target = "compoundProtection", expression = "java(List.copyOf(sample.getCompoundProtection()))")
    @Mapping(target = "solubilityInSolvents", expression = "java(List.copyOf(sample.getSolubilityInSolvents()))")
    @Mapping(target = "residualSolvents", expression = "java(List.copyOf(sample.getResidualSolvents()))")
    @Mapping(target = "purityCalculations", expression = "java(List.copyOf(sample.getPurityCalculations()))")
    protected abstract void copyReactionOutputSample(ReactionOutputSample sample, @MappingTarget ReactionOutputSample copy);

    protected CompoundRef copyCompoundRef(CompoundRef ref) {
        return switch (ref) {
            case CompoundRef.StoredOrVirtual s -> ref; // immutable
            case CompoundRef.Unknown u -> copyUnknownCompoundRef(u);
        };
    }

    protected abstract CompoundRef.Unknown copyUnknownCompoundRef(CompoundRef.Unknown ref);
}

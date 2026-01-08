package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.model.SampleRegistrationRequest;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.epam.indigoeln.reaction.service.mutation.*;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.userLastEntered;

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputHealthHazards.class)
class SetOutputHealthHazardsHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputHealthHazards, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputHealthHazards mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        List<DictionaryItemRef> old = sample.getHealthHazards();
        sample.setHealthHazards(mutation.healthHazards());
        return new MutationResult(mutationHelper.formatSetterSummary("batch health hazards", mutation.healthHazards())
                , null
                , new ReactionOutputSampleMutation.SetOutputHealthHazards(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputActualMol.class)
class SetOutputActualMolHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputActualMol, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputActualMol mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        EnteredValueUndo<MolUnit> undo = mutationHelper.setEnteredValue(sample::getActualMol, sample::setActualMol, mutation.actualMol(), mutation.unit(), mutation.source());
        return new MutationResult(mutationHelper.formatSetterSummary("batch actual mol", mutation.actualMol(), mutation.unit())
                , null
                , new ReactionOutputSampleMutation.SetOutputActualMol(mutation.anchor(), undo.value(), undo.unit(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputActualWeight.class)
class SetOutputActualWeightHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputActualWeight, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputActualWeight mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        EnteredValueUndo<WeightUnit> undo = mutationHelper.setEnteredValue(sample::getActualWeight, sample::setActualWeight, mutation.actualWeight(), mutation.unit(), mutation.source());
        return new MutationResult(mutationHelper.formatSetterSummary("batch actual weight", mutation.actualWeight(), mutation.unit())
                , null
                , new ReactionOutputSampleMutation.SetOutputActualWeight(mutation.anchor(), undo.value(), undo.unit(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.RegisterSample.class)
class RegisterSampleHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.RegisterSample, MutationRedoInfo> {

    @Inject
    CompoundService compoundService;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sampleRow, ReactionOutputSampleMutation.RegisterSample mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        if (sampleRow.getRegistrationStatus() != null) {
            throw new InvalidRequestException("Sample already sent for registration");
        }
        if (sampleRow.getRow().getCompound() instanceof CompoundRef.Unknown) {
            throw new InvalidRequestException("Cannot register sample for unknown compound");
        }
        sampleRow.setRegistrationStatus(SampleRegistrationStatus.IN_PROGRESS);
        SampleEntity sample = compoundService.registerSample(new SampleRegistrationRequest(sampleRow.getRow().getCompound())
                .withNbkBatchNumber(sampleRow.getNbkBatchNumber())
                .withDensity(sampleRow.getDensity())
                .withMolarity(sampleRow.getMolarity())
                .withPurity(sampleRow.getPurity().getValue())
                .withHealthHazards(sampleRow.getHealthHazards())
                .withCompoundState(sampleRow.getComponentState())
                .withBatchComment(sampleRow.getBatchComment())
        );
        sampleRow.setRegistrationStatus(SampleRegistrationStatus.REGISTERED);
        sampleRow.setStrCode(sample.getStrCode());
        sampleRow.setSampleId(sample.getId());
        if (sampleRow.getRow().getCompound() instanceof CompoundRef.Virtual) {
            sampleRow.getRow().setCompound(compoundService.realCompoundRef(sample.getCompound()));
        }
        return new MutationResult("Register sample"
                , null
                , null
        );
    }
}

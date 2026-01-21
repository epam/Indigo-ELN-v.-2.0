package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.model.SampleRegistrationRequest;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.epam.indigoeln.reaction.service.mutation.AbstractReactionOutputSampleMutationHandler;
import com.epam.indigoeln.reaction.service.mutation.EnteredValueUndo;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.util.List;

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputHealthHazards.class)
class SetOutputHealthHazardsHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputHealthHazards> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputHealthHazards mutation) {
        List<DictionaryItemRef> old = sample.getHealthHazards();
        sample.setHealthHazards(mutation.healthHazards());
        return new MutationResult(formatSetterSummary("batch health hazards", mutation.healthHazards())
                , new ReactionOutputSampleMutation.SetOutputHealthHazards(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputActualMol.class)
class SetOutputActualMolHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputActualMol> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputActualMol mutation) {
        EnteredValueUndo<MolUnit> undo = setEnteredValue(sample::getActualMol, sample::setActualMol, mutation.actualMol(), mutation.unit(), mutation.source(), experiment.getRevision());
        return new MutationResult(formatSetterSummary("batch actual mol", mutation.actualMol(), mutation.unit())
                , new ReactionOutputSampleMutation.SetOutputActualMol(mutation.anchor(), undo.value(), undo.unit(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputActualWeight.class)
class SetOutputActualWeightHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputActualWeight> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputActualWeight mutation) {
        EnteredValueUndo<WeightUnit> undo = setEnteredValue(sample::getActualWeight, sample::setActualWeight, mutation.actualWeight(), mutation.unit(), mutation.source(), experiment.getRevision());
        return new MutationResult(formatSetterSummary("batch actual weight", mutation.actualWeight(), mutation.unit())
                , new ReactionOutputSampleMutation.SetOutputActualWeight(mutation.anchor(), undo.value(), undo.unit(), undo.source())
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.RegisterSample.class)
class RegisterSampleHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.RegisterSample> {

    @Inject
    CompoundService compoundService;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sampleRow, ReactionOutputSampleMutation.RegisterSample mutation) {
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
        return new MutationResult("Register sample", null);
    }
}

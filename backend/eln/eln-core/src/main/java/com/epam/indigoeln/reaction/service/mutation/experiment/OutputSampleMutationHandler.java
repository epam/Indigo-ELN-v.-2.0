package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.model.SampleRegistrationRequest;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputHealthHazards.class)
class SetOutputHealthHazardsHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputHealthHazards> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputHealthHazards mutation, ExperimentMutationContext context) {
        sample.setHealthHazards(mutation.healthHazards());
        return new MutationResult(formatSetterSummary("batch health hazards", mutation.healthHazards()));
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputActualMol.class)
class SetOutputActualMolHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputActualMol> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputActualMol mutation, ExperimentMutationContext context) {
        setEnteredValue(sample::setActualMol, mutation.actualMol(), mutation.unit(), experiment.getRevision());
        return new MutationResult(formatSetterSummary("batch actual mol", mutation.actualMol(), mutation.unit()));
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputActualWeight.class)
class SetOutputActualWeightHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputActualWeight> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputActualWeight mutation, ExperimentMutationContext context) {
        setEnteredValue(sample::setActualWeight, mutation.actualWeight(), mutation.unit(), experiment.getRevision());
        return new MutationResult(formatSetterSummary("batch actual weight", mutation.actualWeight(), mutation.unit()));
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.RegisterSample.class)
class RegisterSampleHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.RegisterSample> {

    @Inject
    CompoundService compoundService;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sampleRow, ReactionOutputSampleMutation.RegisterSample mutation, ExperimentMutationContext context) {
        if (sampleRow.getRegistrationStatus() != null) {
            throw new InvalidRequestException("Sample already sent for registration");
        }
        if (sampleRow.getRow().getCompound() instanceof CompoundRef.Unknown) {
            throw new InvalidRequestException("Cannot register sample for unknown compound");
        }
        SampleEntity sample = compoundService.registerSample(new SampleRegistrationRequest(sampleRow.getRow().getCompound())
                .withNbkBatchNumber(sampleRow.getNbkBatchNumber())
                .withDensity(sampleRow.getDensity())
                .withMolarity(sampleRow.getMolarity())
                .withPurity(sampleRow.getPurity().toBigDecimal())
                .withHealthHazards(sampleRow.getHealthHazards())
                .withCompoundState(sampleRow.getComponentState())
                .withBatchComment(sampleRow.getBatchComment())
        );
        if (sampleRow.getRow().getCompound() instanceof CompoundRef.Virtual) {
            sampleRow.getRow().updateCompound(compoundService.realCompoundRef(sample.getCompound()));
        }
        sampleRow.setRegistrationStatus(SampleRegistrationStatus.IN_PROGRESS); // for now, registration is immediate; when switched to async registration, REGISTERED will be set later
        sampleRow.setRegistrationStatus(SampleRegistrationStatus.REGISTERED);
        sampleRow.setStrCode(sample.getStrCode());
        sampleRow.setSampleId(sample.getId());
        return new MutationResult("Register sample");
    }
}

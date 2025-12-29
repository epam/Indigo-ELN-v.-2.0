package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.model.SampleRegistrationRequest;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.reaction.service.mutation.ReactionOutputSampleMutationHandler;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.userLastEntered;

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputHealthHazards.class)
class SetOutputHealthHazardsHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputHealthHazards> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputHealthHazards mutation, MutationContext context) {
        sample.setHealthHazards(mutation.healthHazards());
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputActualMol.class)
class SetOutputActualMolHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputActualMol> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputActualMol mutation, MutationContext context) {
        sample.setActualMol(userLastEntered(mutation.actualMol(), mutation.unit()));
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputActualWeight.class)
class SetOutputActualWeightHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputActualWeight> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputActualWeight mutation, MutationContext context) {
        sample.setActualWeight(userLastEntered(mutation.actualWeight(), mutation.unit()));
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.RegisterSample.class)
class RegisterSampleHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.RegisterSample> {

    @Inject
    CompoundService compoundService;

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sampleRow, ReactionOutputSampleMutation.RegisterSample mutation, MutationContext context) {
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
    }
}

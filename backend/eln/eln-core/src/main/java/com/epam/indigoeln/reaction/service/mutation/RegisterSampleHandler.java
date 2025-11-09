package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.model.SampleRegistrationRequest;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.reaction.model.CompoundRef;
import com.epam.indigoeln.reaction.model.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.SampleRegistrationStatus;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

@Dependent
public class RegisterSampleHandler extends AbstractMutationHandler {

    @Inject
    CompoundService compoundService;

    public void handle(ReactionOutputSample sampleRow, ReactionOutputSampleMutation.RegisterSample mutation) {
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
                .withChemicalName(sampleRow.getRow().getChemicalName())
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

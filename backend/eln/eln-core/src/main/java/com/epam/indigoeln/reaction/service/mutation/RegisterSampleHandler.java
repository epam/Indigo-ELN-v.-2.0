package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.model.SampleRegistrationRequest;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolWeightUnit;
import com.epam.indigoeln.reaction.service.ExperimentModelHelperService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.DEFAULT_ONE;

@ApplicationScoped
public class RegisterSampleHandler extends AbstractMutationHandler {

    @Inject
    CompoundService compoundService;

    public void handle(ExperimentModel model, ReactionOutputSampleMutation.RegisterSample mutation) {
        ReactionOutputSample sampleRow = model.locate(mutation);
        if (sampleRow.getRegistrationStatus() != null) {
            throw new InvalidRequestException("Sample already sent for registration");
        }
        if (sampleRow.getRow().getCompound() instanceof CompoundRef.Unknown) {
            throw new InvalidRequestException("Cannot register sample for unknown compound");
        }
        sampleRow.setRegistrationStatus(SampleRegistrationStatus.IN_PROGRESS);
        SampleEntity sample = compoundService.registerSample(new SampleRegistrationRequest(sampleRow.getRow().getCompound()));
        sampleRow.setRegistrationStatus(SampleRegistrationStatus.REGISTERED);
        sampleRow.setStrCode(sample.getStrCode());
        sampleRow.setSampleId(sample.getId());
        if (sampleRow.getRow().getCompound() instanceof CompoundRef.Virtual) {
            sampleRow.getRow().setCompound(realCompoundRef(sample.getCompound()));
        }
    }
}

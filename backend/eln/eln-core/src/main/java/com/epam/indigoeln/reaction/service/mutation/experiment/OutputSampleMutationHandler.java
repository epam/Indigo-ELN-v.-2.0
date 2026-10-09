package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.model.HealthHazardRef;
import com.epam.indigoeln.eln.model.SaltCodeRef;
import com.epam.indigoeln.eln.model.SampleSource;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.eln.util.ModelUtil;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.SampleRegistrationStatus;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.sampleregistration.api.SampleRegistrationClient;
import com.epam.indigoeln.sampleregistration.model.STRCodeCompound;
import com.epam.indigoeln.sampleregistration.model.SampleRegistrationRequest;
import com.epam.indigoeln.sampleregistration.model.SampleRegistrationResponse;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import one.util.streamex.StreamEx;
import org.eclipse.microprofile.rest.client.inject.RestClient;


@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputHealthHazards.class)
class SetOutputHealthHazardsHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputHealthHazards> {

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputHealthHazards mutation, ExperimentMutationContext context) {
        sample.setHealthHazards(mutation.healthHazards());
        return formatSetterSummary("batch health hazards", mutation.healthHazards());
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputActualMol.class)
class SetOutputActualMolHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputActualMol> {

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputActualMol mutation, ExperimentMutationContext context) {
        setEnteredValue(sample.getActualMol(), sample::setActualMol, mutation.actualMol(), mutation.unit(), experiment.getRevision());
        return formatSetterSummary("batch actual mol", mutation.actualMol(), mutation.unit());
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputActualWeight.class)
class SetOutputActualWeightHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputActualWeight> {

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputActualWeight mutation, ExperimentMutationContext context) {
        setEnteredValue(sample.getActualWeight(), sample::setActualWeight, mutation.actualWeight(), mutation.unit(), experiment.getRevision());
        return formatSetterSummary("batch actual weight", mutation.actualWeight(), mutation.unit());
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.RegisterSample.class)
class RegisterSampleHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.RegisterSample> {

    @Inject
    @RestClient
    SampleRegistrationClient sampleRegistrationClient;
    @Inject
    CompoundService compoundService;
    @Inject
    DictionaryService dictionaryService;

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sampleRow, ReactionOutputSampleMutation.RegisterSample mutation, ExperimentMutationContext context) {
        if (sampleRow.getRegistrationStatus() != null) {
            throw new InvalidRequestException("Sample already sent for registration");
        }
        if (sampleRow.getRow().getCompound().getCompoundID() == null) {
            throw new InvalidRequestException("Cannot register sample for unknown compound");
        }
        CompoundEntity compound = compoundService.getCompound(sampleRow.getRow().getCompound().getCompoundID());
        SaltCodeRef saltCode = dictionaryService.get(compound.getSaltCode());
        SampleRegistrationRequest.SampleRegistrationRequestBuilder request = ModelUtil.buildSampleRegistrationRequest(compound, saltCode)
                .nbkBatchNumber(sampleRow.getNbkBatchNumber())
                .purity(sampleRow.getPurity().isEmpty() ? null : sampleRow.getPurity().toBigDecimal())
                .compoundState(sampleRow.getComponentState() != null ? sampleRow.getComponentState().getId() : null)
                .healthHazards(StreamEx.of(sampleRow.getHealthHazards()).map(HealthHazardRef::getId).toSet())
                .batchComment(sampleRow.getBatchComment());
        if (!sampleRow.getDensity().isEmpty()) {
            request.density(sampleRow.getDensity().toBigDecimal());
        }
        if (!sampleRow.getMolarity().isEmpty()) {
            request.molarity(sampleRow.getMolarity().toBigDecimal());
            request.molarityUnit(sampleRow.getMolarity().getUnit());
        }
        SampleRegistrationResponse response = sampleRegistrationClient.registerSample(request.build());

        if (compound.getSource() == SampleSource.VIRTUAL) {
            compound.setSource(SampleSource.SRS);
            compound.setCompoundKey(new STRCodeCompound(response.strCode().getCompoundCode(), response.strCode().getSaltCode()).toString());
            row.updateCompound(compoundService.compoundRef(compound)); // before registration status is set: updateCompound refuses after that
        }
        sampleRow.setRegistrationStatus(SampleRegistrationStatus.IN_PROGRESS); // for now, registration is immediate; when switched to async registration, REGISTERED will be set later
        sampleRow.setRegistrationStatus(SampleRegistrationStatus.REGISTERED);
        sampleRow.setSampleSource(SampleSource.SRS);
        sampleRow.setSampleKey(response.strCode().toString());
        return "Register sample";
    }
}

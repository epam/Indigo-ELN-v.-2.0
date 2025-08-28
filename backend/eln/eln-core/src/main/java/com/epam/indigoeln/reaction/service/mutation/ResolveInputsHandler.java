package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import com.epam.indigoeln.reaction.service.ExperimentModelHelperService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.*;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.DEFAULT_ONE;

@ApplicationScoped
public class ResolveInputsHandler extends AbstractMutationHandler {

    @Inject
    CompoundService compoundService;
    @Inject
    ExperimentModelHelperService modelHelperService;

    public void handle(ExperimentEntity experiment, ExperimentModel model, ReactionMutation.ResolveInputs mutation) {
        Reaction reaction = model.locate(mutation);
        Set<ReactionInputRole> affectedRoles = EnumSet.noneOf(ReactionInputRole.class);
        mutation.inputSamples().forEach((inputAnchor, sampleId) -> {
            ReactionInput row = model.locateReactionInput(inputAnchor);
            SampleEntity sample = compoundService.getSample(sampleId);
            row.setCompound(compoundService.realCompoundRef(sample.getCompound()));

            ReactionInputSample reactionInputSample = new ReactionInputSample(row, UUID.randomUUID());
            reactionInputSample.setSampleId(sampleId);
            reactionInputSample.setPurity(DEFAULT_ONE);
            reactionInputSample.setStrCode(sample.getStrCode());
            row.setSamples(List.of(reactionInputSample));

            affectedRoles.add(row.getRole());
        });
        modelHelperService.rebuildReactionScheme(experiment, reaction, affectedRoles);
    }
}

package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionInputSample;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.DEFAULT_ONE;

@ApplicationScoped
public class ResolveInputsHandler extends AbstractMutationHandler {

    @Inject
    CompoundService compoundService;

    public void handle(ExperimentModel model, ReactionMutation.ResolveInputs mutation) {
        mutation.inputSamples().forEach((inputNo, sampleId) -> {
            ReactionInput row = model.getReactions().get(mutation.reactionNo()).getInputs().get(inputNo);
            SampleEntity sample = compoundService.getSample(sampleId);
            row.setCompound(realCompoundRef(sample.getCompound()));
            ReactionInputSample reactionInputSample = new ReactionInputSample();
            reactionInputSample.setRow(row);
            reactionInputSample.setSampleId(sampleId);
            reactionInputSample.setPurity(DEFAULT_ONE);
            row.setSamples(List.of(reactionInputSample));
        });
    }
}

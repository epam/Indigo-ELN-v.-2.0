package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.mapper.SampleMapper;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.mapper.DictionaryMapper;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import com.epam.indigoeln.reaction.model.units.DensityUnit;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.NoUnit;
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
    DictionaryMapper dictionaryMapper;
    @Inject
    ExperimentModelHelperService modelHelperService;

    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionMutation.ResolveInputs mutation) {
        Set<ReactionRole> affectedRoles = EnumSet.noneOf(ReactionRole.class);
        mutation.inputSamples().forEach((inputAnchor, sampleId) -> {
            ReactionInput row = model.locate(inputAnchor);
            SampleEntity sample = compoundService.getSample(sampleId);
            row.setCompound(compoundService.realCompoundRef(sample.getCompound()));

            ReactionInputSample reactionInputSample = ReactionInputSample.create(row);
            reactionInputSample.setSampleId(sampleId);
            reactionInputSample.setStrCode(sample.getStrCode());
            reactionInputSample.setDensity(EnteredValue.defaultValue(sample.getDensity(), DensityUnit.G_ML));
            reactionInputSample.setMolarity(EnteredValue.defaultValue(sample.getMolarity(), sample.getMolarityUnit()));
            reactionInputSample.setPurity(sample.getPurity() != null ? EnteredValue.defaultValue(sample.getPurity(), NoUnit.NO_UNIT) : DEFAULT_ONE);
            reactionInputSample.setHealthHazards(dictionaryMapper.itemToRefList(sample.getHealthHazards()));
            row.setSamples(List.of(reactionInputSample));

            affectedRoles.add(row.getRole());
        });
        modelHelperService.rebuildReactionScheme(experiment, reaction, affectedRoles);
    }
}

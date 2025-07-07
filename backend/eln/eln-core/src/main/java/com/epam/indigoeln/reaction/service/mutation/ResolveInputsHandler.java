package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.util.IndigoUtil;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.indigowrapper.IndigoReaction;
import com.epam.indigoeln.indigowrapper.IndigoWrapper;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import static com.epam.indigoeln.eln.util.IndigoUtil.rebuildReactionScheme;
import static com.epam.indigoeln.reaction.model.units.EnteredValue.DEFAULT_ONE;

@ApplicationScoped
public class ResolveInputsHandler extends AbstractMutationHandler {

    @Inject
    CompoundService compoundService;
    @Inject
    IndigoWrapper indigoWrapper;

    public void handle(ExperimentEntity experiment, ExperimentModel model, ReactionMutation.ResolveInputs mutation) {
        indigoWrapper.withSession(indigo -> {
            mutation.inputSamples().forEach((inputNo, sampleId) -> {
                ReactionInput row = model.getReactions().get(mutation.reactionNo()).getInputs().get(inputNo);
                SampleEntity sample = compoundService.getSample(sampleId);
                row.setCompound(realCompoundRef(sample.getCompound()));

                ReactionInputSample reactionInputSample = new ReactionInputSample();
                reactionInputSample.setRow(row);
                reactionInputSample.setSampleId(sampleId);
                reactionInputSample.setPurity(DEFAULT_ONE);
                row.setSamples(List.of(reactionInputSample));

                IndigoReaction reactionScheme = indigo.loadReaction(row.getReaction().getMolFile());

                List<IndigoMolecule> molecules = new ArrayList<>();
                for (ReactionInput input : row.getReaction().getInputs()) {
                    String molfile = switch (input.getCompound()) {
                        case CompoundRef.Stored stored -> compoundService.getCompound(stored.getCompoundID()).getMolFile();
                        case CompoundRef.Virtual virtual -> virtual.getMolFile();
                        case CompoundRef.Unknown unknown -> null;
                    };
                    if (molfile != null) {
                        molecules.add(indigo.loadMolecule(molfile));
                    }
                }
                rebuildReactionScheme(reactionScheme, row.getRole(), molecules);

                byte[] picture = indigo.renderToBuffer(reactionScheme);
                row.getReaction().setMolFile(reactionScheme.rxnfile());
                experiment.setPicture(picture);
            });
        });
    }
}

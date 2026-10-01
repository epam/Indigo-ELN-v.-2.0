package com.epam.indigoeln.reaction.service.mutation.experiment.listener;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.eln.indigowrapper.IndigoReaction;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionAnchor;
import com.epam.indigoeln.reaction.service.ExperimentModelHelperService;
import com.epam.indigoeln.reaction.service.mutation.ExperimentMutationListener;
import com.epam.indigoeln.reaction.service.mutation.experiment.ExperimentMutationContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Dependent
@Priority(ExperimentMutationListener.DEFAULT_PRIORITY)
public class UpdateExperimentRxnfilesListener implements ExperimentMutationListener {

    @Inject
    ExperimentModelHelperService experimentModelHelperService;

    @Inject
    IndigoAPI indigoAPI;

    private final Map<ReactionAnchor, @Nullable String> oldRxnfiles = new HashMap<>();
    private final Map<ReactionAnchor, List<Object>> oldReactionKeys = new HashMap<>();

    @Override
    public void beforeHandle(ExperimentEntity experiment, ExperimentMutationContext context) {
        for (Reaction reaction : experiment.getModel().getReactions()) {
            oldRxnfiles.put(reaction.getAnchor(), reaction.getRxnfile());
            oldReactionKeys.put(reaction.getAnchor(), experimentModelHelperService.makeReactionKey(reaction));
        }
    }

    @Override
    public void afterRecalculate(ExperimentEntity experiment, ExperimentMutationContext context) {
        for (Reaction reaction : experiment.getModel().getReactions()) {
            IndigoReaction indigoReaction;
            String oldRxnfile = oldRxnfiles.get(reaction.getAnchor());
            List<Object> oldReactionKey = oldReactionKeys.get(reaction.getAnchor());
            if (!Objects.equals(oldRxnfile, reaction.getRxnfile())) {
                // rxnfile was modified
                indigoReaction = reaction.getRxnfile() == null ? indigoAPI.createReaction() : indigoAPI.loadReaction(reaction.getRxnfile());
            } else if (!Objects.equals(oldReactionKey, experimentModelHelperService.makeReactionKey(reaction))) {
                // inputs/outputs was modified, update rxnfile accordingly
                indigoReaction = experimentModelHelperService.rebuildReactionRxnFile(reaction.getInputs(), reaction.getOutputs());
                reaction.setRxnfile(indigoReaction.rxnfile());
            } else {
                continue; // nothing changed
            }
            byte[] picture = experimentModelHelperService.rebuildReactionPicture(experiment, indigoReaction);
            if (reaction == experiment.getModel().getReactions().getFirst()) {
                experiment.setPicture(picture);
            }
        }
    }
}

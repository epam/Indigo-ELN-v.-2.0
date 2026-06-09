package com.epam.indigoeln.reaction.service.mutation.experiment.listener;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoReaction;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionAnchor;
import com.epam.indigoeln.reaction.service.ExperimentModelHelperService;
import com.epam.indigoeln.reaction.service.mutation.ExperimentModelMutationListener;
import com.epam.indigoeln.reaction.service.mutation.experiment.ExperimentMutationContext;
import com.epam.indigoeln.reaction.util.StreamUtil;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.epam.indigoeln.common.util.ModelUtil.updateCollection;

@Dependent
@Priority(ExperimentModelMutationListener.DEFAULT_PRIORITY)
public class UpdateExperimentRxnfilesListener implements ExperimentModelMutationListener {

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
        boolean anyRxnfileChanged = false;
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
            anyRxnfileChanged = true;
            String image = experimentModelHelperService.rebuildReactionPicture(experiment, reaction, indigoReaction);
            context.getResponse().getReactionImages().put(reaction.getAnchor(), image);
        }
        if (anyRxnfileChanged || oldRxnfiles.size() != experiment.getModel().getReactions().size()) {
            List<String> rxnFiles = StreamEx.of(experiment.getModel().getReactions())
                    .map(Reaction::getRxnfile)
                    .collect(StreamUtil.toListNotNull());
            updateCollection(experiment.getRxnfiles(), rxnFiles);
        }
    }
}

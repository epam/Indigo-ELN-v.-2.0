package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.repository.DictionaryItemRepository;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.indigowrapper.IndigoReaction;
import com.epam.indigoeln.indigowrapper.IndigoRendererAPI;
import com.epam.indigoeln.reaction.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import one.util.streamex.StreamEx;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.indigoeln.eln.util.IndigoUtil.addToReaction;
import static com.epam.indigoeln.eln.util.IndigoUtil.reactionIterable;

@ApplicationScoped
public class ExperimentModelHelperService {

    private static final ReactionRole[] COMPONENT_ORDER = {ReactionRole.OUTPUT, ReactionRole.CATALYST, ReactionRole.REACTANT};

    @Inject
    CompoundService compoundService;
    @Inject
    IndigoAPI indigo;
    @Inject
    IndigoRendererAPI indigoRenderer;
    @Inject
    DictionaryItemRepository dictionaryItemRepository;

    public void rebuildReactionPicture(ExperimentEntity experiment, Reaction reaction, IndigoReaction indigoReaction) {
        indigoRenderer.setRenderOptions("svg", 500, 200);
        byte[] buf = indigoRenderer.renderToBuffer(indigoReaction);
        experiment.setPicture(buf);
    }

    public void rebuildUsedCompounds(ExperimentEntity experiment, ExperimentModel model) {
        Set<CompoundEntity> usedCompounds = StreamEx.of(model.getReactions())
                .flatMap(r -> Stream.concat(r.getInputs().stream(), r.getOutputs().stream()))
                .map(row -> switch (row.getCompound()) {
                    case CompoundRef.Stored stored -> compoundService.getCompound(stored.getCompoundID());
                    case CompoundRef.Virtual virtual -> compoundService.getCompound(virtual.getCompoundID());
                    case CompoundRef.Unknown unknown -> null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        experiment.setCompounds(usedCompounds);
    }

    public void rebuildReactionRxnFile(ExperimentEntity experiment, Reaction reaction, Set<ReactionRole> affectedRoles, IndigoReaction indigoReaction) {
        for (ReactionRole role : COMPONENT_ORDER) {
            if (!affectedRoles.contains(role)) {
                continue;
            }

            List<IndigoMolecule> molecules = new ArrayList<>();
            // noinspection rawtypes,unchecked
            Iterable<ReactionRow> rows = role == ReactionRole.OUTPUT ? (Iterable) reaction.getOutputs() : (Iterable) reaction.inputsOfType(role);
            for (ReactionRow input : rows) {
                if (input.getCompound().getCompoundID() != null) {
                    CompoundEntity compound = compoundService.getCompound(input.getCompound().getCompoundID());
                    molecules.add(indigo.loadMolecule(compound.getMolFile()));
                }
            }

            reactionIterable(indigoReaction, role).forEach(IndigoMolecule::remove);
            // TODO sometimes it adds in reverse order, sometimes not
            molecules.reversed().forEach(molecule -> addToReaction(indigoReaction, role, molecule));
        }
        reaction.setRxnfile(indigoReaction.rxnfile());
    }

    // !!! replace with walk methods
    public static void visitModel(ExperimentModel model, Consumer<ExperimentModelNode> visitor) {
        visitor.accept(model);
        for (Reaction reaction : model.getReactions()) {
            visitor.accept(reaction);
            for (ReactionInput input : reaction.getInputs()) {
                visitor.accept(input);
                for (ReactionInputSample sample : input.getSamples()) {
                    visitor.accept(sample);
                }
            }
            for (ReactionOutput output : reaction.getOutputs()) {
                visitor.accept(output);
                for (ReactionOutputSample sample : output.getSamples()) {
                    visitor.accept(sample);
                }
            }
        }
    }
}

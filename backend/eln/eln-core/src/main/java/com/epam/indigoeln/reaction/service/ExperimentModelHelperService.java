package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.indigowrapper.IndigoReaction;
import com.epam.indigoeln.indigowrapper.IndigoRendererAPI;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.util.ExperimentModelUtil2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@ApplicationScoped
public class ExperimentModelHelperService {

    private static final ReactionRole[] SCHEMA_ORDER = new ReactionRole[]{ReactionRole.REACTANT, ReactionRole.CATALYST, ReactionRole.OUTPUT};

    @Inject
    CompoundService compoundService;
    @Inject
    IndigoAPI indigo;
    @Inject
    IndigoRendererAPI indigoRenderer;

    public String rebuildReactionPicture(ExperimentEntity experiment, Reaction reaction, IndigoReaction indigoReaction) {
        indigoRenderer.setRenderOptions("svg", 500, 200);
        byte[] buf = indigoRenderer.renderToBuffer(indigoReaction);
        experiment.setPicture(buf);
        return new String(buf);
    }

    public List<Object> makeReactionKey(Reaction reaction) {
        List<Object> key = new ArrayList<>();
        for (ReactionRow row : Iterables.concat(reaction.getInputs(), reaction.getOutputs())) {
            ReactionRole role = ExperimentModelUtil2.getRoleInSchema(row);
            if (role != null) {
                key.add(role);
                key.add(checkNotNull(row.getCompound().getCompoundID()));
            }
        }
        return key;
    }

    public Multimap<ReactionRole, CompoundRef.StoredOrVirtual> makeCompoundRefs(ExperimentModel model) {
        Multimap<ReactionRole, CompoundRef.StoredOrVirtual> map = HashMultimap.create();
        for (Reaction reaction : model.getReactions()) {
            for (ReactionInput input : reaction.getInputs()) {
                if (input.getCompound() instanceof CompoundRef.StoredOrVirtual c) {
                    map.put(input.getRole(), c);
                }
            }
            for (ReactionOutput output : reaction.getOutputs()) {
                if (output.getCompound() instanceof CompoundRef.StoredOrVirtual c) {
                    map.put(ReactionRole.OUTPUT, c);
                }
            }
        }
        return map;
    }

    public IndigoReaction rebuildReactionRxnFile(List<ReactionInput> inputs, List<ReactionOutput> outputs) {
        IndigoReaction indigoReaction = indigo.createReaction();
        for (ReactionRow row : Iterables.concat(inputs, outputs)) {
            row.setRxnPosition(null);
        }
        for (ReactionRole role : SCHEMA_ORDER) {
            int rxnPosition = -1;
            for (ReactionRow row : Iterables.concat(inputs, outputs)) {
                if (ExperimentModelUtil2.getRoleInSchema(row) == role) {
                    row.setRxnPosition(++rxnPosition);
                    CompoundEntity compound = compoundService.getCompound(checkNotNull(row.getCompound().getCompoundID()));
                    IndigoMolecule molecule = indigo.loadMolecule(compound.getMolFile());
                    switch (role) {
                        case REACTANT -> indigoReaction.addReactant(molecule);
                        case CATALYST -> indigoReaction.addCatalyst(molecule);
                        case OUTPUT -> indigoReaction.addProduct(molecule);
                    }
                }
            }
        }
        return indigoReaction;
    }
}

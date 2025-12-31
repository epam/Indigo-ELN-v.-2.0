package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.eln.model.ACLEntryDTO;
import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.patch.ACLEntryPatch;
import com.epam.indigoeln.reaction.model.patch.ExperimentModelPatch;
import com.epam.indigoeln.reaction.model.patch.ReactionPatch;
import com.epam.indigoeln.reaction.model.patch.handler2.ListDiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.MetamodelDiffHandler;

public class ExperimentModelMetamodel {

    public static final Metamodel<ExperimentModel, ExperimentModelPatch> INSTANCE = Metamodels.createMetamodel("ACLEntry", m -> {
        m.property("lastUsedAnchor", ExperimentModel::getLastUsedAnchor, ExperimentModel::setLastUsedAnchor, ExperimentModelPatch::getLastUsedAnchor, ExperimentModelPatch::setLastUsedAnchor);
        m.property("reactions", ExperimentModel::getReactions, ExperimentModel::setReactions, ExperimentModelPatch::getReactions, ExperimentModelPatch::setReactions, new ListDiffHandler<>(Reaction::getAnchor, new MetamodelDiffHandler<>(ReactionMetamodel.INSTANCE, ReactionPatch::new)));
    });
}

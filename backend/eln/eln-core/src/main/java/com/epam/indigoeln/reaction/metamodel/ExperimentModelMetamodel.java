package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.patch.ExperimentModelPatch;
import com.epam.indigoeln.reaction.model.patch.ReactionPatch;
import com.epam.indigoeln.reaction.model.patch.handler2.ListDiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.MetamodelDiffHandler;

public class ExperimentModelMetamodel {

    public static final Metamodel<ExperimentModel, ExperimentModelPatch> INSTANCE = Metamodels.createMetamodel("ACLEntry", m -> {
        m.property("schemaVersion", ExperimentModel::getSchemaVersion, ExperimentModel::setSchemaVersion, ExperimentModelPatch::getSchemaVersion, ExperimentModelPatch::setSchemaVersion);
        m.property("significantFigures", ExperimentModel::getSignificantFigures, ExperimentModel::setSignificantFigures, ExperimentModelPatch::getSignificantFigures, ExperimentModelPatch::setSignificantFigures);
        m.listProperty("reactions", ExperimentModel::getReactions, ExperimentModel::setReactions, ExperimentModelPatch::getReactions, ExperimentModelPatch::setReactions, new ListDiffHandler<>(Reaction::getAnchor, new MetamodelDiffHandler<>(ReactionMetamodel.INSTANCE, ReactionPatch::new)));
    });
}

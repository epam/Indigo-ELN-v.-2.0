package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.patch.ExperimentModelPatch;
import com.epam.indigoeln.reaction.model.patch.ListPatch;
import com.epam.indigoeln.reaction.model.patch.ReactionPatch;
import com.epam.indigoeln.reaction.model.patch.handler2.ListDiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.MetamodelDiffHandler;

import java.util.List;

import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.listProperty;
import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.property;

public class ExperimentModelMetamodel {

    public static final ModelProperty<ExperimentModel, Integer, ExperimentModelPatch, Integer> SCHEMA_VERSION = property("schemaVersion",ExperimentModel::getSchemaVersion, ExperimentModel::setSchemaVersion, ExperimentModelPatch::getSchemaVersion, ExperimentModelPatch::setSchemaVersion);
    public static final ModelProperty<ExperimentModel, Integer, ExperimentModelPatch, Integer> SIGNIFICANT_FIGURES = property("significantFigures", ExperimentModel::getSignificantFigures, ExperimentModel::setSignificantFigures, ExperimentModelPatch::getSignificantFigures, ExperimentModelPatch::setSignificantFigures);
    public static final ModelProperty<ExperimentModel, List<Reaction>, ExperimentModelPatch, ListPatch<Reaction, ReactionPatch>> REACTIONS = listProperty("reactions", ExperimentModel::getReactions, ExperimentModel::setReactions, ExperimentModelPatch::getReactions, ExperimentModelPatch::setReactions, new ListDiffHandler<>(Reaction::getAnchor, new MetamodelDiffHandler<>(ReactionMetamodel.INSTANCE, ReactionPatch::new)));

    public static final Metamodel<ExperimentModel, ExperimentModelPatch> INSTANCE = new Metamodel<>("ACLEntry", List.of(
            SCHEMA_VERSION,
            SIGNIFICANT_FIGURES,
            REACTIONS
    ));
}

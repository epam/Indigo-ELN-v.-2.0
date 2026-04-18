package com.epam.indigoeln.reaction.metamodel;

import com.epam.indigoeln.reaction.metamodel.property.Metamodel;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;

import java.util.List;

import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.listProperty;
import static com.epam.indigoeln.reaction.metamodel.property.ModelProperty.property;

public class ExperimentModelMetamodel {

    public static final ModelProperty<ExperimentModel, Integer> SIGNIFICANT_FIGURES = property("significantFigures", ExperimentModel::getSignificantFigures, ExperimentModel::setSignificantFigures);
    public static final ModelProperty<ExperimentModel, List<Reaction>> REACTIONS = listProperty("reactions", ExperimentModel::getReactions, ExperimentModel::setReactions, ReactionMetamodel.INSTANCE);

    public static final Metamodel<ExperimentModel> INSTANCE = new Metamodel<>("ACLEntry", List.of(
            SIGNIFICANT_FIGURES,
            REACTIONS
    ));
}

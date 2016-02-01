package com.epam.indigoeln.core.service.calculation.helper;

import java.util.ArrayList;
import java.util.List;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.epam.indigoeln.web.rest.dto.calculation.ReactionPropertiesDTO;

/**
 * Helper class for Reaction structure calculations
 */
public final class ReactionCalcHelper {

    private ReactionCalcHelper() {
    }

    public static ReactionPropertiesDTO extractReactionComponents(String reaction) {

        IndigoObject handle = new Indigo().loadReaction(reaction);

        //fetch reactants
        List<String> reactants = new ArrayList<>();
        for(IndigoObject reactant : handle.iterateReactants()) {
            reactants.add(reactant.molfile());
        }

        //fetch components
        List<String> products = new ArrayList<>();
        for(IndigoObject product : handle.iterateProducts()) {
            reactants.add(product.molfile());
        }

        return new ReactionPropertiesDTO(reaction, reactants, products);
    }

    public static ReactionPropertiesDTO combineReactionComponents(ReactionPropertiesDTO reactionProperties) {
        Indigo indigo = new Indigo();
        IndigoObject handle = indigo.createReaction();

        //add reactants to the structure
        for (String reactant : reactionProperties.getReactants()) {
            handle.addReactant(indigo.loadMolecule(reactant));
        }

        //add products to the structure
        for (String product : reactionProperties.getReactants()) {
            handle.addProduct(indigo.loadMolecule(product));
        }

        reactionProperties.setStructure(handle.rxnfile());
        return reactionProperties;
    }

    public static boolean isValidReaction(String reaction) {
        IndigoObject handle = new Indigo().loadQueryReaction(reaction);
        return (handle.countReactants() > 0) && (handle.countProducts() > 0);
    }
}

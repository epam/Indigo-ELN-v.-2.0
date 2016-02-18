package com.epam.indigoeln.core.service.calculation;

import java.util.List;
import java.util.Map;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.epam.indigo.IndigoRenderer;
import com.epam.indigoeln.core.service.calculation.helper.RendererResult;
import org.springframework.stereotype.Service;

import com.epam.indigoeln.web.rest.dto.calculation.ReactionPropertiesDTO;
import com.epam.indigoeln.core.service.calculation.helper.MoleculeCalcHelper;
import com.epam.indigoeln.core.service.calculation.helper.ReactionCalcHelper;
import com.epam.indigoeln.core.service.calculation.helper.CommonCalcHelper;

/**
 * Service for calculations under reaction or molecular structures defined in special text format
 */
@Service
public class CalculationService {

    private static boolean STEREOCHEM_ERRORS = true;
    private static String MOLECULE_TYPE = "molecule";
    private static String REACTION_TYPE = "reaction";

    /**
     * Check, that chemistry structures of reactions or molecules are equals
     *
     * @param chemistryItems list of chemistry structures
     * @param isReaction are chemistry items reactions or molecules
     * @return true if all chemistry items equals
     */
    public boolean chemistryEquals(List<String> chemistryItems, boolean isReaction) {
        return CommonCalcHelper.chemistryEquals(chemistryItems, isReaction);
    }

    /**
     * Return calculated information for molecular structure
     * (name, formula, molecular weight, exact molecular weight, is chiral)
     *
     * @param molecule structure of molecule
     * @return map of calculated attributes
     */
    public Map<String, String> getMolecularInformation(String molecule) {
        return MoleculeCalcHelper.getMolecularInformation(molecule);
    }

    /**
     * Check, that molecule is empty (does not contains any atoms)
     *
     * @param molecule structure of molecule
     * @return true if molecule empty
     */
    public boolean isMoleculeEmpty(String molecule) {
        return MoleculeCalcHelper.isMoleculeEmpty(molecule);
    }

    /**
     * Check, that molecule is chiral
     *
     * @param molecule structure of molecule
     * @return true if molecule is chiral
     */
    public boolean isMoleculeChiral(String molecule) {
        return MoleculeCalcHelper.isMoleculeChiral(molecule);
    }

    /**
     * Extract components (products and reactants) of given reaction
     * @param reaction reaction structure
     * @return reaction components
     */
    public ReactionPropertiesDTO extractReactionComponents(String reaction) {
        return ReactionCalcHelper.extractReactionComponents(reaction);
    }

    /**
     * Combine reaction components (products and reactants) with existing reaction structure
     * Reaction structure received as string field of DTO and will be enriched by reactants and products received in
     * DTO list fields
     * @param reactionDTO reaction DTO
     * @return reaction DTO enriched by reactants and products
     */
    public ReactionPropertiesDTO combineReactionComponents(ReactionPropertiesDTO reactionDTO) {
        return ReactionCalcHelper.combineReactionComponents(reactionDTO);
    }

    /**
     * Method to determine if chemistry contains a valid reaction (any products and reactants present)
     *
     * @param reaction structure of reaction
     * @return is reaction valid
     */
    public boolean isValidReaction(String reaction) {
        return ReactionCalcHelper.isValidReaction(reaction);
    }

    /**
     * Render molecule/reaction by its string representation
     * @param structure string structure representation (Mol, Smiles etc.)
     * @param structureType molecule or reaction
     * @param width width of final image
     * @param height height of fnal image
     * @return RendererResult
     */
    public RendererResult getStructureWithImage(String structure, String structureType, Integer width, Integer height) {

        Indigo indigo = new Indigo();
        indigo.setOption("ignore-stereochemistry-errors", STEREOCHEM_ERRORS);

        IndigoRenderer renderer = CommonCalcHelper.getRenderer(indigo, width, height);
        IndigoObject io =  MOLECULE_TYPE.equals(structureType) ? indigo.loadMolecule(structure) :
                indigo.loadReaction(structure);

        // auto-generate coordinates as Bingo DB doesn't store them
        io.layout();

        return new RendererResult(renderer.renderToBuffer(io));
    }


}

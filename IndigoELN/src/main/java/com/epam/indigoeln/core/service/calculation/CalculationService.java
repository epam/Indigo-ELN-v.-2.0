package com.epam.indigoeln.core.service.calculation;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.epam.indigoeln.config.bingo.IndigoProvider;
import com.epam.indigoeln.core.service.calculation.helper.RendererResult;
import com.epam.indigoeln.core.service.codetable.CodeTableService;
import com.epam.indigoeln.web.rest.dto.calculation.ReactionPropertiesDTO;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for calculations under reaction or molecular structures defined in special text format
 */
@Service
public class CalculationService {

    private static final String SALT_CODE    = "SALT_CODE";
    private static final String SALT_DESC    = "SALT_DESC";
    private static final String SALT_WEIGHT  = "SALT_WEIGHT";
    private static final String SALT_FORMULA = "SALT_FORMULA";

    private static final Map<String, String> SALT_METADATA_DEFAULT = ImmutableMap.of(
            SALT_CODE, "0",
            SALT_DESC, "Parent Structure",
            SALT_WEIGHT, "0",
            SALT_FORMULA, "");

    @Autowired
    private IndigoProvider indigoProvider;

    @Autowired
    private CodeTableService codeTableService;

    public boolean isMolecule(String s) {
        try {
            indigoProvider.indigo().loadMolecule(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isReaction(String s) {
        try {
            indigoProvider.indigo().loadReaction(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Optional<Boolean> isEmptyMolecule(String molecule) {
        try {
            return Optional.of(indigoProvider.indigo().loadMolecule(molecule).countComponents() == 0);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<Boolean> isEmptyReaction(String reaction) {
        try {
            indigoProvider.indigo().loadReaction(reaction);
        } catch (Exception e) {
            try {
                if (indigoProvider.indigo().loadMolecule(reaction).countComponents() == 0) {
                    return Optional.of(true);
                }
            } catch (Exception e1) {
                return Optional.empty();
            }
        }
        return Optional.of(false);
    }

    /**
     * Check, that chemistry structures of reactions or molecules are equals
     *
     * @param chemistryItems list of chemistry structures
     * @param isReaction are chemistry items reactions or molecules
     * @return true if all chemistry items equals
     */
    public boolean chemistryEquals(List<String> chemistryItems, boolean isReaction) {
        Indigo indigo = indigoProvider.indigo();
        IndigoObject prevHandle = null;
        for(String chemistry : chemistryItems) {
            IndigoObject handle = isReaction(chemistry) ? indigo.loadReaction(chemistry) : indigo.loadMolecule(chemistry);
            if (prevHandle != null && indigo.exactMatch(handle, prevHandle) == null) {
                return false;
            }
            prevHandle = handle;
        }
        return true;
    }

    /**
     * Return calculated information for molecular structure
     * (name, formula, molecular weight, exact molecular weight, is chiral)
     *
     * @param molecule structure of molecule
     * @return map of calculated attributes
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getMolecularInformation(String molecule, Optional<String> saltCodeOpt, Optional<Float> saltEqOpt) {
        Map<String, String> result = new HashMap<>();

        IndigoObject handle = indigoProvider.indigo().loadMolecule(molecule);

        Map<String, String> saltMetadata = getSaltMetadata(saltCodeOpt).orElse(SALT_METADATA_DEFAULT);
        float saltEq = saltEqOpt.orElse(1.0f);
        float molecularWeightOriginal = handle.molecularWeight();
        float saltWeight =  Optional.ofNullable(saltMetadata.get(SALT_WEIGHT)).map(Float::valueOf).orElse(0.0f);
        float molecularWeightCalculated = molecularWeightOriginal + saltEq * saltWeight;

        result.put("name", handle.name());
        result.put("molecule", molecule);
        result.put("molecularFormula", handle.grossFormula());
        result.put("molecularWeightOriginal", String.valueOf(molecularWeightOriginal));
        result.put("exactMolecularWeight", String.valueOf(handle.monoisotopicMass()));
        result.put("isChiral", String.valueOf(handle.isChiral()));
        result.put("molecularWeight", String.valueOf(molecularWeightCalculated));
        result.put("saltCode", saltMetadata.get(SALT_CODE));
        result.put("saltDesc", saltMetadata.get(SALT_DESC));
        result.put("saltFormula", saltMetadata.get(SALT_FORMULA));
        result.put("saltWeight", String.valueOf(saltWeight));
        result.put("saltEQ", String.valueOf(saltEq));

        return result;
    }

    /**
     * Check, that molecule is empty (does not contains any atoms)
     *
     * @param molecule structure of molecule
     * @return true if molecule empty
     */
    public boolean isMoleculeEmpty(String molecule) {
        return indigoProvider.indigo().loadMolecule(molecule).countAtoms() == 0;
    }

    /**
     * Check, that molecule is chiral
     *
     * @param molecule structure of molecule
     * @return true if molecule is chiral
     */
    public boolean isMoleculeChiral(String molecule) {
        return indigoProvider.indigo().loadMolecule(molecule).isChiral();
    }

    /**
     * Extract components (products and reactants) of given reaction
     * @param reaction reaction structure
     * @return reaction components
     */
    public ReactionPropertiesDTO extractReactionComponents(String reaction) {
        List<String> reactants = new ArrayList<>();
        List<String> products = new ArrayList<>();

        if (isMolecule(reaction)) {
            reactants.add(indigoProvider.indigo().loadMolecule(reaction).molfile());
        } else {
            IndigoObject handle = indigoProvider.indigo().loadReaction(reaction);

            for (IndigoObject reactant : handle.iterateReactants()) {
                reactants.add(reactant.molfile());
            }

            for (IndigoObject product : handle.iterateProducts()) {
                products.add(product.molfile());
            }
        }

        return new ReactionPropertiesDTO(reaction, reactants, products);
    }

    /**
     * Combine reaction components (products and reactants) with existing reaction structure
     * Reaction structure received as string field of DTO and will be enriched by reactants and products received in
     * DTO list fields
     * @param reactionDTO reaction DTO
     * @return reaction DTO enriched by reactants and products
     */
    public ReactionPropertiesDTO combineReactionComponents(ReactionPropertiesDTO reactionDTO) {
        Indigo indigo = indigoProvider.indigo();
        IndigoObject handle = indigo.createReaction();

        //add reactants to the structure
        for (String reactant : reactionDTO.getReactants()) {
            handle.addReactant(indigo.loadMolecule(reactant));
        }

        //add products to the structure
        for (String product : reactionDTO.getProducts()) {
            handle.addProduct(indigo.loadMolecule(product));
        }

        reactionDTO.setStructure(handle.rxnfile());
        return reactionDTO;
    }

    /**
     * Method to determine if chemistry contains a valid reaction (any products and reactants present)
     *
     * @param reaction structure of reaction
     * @return is reaction valid
     */
    public boolean isValidReaction(String reaction) {
        IndigoObject handle = indigoProvider.indigo().loadQueryReaction(reaction);
        return (handle.countReactants() > 0) && (handle.countProducts() > 0);
    }

    /**
     * Render molecule/reaction by its string representation
     * @param structure string structure representation (Mol, Smiles etc.)
     * @param structureType molecule or reaction
     * @return RendererResult
     */
    public RendererResult getStructureWithImage(String structure, String structureType) {
        Indigo indigo = indigoProvider.indigo();
        IndigoObject io = isMolecule(structure) ? indigo.loadMolecule(structure) : indigo.loadReaction(structure);
        return new RendererResult(indigoProvider.renderer(indigo).renderToBuffer(io));
    }

    private Optional<Map> getSaltMetadata(Optional<String> saltCode) {
        if(!saltCode.isPresent()) {
            return Optional.empty();
        }

        List<Map> codeTable = codeTableService.getCodeTable(CodeTableService.TABLE_SALT_CODE);
        return codeTable.stream().filter(tableRow -> saltCode.get().equals(tableRow.get(SALT_CODE))).findAny();
    }

}
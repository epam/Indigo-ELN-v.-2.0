package com.epam.indigoeln.core.service.calculation;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.epam.indigo.IndigoRenderer;
import com.epam.indigoeln.core.service.calculation.helper.RendererResult;
import com.epam.indigoeln.web.rest.dto.calculation.ReactionPropertiesDTO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for calculations under reaction or molecular structures defined in special text format
 */
@Service
public class CalculationService {

    private static final SaltMetadata SALT_METADATA_DEFAULT = new SaltMetadata("0", "Parent Structure", 0.0f, null);

    @Autowired
    private Indigo indigo;

    @Autowired
    private IndigoRenderer indigoRenderer;

    private static final Map<String, SaltMetadata> SALT_METADATA_MAP =  new HashMap<String, SaltMetadata>() {
        private static final long serialVersionUID = -2938945838635105351L;
        {
            put("0", SALT_METADATA_DEFAULT);
            put("1", new SaltMetadata("1", "HYDROCHLORIDE",    36.461f,   "HCl"));
            put("2", new SaltMetadata("2", "SODIUM",           22.9898f,  "Na"));
            put("3", new SaltMetadata("3", "HYDRATE",          18.02f,    "H2O"));
            put("4", new SaltMetadata("4", "HYDROBROMIDE",     80.912f,   "HBr"));
            put("5", new SaltMetadata("5", "HYDROIODIDE",      127.9124f, "Hl"));
        }
    };

    public static class SaltMetadata {
        public final String saltCode;
        public final String saltDesc;
        public final float  saltWeight;
        public final String saltFormula;

        public SaltMetadata(String saltCode, String saltDesc, float saltWeight, String saltFormula) {
            this.saltCode = saltCode;
            this.saltDesc = saltDesc;
            this.saltWeight = saltWeight;
            this.saltFormula = saltFormula;
        }
    }

    /**
     * Check, that chemistry structures of reactions or molecules are equals
     *
     * @param chemistryItems list of chemistry structures
     * @param isReaction are chemistry items reactions or molecules
     * @return true if all chemistry items equals
     */
    public boolean chemistryEquals(List<String> chemistryItems, boolean isReaction) {
        IndigoObject prevHandle = null;
        for(String chemistry : chemistryItems) {
            IndigoObject handle = isReaction ? indigo.loadReaction(chemistry) : indigo.loadMolecule(chemistry);
            if(prevHandle != null && indigo.exactMatch(handle, prevHandle) == null) {
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
    public Map<String, String> getMolecularInformation(String molecule, String saltCode, Float saltEq) {
        Map<String, String> result = new HashMap<>();

        IndigoObject handle = indigo.loadMolecule(molecule);

        float molecularWeightOriginal = handle.molecularWeight();
        SaltMetadata saltMetadata = getSaltMetadata(saltCode);

        result.put("name", handle.name());
        result.put("molecularFormula", handle.grossFormula());
        result.put("molecularWeightOriginal", String.valueOf(molecularWeightOriginal));
        result.put("exactMolecularWeight", String.valueOf(handle.monoisotopicMass()));
        result.put("isChiral", String.valueOf(handle.isChiral()));
        result.put("molecularWeight", String.valueOf(getMolecularWeight(molecularWeightOriginal, saltCode, saltEq)));
        result.put("saltDesc", saltMetadata.saltDesc);
        result.put("saltFormula", saltMetadata.saltFormula);
        result.put("saltWeight", String.valueOf(saltMetadata.saltWeight));
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
        return indigo.loadMolecule(molecule).countAtoms() == 0;
    }

    /**
     * Check, that molecule is chiral
     *
     * @param molecule structure of molecule
     * @return true if molecule is chiral
     */
    public boolean isMoleculeChiral(String molecule) {
        return indigo.loadMolecule(molecule).isChiral();
    }

    /**
     * Extract components (products and reactants) of given reaction
     * @param reaction reaction structure
     * @return reaction components
     */
    public ReactionPropertiesDTO extractReactionComponents(String reaction) {
        IndigoObject handle = indigo.loadReaction(reaction);

        //fetch reactants
        List<String> reactants = new ArrayList<>();
        for(IndigoObject reactant : handle.iterateReactants()) {
            reactants.add(reactant.molfile());
        }

        //fetch components
        List<String> products = new ArrayList<>();
        for(IndigoObject product : handle.iterateProducts()) {
            products.add(product.molfile());
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
        IndigoObject handle = indigo.createReaction();

        //add reactants to the structure
        for (String reactant : reactionDTO.getReactants()) {
            handle.addReactant(indigo.loadMolecule(reactant));
        }

        //add products to the structure
        for (String product : reactionDTO.getReactants()) {
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
        IndigoObject handle = indigo.loadQueryReaction(reaction);
        return (handle.countReactants() > 0) && (handle.countProducts() > 0);
    }

    /**
     * Render molecule/reaction by its string representation
     * @param structure string structure representation (Mol, Smiles etc.)
     * @param structureType molecule or reaction
     * @return RendererResult
     */
    public RendererResult getStructureWithImage(String structure, String structureType) {
        IndigoObject io = StringUtils.equals(structureType, "molecule") ? indigo.loadMolecule(structure) :
                indigo.loadReaction(structure);

        // auto-generate coordinates as Bingo DB doesn't store them
        io.layout();

        return new RendererResult(indigoRenderer.renderToBuffer(io));
    }

    private static SaltMetadata getSaltMetadata(String saltCode) {
        return SALT_METADATA_MAP.getOrDefault(saltCode, SALT_METADATA_DEFAULT);
    }

    private static float getMolecularWeight(float molWeightExisting, String saltCode, float saltEq) {
        return molWeightExisting + saltEq * getSaltMetadata(saltCode).saltWeight;
    }
}

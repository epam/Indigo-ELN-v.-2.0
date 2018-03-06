/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.core.service.calculation;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.epam.indigoeln.config.bingo.IndigoProvider;
import com.epam.indigoeln.core.service.calculation.helper.RendererResult;
import com.epam.indigoeln.core.service.codetable.CodeTableService;
import com.epam.indigoeln.web.rest.dto.calculation.ReactionPropertiesDTO;
import com.google.common.collect.ImmutableMap;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.epam.indigoeln.core.service.codetable.CodeTableService.*;

/**
 * Service for calculations under reaction or molecular structures defined in special text format.
 *
 * @author Sergei Bolbin
 */
@Service
public class CalculationService {

    /**
     * Logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CalculationService.class);

    private static final String EXCEPTION_OCCURRED = "Exception occurred: ";

    private static final Map<String, String> SALT_METADATA_DEFAULT = ImmutableMap.of(
            SALT_CODE, "0",
            SALT_DESC, "Parent Structure",
            SALT_WEIGHT, "0",
            SALT_FORMULA, "");

    @Autowired
    private IndigoProvider indigoProvider;

    @Autowired
    private CodeTableService codeTableService;

    private boolean isMolecule(String s) {
        try {
            indigoProvider.indigo().loadMolecule(s);
            return true;
        } catch (Exception e) {
            LOGGER.trace(EXCEPTION_OCCURRED, e);
            return false;
        }
    }

    private boolean isReaction(String s) {
        try {
            indigoProvider.indigo().loadReaction(s);
            return true;
        } catch (Exception e) {
            LOGGER.trace(EXCEPTION_OCCURRED, e);
            return false;
        }
    }

    /**
     * Returns <code>true</code> if the string molecule's representation is empty.
     *
     * @param molecule String molecule's representation
     * @return Returns <code>true</code> if the string molecule's representation is empty or
     * <code>false</code> if it's not empty
     */
    public Optional<Boolean> isEmptyMolecule(String molecule) {
        try {
            return Optional.of(indigoProvider.indigo().loadMolecule(molecule).countComponents() == 0);
        } catch (Exception e) {
            LOGGER.trace(EXCEPTION_OCCURRED, e);
            return Optional.empty();
        }
    }

    /**
     * Returns <code>true</code> if the string reaction's representation is empty.
     *
     * @param reaction String reaction's representation
     * @return Returns <code>true</code> if the string reaction's representation is empty or
     * <code>false</code> if it's not empty
     */
    public Optional<Boolean> isEmptyReaction(String reaction) {
        try {
            indigoProvider.indigo().loadReaction(reaction);
        } catch (Exception e) {
            LOGGER.trace(EXCEPTION_OCCURRED, e);
            try {
                if (indigoProvider.indigo().loadMolecule(reaction).countComponents() == 0) {
                    return Optional.of(true);
                }
            } catch (Exception e1) {
                LOGGER.trace(EXCEPTION_OCCURRED, e1);
                return Optional.empty();
            }
        }
        return Optional.of(false);
    }

    /**
     * Check, that chemistry structures of reactions or molecules are equals.
     *
     * @param chemistryItems list of chemistry structures
     * @return true if all chemistry items equals
     */
    public boolean chemistryEquals(List<String> chemistryItems) {
        Indigo indigo = indigoProvider.indigo();
        IndigoObject prevHandle = null;
        for (String chemistry : chemistryItems) {
            IndigoObject handle = isReaction(chemistry) ? indigo.loadReaction(chemistry)
                    : indigo.loadMolecule(chemistry);
            if (prevHandle != null && indigo.exactMatch(handle, prevHandle) == null) {
                return false;
            }
            prevHandle = handle;
        }
        return true;
    }

    /**
     * Check that at least one structure in list contains query structure.
     *
     * @param structures list with structures to check
     * @param query      query structure to find in list
     * @return true if at least one structure in list contains query structure (substructure matching is used)
     */
    public boolean listContainsStructure(List<String> structures, String query) {
        val indigo = indigoProvider.indigo();

        for (String s : structures) {
            val targetHandle = isReaction(s) ? indigo.loadReaction(s) : indigo.loadMolecule(s);
            val queryHandle = isReaction(query) ? indigo.loadQueryReaction(query) : indigo.loadQueryMolecule(query);
            queryHandle.aromatize();

            if (indigo.substructureMatcher(targetHandle).match(queryHandle) != null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Return calculated information for molecular structure
     * (name, formula, molecular weight, exact molecular weight, is chiral).
     *
     * @param molecule    structure of molecule
     * @param saltCodeOpt Salt Code
     * @param saltEqOpt   Salt Eq
     * @return map of calculated attributes
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getMolecularInformation(String molecule,
                                                       Optional<String> saltCodeOpt, Optional<Float> saltEqOpt) {
        Map<String, String> result = new HashMap<>();

        IndigoObject handle = indigoProvider.indigo().loadMolecule(molecule);

        Map<String, String> saltMetadata = getSaltMetadata(saltCodeOpt).orElse(SALT_METADATA_DEFAULT);
        float saltEq = saltEqOpt.orElse(1.0f);
        float molecularWeightOriginal = handle.molecularWeight();
        float saltWeight = StringUtils.isBlank(saltMetadata.get(SALT_WEIGHT)) ? 0.0f : Float.valueOf(saltMetadata.get(SALT_WEIGHT));
        float molecularWeightCalculated = molecularWeightOriginal + saltEq * saltWeight;

        String image = getStructureWithImage(molecule).getImageBase64();

        result.put("name", handle.name());
        result.put("molecule", molecule);
        result.put("image", image);
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

        String molecularFormulaFull = handle.grossFormula();
        if (saltCodeOpt.isPresent() && saltEqOpt.isPresent() && !StringUtils.equals(saltMetadata.get(SALT_DESC), SALT_METADATA_DEFAULT.get(SALT_DESC))) {
            molecularFormulaFull += ("*" + ((saltEq == (long) saltEq) ? String.valueOf((long) saltEq) : String.valueOf(saltEq)) + "(" + StringUtils.trim(saltMetadata.get(SALT_DESC)) + ")");
        }
        result.put("molecularFormulaFull", molecularFormulaFull);

        return result;
    }

    /**
     * * Returns rows from table with name {@code tableName}.
     * <p>
     * Table should be stored in data/{@code tableName}.csv.
     *
     * @param tableName Table's name
     * @return Data rows from {@code tableName} table
     */
    public List<Map<String, String>> getCodeTableRows(String tableName) {
        return codeTableService.getCodeTable(tableName);
    }

    /**
     * Check, that molecule is empty (does not contains any atoms).
     *
     * @param molecule structure of molecule
     * @return true if molecule empty
     */
    public boolean isMoleculeEmpty(String molecule) {
        return indigoProvider.indigo().loadMolecule(molecule).countAtoms() == 0;
    }

    /**
     * Check, that molecule is chiral.
     *
     * @param molecule structure of molecule
     * @return true if molecule is chiral
     */
    public boolean isMoleculeChiral(String molecule) {
        return indigoProvider.indigo().loadMolecule(molecule).isChiral();
    }

    /**
     * Extract components (products and reactants) of given reaction.
     *
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
     * Combine reaction components (products and reactants) with existing reaction structure.
     * Reaction structure received as string field of DTO and will be enriched by reactants and products received in
     * DTO list fields.
     *
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
     * Method to determine if chemistry contains a valid reaction (any products and reactants present).
     *
     * @param reaction structure of reaction
     * @return is reaction valid
     */
    public boolean isValidReaction(String reaction) {
        IndigoObject handle = indigoProvider.indigo().loadQueryReaction(reaction);
        return (handle.countReactants() > 0) && (handle.countProducts() > 0);
    }

    /**
     * Render molecule/reaction by its string representation.
     *
     * @param structure string structure representation (Mol, Smiles etc.)
     * @return RendererResult
     */
    public RendererResult getStructureWithImage(String structure) {
        Indigo indigo = indigoProvider.indigo();
        IndigoObject io = isMolecule(structure) ? indigo.loadMolecule(structure) : indigo.loadReaction(structure);
        return new RendererResult(indigoProvider.renderer(indigo).renderToBuffer(io));
    }

    private Optional<Map<String, String>> getSaltMetadata(Optional<String> saltCode) {
        if (!saltCode.isPresent()) {
            return Optional.empty();
        }

        List<Map<String, String>> codeTable = codeTableService.getCodeTable(CodeTableService.TABLE_SALT_CODE);
        return codeTable.stream().filter(tableRow -> saltCode.get().equals(tableRow.get(SALT_CODE))).findAny();
    }
}

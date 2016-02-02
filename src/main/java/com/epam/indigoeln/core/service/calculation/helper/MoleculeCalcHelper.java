package com.epam.indigoeln.core.service.calculation.helper;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for molecular structure calculations
 */
public final class MoleculeCalcHelper {

    public static final String PARAM_MOLECULAR_NAME = "name";
    public static final String PARAM_MOLECULAR_FORMULA = "molecularFormula";
    public static final String PARAM_MOLECULAR_WEIGHT = "molecularWeight";
    public static final String PARAM_MOLECULAR_EXACT_WEIGHT = "exactMolecularWeight";
    public static final String PARAM_MOLECULAR_IS_CHIRAL = "isChiral";

    private MoleculeCalcHelper() {
    }

    public static Map<String, String> getMolecularInformation(String molecule) {
        Map<String, String> result = new HashMap<>();
        IndigoObject handle = new Indigo().loadMolecule(molecule);

        result.put(PARAM_MOLECULAR_NAME,         handle.name());
        result.put(PARAM_MOLECULAR_FORMULA,      handle.grossFormula());
        result.put(PARAM_MOLECULAR_WEIGHT,       String.valueOf(handle.molecularWeight()));
        result.put(PARAM_MOLECULAR_EXACT_WEIGHT, String.valueOf(handle.monoisotopicMass()));
        result.put(PARAM_MOLECULAR_IS_CHIRAL,    String.valueOf(handle.isChiral()));
        return result;
    }

    public static boolean isMoleculeChiral(String molecule) {
        return new Indigo().loadMolecule(molecule).isChiral();
    }

    public static boolean isMoleculeEmpty(String molecule) {
        return new Indigo().loadMolecule(molecule).countAtoms() == 0;
    }
}

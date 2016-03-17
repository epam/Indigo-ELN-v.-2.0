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
    public static final String PARAM_MOLECULAR_WEIGHT_EXACT = "molecularWeightExact";
    public static final String PARAM_MOLECULAR_WEIGHT = "molecularWeight";
    public static final String PARAM_MOLECULAR_EXACT_WEIGHT = "exactMolecularWeight";
    public static final String PARAM_MOLECULAR_IS_CHIRAL = "isChiral";

    public static final String PARAM_SALT_DESC = "saltDesc";
    public static final String PARAM_SALT_FORMULA = "saltFormula";
    public static final String PARAM_SALT_WEIGHT = "saltWeight";
    public static final String PARAM_SALT_EQ = "saltEQ";

    private static final SaltMetadata SALT_METADATA_DEFAULT = new SaltMetadata("0", "Parent Structure", 0.0f, null);

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

    private MoleculeCalcHelper() {
    }

    public static Map<String, String> getMolecularInformation(String molecule, String saltCode, float saltEq) {
        Map<String, String> result = new HashMap<>();
        IndigoObject handle = new Indigo().loadMolecule(molecule);
        float molecularWeightExact = handle.molecularWeight();
        SaltMetadata saltMetadata = getSaltMetadata(saltCode);

        result.put(PARAM_MOLECULAR_NAME,         handle.name());
        result.put(PARAM_MOLECULAR_FORMULA,      handle.grossFormula());
        result.put(PARAM_MOLECULAR_WEIGHT_EXACT, String.valueOf(molecularWeightExact));
        result.put(PARAM_MOLECULAR_EXACT_WEIGHT, String.valueOf(handle.monoisotopicMass()));
        result.put(PARAM_MOLECULAR_IS_CHIRAL,    String.valueOf(handle.isChiral()));
        result.put(PARAM_MOLECULAR_WEIGHT,       String.valueOf(getMolecularWeight(molecularWeightExact, saltCode, saltEq)));

        result.put(PARAM_SALT_DESC,    saltMetadata.saltDesc);
        result.put(PARAM_SALT_FORMULA, saltMetadata.saltFormula);
        result.put(PARAM_SALT_WEIGHT,  String.valueOf(saltMetadata.saltWeight));
        result.put(PARAM_SALT_EQ,      String.valueOf(saltEq));

        return result;
    }

    public static boolean isMoleculeChiral(String molecule) {
        return new Indigo().loadMolecule(molecule).isChiral();
    }

    public static boolean isMoleculeEmpty(String molecule) {
        return new Indigo().loadMolecule(molecule).countAtoms() == 0;
    }

    private static SaltMetadata getSaltMetadata(String saltCode) {
        return SALT_METADATA_MAP.getOrDefault(saltCode, SALT_METADATA_DEFAULT);
    }

    private static float getMolecularWeight(float molWeightExisting, String saltCode, float saltEq) {
        return molWeightExisting + saltEq * getSaltMetadata(saltCode).saltWeight;
    }
}

package com.epam.indigoeln.core.chemistry.experiment.common.units;


import java.util.Map;
import java.util.TreeMap;


public class UnitFactory2 {

    private static final int STD_DISPLAY_FIGS = 3;

    private UnitFactory2() {
        // Hide the default constructor
    }

    public static Unit2 createUnitOfType(UnitType ut) {
        Unit2 result = null;
        switch (ut.getOrdinal()) {
            case UnitType.MASS_ORDINAL:
                result = new Unit2("MG", ut, "mg", "Milligrams", "MG", 1, 1);
                break;
            case UnitType.MOLES_ORDINAL:
                result = new Unit2("MMOL", ut, "mmol", "Micromole", "MMOL", 1, STD_DISPLAY_FIGS);
                break;
            case UnitType.VOLUME_ORDINAL:
                result = new Unit2("ML", ut, "mL", "Milliliter", "ML", 1, STD_DISPLAY_FIGS); // Times ten to the -3rd
                break;
            case UnitType.MOLAR_ORDINAL:
                result = new Unit2("M", ut, "M", "Molar", "M", 1, STD_DISPLAY_FIGS); // Times ten to the 0th power
                break;
            case UnitType.DENSITY_ORDINAL:
                result = new Unit2("GML", ut, "g/mL", "Grams/Milliliter", "GML", 1, STD_DISPLAY_FIGS);
                break;
            case UnitType.SCALAR_ORDINAL:
                result = new Unit2("SCAL", ut, "", "Scalar - no units appropriate", "SCAL", 1, STD_DISPLAY_FIGS);
                break;
            case UnitType.LOADING_ORDINAL:
                result = new Unit2("MMOLG", ut, "mmol/g", "Millimole/Gram", "MMOLG", 1, STD_DISPLAY_FIGS);
                break;
            case UnitType.TEMP_ORDINAL:
                result = new Unit2("C", ut, "C", "Celcius", "C", 1, STD_DISPLAY_FIGS);
                break;
            default:
                break;
        }
        return result;
    }

    static Map<String, Unit2> getUnitsOfType(UnitType ut) {
        TreeMap<String, Unit2> result = new TreeMap<>();
        switch (ut.getOrdinal()) {
            case UnitType.MASS_ORDINAL:
                result.put("MG", new Unit2("MG", ut, "mg", "Milligrams", "MG", 1, 1));
                result.put("GM", new Unit2("GM", ut, "g", "Grams", "MG", 1000, STD_DISPLAY_FIGS));
                result.put("KG", new Unit2("KG", ut, "kg", "Kilograms", "MG", 1000000, STD_DISPLAY_FIGS));
                break;
            case UnitType.MOLES_ORDINAL:
                result.put("UMOL", new Unit2("UMOL", ut, "umol", "Micromole", "MMOL", 0.001, 1));
                result.put("MMOL", new Unit2("MMOL", ut, "mmol", "Millimole", "MMOL", 1, STD_DISPLAY_FIGS));
                result.put("MOL", new Unit2("MOL", ut, "mol", "Mole", "MMOL", 1000, STD_DISPLAY_FIGS));
                break;
            case UnitType.VOLUME_ORDINAL:
                result.put("UL", new Unit2("UL", ut, "uL", "Microliter", "ML", 0.001, 1)); // Times ten to the -6th
                result.put("ML", new Unit2("ML", ut, "mL", "Milliliter", "ML", 1, STD_DISPLAY_FIGS)); // Times ten to the -3rd
                result.put("L", new Unit2("L", ut, "L", "Liter", "ML", 1000, STD_DISPLAY_FIGS)); // 1 L
                break;
            case UnitType.MOLAR_ORDINAL:
                result.put("MM", new Unit2("MM", ut, "mM", "Milli-Molar", "M", 0.001, STD_DISPLAY_FIGS)); // Times ten to the -3rd
                result.put("M", new Unit2("M", ut, "M", "Molar", "M", 1, STD_DISPLAY_FIGS)); // Times ten to the 0th.
                break;
            case UnitType.DENSITY_ORDINAL:
                result.put("GML", new Unit2("GML", ut, "g/mL", "Grams/Milliliter", "GML", 1, STD_DISPLAY_FIGS));
                break;
            case UnitType.SCALAR_ORDINAL:
                result.put("SCAL", new Unit2("SCAL", ut, "", "Scalar - no units appropriate", "SCAL", 1, STD_DISPLAY_FIGS));
                break;
            case UnitType.LOADING_ORDINAL:
                result.put("MMOLG", new Unit2("MMOLG", ut, "mmol/g", "Millimole/Gram", "MMOLG", 1, STD_DISPLAY_FIGS));
                break;
            case UnitType.TEMP_ORDINAL:
                result.put("C", new Unit2("C", ut, "C", "Celcius", "C", 1, STD_DISPLAY_FIGS));
                break;
            default:
                result.put("SCAL", new Unit2("SCAL", ut, "", "", "Scalar - no units appropriate", 1, STD_DISPLAY_FIGS));
                break;
        }
        return result;
    }

}

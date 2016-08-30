package com.chemistry.enotebook.experiment.common.units;

import java.util.Map;
import java.util.TreeMap;

//import com.chemistry.enotebook.experiment.datamodel.user.NotebookUser;

/**
 * Molar Concentrations
 * <p>
 * 1 Molar = 1 M = 1 mole/L = 1 mmole/mL 1 millimolar = 1 mM = 1 mmole/L = 1 umole/mL 1 micromolar = 1 uM = 1 umole/L = 1 nmole/mL 1
 * nanomolar = 1 nM = 1 nmole/L = 1 pmole/mL
 * <p>
 * Weight
 * <p>
 * 1 kilogram = 1 kg = 1000 grams 1 gram = 1 g = 1 gram = 1000 milligrams 1 milligram = 1 mg = 1000 micrograms 1 microgram = 1 ug =
 * 1000 nanograms 1 nanogram = 1 ng = 1000 picograms
 * <p>
 * Volume
 * <p>
 * 1 Liter = 1 L = 1000 milliliters 1 milliliter = 1 mL = 1000 microliters 1 microliter = 1 uL = 1000 nanoliters
 */
class UnitFactory {
    private static final int STD_DISPLAY_FIGS = 3;

    public static Unit createUnitOfType(UnitType ut) {
        Unit result = null;
        switch (ut.getOrdinal()) {
            case UnitType.MASS_ORDINAL:
                result = new Unit("MG", ut, "mg", "Milligrams", "MG", 1, 1);
                break;
            case UnitType.MOLES_ORDINAL:
                result = new Unit("MMOL", ut, "mmol", "Millimole", "MMOL", 1, STD_DISPLAY_FIGS);
                break;
            case UnitType.VOLUME_ORDINAL:
                result = new Unit("ML", ut, "mL", "Milliliter", "ML", 1, STD_DISPLAY_FIGS); // Times ten to the -3rd
                break;
            case UnitType.MOLAR_ORDINAL:
                result = new Unit("M", ut, "M", "Molar", "M", 1, STD_DISPLAY_FIGS); // Times ten to the 0th power
                break;
            case UnitType.DENSITY_ORDINAL:
                result = new Unit("GML", ut, "g/mL", "Grams/Milliliter", "GML", 1, STD_DISPLAY_FIGS);
                break;
            case UnitType.SCALAR_ORDINAL:
                result = new Unit("SCAL", ut, "", "Scalar - no units appropriate", "SCAL", 1, STD_DISPLAY_FIGS);
                break;
            case UnitType.LOADING_ORDINAL:
                result = new Unit("MMOLG", ut, "mmol/g", "Millimole/Gram", "MMOLG", 1, STD_DISPLAY_FIGS);
                break;
            case UnitType.TEMP_ORDINAL:
                result = new Unit("C", ut, "C", "Celcius", "C", 1, STD_DISPLAY_FIGS);
                break;
            default:
                break;
        }
        return result;
    }

    static Map<String, Unit> getUnitsOfType(UnitType ut) {
        TreeMap<String, Unit> result = new TreeMap<>();
        switch (ut.getOrdinal()) {
            case UnitType.MASS_ORDINAL:
                result.put("MG", new Unit("MG", ut, "mg", "Milligrams", "MG", 1, 1));
                result.put("GM", new Unit("GM", ut, "g", "Grams", "MG", 1000, STD_DISPLAY_FIGS));
                result.put("KG", new Unit("KG", ut, "kg", "Kilograms", "MG", 1000000, 6));
                break;
            case UnitType.MOLES_ORDINAL:
                result.put("UMOL", new Unit("UMOL", ut, "umol", "Micromole", "MMOL", 0.001, 1));
                result.put("MMOL", new Unit("MMOL", ut, "mmol", "Millimole", "MMOL", 1, STD_DISPLAY_FIGS));
                result.put("MOL", new Unit("MOL", ut, "mol", "Mole", "MMOL", 1000, STD_DISPLAY_FIGS));
                break;
            case UnitType.VOLUME_ORDINAL:
                result.put("UL", new Unit("UL", ut, "uL", "Microliter", "ML", 0.001, 1)); // Times ten to the -6th
                result.put("ML", new Unit("ML", ut, "mL", "Milliliter", "ML", 1, STD_DISPLAY_FIGS)); // Times ten to the -3rd
                result.put("L", new Unit("L", ut, "L", "Liter", "ML", 1000, STD_DISPLAY_FIGS)); // 1 L
                break;
            case UnitType.MOLAR_ORDINAL:
                result.put("MM", new Unit("MM", ut, "mM", "Milli-Molar", "M", 0.001, STD_DISPLAY_FIGS)); // Times ten to the -3rd
                result.put("M", new Unit("M", ut, "M", "Molar", "M", 1, STD_DISPLAY_FIGS)); // Times ten to the 0th.
                break;
            case UnitType.DENSITY_ORDINAL:
                result.put("GML", new Unit("GML", ut, "g/mL", "Grams/Milliliter", "GML", 1, STD_DISPLAY_FIGS));
                break;
            case UnitType.SCALAR_ORDINAL:
                result.put("SCAL", new Unit("SCAL", ut, "", "Scalar - no units appropriate", "SCAL", 1, STD_DISPLAY_FIGS));
                break;
            case UnitType.LOADING_ORDINAL:
                result.put("MMOLG", new Unit("MMOLG", ut, "mmol/g", "Millimole/Gram", "MMOLG", 1, STD_DISPLAY_FIGS));
                break;
            case UnitType.TEMP_ORDINAL:
                result.put("C", new Unit("C", ut, "C", "Celcius", "C", 1, STD_DISPLAY_FIGS));
                break;
            default:
                result.put("SCAL", new Unit("SCAL", ut, "", "", "Scalar - no units appropriate", 1, STD_DISPLAY_FIGS));
                break;
        }
        return result;
    }

}

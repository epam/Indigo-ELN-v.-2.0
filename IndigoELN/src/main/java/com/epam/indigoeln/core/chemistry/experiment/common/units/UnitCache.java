package com.epam.indigoeln.core.chemistry.experiment.common.units;

import java.util.Map;
import java.util.TreeMap;

/**
 * Unit types are based on (T)ime, (M)ass, (L)ength, (W)eight, (D)ensity
 */
class UnitCache {
    private static UnitCache instance;
    private TreeMap unitsByType = null;
    private TreeMap<Object, Unit> units = null; // Cached by code
    private TreeMap<String, Unit> unitsByDisplayName = null;

    // Constructor
    private UnitCache() {
        units = new TreeMap<>();
        unitsByType = new TreeMap();
        unitsByDisplayName = new TreeMap<>();
        init();
    }

    public static UnitCache getInstance() {
        if (instance == null)
            createInstance();
        return instance;
    }

    // Double-Checked locking
    private static void createInstance() {
        if (instance == null) {
            instance = new UnitCache();
        }
    }

    public Unit getUnit(String unitCode) {
        Unit result = null;

        String uc = unitCode;

        if ("G".equals(unitCode))
            uc = "GM";
        if ("SCLR".equals(unitCode))
            uc = "SCAL";

        if (units.containsKey(uc))
            result = units.get(uc);
        if (units.containsKey(uc.toUpperCase()))
            result = units.get(uc.toUpperCase());
        if (unitsByDisplayName.containsKey(uc))
            result = unitsByDisplayName.get(uc);

        return result;
    }

    private void init() {
        for (UnitType ut : UnitType.VALUES) {
            addMap(ut, UnitFactory.getUnitsOfType(ut));
        }
    }

    private void addMap(UnitType type, Map mp) {
        if (!unitsByType.containsKey(type))
            unitsByType.put(type, mp);
        Unit tempUnit;
        for (Object key : mp.keySet()) {
            if (!units.containsKey(key)) {
                tempUnit = (Unit) mp.get(key);
                unitsByDisplayName.put(tempUnit.getDisplayValue(), tempUnit);
                units.put(key, tempUnit);
            }
        }
    }
}

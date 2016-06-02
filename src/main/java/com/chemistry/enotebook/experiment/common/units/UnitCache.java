package com.chemistry.enotebook.experiment.common.units;

import java.util.Map;
import java.util.TreeMap;

/**
 * Unit types are based on (T)ime, (M)ass, (L)ength, (W)eight, (D)ensity
 */
public class UnitCache {
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
        if (unitCode.equals("G"))
            unitCode = "GM";
        if (unitCode.equals("SCLR"))
            unitCode = "SCAL";
        if (units.containsKey(unitCode))
            result = units.get(unitCode);
        if (units.containsKey(unitCode.toUpperCase()))
            result = units.get(unitCode.toUpperCase());
        if (unitsByDisplayName.containsKey(unitCode))
            result = unitsByDisplayName.get(unitCode);
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

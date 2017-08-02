package com.chemistry.enotebook.experiment.common.units;

import java.util.Map;
import java.util.TreeMap;

public class UnitCache2 {

    private static UnitCache2 instance;
    private TreeMap<UnitType, Map<String, Unit2>> unitsByType = null;
    private TreeMap<String, Unit2> units = null; // Cached by code
    private TreeMap<String, Unit2> unitsByDisplayName = null;

    // Constructor
    private UnitCache2() {
        units = new TreeMap<>();
        unitsByType = new TreeMap<>();
        unitsByDisplayName = new TreeMap<>();
        init();
    }

    public static UnitCache2 getInstance() {
        if (instance == null)
            createInstance();
        return instance;
    }

    // Double-Checked locking
    private static void createInstance() {
        if (instance == null) {
            instance = new UnitCache2();
        }
    }

    public Unit2 getUnit(String unitCode) {
        Unit2 result = null;

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
            addMap(ut, UnitFactory2.getUnitsOfType(ut));
        }
    }

    private void addMap(UnitType type, Map<String, Unit2> mp) {
        if (!unitsByType.containsKey(type))
            unitsByType.put(type, mp);
        Unit2 tempUnit;
        for (String key : mp.keySet()) {
            if (!units.containsKey(key)) {
                tempUnit = mp.get(key);
                unitsByDisplayName.put(tempUnit.getDisplayValue(), tempUnit);
                units.put(key, tempUnit);
            }
        }
    }
}


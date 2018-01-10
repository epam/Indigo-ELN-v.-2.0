package com.epam.indigoeln.core.chemistry.experiment.common.units;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Unit types are based on (T)ime, (M)ass, (L)ength, (W)eight, (D)ensity.
 */
final class UnitCache {
    private static volatile UnitCache instance;
    private TreeMap<UnitType, Map<String, Unit>> unitsByType = null;
    private TreeMap<Object, Unit> units = null; // Cached by code
    private TreeMap<String, Unit> unitsByDisplayName = null;

    // Constructor
    private UnitCache() {
        units = new TreeMap<>();
        unitsByType = new TreeMap<>();
        unitsByDisplayName = new TreeMap<>();
        init();
    }

    public static UnitCache getInstance() {
        if (instance == null) {
            createInstance();
        }
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

        if ("G".equals(unitCode)) {
            uc = "GM";
        }
        if ("SCLR".equals(unitCode)) {
            uc = "SCAL";
        }
        if (units.containsKey(uc)) {
            result = units.get(uc);
        }
        if (units.containsKey(uc.toUpperCase(Locale.getDefault()))) {
            result = units.get(uc.toUpperCase(Locale.getDefault()));
        }
        if (unitsByDisplayName.containsKey(uc)) {
            result = unitsByDisplayName.get(uc);
        }

        return result;
    }

    private void init() {
        for (UnitType ut : UnitType.VALUES) {
            addMap(ut, UnitFactory.getUnitsOfType(ut));
        }
    }

    private void addMap(UnitType type, Map<String, Unit> mp) {
        if (!unitsByType.containsKey(type)) {
            unitsByType.put(type, mp);
        }

        mp.forEach((key, value) -> {
            if (!units.containsKey(key)) {
                unitsByDisplayName.put(value.getDisplayValue(), value);
                units.put(key, value);
            }
        });
    }
}

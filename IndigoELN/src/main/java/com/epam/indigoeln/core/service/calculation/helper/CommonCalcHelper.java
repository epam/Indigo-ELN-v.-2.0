package com.epam.indigoeln.core.service.calculation.helper;

import java.util.List;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;

/**
 * Common helper class for chemistry structure calculations
 */
public final class CommonCalcHelper {

    private CommonCalcHelper() {
    }

    public static boolean chemistryEquals(List<String> chemistryItems, boolean isReaction) {
        Indigo indigo = new Indigo();
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
}

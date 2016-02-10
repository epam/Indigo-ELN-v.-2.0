package com.epam.indigoeln.core.service.calculation.helper;

import java.util.List;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.epam.indigo.IndigoRenderer;

/**
 * Common helper class for chemistry structure calculations
 */
public final class CommonCalcHelper {

    private static String LABEL_MODE = "hetero";
    private static String PICTURE_FORMAT = "svg";
    private static double BOND_LENGTH = -1d;
    private static boolean COLOR = true;

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

    public static IndigoRenderer getRenderer(Indigo indigo, Integer width, Integer height) {

        IndigoRenderer renderer = new IndigoRenderer(indigo);

        //set renderer options
        indigo.setOption("render-bond-length", BOND_LENGTH);
        indigo.setOption("render-label-mode", LABEL_MODE);
        indigo.setOption("render-output-format", PICTURE_FORMAT);
        indigo.setOption("render-coloring", COLOR);
        if (width != null && height != null) {
            indigo.setOption("render-image-size", width, height);
        }

        return renderer;
    }
}

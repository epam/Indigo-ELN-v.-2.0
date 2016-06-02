/****************************************************************************
 * Copyright (C) 2009-2015 EPAM Systems
 * <p>
 * This file is part of Indigo ELN.
 * <p>
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 3 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 * <p>
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 ***************************************************************************/
package com.chemistry.enotebook.experiment.datamodel.batch;

import com.chemistry.enotebook.domain.CeNConstants;

import java.io.Serializable;

/**
 * From Josh Bloch's implementation of a type-safe Enum class.
 * <p>
 * The idea here is to classify reactions by type and to allow them to be comparable so they can be sorted on type.
 * <p>
 * Warning: because there is an ordinal variable here the compareTo function can differ depending on which version of the enum was
 * called first.
 * <p>
 * Starting Material is the large component on the left side of the reaction arrow. There usually will only be one. Reactants are
 * also on the left side of the arrow and the two: Starting Material and Reactants comprise all components that contribute
 * structurally to products. Reagents are used to further the reaction but do not add structurally to the product(s). Solvents are
 * solvents.
 * <p>
 * Jeremy Edwards suggests Starting Materials appear at the top of the stoich grid or as the left most addition depending on
 * portrait or landscape orientation of table header.
 */
public class BatchType implements Serializable {
    // Possible Reaction Components
    public static final int START_MTRL_ORDINAL = 1; // Not currently used: Legacy designation
    public static final int REACTANT_ORDINAL = 2;
    public static final int REAGENT_ORDINAL = 4;
    public static final int SOLVENT_ORDINAL = 8;
    public static final int ACTUAL_PRODUCT_ORDINAL = 32;
    public static final BatchType REACTANT = new BatchType("REACTANT", REACTANT_ORDINAL);
    public static final BatchType SOLVENT = new BatchType("SOLVENT", SOLVENT_ORDINAL);
    public static final BatchType ACTUAL_PRODUCT = new BatchType("ACTUAL", ACTUAL_PRODUCT_ORDINAL);
    static final BatchType START_MTRL = new BatchType("STARTING MATERIAL", START_MTRL_ORDINAL);
    static final BatchType REAGENT = new BatchType("REAGENT", REAGENT_ORDINAL);
    static final long serialVersionUID = -8264055243665026767L;
    // Reaction Products
    private static final int INTENDED_PRODUCT_ORDINAL = 16;
    public static final BatchType INTENDED_PRODUCT = new BatchType("INTENDED", INTENDED_PRODUCT_ORDINAL);
    private static final int HASH_PRIME = 10093;
    private final String type;
    // Assign an ordinal to this suit
    private final int ordinal;

    // Private Constructor
    private BatchType(String type, int ordinal) {
        this.type = type;
        this.ordinal = ordinal;
    }

    public static BatchType getBatchType(String typeStr) {
        if (typeStr != null) {
            typeStr = typeStr.trim();
            if (typeStr.length() > 0) {
                if (typeStr.equalsIgnoreCase("ACTUAL")) {
                    return ACTUAL_PRODUCT;
                } else if (typeStr.equalsIgnoreCase("INTENDED")) {
                    return INTENDED_PRODUCT;
                } else if (typeStr.equalsIgnoreCase(CeNConstants.BATCH_TYPE_SOLVENT)) {
                    return SOLVENT;
                } else if (typeStr.equalsIgnoreCase(CeNConstants.BATCH_TYPE_REAGENT)) {
                    return REAGENT;
                } else if (typeStr.equalsIgnoreCase(CeNConstants.BATCH_TYPE_REACTANT)) {
                    return REACTANT;
                } else if (typeStr.equalsIgnoreCase("STARTING MATERIAL")) {
                    return START_MTRL;
                }
            }
        }
        return null;
    }

    // Using the ordinal ranking we can adjust which reaction type is
    // most to least important to us.
    public int compareTo(Object o) {
        return ordinal - ((BatchType) o).ordinal;
    }

    public int getOrdinal() {
        return ordinal;
    } // Can be used in switch statements

    public String toString() {
        return type;
    }

    // Override-prevention methods
    public final boolean equals(Object that) {
        if (that instanceof BatchType) {
            BatchType batchTypeExternal = (BatchType) that;
            if (batchTypeExternal.ordinal == this.ordinal) {
                return true;
            }
        }
        return false;
    }

    public final int hashCode() {
        return type.hashCode() * HASH_PRIME;
    }

}

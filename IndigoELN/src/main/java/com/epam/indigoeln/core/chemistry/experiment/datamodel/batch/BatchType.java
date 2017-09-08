/****************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ***************************************************************************/
package com.epam.indigoeln.core.chemistry.experiment.datamodel.batch;

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

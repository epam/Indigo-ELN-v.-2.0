/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.web.rest.dto.calculation;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple transfer object for Reaction parameters.
 */
public class ReactionPropertiesDTO {

    private String structure; //reaction structure (molfile)
    private List<String> reactants = new ArrayList<>(); // list of reactants (structures of molecules)
    private List<String> products = new ArrayList<>(); // list of products (structures of molecules)

    public ReactionPropertiesDTO() {
        super();
    }

    public ReactionPropertiesDTO(String structure, List<String> reactants, List<String> products) {
        this.structure = structure;
        this.reactants = reactants != null ? reactants : this.reactants;
        this.products = products != null ? products : this.products;
    }

    public String getStructure() {
        return structure;
    }

    public List<String> getReactants() {
        return reactants;
    }

    public List<String> getProducts() {
        return products;
    }

    public void setStructure(String structure) {
        this.structure = structure;
    }

    public void setReactants(List<String> reactants) {
        this.reactants = reactants != null ? reactants : new ArrayList<>();
    }

    public void setProducts(List<String> products) {
        this.products = products != null ? products : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "ReactionPropertiesDTO{"
                + "structure='" + structure + '\''
                + ", reactants=" + reactants
                + ", products=" + products
                + '}';
    }
}

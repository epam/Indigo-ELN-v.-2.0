package com.epam.indigoeln.web.rest.dto.calculation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple transfer object for Reaction parameters
 */
public class ReactionPropertiesDTO {

    private static final long serialVersionUID = 7723901161577149296L;

    private String structure; //reaction structure (molfile)
    private List<String> reactants = new ArrayList<>(); // list of reactants (structures of molecules)
    private List<String> products = new ArrayList<>(); // list of products (structures of molecules)

    public ReactionPropertiesDTO() {
        super();
    }

    public ReactionPropertiesDTO(String structure, List<String> reactants, List<String> products) {
        this.structure = structure;
        this.reactants = reactants != null ? reactants : this.reactants;
        this.products  = products  != null ? products  : this.products;
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
        return "ReactionPropertiesDTO{" +
                "structure='" + structure + '\'' +
                ", reactants=" + reactants +
                ", products=" + products +
                '}';
    }
}

package com.epam.indigoeln.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection=Notebook.COLLECTION_NAME)
public class Notebook extends BasicModelObject {

    public static final String COLLECTION_NAME = "notebook";
    private static final long serialVersionUID = 1762412679516966928L;

//    @Pattern(regexp = "^\\d{8}")
//    private String name;

    @JsonIgnore
    @DBRef
    private List<Experiment> experiments = new ArrayList<>();

    public List<Experiment> getExperiments() {
        return experiments;
    }

    public void setExperiments(List<Experiment> experiments) {
        this.experiments = experiments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notebook)) return false;
        if (!super.equals(o)) return false;
        Notebook notebook = (Notebook) o;
        return Objects.equal(experiments, notebook.experiments);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), experiments);
    }

    @Override
    public String toString() {
        return "Notebook{" +
                "experiments=" + experiments +
                "} " + super.toString();
    }
}
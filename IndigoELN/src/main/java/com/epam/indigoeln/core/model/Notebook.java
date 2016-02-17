package com.epam.indigoeln.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection=Notebook.COLLECTION_NAME)
public class Notebook extends BasicModelObject {

    public static final String COLLECTION_NAME = "notebook";
    private static final long serialVersionUID = 1762412679516966928L;

    private String description;

    @JsonIgnore
    @DBRef
    private List<Experiment> experiments = new ArrayList<>();


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Experiment> getExperiments() {
        return experiments;
    }

    public void setExperiments(List<Experiment> experiments) {
        this.experiments = experiments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Notebook notebook = (Notebook) o;

        if (description != null ? !description.equals(notebook.description) : notebook.description != null)
            return false;
        return experiments != null ? experiments.equals(notebook.experiments) : notebook.experiments == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (experiments != null ? experiments.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Notebook{" +
                "description='" + description + '\'' +
                ", experiments=" + experiments +
                '}';
    }
}
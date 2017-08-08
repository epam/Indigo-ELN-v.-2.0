package com.epam.indigoeln.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@ToString
@EqualsAndHashCode(callSuper = true)
@Document(collection = Notebook.COLLECTION_NAME)
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
}
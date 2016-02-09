package com.epam.indigoeln.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document(collection=Notebook.COLLECTION_NAME)
public class Notebook {
    public static final String COLLECTION_NAME = "notebook";

    @Id
    private String id;

    @Pattern(regexp = "^\\d{8}")
    private String name;

    @JsonIgnore
    @DBRef
    private List<Experiment> experiments = new ArrayList<>();

    private Set<UserPermission> accessList = new HashSet<>();

    public Notebook() {
    }

    public Notebook(String name) {
        this.setName(name);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Experiment> getExperiments() {
        return experiments;
    }

    public void setExperiments(List<Experiment> experiments) {
        this.experiments = experiments;
    }

    public Set<UserPermission> getAccessList() {
        return accessList;
    }

    public void setAccessList(Set<UserPermission> accessList) {
        this.accessList = accessList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notebook)) return false;

        Notebook notebook = (Notebook) o;

        if (id != null ? !id.equals(notebook.id) : notebook.id != null) return false;
        if (name != null ? !name.equals(notebook.name) : notebook.name != null) return false;
        if (experiments != null ? !experiments.equals(notebook.experiments) : notebook.experiments != null) return false;
        if (accessList != null ? !accessList.equals(notebook.accessList) : notebook.accessList != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (experiments != null ? experiments.hashCode() : 0);
        result = 31 * result + (accessList != null ? accessList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Notebook{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                "}";
    }
}
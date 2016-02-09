package com.epam.indigoeln.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document(collection=Notebook.COLLECTION_NAME)
public class Notebook {
    public static final String COLLECTION_NAME = "notebook";

    @Id
    private String id;

    @NotNull
    private Long sequenceId;

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

    public Long getSequenceId() {
        return sequenceId;
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

    public void setSequenceId(Long sequenceId) {
        this.sequenceId = sequenceId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notebook)) return false;
        Notebook notebook = (Notebook) o;
        return  Objects.equal(id, notebook.id) &&
                Objects.equal(name, notebook.name) &&
                Objects.equal(sequenceId, notebook.sequenceId) &&
                Objects.equal(experiments, notebook.experiments) &&
                Objects.equal(accessList, notebook.accessList);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, name, sequenceId, experiments, accessList);
    }


    @Override
    public String toString() {
        return "Notebook{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", sequenceId=" + sequenceId +
                '}';
    }
}
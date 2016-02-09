package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.UserPermission;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for Notebook
 */
public class NotebookDTO implements Serializable {

    private static final long serialVersionUID = -510169140377086627L;

    @JsonProperty("id")
    private Long sequenceId;

    @NotNull
    @Pattern(regexp = "^\\d{8}")
    private String name;

    private Set<UserPermission> accessList;

    public NotebookDTO() {
    }

    public NotebookDTO(Notebook notebook) {
        this.sequenceId = notebook.getSequenceId();
        this.name = notebook.getName();
        this.accessList = notebook.getAccessList();
    }

    public Long getSequenceId() {
        return sequenceId;
    }

    public String getName() {
        return name;
    }

    public Set<UserPermission> getAccessList() {
        return accessList;
    }

    public void setSequenceId(Long sequenceId) {
        this.sequenceId = sequenceId;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setAccessList(Set<UserPermission> accessList) {
        this.accessList = accessList;
    }

    @Override
    public String toString() {
        return "NotebookDTO{" +
                "name='" + name + "\'" +
                "}";
    }
}
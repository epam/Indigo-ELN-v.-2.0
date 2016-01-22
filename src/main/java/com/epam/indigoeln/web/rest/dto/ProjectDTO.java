package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.model.UserPermission;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * DTO for Project
 */
public class ProjectDTO implements Serializable {

    private String id;

    @NotNull
    @Size(max = 8)
    private String name;

    private List<UserPermission> accessList;

    public ProjectDTO() {
    }

    public ProjectDTO(Project project) {
        this.id = project.getId();
        this.name = project.getName();
        this.accessList = project.getAccessList();
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

    public List<UserPermission> getAccessList() {
        return accessList;
    }

    public void setAccessList(List<UserPermission> accessList) {
        this.accessList = accessList;
    }

    @Override
    public String toString() {
        return "ProjectDTO{" +
                "name='" + name + "\'" +
                "}";
    }
}
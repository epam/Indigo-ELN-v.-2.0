package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.Notebook;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * DTO of Notebook
 */
public class NotebookDTO {

    @NotNull
    @Size(max = 8)
    private String name;

    private List<String> experimentIds;

    private List<String> userIds;

    public NotebookDTO() {
    }

    public NotebookDTO(String name) {
        this.setName(name);
    }

    public NotebookDTO(Notebook notebook) {
        this(notebook.getName());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getExperimentIds() {
        return experimentIds;
    }

    public void setExperimentIds(List<String> experimentIds) {
        this.experimentIds = experimentIds;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    @Override
    public String toString() {
        return "NotebookDTO{" +
                "name='" + name + "\'" +
                "}";
    }

}

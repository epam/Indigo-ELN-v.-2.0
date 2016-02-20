package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.Notebook;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;

/**
 * DTO for Notebook
 */
@JsonTypeName("Notebook")
public class NotebookDTO extends BasicDTO {

    private static final long serialVersionUID = -1261267009811486561L;

    private String description;
    private List<ExperimentDTO> experiments;

    public NotebookDTO() {
    }

    public NotebookDTO(Notebook notebook) {
        super(notebook);
        this.description = notebook.getDescription();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "NotebookDTO{} " + super.toString();
    }
}
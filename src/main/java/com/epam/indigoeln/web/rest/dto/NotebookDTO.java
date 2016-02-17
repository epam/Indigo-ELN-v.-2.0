package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.Notebook;

import java.util.List;

/**
 * DTO for Notebook
 */
public class NotebookDTO extends BasicDTO {

    private static final long serialVersionUID = -1261267009811486561L;

    private String description;
    private List<ExperimentDTO> experiments;

    public NotebookDTO() {
    }

    public NotebookDTO(Notebook notebook) {

        super(notebook);

        this.description = notebook.getDescription();
//        this.experiments = notebook.getExperiments() != null ?
//                notebook.getExperiments().stream().map(NotebookDTO::new).collect(Collectors.toList()) : new ArrayList<>();
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
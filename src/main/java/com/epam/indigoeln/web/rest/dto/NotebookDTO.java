package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.Notebook;

/**
 * DTO for Notebook
 */
public class NotebookDTO extends BasicDTO {

    private static final long serialVersionUID = -510169140377086627L;

//    @NotNull
//    @Pattern(regexp = "^\\d{8}")
//    private String name;

    public NotebookDTO() {
    }

    public NotebookDTO(Notebook notebook) {
        super(notebook);
    }

    @Override
    public String toString() {
        return "NotebookDTO{} " + super.toString();
    }
}
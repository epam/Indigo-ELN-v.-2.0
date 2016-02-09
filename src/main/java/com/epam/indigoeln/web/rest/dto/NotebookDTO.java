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
public class NotebookDTO extends BasicDTO {

    private static final long serialVersionUID = -510169140377086627L;

//    @NotNull
//    @Pattern(regexp = "^\\d{8}")
//    private String name;

    public NotebookDTO() {
    }

    public NotebookDTO(Notebook notebook) {
        super(notebook.getSequenceId(),
              notebook.getName(),
              notebook.getAccessList(),
              new UserDTO(notebook.getAuthor()),
              new UserDTO(notebook.getLastModifiedBy()),
              notebook.getCreationDate(),
              notebook.getLastEditDate());
    }

    @Override
    public String toString() {
        return "NotebookDTO{} " + super.toString();
    }
}
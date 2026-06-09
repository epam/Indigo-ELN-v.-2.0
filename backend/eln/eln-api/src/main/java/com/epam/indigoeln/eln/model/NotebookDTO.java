package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NotebookDTO extends BaseNotebookDTO {

    @NotNull
    List<ACLEntryDTO> acl;

    @NotNull
    Integer aclCount;

    @Override
    public String toString() {
        return "NotebookDTO{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                '}';
    }
}

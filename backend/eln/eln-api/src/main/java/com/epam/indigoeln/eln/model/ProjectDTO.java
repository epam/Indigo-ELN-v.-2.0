package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ProjectDTO extends BaseProjectDTO {

    @NotNull
    List<ACLEntryDTO> acl;

    @NotNull
    Integer aclCount;

    @Override
    public String toString() {
        return "ProjectDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}

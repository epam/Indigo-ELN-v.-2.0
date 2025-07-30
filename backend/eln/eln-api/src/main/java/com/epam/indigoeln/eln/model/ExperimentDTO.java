package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExperimentDTO extends BaseExperimentDTO {

    @NotNull
    List<ACLEntryDTO> acl;

    @NotNull
    Integer aclCount;

    @Override
    public String toString() {
        return "ExperimentDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}

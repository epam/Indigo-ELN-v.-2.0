package com.epam.indigoeln.eln.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Getter
@Setter
public class ExperimentDTO extends BaseDTO {

    @NotEmpty
    String name;

    @NotNull
    ExperimentStatus status;

    @Nullable
    String description;

    @NotNull
    Boolean marked;

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

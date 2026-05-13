package com.epam.indigoeln.eln.model;

import com.epam.indigoeln.common.model.BaseDTO;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseExperimentDTO extends BaseDTO {

    @NotEmpty
    String name;

    @NotNull
    ExperimentStatus status;

    @NotNull
    Boolean marked;

    @NotNull
    Integer revision;

    public ExperimentRef toRef() {
        return new ExperimentRef(getId(), name);
    }

    @Override
    public String toString() {
        return "Experiment{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                '}';
    }
}

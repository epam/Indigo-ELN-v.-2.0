package com.epam.indigoeln.eln.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ExperimentRevision")
@IdClass(ExperimentRevisionEntity.CompositeID.class)
public class ExperimentRevisionEntity extends BaseRevisionEntity {

    @Id
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(insertable = false, updatable = false)
    private ExperimentEntity experiment;

    public record CompositeID(
            ExperimentEntity experiment,
            Integer revision
    ) implements Serializable {}
}

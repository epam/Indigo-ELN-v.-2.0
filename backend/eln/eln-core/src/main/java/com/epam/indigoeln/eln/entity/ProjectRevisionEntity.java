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
@Entity(name = "ProjectRevision")
@IdClass(ProjectRevisionEntity.CompositeID.class)
public class ProjectRevisionEntity extends BaseRevisionEntity {

    @Id
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(insertable = false, updatable = false)
    private ProjectEntity project;

    public record CompositeID(
            ProjectEntity project,
            Integer revision
    ) implements Serializable {}
}

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
@Entity(name = "NotebookRevision")
@IdClass(NotebookRevisionEntity.CompositeID.class)
public class NotebookRevisionEntity extends BaseRevisionEntity {

    @Id
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(insertable = false, updatable = false)
    private NotebookEntity notebook;

    public record CompositeID(
            NotebookEntity notebook,
            Integer revision
    ) implements Serializable {}
}

package com.epam.indigoeln.eln.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "NotebookRevision")
public class NotebookRevisionEntity extends BaseRevisionEntity {

    @EmbeddedId
    private NotebookRevisionID id;

    @NotNull
    @MapsId("notebookId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(insertable = false, updatable = false)
    private NotebookEntity notebook;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotebookRevisionID implements Serializable {

        @Column(name = "notebook_id")
        private UUID notebookId;

        @Column(name = "revision")
        private Integer revision;
    }
}

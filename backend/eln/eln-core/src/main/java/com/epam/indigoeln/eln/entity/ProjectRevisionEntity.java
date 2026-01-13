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
@Entity(name = "ProjectRevision")
public class ProjectRevisionEntity extends BaseRevisionEntity {

    @EmbeddedId
    private ProjectRevisionID id;

    @NotNull
    @MapsId("projectId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(insertable = false, updatable = false)
    private ProjectEntity project;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectRevisionID implements Serializable {

        @Column(name = "project_id")
        private UUID projectId;

        @Column(name = "revision")
        private Integer revision;
    }
}

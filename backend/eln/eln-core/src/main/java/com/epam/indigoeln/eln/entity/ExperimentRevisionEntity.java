package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.config.hibernate.ExperimentSnapshotConverter;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

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

    @Nullable
    private Integer version;

    @Nullable
    @JdbcTypeCode(SqlTypes.JSON)
    @Convert(converter = ExperimentSnapshotConverter.class)
    private ExperimentSnapshot snapshot;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(updatable = false)
    private ExperimentEditSessionEntity editSession;

    public record CompositeID(
            ExperimentEntity experiment,
            Integer revision
    ) implements Serializable {}
}

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
@NamedEntityGraph(
        name = "ExperimentRevision.list",
        attributeNodes = {
                @NamedAttributeNode("diff"),
                @NamedAttributeNode("mutation"),
                @NamedAttributeNode("user"),
        }
)
@NamedEntityGraph(
        name = "ExperimentRevision.range",
        attributeNodes = {
                @NamedAttributeNode("diff")
        }
)
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
    @Basic(fetch = FetchType.LAZY)
    private ExperimentSnapshot snapshot;

    @Basic(fetch = FetchType.LAZY)
    private String @Nullable [] messages;

    @Basic(fetch = FetchType.LAZY)
    private String @Nullable [] debugMessages;

    public record CompositeID(
            ExperimentEntity experiment,
            Integer revision
    ) implements Serializable {}
}

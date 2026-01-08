package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.config.hibernate.ExperimentPatchConverter;
import com.epam.indigoeln.eln.config.hibernate.MutationConverter;
import com.epam.indigoeln.eln.config.hibernate.MutationRedoInfoConverter;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.patch.ExperimentPatch;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ExperimentRevision")
public class ExperimentRevisionEntity {

    @EmbeddedId
    private ExperimentRevisionID id;

    @NotNull
    @MapsId("experimentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(insertable = false, updatable = false)
    private ExperimentEntity experiment;

    @NotNull
    @Column(insertable = false, updatable = false)
    private Integer revision;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(updatable = false)
    protected UserEntity user;

    @NotNull
    @Column(updatable = false)
    protected ZonedDateTime datetime;

    @NotNull
    private String summary;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Convert(converter = MutationConverter.class)
    private Mutation mutation;

    @Nullable
    @JdbcTypeCode(SqlTypes.JSON)
    @Convert(converter = MutationRedoInfoConverter.class)
    private MutationRedoInfo redoInfo;

    @Nullable
    @JdbcTypeCode(SqlTypes.JSON)
    @Convert(converter = MutationConverter.class)
    private Mutation reverseMutation;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Convert(converter = ExperimentPatchConverter.class)
    private ExperimentPatch diff;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExperimentRevisionID implements Serializable {

        @Column(name = "experiment_id")
        private UUID experimentId;

        @Column(name = "revision")
        private Integer revision;
    }
}

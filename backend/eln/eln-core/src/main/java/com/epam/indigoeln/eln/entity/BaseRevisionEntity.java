package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.config.hibernate.MutationConverter;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseRevisionEntity {

    @Id
    @NotNull
    @Column(updatable = false)
    private Integer revision;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(updatable = false)
    protected UserEntity user;

    @NotNull
    @Column(updatable = false)
    protected Instant datetime;

    @NotNull
    private String summary;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Convert(converter = MutationConverter.class)
    @Basic(fetch = FetchType.LAZY)
    private Mutation mutation;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Basic(fetch = FetchType.LAZY)
    private JsonNode diff;

    @Nullable
    private Integer undoFor;

    @Nullable
    private Integer redoFor;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(revision).append(" by ").append(getUser().getUsername()).append(": ").append(getSummary());
        if (getUndoFor() != null) {
            sb.append(" [undo for ").append(getUndoFor()).append("]");
        }
        if (getRedoFor() != null) {
            sb.append(" [redo for ").append(getRedoFor()).append("]");
        }
        return sb.toString();
    }
}

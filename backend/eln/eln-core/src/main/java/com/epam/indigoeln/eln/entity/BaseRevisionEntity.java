package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.config.hibernate.MutationConverter;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

import java.time.ZonedDateTime;

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
    protected ZonedDateTime datetime;

    @NotNull
    private String summary;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Convert(converter = MutationConverter.class)
    private Mutation mutation;

    @Nullable
    @JdbcTypeCode(SqlTypes.JSON)
    @Convert(converter = MutationConverter.class)
    private Mutation reverseMutation;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Basic(fetch = FetchType.LAZY)
    private String diff;
}

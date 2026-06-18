package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.common.entity.IdentifiableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity extends IdentifiableEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(updatable = false)
    protected UserEntity createdBy;

    @NotNull
    @Column(updatable = false)
    protected Instant createdAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    protected UserEntity modifiedBy;

    @NotNull
    protected Instant modifiedAt;
}

package com.epam.indigoeln.eln.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

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
    protected ZonedDateTime createdAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    protected UserEntity modifiedBy;

    @NotNull
    protected ZonedDateTime modifiedAt;
}

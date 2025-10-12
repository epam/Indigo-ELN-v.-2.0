package com.epam.indigoeln.eln.entity;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity extends IdentifiableEntity {

    @NotNull
    @ManyToOne
    @JoinColumn(updatable = false)
    protected UserEntity createdBy;

    @NotNull
    @Column(updatable = false)
    protected ZonedDateTime createdAt;

    @NotNull
    @ManyToOne
    protected UserEntity modifiedBy;

    @NotNull
    protected ZonedDateTime modifiedAt;
}

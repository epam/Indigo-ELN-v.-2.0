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
    @JoinColumn(name = "created_by_id", updatable = false)
    protected UserEntity createdBy;

    @NotNull
    @Column(name = "created_at", updatable = false)
    protected ZonedDateTime createdAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modified_by_id")
    protected UserEntity modifiedBy;

    @NotNull
    @Column(name = "modified_at")
    protected ZonedDateTime modifiedAt;
}

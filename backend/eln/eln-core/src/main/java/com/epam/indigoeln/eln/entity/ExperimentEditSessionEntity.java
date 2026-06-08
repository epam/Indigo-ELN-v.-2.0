package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.common.entity.IdentifiableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.time.ZonedDateTime;

@Getter
@Setter
@Entity(name = "ExperimentEditSession")
public class ExperimentEditSessionEntity extends IdentifiableEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(updatable = false)
    private ExperimentEntity experiment;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(updatable = false)
    protected UserEntity user;

    @NotNull
    @Column(updatable = false)
    protected ZonedDateTime started;

    @NotNull
    @Column
    protected ZonedDateTime lastActive;

    @Nullable
    @Column
    protected ZonedDateTime finished;
}

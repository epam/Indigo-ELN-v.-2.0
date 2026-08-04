package com.epam.indigoeln.eln.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.FetchType;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

@Getter
@Setter
@MappedSuperclass
public abstract class AbstractAttachment<P extends BaseEntity> extends BaseEntity {

    @NotEmpty
    private String name;

    @NotNull
    private Long size;

    @NotNull
    private Boolean deleted;

    @Basic(fetch = FetchType.LAZY)
    private byte @NotNull[] content;

    @Nullable
    public abstract P getParent();

    public abstract void setParent(@Nullable P parent);
}

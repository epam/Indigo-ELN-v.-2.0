package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.compound.entity.SampleEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Entity(name = "DictionaryItem")
@ToString(of = {"id", "dictionary", "ordinal", "name", "active"})
public class DictionaryItemEntity extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(updatable = false)
    private DictionaryEntity dictionary;

    @NotNull
    private Integer ordinal;

    @NotEmpty
    private String name;

    @Nullable
    private String description;

    @NotNull
    private Boolean active;
}

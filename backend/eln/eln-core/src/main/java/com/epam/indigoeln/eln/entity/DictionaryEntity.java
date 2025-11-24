package com.epam.indigoeln.eln.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

import java.util.Set;

@Getter
@Setter
@Entity(name = "Dictionary")
@ToString(of = {"id", "code", "name"})
public class DictionaryEntity extends BaseEntity {

    @NotEmpty
    private String code;

    @NotEmpty
    private String name;

    @Nullable
    private String description;

    @NotNull
    private Boolean userEditable;

    @NotNull
    @OneToMany(mappedBy = "dictionary")
    private Set<DictionaryItemEntity> items;
}

package com.epam.indigoeln.eln.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

@Getter
@Setter
@Entity(name = "DictionaryItem")
@Table(name = "Dictionary_Item")
@ToString(of = {"id", "dictionary", "ordinal", "name", "active"})
public class DictionaryItemEntity extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dictionary_id", updatable = false)
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

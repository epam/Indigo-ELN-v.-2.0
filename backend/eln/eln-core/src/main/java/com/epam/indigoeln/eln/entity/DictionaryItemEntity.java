package com.epam.indigoeln.eln.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

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

    @NotNull
    private Boolean deleted;

    @Nullable
    @JdbcTypeCode(SqlTypes.JSON)
    private String details;
}

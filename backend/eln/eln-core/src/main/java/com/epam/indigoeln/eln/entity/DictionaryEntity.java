package com.epam.indigoeln.eln.entity;

import com.epam.indigoeln.eln.model.Dictionary;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

@Getter
@Setter
@Entity(name = "DictionaryItem")
@Table(name = "Dictionary_Item")
@ToString(of = {"id", "dictionary", "ordinal", "name", "deleted"})
public class DictionaryEntity extends IdentifiableEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private Dictionary dictionary;

    @NotNull
    private Integer ordinal;

    @NotEmpty
    private String name;

    private String description;

    @NotNull
    private Boolean deleted;
}

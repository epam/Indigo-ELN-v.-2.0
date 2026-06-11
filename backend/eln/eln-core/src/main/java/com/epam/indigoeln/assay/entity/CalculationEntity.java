package com.epam.indigoeln.assay.entity;

import com.epam.indigoeln.assay.model.AssayValueType;
import com.epam.indigoeln.assay.model.CalculationKind;
import com.epam.indigoeln.eln.entity.BaseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

/**
 * Definition of how a derived value is computed: either a built-in library calculation
 * (identified by {@code libraryId}) or a custom formula ({@code expression}).
 */
@Getter
@Setter
@Entity(name = "Calculation")
@ToString(of = {"id", "name", "calcKind"})
public class CalculationEntity extends BaseEntity {

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    private AssayEntity assay;

    @NotEmpty
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private CalculationKind calcKind;

    @Nullable
    private String libraryId;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    private String expression;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    @JdbcTypeCode(SqlTypes.JSON)
    @Type(JsonNodeType.class)
    private JsonNode params;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    @JdbcTypeCode(SqlTypes.JSON)
    @Type(JsonNodeType.class)
    private JsonNode inputSelectors;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private AssayValueType outputValueType = AssayValueType.NUMERIC;

    @Nullable
    private String outputUnit;
}

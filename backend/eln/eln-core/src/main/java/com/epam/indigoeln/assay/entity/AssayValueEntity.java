package com.epam.indigoeln.assay.entity;

import com.epam.indigoeln.assay.model.AssayValueKind;
import com.epam.indigoeln.assay.model.AssayValueType;
import com.epam.indigoeln.assay.model.ValueQualifier;
import com.epam.indigoeln.eln.entity.BaseEntity;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * The unified node of the assay dependency graph. Raw readings, well-content concentrations
 * and derived results are all values, so propagation and cycle detection operate uniformly.
 *
 * <p>The self {@code dependsOn} / {@code dependents} many-to-many over {@code Value_Dependency}
 * forms the directed acyclic graph of value derivations.
 */
@Getter
@Setter
@Entity(name = "AssayValue")
@Table(name = "Assay_Value")
@ToString(of = {"id", "valueKind", "readout"})
public class AssayValueEntity extends BaseEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private AssayValueKind valueKind;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private AssayValueType valueType = AssayValueType.NUMERIC;

    @Nullable
    private String readout;

    @Nullable
    private BigDecimal numericValue;

    @Nullable
    private String unit;

    @Nullable
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private ValueQualifier qualifier;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    private String textValue;

    @Nullable
    private Boolean boolValue;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    @JdbcTypeCode(SqlTypes.JSON)
    @Type(JsonNodeType.class)
    private JsonNode payload;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    private CalculationEntity calculation;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private ExperimentEntity experiment;

    @NotNull
    private Boolean isStale = false;

    @NotNull
    private Boolean isFrozen = false;

    @Nullable
    private ZonedDateTime computedAt;

    /** Upstream values this value is derived from. */
    @ManyToMany
    @JoinTable(name = "Value_Dependency",
            joinColumns = @JoinColumn(name = "value_id"),
            inverseJoinColumns = @JoinColumn(name = "depends_on_value_id"))
    private Set<AssayValueEntity> dependsOn = new HashSet<>(0);

    /** Downstream values derived from this value. */
    @ManyToMany(mappedBy = "dependsOn")
    private Set<AssayValueEntity> dependents = new HashSet<>(0);
}

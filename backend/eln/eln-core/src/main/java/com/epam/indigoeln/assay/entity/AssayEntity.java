package com.epam.indigoeln.assay.entity;

import com.epam.indigoeln.assay.model.AssayStatus;
import com.epam.indigoeln.eln.entity.BaseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.search.PostgreSQLTSVectorType;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
 * Reusable assay / protocol definition. The {@code definition} holds the default plate
 * layout, readout definitions, control definitions and calculation definitions. When an
 * assay is applied to a plate the definition is snapshotted onto that plate, so subsequent
 * edits to the shared assay do not retroactively change existing plates.
 */
@Getter
@Setter
@Entity(name = "Assay")
@ToString(of = {"id", "name"})
public class AssayEntity extends BaseEntity {

    @NotEmpty
    private String name;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private AssayStatus status = AssayStatus.DRAFT;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    @JdbcTypeCode(SqlTypes.JSON)
    @Type(JsonNodeType.class)
    private JsonNode definition;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    @Type(PostgreSQLTSVectorType.class)
    @Column(insertable = false, updatable = false)
    private String searchVector;
}

package com.epam.indigoeln.assay.entity;

import com.epam.indigoeln.eln.common.entity.IdentifiableEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * Raised when propagation would change a value living in a signed / locked experiment. The
 * frozen value is left untouched and the proposed new value is recorded for human review.
 */
@Getter
@Setter
@Entity(name = "ValueConflict")
@Table(name = "Value_Conflict")
public class ValueConflictEntity extends IdentifiableEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "frozen_value_id")
    private AssayValueEntity frozenValue;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggering_value_id")
    private AssayValueEntity triggeringValue;

    @NotNull
    private ZonedDateTime detectedAt;

    @NotNull
    private Boolean resolved = false;

    @Nullable
    private ZonedDateTime resolvedAt;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity resolvedBy;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    private String detail;

    @Nullable
    private BigDecimal proposedNumericValue;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    @JdbcTypeCode(SqlTypes.JSON)
    @Type(JsonNodeType.class)
    private JsonNode proposedPayload;
}

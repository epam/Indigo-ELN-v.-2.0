package com.epam.indigoeln.assay.entity;

import com.epam.indigoeln.assay.model.ValueScopeType;
import com.epam.indigoeln.eln.common.entity.IdentifiableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;
import org.jspecify.annotations.Nullable;

/**
 * Describes what an {@link AssayValueEntity} represents: a single well, a group of wells, a
 * whole plate, a group of plates, or an arbitrary cross-experiment grouping. A value may have
 * many scope rows (e.g. a value aggregated over wells spread across several plates).
 */
@Getter
@Setter
@Entity(name = "AssayValueScope")
@Table(name = "Assay_Value_Scope")
public class AssayValueScopeEntity extends IdentifiableEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "value_id")
    private AssayValueEntity value;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private ValueScopeType scopeType;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    private WellEntity well;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    private PlateEntity plate;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "well_content_id")
    private WellContentEntity wellContent;
}

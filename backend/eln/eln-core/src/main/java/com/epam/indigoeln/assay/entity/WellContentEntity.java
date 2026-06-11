package com.epam.indigoeln.assay.entity;

import com.epam.indigoeln.assay.model.WellContentKind;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.eln.common.entity.IdentifiableEntity;
import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
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

import java.math.BigDecimal;

/**
 * One of 0..many contents of a well: a registered {@link SampleEntity} batch, a named control
 * (dictionary item) or a free-text / external identifier, each with an optional concentration.
 */
@Getter
@Setter
@Entity(name = "WellContent")
@Table(name = "Well_Content")
public class WellContentEntity extends IdentifiableEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "well_id")
    private WellEntity well;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private WellContentKind contentKind;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    private SampleEntity sample;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "control_type_id")
    private DictionaryItemEntity controlType;

    @Nullable
    private String freeText;

    @Nullable
    private BigDecimal concentration;

    @Nullable
    private String concentrationUnit;

    @NotNull
    private Integer ordinal = 0;
}

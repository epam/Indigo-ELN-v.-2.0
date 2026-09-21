package com.epam.indigoeln.sampleregistration.entity;

import com.epam.indigoeln.common.model.units.MolarityUnit;
import com.epam.indigoeln.eln.common.config.SearchVectorType;
import com.epam.indigoeln.eln.common.entity.IdentifiableEntity;
import com.epam.indigoeln.eln.common.util.SearchVector;
import com.epam.indigoeln.sampleregistration.config.STRCodeSampleConverter;
import com.epam.indigoeln.sampleregistration.model.STRCodeSample;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.LazyGroup;
import org.hibernate.annotations.Type;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity(name = "SRSSample")
@Table(name = "SRS_Sample")
@ToString(of = {"id", "compound"})
public class SRSSampleEntity extends IdentifiableEntity {

    @NotNull
    @Column(updatable = false)
    protected Instant createdAt;

    @NotNull
    @ManyToOne(optional = false)
    private SRSCompoundEntity compound;

    @NotNull
    private String nbkBatchNumber;

    @NotNull
    @Convert(converter = STRCodeSampleConverter.class)
    private STRCodeSample strCode;

    @Nullable
    private String externalNumber;

    @Nullable
    private BigDecimal density;

    @Nullable
    private BigDecimal molarity;

    @Nullable
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private MolarityUnit molarityUnit;

    @Nullable
    private BigDecimal purity;

    @Nullable
    private UUID compoundState;

    @Nullable
    private String batchComment;

    @NotNull
    @Basic(fetch = FetchType.LAZY)
    @LazyGroup("searchVector")
    @Type(SearchVectorType.class)
    @Column(name = "search_vector", columnDefinition = "tsvector")
    @ColumnTransformer(write = "calculate_tsvector(?)")
    private SearchVector searchVector;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    @JdbcTypeCode(SqlTypes.ARRAY)
    private UUID[] healthHazards;
}

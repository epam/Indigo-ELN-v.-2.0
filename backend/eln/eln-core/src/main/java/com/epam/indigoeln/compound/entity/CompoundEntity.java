package com.epam.indigoeln.compound.entity;

import com.epam.indigoeln.common.model.MolFormula;
import com.epam.indigoeln.eln.common.config.MolFormulaConverter;
import com.epam.indigoeln.eln.common.entity.IdentifiableEntity;
import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.model.SampleSource;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;
import org.jspecify.annotations.Nullable;

@Getter
@Setter
@NoArgsConstructor
@Entity(name = "Compound")
@ToString(of = {"id", "chemicalName", "formula", "canSmiles", "source", "compoundKey"})
public class CompoundEntity extends IdentifiableEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private SampleSource source;

    @Nullable
    private String compoundKey;

    @Nullable
    private String casNumber;

    @NotEmpty
    private String canSmiles;

    @NotNull
    @ManyToOne
    private DictionaryItemEntity stereoisomerCode;

    @NotNull
    @ManyToOne
    private DictionaryItemEntity saltCode;

    @Nullable
    @Column(name = "salt_eq_100")
    private Integer saltEQ100;

    @Nullable
    private String chemicalName;

    @NotNull
    @Convert(converter = MolFormulaConverter.class)
    private MolFormula formula;

    @NotNull
    private Double molWeight;

    @NotNull
    private Double exactMass;

    @NotEmpty
    @Basic(fetch = FetchType.LAZY)
    private String molFile;

    @NotNull
    @Basic(fetch = FetchType.LAZY)
    private byte[] picture;

    public CompoundEntity(SampleSource source, @Nullable String compoundKey, String canSmiles, DictionaryItemEntity stereoisomerCode, DictionaryItemEntity saltCode, @Nullable Integer saltEQ100) {
        this.source = source;
        this.compoundKey = compoundKey;
        this.canSmiles = canSmiles;
        this.stereoisomerCode = stereoisomerCode;
        this.saltCode = saltCode;
        this.saltEQ100 = saltEQ100;
    }

    @Nullable
    @Transient
    public Double getSaltEQ() {
        return saltEQ100 != null ? saltEQ100 / 100.0 : null;
    }
}

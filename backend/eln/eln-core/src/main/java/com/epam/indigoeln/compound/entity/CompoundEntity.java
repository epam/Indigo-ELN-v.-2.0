package com.epam.indigoeln.compound.entity;

import com.epam.indigoeln.eln.common.entity.IdentifiableEntity;
import com.epam.indigoeln.eln.config.hibernate.STRCodeCompoundConverter;
import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.model.CompoundExternalSource;
import com.epam.indigoeln.eln.model.STRCodeCompound;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Compound")
@ToString(of = {"id", "name", "formula", "canSmiles", "strCode"})
public class CompoundEntity extends IdentifiableEntity {

    @Nullable
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private CompoundExternalSource externalSource;

    @Nullable
    private String compoundKey;

    @Nullable
    @Convert(converter = STRCodeCompoundConverter.class)
    private STRCodeCompound strCode; // STR code for compounds registered from Indigo ELN

    @Nullable
    private String casNumber;

    @Nullable
    private String externalNumber;

    @NotEmpty
    private String canSmiles;

    @Nullable
    @ManyToOne
    private DictionaryItemEntity stereoisomerCode;

    @Nullable
    @ManyToOne
    private DictionaryItemEntity saltCode;

    @Nullable
    @Column(name = "salt_eq_100")
    private Integer saltEQ100;

    @Nullable
    private String chemicalName;

    @NotEmpty
    private String formula;

    @NotNull
    private BigDecimal molWeight;

    @NotNull
    private BigDecimal exactMass;

    @NotEmpty
    @Basic(fetch = FetchType.LAZY)
    private String molFile;

    @NotNull
    @Basic(fetch = FetchType.LAZY)
    private byte[] picture;

    @OneToMany(mappedBy = "compound") // TODO make many-to-many and store percentage in link entity
    private Set<SampleEntity> samples = new HashSet<>(0);

    @Nullable
    @Transient
    public Double getSaltEQ() {
        return saltEQ100 != null ? saltEQ100 / 100.0 : null;
    }

    public void setSaltEQ(@Nullable Double saltEQ) {
        this.saltEQ100 = saltEQ != null ? (int) (saltEQ * 100.0) : null;
    }
}

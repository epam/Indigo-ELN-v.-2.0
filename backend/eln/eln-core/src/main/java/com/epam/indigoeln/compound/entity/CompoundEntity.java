package com.epam.indigoeln.compound.entity;

import com.epam.indigoeln.eln.config.hibernate.STRCodeCompoundConverter;
import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.entity.IdentifiableEntity;
import com.epam.indigoeln.eln.entity.SaltCodeEntity;
import com.epam.indigoeln.eln.model.CompoundSource;
import com.epam.indigoeln.eln.model.STRCodeCompound;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Compound")
@ToString(of = {"id", "name", "formula", "canSmiles", "strCode"})
public class CompoundEntity extends IdentifiableEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private CompoundSource source;

    @Nullable
    @Convert(converter = STRCodeCompoundConverter.class)
    private STRCodeCompound strCode; // STR code for compounds registered from Indigo ELN

    @Nullable
    private String casNumber;

    @NotEmpty
    private String canSmiles;

    @Nullable
    @ManyToOne
    private DictionaryItemEntity stereoisomerCode;

    @Nullable
    @ManyToOne
    private SaltCodeEntity saltCode;

    @Nullable
    @Column(name = "salt_eq_100")
    private Integer saltEQ100;

    @NotEmpty
    private String formula;

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

    @OneToMany(mappedBy = "compound") // TODO make many-to-many and store percentage in link entity
    private Set<SampleEntity> samples = new HashSet<>(0);
}

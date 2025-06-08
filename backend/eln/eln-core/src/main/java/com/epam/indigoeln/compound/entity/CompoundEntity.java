package com.epam.indigoeln.compound.entity;

import com.epam.indigoeln.compound.model.CompoundSource;
import com.epam.indigoeln.eln.entity.IdentifiableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Compound")
@ToString(of = {"id", "name", "formula", "canonicalSmiles"})
public class CompoundEntity extends IdentifiableEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private CompoundSource source;

    @Column(name = "compound_key")
    private String compoundKey; // ID specific to source

    @NotEmpty
    @Column(name = "canonical_smiles")
    private String canonicalSmiles; // natural key

    @NotEmpty
    private String formula;

    @Nullable
    private String name;

    @NotNull
    @Column(name = "mol_weight")
    private Double molWeight;

    @NotEmpty
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "mol_file")
    private String molFile;

    @OneToMany(mappedBy = "compound") // TODO make many-to-many and store percentage in link entity
    private Set<SampleEntity> samples = new HashSet<>(0);
}

package com.epam.indigoeln.compound.entity;

import com.epam.indigoeln.eln.config.hibernate.NbkBatchNumberConverter;
import com.epam.indigoeln.eln.config.hibernate.STRCodeSampleConverter;
import com.epam.indigoeln.eln.entity.BaseEntity;
import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.model.NbkBatchNumber;
import com.epam.indigoeln.eln.model.STRCodeSample;
import com.epam.indigoeln.reaction.model.units.MolarityUnit;
import io.hypersistence.utils.hibernate.type.search.PostgreSQLTSVectorType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.Type;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity(name = "Sample")
@ToString(of = {"id", "compound"})
@SecondaryTable(name = "Sample_Is_Marked",
        pkJoinColumns = @PrimaryKeyJoinColumn(name = "sample_id", referencedColumnName = "id")
)
@NamedEntityGraph(
        name = "Sample.find",
        attributeNodes = {
                @NamedAttributeNode("createdBy"),
                @NamedAttributeNode("modifiedBy"),
                @NamedAttributeNode("compound"),
                @NamedAttributeNode("marked")
        }
)
public class SampleEntity extends BaseEntity {

    @NotNull
    @ManyToOne(optional = false)
    private CompoundEntity compound;

    @Nullable
    @Convert(converter = NbkBatchNumberConverter.class)
    private NbkBatchNumber nbkBatchNumber;

    @Nullable
    @Convert(converter = STRCodeSampleConverter.class)
    private STRCodeSample strCode;

    @Nullable
    private String externalNumber;

    @Nullable
    private Double density;

    @Nullable
    private Double molarity;

    @Nullable
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private MolarityUnit molarityUnit;

    @Nullable
    private Double purity;

    @Nullable
    @ManyToOne
    private DictionaryItemEntity compoundState;

    @Nullable
    private String batchComment;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    @Type(PostgreSQLTSVectorType.class)
    @Column(insertable = false, updatable = false)
    private String searchVector;

    @ManyToMany
    @JoinTable(name = "Sample_Health_Hazard", joinColumns = @JoinColumn(name = "sample_id"), inverseJoinColumns = @JoinColumn(name = "health_hazard_id"))
    private Set<DictionaryItemEntity> healthHazards = new HashSet<>(0);

    @ManyToMany
    @JoinTable(name = "Sample_Mark", joinColumns = @JoinColumn(name = "sample_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<UserEntity> markedBy = new HashSet<>(0);

    @Basic(fetch = FetchType.LAZY)
    @Column(table = "Sample_Is_Marked", updatable = false)
    @Fetch(FetchMode.SELECT)
    private Boolean marked;
}

package com.epam.indigoeln.compound.entity;

import com.epam.indigoeln.common.model.units.MolarityUnit;
import com.epam.indigoeln.eln.common.config.SearchVectorType;
import com.epam.indigoeln.eln.common.util.SearchVector;
import com.epam.indigoeln.eln.config.hibernate.NbkBatchNumberConverter;
import com.epam.indigoeln.eln.entity.BaseEntity;
import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.model.NbkBatchNumber;
import com.epam.indigoeln.eln.model.SampleSource;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.SecondaryTable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.LazyGroup;
import org.hibernate.annotations.Type;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
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

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private SampleSource source;

    @Nullable
    private String sampleKey;

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
    @ManyToOne
    private DictionaryItemEntity compoundState;

    @Nullable
    private String batchComment;

    @NotNull
    @Basic(fetch = FetchType.LAZY)
    @LazyGroup("searchVector")
    @Type(SearchVectorType.class)
    @Column(name = "search_vector", columnDefinition = "tsvector")
    @ColumnTransformer(write = "calculate_tsvector(?)")
    private SearchVector searchVector;

    @ManyToMany
    @JoinTable(name = "Sample_Health_Hazard", joinColumns = @JoinColumn(name = "sample_id"), inverseJoinColumns = @JoinColumn(name = "health_hazard_id"))
    private Set<DictionaryItemEntity> healthHazards = HashSet.newHashSet(0);

    @ManyToMany
    @JoinTable(name = "Sample_Mark", joinColumns = @JoinColumn(name = "sample_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<UserEntity> markedBy = HashSet.newHashSet(0);

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    @Column(table = "Sample_Is_Marked", updatable = false)
    @Fetch(FetchMode.SELECT)
    @LazyGroup("view")
    private Boolean marked;
}

package com.epam.indigoeln.compound.entity;

import com.epam.indigoeln.eln.config.hibernate.NotebookBatchNumberConverter;
import com.epam.indigoeln.eln.config.hibernate.STRCodeSampleConverter;
import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.entity.IdentifiableEntity;
import com.epam.indigoeln.eln.model.NotebookBatchNumber;
import com.epam.indigoeln.eln.model.STRCodeSample;
import com.epam.indigoeln.reaction.model.units.MolarityUnit;
import io.hypersistence.utils.hibernate.type.search.PostgreSQLTSVectorType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.Type;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity(name = "Sample")
@ToString(of = {"id", "compound"})
public class SampleEntity extends IdentifiableEntity {

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "compound_id")
    private CompoundEntity compound;

    @Nullable
    @Column(name = "notebook_batch_number")
    @Convert(converter = NotebookBatchNumberConverter.class)
    private NotebookBatchNumber notebookBatchNumber;

    @Nullable
    @Column(name = "str_code")
    @Convert(converter = STRCodeSampleConverter.class)
    private STRCodeSample strCode;

    @Nullable
    private Double density;

    @Nullable
    private Double molarity;

    @Nullable
    @Column(name = "molarity_unit")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private MolarityUnit molarityUnit;

    @Nullable
    private Double purity;

    @Nullable
    @ManyToOne
    @JoinColumn(name = "compound_state_id")
    private DictionaryItemEntity compoundState;

    @Nullable
    @Column(name = "batch_comment")
    private String batchComment;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    @Type(PostgreSQLTSVectorType.class)
    @Column(name = "search_vector", insertable = false, updatable = false)
    private String searchVector;

    @ManyToMany
    @JoinTable(name = "Sample_Health_Hazard", joinColumns = @JoinColumn(name = "sample_id"), inverseJoinColumns = @JoinColumn(name = "health_hazard_id"))
    private Set<DictionaryItemEntity> healthHazards = new LinkedHashSet<>(0);
}

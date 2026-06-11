package com.epam.indigoeln.assay.entity;

import com.epam.indigoeln.assay.model.PlateFormat;
import com.epam.indigoeln.eln.entity.BaseEntity;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A microtitre plate belonging to an experiment. Standard SBS formats and custom / sparse
 * layouts are both supported (sparse plates simply omit the absent {@link WellEntity} rows).
 */
@Getter
@Setter
@Entity(name = "Plate")
@ToString(of = {"id", "name"})
public class PlateEntity extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(updatable = false)
    private ExperimentEntity experiment;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    private AssayEntity assay;

    @NotEmpty
    private String name;

    @Nullable
    private String barcode;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private PlateFormat plateFormat;

    @NotNull
    private Integer rowCount;

    @NotNull
    private Integer colCount;

    @Nullable
    @Basic(fetch = FetchType.LAZY)
    @JdbcTypeCode(SqlTypes.JSON)
    @Type(JsonNodeType.class)
    private JsonNode definitionSnapshot;

    @OneToMany(mappedBy = "plate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WellEntity> wells = new ArrayList<>(0);
}

package com.epam.indigoeln.assay.entity;

import com.epam.indigoeln.assay.model.WellType;
import com.epam.indigoeln.eln.common.entity.IdentifiableEntity;
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
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;

import java.util.ArrayList;
import java.util.List;

/**
 * A single addressable well on a {@link PlateEntity}. Identified by its zero-based row/column
 * indices and a human-readable address such as {@code A1}.
 */
@Getter
@Setter
@Entity(name = "Well")
@ToString(of = {"id", "address"})
public class WellEntity extends IdentifiableEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plate_id", updatable = false)
    private PlateEntity plate;

    @NotNull
    private Integer rowIndex;

    @NotNull
    private Integer colIndex;

    @NotEmpty
    private String address;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private WellType wellType = WellType.EMPTY;

    @OneToMany(mappedBy = "well", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WellContentEntity> contents = new ArrayList<>(0);
}

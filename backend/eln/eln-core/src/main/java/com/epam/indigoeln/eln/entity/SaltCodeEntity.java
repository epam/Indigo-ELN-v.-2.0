package com.epam.indigoeln.eln.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity(name = "SaltCode")
@Table(name = "Salt_Code")
@ToString(of = {"id", "code", "ordinal", "name"})
public class SaltCodeEntity extends IdentifiableEntity {

    @NotEmpty
    private String code;

    @NotEmpty
    private String name;

    @NotNull
    private Integer charge;

    @NotNull
    @Column(name = "mol_weight")
    private Double molWeight;
}

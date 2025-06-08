package com.epam.indigoeln.compound.entity;

import com.epam.indigoeln.eln.entity.IdentifiableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity(name = "Sample")
@ToString(of = {"id", "compound"})
public class SampleEntity extends IdentifiableEntity {

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "compound_id")
    private CompoundEntity compound;

    @Column(name = "batch_number")
    private String batchNumber;
}

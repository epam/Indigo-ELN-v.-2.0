package com.epam.indigoeln.compound.entity;

import com.epam.indigoeln.eln.model.STRCodeSample;
import com.epam.indigoeln.eln.config.hibernate.STRCodeSampleConverter;
import com.epam.indigoeln.eln.entity.IdentifiableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

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
    @Column(name = "batch_number")
    private String batchNumber;

    @Nullable
    @Column(name = "str_code")
    @Convert(converter = STRCodeSampleConverter.class)
    private STRCodeSample strCode;
}

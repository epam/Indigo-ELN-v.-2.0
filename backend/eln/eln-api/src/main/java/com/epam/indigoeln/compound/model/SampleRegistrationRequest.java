package com.epam.indigoeln.compound.model;

import com.epam.indigoeln.eln.model.ComponentStateRef;
import com.epam.indigoeln.eln.model.HealthHazardRef;
import com.epam.indigoeln.eln.model.NbkBatchNumber;
import com.epam.indigoeln.reaction.model.CompoundRef;
import com.epam.indigoeln.reaction.model.units.DensityUnit;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolarityUnit;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;

@Data
@With
@AllArgsConstructor(onConstructor_ = @JsonCreator)
public class SampleRegistrationRequest {

    @NotNull
    private CompoundRef compound;

    @Nullable
    private NbkBatchNumber nbkBatchNumber;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private EnteredValue<DensityUnit> density = EnteredValue.empty();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private EnteredValue<MolarityUnit> molarity = EnteredValue.empty();

    @Nullable
    private BigDecimal purity;

    @Nullable
    @Size(min = 1)
    private List<HealthHazardRef> healthHazards;

    @Nullable
    private ComponentStateRef compoundState;

    @Nullable
    private String batchComment;

    public SampleRegistrationRequest(CompoundRef compound) {
        this.compound = compound;
    }
}

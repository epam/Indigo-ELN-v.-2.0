package com.epam.indigoeln.compound.model;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.NbkBatchNumber;
import com.epam.indigoeln.reaction.model.CompoundRef;
import com.epam.indigoeln.reaction.model.units.DensityUnit;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolarityUnit;
import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Data
@With
@AllArgsConstructor(onConstructor_ = @JsonCreator)
public class SampleRegistrationRequest {

    @NotNull
    private CompoundRef compound;

    @Nullable
    private NbkBatchNumber nbkBatchNumber;

    @Nullable
    private EnteredValue<DensityUnit> density;

    @Nullable
    private EnteredValue<MolarityUnit> molarity;

    @Nullable
    private Double purity;

    @Nullable
    private List<DictionaryItemRef> healthHazards;

    @Nullable
    private DictionaryItemRef compoundState;

    @Nullable
    private String chemicalName;

    @Nullable
    private String batchComment;

    public SampleRegistrationRequest(CompoundRef compound) {
        this.compound = compound;
    }
}

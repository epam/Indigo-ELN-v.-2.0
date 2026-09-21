package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.common.model.units.DensityUnit;
import com.epam.indigoeln.common.model.units.MolarityUnit;
import com.epam.indigoeln.common.model.units.NoUnit;
import com.epam.indigoeln.common.model.units.VolumeUnit;
import com.epam.indigoeln.eln.model.HealthHazardRef;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Data
@ToString(exclude = "row", callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(exclude = "row", callSuper = false)
public sealed abstract class ReactionSample<P extends ReactionRow> implements ExperimentNode permits ReactionInputSample, ReactionOutputSample {

    @JsonBackReference
    @Setter(AccessLevel.PACKAGE)
    protected P row;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    protected EnteredValue<DensityUnit> density = EnteredValue.empty();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    protected EnteredValue<MolarityUnit> molarity = EnteredValue.empty();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    protected EnteredValue<VolumeUnit> volume = EnteredValue.empty();

    @NotNull
    protected EnteredValue<NoUnit> purity;

    @Nullable
    protected String sampleKey;

    @NotNull
    protected List<HealthHazardRef> healthHazards = List.of();

    public void moveInto(P newParent) {
        delete();
        insertInto(newParent);
    }

    protected abstract void insertInto(P newParent);

    protected abstract void delete();
}

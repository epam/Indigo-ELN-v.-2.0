package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.epam.indigoeln.reaction.util.ExperimentModelUtil;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.jspecify.annotations.Nullable;

@Data
@ToString(exclude = "reaction")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(exclude = "reaction", callSuper = false)
public sealed abstract class ReactionRow implements ExperimentNode permits ReactionInput, ReactionOutput {

    @JsonBackReference
    protected final Reaction reaction;

    @NotNull
    @Setter(AccessLevel.PROTECTED)
    protected CompoundRef compound;

    @NotNull
    protected EnteredValue<NoUnit> eq; // = 1

    @Nullable
    protected Integer rxnPosition;

    @JsonIgnore
    @AssertTrue(message = "invalid rxnPosition")
    protected boolean isRxnPositionValid() {
        boolean rxnPositionExpected = ExperimentModelUtil.getRoleInSchema(this) != null;
        return (rxnPosition != null) == rxnPositionExpected;
    }

    public abstract void delete();
}

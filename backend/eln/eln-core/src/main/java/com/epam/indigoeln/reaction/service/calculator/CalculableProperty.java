package com.epam.indigoeln.reaction.service.calculator;

import com.epam.indigoeln.common.model.units.MeasurementUnit;
import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.EnteredValue;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ExperimentNode;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionInputSample;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.ReactionOutputSample;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@RequiredArgsConstructor
@EqualsAndHashCode(of = {"container", "modelProperty"}, callSuper = false)
public class CalculableProperty<C extends ExperimentNode, U extends MeasurementUnit> implements Calculation<U> {

    @Getter
    private final C container;
    @Getter
    private final ModelProperty<C, EnteredValue<U>> modelProperty;
    private final U unit;
    @Getter
    private final int ordinal;
    @Getter
    @Nullable
    private Formula<U> calculatedFrom;

    private EnteredValue<U> snapshot = EnteredValue.empty();
    @Nullable
    private Formula<U> snapshotCalculatedFrom;

    @Getter
    private final List<Formula<?>> downstream = new ArrayList<>();

    @Override
    public EnteredValue<U> get() {
        return checkNotNull(modelProperty.get(container));
    }

    public void setValue(EnteredValue<U> value, @Nullable Formula<U> from) {
        if (value instanceof EnteredValue.Calculated<U> v) {
            value = v.withUnit(unit);
        }
        modelProperty.set(container, value);
        this.calculatedFrom = from;
    }

    public void snapshot() {
        snapshot = get();
        snapshotCalculatedFrom = calculatedFrom;
    }

    public void revert() {
        setValue(snapshot, null);
        calculatedFrom = snapshotCalculatedFrom;
    }

    private String containerDisplayName(ExperimentNode container) {
        return switch (container) {
            case ExperimentModel m -> "model";
            case Reaction r -> "reactions." + r.getModel().getReactions().indexOf(container);
            case ReactionInput i -> containerDisplayName(i.getReaction()) + ".inputs." + i.getReaction().getInputs().indexOf(container);
            case ReactionInputSample s -> containerDisplayName(s.getRow()) + ".samples." + s.getRow().getSamples().indexOf(s);
            case ReactionOutput o -> containerDisplayName(o.getReaction()) + ".outputs." + o.getReaction().getOutputs().indexOf(container);
            case ReactionOutputSample s -> containerDisplayName(s.getRow()) + ".samples." + s.getRow().getSamples().indexOf(s);
            default -> throw new IllegalArgumentException(container.getClass().getName());
        };
    }

    public String getName() {
        return containerDisplayName(container) + '.' + modelProperty.name();
    }

    @Override
    public String toString() {
        return getName() + ": " + get();
    }
}

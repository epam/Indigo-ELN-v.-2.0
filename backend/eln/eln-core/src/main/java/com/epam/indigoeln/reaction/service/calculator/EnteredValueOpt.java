package com.epam.indigoeln.reaction.service.calculator;

import com.epam.indigoeln.reaction.metamodel.property.ModelProperty;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MeasurementUnit;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.defaultValue;
import static com.epam.indigoeln.reaction.model.units.EnteredValue.fixed;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class EnteredValueOpt<U extends MeasurementUnit> {

    public static final EnteredValueOpt<MolUnit> ZERO_MOL = opt(defaultValue(0.0, 0, MolUnit.MOL));
    public static final EnteredValueOpt<NoUnit> DEFAULT_ONE_HUNDRED = opt(defaultValue(100.0, 1, NoUnit.NO_UNIT));
    public static final EnteredValueOpt<NoUnit> ONE_HUNDREDTH = opt(fixed(0.01, 1, NoUnit.NO_UNIT));

    @Nullable
    public abstract EnteredValue<U> getValue();

    public static <U extends MeasurementUnit> EnteredValueOpt<U> opt(@Nullable EnteredValue<U> value) {
        return value != null ? new Value<>(value) : empty();
    }

    public EnteredValueOpt<U> add(EnteredValueOpt<U> other) {
        return opt(EnteredValue.add(getValue(), other.getValue()));
    }

    public EnteredValueOpt<U> subtract(EnteredValueOpt<U> other) {
        return opt(EnteredValue.subtract(this.getValue(), other.getValue()));
    }

    public <R extends MeasurementUnit> EnteredValueOpt<R> multiply(EnteredValueOpt<?> other) {
        return opt(EnteredValue.multiply(getValue(), other.getValue()));
    }

    public <R extends MeasurementUnit> EnteredValueOpt<R> divide(EnteredValueOpt<?> other) {
        return EnteredValueOpt.opt(EnteredValue.divide(getValue(), other.getValue()));
    }

    public static <U extends MeasurementUnit> EnteredValueOpt<U> empty() {
        //noinspection unchecked
        return (EnteredValueOpt<U>) Value.EMPTY;
    }

    @Override
    public String toString() {
        return getValue() != null ? getValue().toString() : "EMPTY";
    }

    public static EnteredValueOpt<MolUnit> sum(List<? extends EnteredValueOpt<MolUnit>> list) {
        EnteredValueOpt<MolUnit> sum = ZERO_MOL;
        for (EnteredValueOpt<MolUnit> item : list) {
            sum = sum.add(item);
        }
        return sum;
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode(of = {"container", "property"}, callSuper = false)
    public static class Property<C extends ExperimentNode, U extends MeasurementUnit> extends EnteredValueOpt<U> {

        @Getter
        private final C container;
        @Getter
        private final ModelProperty<C, @Nullable EnteredValue<U>> property;
        @Getter
        private final int ordinal;
        @Nullable
        private EnteredValue<U> snapshot;
        @Getter
        private final List<Formula<?>> downstream = new ArrayList<>();

        @Override
        @Nullable
        public EnteredValue<U> getValue() {
            return property.get(container);
        }

        public void setValue(@Nullable EnteredValue<U> value) {
            property.set(container, value);
        }

        public void setValueUnchecked(@Nullable EnteredValue<?> value) {
            //noinspection unchecked
            property.set(container, (EnteredValue<U>) value);
        }

        public void snapshot() {
            snapshot = getValue();
        }

        public void revert() {
            setValue(snapshot);
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
            return containerDisplayName(container) + '.' + property.name();
        }

        @Override
        public String toString() {
            return getName() + ": " + super.toString();
        }
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode(of = "value", callSuper = false)
    public static class Value<U extends MeasurementUnit> extends EnteredValueOpt<U> {

        static final EnteredValueOpt<MeasurementUnit> EMPTY = new Value<>(null);

        @Getter
        @Nullable
        private final EnteredValue<U> value;
    }
}

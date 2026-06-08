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

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.defaultValue;
import static com.epam.indigoeln.reaction.model.units.EnteredValue.fixed;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class EnteredValueOpt<U extends MeasurementUnit> {

    public static final EnteredValueOpt<MolUnit> ZERO_MOL = opt(EnteredValue.ZERO_MOL);
    public static final EnteredValueOpt<NoUnit> DEFAULT_ONE_HUNDRED = opt(defaultValue(100.0, 1, NoUnit.NO_UNIT));
    public static final EnteredValueOpt<NoUnit> ONE_HUNDREDTH = opt(fixed(0.01, 1, NoUnit.NO_UNIT));

    @Nullable
    public abstract EnteredValue<U> getValue();

    public static <U extends MeasurementUnit> EnteredValueOpt<U> opt(@Nullable EnteredValueOpt<?> left, @Nullable EnteredValueOpt<?> right, @Nullable EnteredValue<U> value) {
        return value != null ? new Value<>(left, right, value) : empty();
    }

    public static <U extends MeasurementUnit> EnteredValueOpt<U> opt(@Nullable EnteredValue<U> value) {
        return value != null ? new Value<>(null, null, value) : empty();
    }

    public static <C extends ExperimentNode, U extends MeasurementUnit> EnteredValueOpt.Property<C, U> prop(C container, ModelProperty<C, EnteredValue<U>> property, boolean canOverwrite) {
        return new Property<>(container, property, canOverwrite);
    }

    public abstract void collectInputs(Consumer<@Nullable EnteredValueOpt<?>> consumer);

    public EnteredValueOpt<U> add(EnteredValueOpt<U> other) {
        return opt(this, other, EnteredValue.add(getValue(), other.getValue()));
    }

    public EnteredValueOpt<U> subtract(EnteredValueOpt<U> other) {
        return opt(this, other, EnteredValue.subtract(this.getValue(), other.getValue()));
    }

    public <R extends MeasurementUnit> EnteredValueOpt<R> multiply(EnteredValueOpt<?> other) {
        return opt(this, other, EnteredValue.multiply(getValue(), other.getValue()));
    }

    public <R extends MeasurementUnit> EnteredValueOpt<R> divide(EnteredValueOpt<?> other) {
        return EnteredValueOpt.opt(this, other, EnteredValue.divide(getValue(), other.getValue()));
    }

    public static <U extends MeasurementUnit> EnteredValueOpt<U> empty() {
        //noinspection unchecked
        return (EnteredValueOpt<U>) Value.EMPTY;
    }

    @Override
    public String toString() {
        return getValue() != null ? getValue().toString() : "EMPTY";
    }

    public static EnteredValueOpt<MolUnit> sum(Stream<EnteredValueOpt<MolUnit>> stream) {
        AtomicReference<EnteredValueOpt<MolUnit>> sum = new AtomicReference<>(opt(null, null, EnteredValue.ZERO_MOL));
        stream.forEach(x -> {
            sum.accumulateAndGet(x, EnteredValueOpt::add);
        });
        return sum.get();
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode(of = {"container", "property"}, callSuper = false)
    public static class Property<C extends ExperimentNode, U extends MeasurementUnit> extends EnteredValueOpt<U> {

        @Getter
        private final C container;
        private final ModelProperty<C, EnteredValue<U>> property;
        @Getter
        private final boolean canOverwrite;

        @Nullable
        private EnteredValueOpt<U> lastSetValue;

        public String getName() {
            return property.name();
        }

        @Override
        @Nullable
        public EnteredValue<U> getValue() {
            return property.get(container);
        }

        public void setValue(EnteredValueOpt<U> value) {
            property.set(container, value.getValue());
            lastSetValue = value;
        }

        public void reset(boolean eraseDefault) {
            if (eraseDefault) {
                property.set(container, null);
            } else {
                property.set(container, property.defaultValue());
            }
            lastSetValue = null;
        }

        @Override
        public void collectInputs(Consumer<@Nullable EnteredValueOpt<?>> consumer) {
            consumer.accept(this);
            consumer.accept(lastSetValue);
        }

        private String containerDisplayName(ExperimentNode container) {
            return switch (container) {
                case ExperimentModel m -> "Model";
                case Reaction r -> "Reaction" + r.getModel().getReactions().indexOf(container);
                case ReactionInput i -> containerDisplayName(i.getReaction()) + "/Input" + i.getReaction().getInputs().indexOf(container);
                case ReactionInputSample s -> containerDisplayName(s.getRow()) + "/Sample" + s.getRow().getSamples().indexOf(s);
                case ReactionOutput o -> containerDisplayName(o.getReaction()) + "/Outputs" + o.getReaction().getOutputs().indexOf(container);
                case ReactionOutputSample s -> containerDisplayName(s.getRow()) + "/Samples" + s.getRow().getSamples().indexOf(s);
                default -> throw new IllegalArgumentException(container.getClass().getName());
            };
        }

        @Override
        public String toString() {
            return containerDisplayName(container) + '.' + property.name() + ": " + super.toString();
        }
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode(of = "value", callSuper = false)
    public static class Value<U extends MeasurementUnit> extends EnteredValueOpt<U> {

        static final EnteredValueOpt<MeasurementUnit> EMPTY = new Value<>(null, null, null);

        @Nullable
        private final EnteredValueOpt<?> left;
        @Nullable
        private final EnteredValueOpt<?> right;

        @Getter
        @Nullable
        private final EnteredValue<U> value;

        @Override
        public void collectInputs(Consumer<@Nullable EnteredValueOpt<?>> consumer) {
            consumer.accept(left);
            consumer.accept(right);
        }
    }
}

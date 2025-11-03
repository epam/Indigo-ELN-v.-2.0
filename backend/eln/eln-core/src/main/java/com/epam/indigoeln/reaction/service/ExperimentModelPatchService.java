package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.patch.*;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.EnteredValueSource;
import com.epam.indigoeln.reaction.model.units.MeasurementUnit;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Slf4j
@Transactional
@ApplicationScoped
@SuppressWarnings("OptionalAssignedToNull")
public class ExperimentModelPatchService {

    private static final ValueHandler<Object, Object, Object> DEFAULT_VALUE_HANDLER = new DefaultValueHandler<>();
    private static final ValueHandler<Object, EnteredValue<NoUnit>, EnteredValuePatch<NoUnit>> ENTERED_VALUE_HANDLER = new EnteredValueHandler<>();
    private static final ReactionOutputSampleValueHandler REACTION_OUTPUT_SAMPLE_VALUE_HANDLER = new ReactionOutputSampleValueHandler();
    private static final ReactionOutputValueHandler REACTION_OUTPUT_VALUE_HANDLER = new ReactionOutputValueHandler();
    private static final ReactionInputSampleValueHandler REACTION_INPUT_SAMPLE_VALUE_HANDLER = new ReactionInputSampleValueHandler();
    private static final ReactionInputValueHandler REACTION_INPUT_VALUE_HANDLER = new ReactionInputValueHandler();
    private static final ReactionValueHandler REACTION_VALUE_HANDLER = new ReactionValueHandler();
    private static final ExperimentModelValueHandler EXPERIMENT_MODEL_VALUE_HANDLER = new ExperimentModelValueHandler();

    public ExperimentModelPatch createPatch(ExperimentModel a, ExperimentModel b) {
        Flag updated = new Flag();
        //noinspection DataFlowIssue,OptionalGetWithoutIsPresent
        return EXPERIMENT_MODEL_VALUE_HANDLER.compare(updated, a, b, null).get();
    }

    public ExperimentModel applyPatch(ExperimentModel model, ExperimentModelPatch patch) {
        //noinspection DataFlowIssue
        return EXPERIMENT_MODEL_VALUE_HANDLER.apply(null, model, Optional.of(patch));
    }

    @Nullable
    private static <T> Optional<T> diff(Flag updated, @Nullable T a, @Nullable T b) {
        return diff(updated, a, b, defaultValueHandler());
    }

    @Nullable
    private static <T, P> Optional<P> diff(Flag updated, @Nullable T a, @Nullable T b, ValueHandler<?, T, P> handler) {
        return handler.compare(updated, a, b, null);
    }

    @Nullable
    private static <T, R> Optional<R> diff(Flag updated, @Nullable T a, @Nullable T b, Function<T, @Nullable R> valueFn) {
        return diff(updated, a, b, valueFn, defaultValueHandler());
    }

    @Nullable
    private static <C, T, P> Optional<P> diff(Flag updated, @Nullable C a, @Nullable C b, Function<C, @Nullable T> valueFn, ValueHandler<?, T, P> handler) {
        return handler.compare(updated, a != null ? valueFn.apply(a) : null, b != null ? valueFn.apply(b) : null, null);
    }

    private static <C, T> void restore(C target, @Nullable Optional<T> patch, BiConsumer<C, T> setterFn) {
        restore(target, patch, (x) -> null, setterFn, defaultValueHandler());
    }

    private static <C, T, P> void restore(C container, @Nullable Optional<P> patch, Function<C, @Nullable T> getterFn, BiConsumer<C, T> setterFn, ValueHandler<C, T, P> valueHandler) {
        T value = getterFn.apply(container);
        T newValue = valueHandler.apply(container, value, patch);
        if (newValue != value) {
            setterFn.accept(container, newValue);
        }
    }

    private static class ExperimentModelValueHandler extends AbstractValueHandler<Void, ExperimentModel, ExperimentModelPatch> {

        private final ListValueHandler<ExperimentModel, Reaction, Anchor.Reaction, ReactionPatch> reactionsHandler = new ListValueHandler<>(Reaction::getAnchor, REACTION_VALUE_HANDLER);

        @Override
        protected ExperimentModelPatch doCompare(Flag updated, @Nullable ExperimentModel a, ExperimentModel b, @Nullable Optional<Integer> from) {
            Preconditions.checkState(a != null);
            ExperimentModelPatch patch = new ExperimentModelPatch();
            patch.setLastUsedAnchor(diff(updated, a, b, ExperimentModel::getLastUsedAnchor));
            patch.setReactions(diff(updated, a, b, ExperimentModel::getReactions, reactionsHandler));
            updated.set();
            return patch;
        }

        @Override
        protected ExperimentModel doApply(Void container, @Nullable ExperimentModel value, ExperimentModelPatch patch) {
            Preconditions.checkState(value != null);
            restore(value, patch.getLastUsedAnchor(), ExperimentModel::setLastUsedAnchor);
            restore(value, patch.getReactions(), ExperimentModel::getReactions, ExperimentModel::setReactions, reactionsHandler);
            return value;
        }
    }

    private static class ReactionValueHandler extends AbstractListElementValueHandler<ExperimentModel, Reaction, Anchor.Reaction, ReactionPatch> {

        private final ListValueHandler<Reaction, ReactionInput, Anchor.Input, ReactionInputPatch> inputsHandler = new ListValueHandler<>(ReactionInput::getAnchor, REACTION_INPUT_VALUE_HANDLER);
        private final ListValueHandler<Reaction, ReactionOutput, Anchor.Output, ReactionOutputPatch> outputsHandler = new ListValueHandler<>(ReactionOutput::getAnchor, REACTION_OUTPUT_VALUE_HANDLER);

        @Override
        protected ReactionPatch doCompare(Flag updated, @Nullable Reaction a, Reaction b, @Nullable Optional<Integer> from) {
            ReactionPatch patch = new ReactionPatch();
            doCompareBase(updated, a, b, from, Reaction::getAnchor, patch);
            patch.setRxnFile(diff(updated, a, b, Reaction::getRxnfile));
            patch.setRxnVersion(diff(updated, a, b, Reaction::getRxnVersion));
            patch.setInputs(diff(updated, a, b, Reaction::getInputs, inputsHandler));
            patch.setOutputs(diff(updated, a, b, Reaction::getOutputs, outputsHandler));
            return patch;
        }

        @Override
        protected Reaction doApply(ExperimentModel container, @Nullable Reaction value, ReactionPatch patch) {
            if (value == null) {
                Preconditions.checkState(patch.getAnchor() != null && patch.getAnchor().isPresent());
                value = Reaction.createWithAnchor(container, patch.getAnchor().get());
            }
            restore(value, patch.getRxnFile(), Reaction::setRxnfile);
            restore(value, patch.getRxnVersion(), Reaction::setRxnVersion);
            restore(value, patch.getInputs(), Reaction::getInputs, Reaction::setInputs, inputsHandler);
            restore(value, patch.getOutputs(), Reaction::getOutputs, Reaction::setOutputs, outputsHandler);
            return value;
        }
    }

    private static class ReactionInputValueHandler extends AbstractReactionRowValueHandler<Reaction, ReactionInput, Anchor.Input, ReactionInputPatch> {

        private final ListValueHandler<ReactionInput, ReactionInputSample, Anchor.InputSample, ReactionInputSamplePatch> samplesHandler = new ListValueHandler<>(ReactionInputSample::getAnchor, REACTION_INPUT_SAMPLE_VALUE_HANDLER);

        @Override
        protected ReactionInputPatch doCompare(Flag updated, @Nullable ReactionInput a, ReactionInput b, @Nullable Optional<Integer> from) {
            ReactionInputPatch patch = new ReactionInputPatch();
            doCompareBase(updated, a, b, from, ReactionInput::getAnchor, patch);
            patch.setRole(diff(updated, a, b, ReactionInput::getRole));
            patch.setMol(diff(updated, a, b, ReactionInput::getMol, enteredValueHandler()));
            patch.setLimiting(diff(updated, a, b, ReactionInput::isLimiting));
            patch.setSamples(diff(updated, a, b, ReactionInput::getSamples, samplesHandler));
            return patch;
        }

        @Override
        protected ReactionInput doApply(Reaction container, @Nullable ReactionInput value, ReactionInputPatch patch) {
            if (value == null) {
                Preconditions.checkState(patch.getAnchor() != null && patch.getAnchor().isPresent());
                value = ReactionInput.createWithAnchor(container, patch.getAnchor().get());
            }
            doApplyBase(value, patch);
            restore(value, patch.getRole(), ReactionInput::setRole);
            restore(value, patch.getMol(), ReactionInput::getMol, ReactionInput::setMol, enteredValueHandler());
            restore(value, patch.getLimiting(), ReactionInput::setLimiting);
            restore(value, patch.getSamples(), ReactionInput::getSamples, ReactionInput::setSamples, samplesHandler);
            return value;
        }
    }

    private static class ReactionOutputValueHandler extends AbstractReactionRowValueHandler<Reaction, ReactionOutput, Anchor.Output, ReactionOutputPatch> {

        private final ListValueHandler<ReactionOutput, ReactionOutputSample, Anchor.OutputSample, ReactionOutputSamplePatch> samplesHandler = new ListValueHandler<>(ReactionOutputSample::getAnchor, REACTION_OUTPUT_SAMPLE_VALUE_HANDLER);

        @Override
        protected ReactionOutputPatch doCompare(Flag updated, @Nullable ReactionOutput a, ReactionOutput b, @Nullable Optional<Integer> from) {
            ReactionOutputPatch patch = new ReactionOutputPatch();
            doCompareBase(updated, a, b, from, ReactionOutput::getAnchor, patch);
            patch.setChemicalName(diff(updated, a, b, ReactionOutput::getChemicalName));
            patch.setType(diff(updated, a, b, ReactionOutput::getType));
            patch.setTheoMol(diff(updated, a, b, ReactionOutput::getTheoMol, enteredValueHandler()));
            patch.setTheoWeight(diff(updated, a, b, ReactionOutput::getTheoWeight, enteredValueHandler()));
            patch.setSamples(diff(updated, a, b, ReactionOutput::getSamples, samplesHandler));
            return patch;
        }

        @Override
        protected ReactionOutput doApply(Reaction container, @Nullable ReactionOutput value, ReactionOutputPatch patch) {
            if (value == null) {
                Preconditions.checkState(patch.getAnchor() != null && patch.getAnchor().isPresent());
                value = ReactionOutput.createWithAnchor(container, patch.getAnchor().get());
            }
            doApplyBase(value, patch);
            restore(value, patch.getChemicalName(), ReactionOutput::setChemicalName);
            restore(value, patch.getType(), ReactionOutput::setType);
            restore(value, patch.getTheoMol(), ReactionOutput::getTheoMol, ReactionOutput::setTheoMol, enteredValueHandler());
            restore(value, patch.getTheoWeight(), ReactionOutput::getTheoWeight, ReactionOutput::setTheoWeight, enteredValueHandler());
            restore(value, patch.getSamples(), ReactionOutput::getSamples, ReactionOutput::setSamples, samplesHandler);
            return value;
        }
    }

    private static class ReactionInputSampleValueHandler extends AbstractReactionSampleValueHandler<ReactionInput, ReactionInputSample, Anchor.InputSample, ReactionInputSamplePatch> {

        @Override
        protected ReactionInputSamplePatch doCompare(Flag updated, @Nullable ReactionInputSample a, ReactionInputSample b, @Nullable Optional<Integer> from) {
            ReactionInputSamplePatch patch = new ReactionInputSamplePatch();
            doCompareBase(updated, a, b, from, ReactionInputSample::getAnchor, patch);
            patch.setSampleId(diff(updated, a, b, ReactionInputSample::getSampleId));
            patch.setChemicalName(diff(updated, a, b, ReactionInputSample::getChemicalName));
            patch.setMol(diff(updated, a, b, ReactionInputSample::getMol, enteredValueHandler()));
            patch.setWeight(diff(updated, a, b, ReactionInputSample::getWeight, enteredValueHandler()));
            return patch;
        }

        @Override
        protected ReactionInputSample doApply(ReactionInput container, @Nullable ReactionInputSample value, ReactionInputSamplePatch patch) {
            if (value == null) {
                Preconditions.checkState(patch.getAnchor() != null && patch.getAnchor().isPresent());
                value = ReactionInputSample.createWithAnchor(container, patch.getAnchor().get());
            }
            doApplyBase(value, patch);
            restore(value, patch.getSampleId(), ReactionInputSample::setSampleId);
            restore(value, patch.getChemicalName(), ReactionInputSample::setChemicalName);
            restore(value, patch.getMol(), ReactionInputSample::getMol, ReactionInputSample::setMol, enteredValueHandler());
            restore(value, patch.getWeight(), ReactionInputSample::getWeight, ReactionInputSample::setWeight, enteredValueHandler());
            return value;
        }
    }

    private static class ReactionOutputSampleValueHandler extends AbstractReactionSampleValueHandler<ReactionOutput, ReactionOutputSample, Anchor.OutputSample, ReactionOutputSamplePatch> {

        @Override
        protected ReactionOutputSamplePatch doCompare(Flag updated, @Nullable ReactionOutputSample a, ReactionOutputSample b, @Nullable Optional<Integer> from) {
            ReactionOutputSamplePatch patch = new ReactionOutputSamplePatch();
            doCompareBase(updated, a, b, from, ReactionOutputSample::getAnchor, patch);
            patch.setNbkBatchNumber(diff(updated, a, b, ReactionOutputSample::getNbkBatchNumber));
            patch.setActualMol(diff(updated, a, b, ReactionOutputSample::getActualMol, enteredValueHandler()));
            patch.setActualWeight(diff(updated, a, b, ReactionOutputSample::getActualWeight, enteredValueHandler()));
            patch.setYield(diff(updated, a, b, ReactionOutputSample::getYield, enteredValueHandler()));
            patch.setRegistrationStatus(diff(updated, a, b, ReactionOutputSample::getRegistrationStatus));
            patch.setSampleId(diff(updated, a, b, ReactionOutputSample::getSampleId));
            patch.setHandlingPrecautions(diff(updated, a, b, ReactionOutputSample::getHandlingPrecautions));
            patch.setStorageInstructions(diff(updated, a, b, ReactionOutputSample::getStorageInstructions));
            patch.setCompoundProtection(diff(updated, a, b, ReactionOutputSample::getCompoundProtection));
            // TODO other attributes
            return patch;
        }

        @Override
        protected ReactionOutputSample doApply(ReactionOutput container, @Nullable ReactionOutputSample value, ReactionOutputSamplePatch patch) {
            if (value == null) {
                Preconditions.checkState(patch.getAnchor() != null && patch.getAnchor().isPresent());
                value = ReactionOutputSample.createWithAnchor(container, patch.getAnchor().get());
            }
            doApplyBase(value, patch);
            restore(value, patch.getNbkBatchNumber(), ReactionOutputSample::setNbkBatchNumber);
            restore(value, patch.getActualMol(), ReactionOutputSample::getActualMol, ReactionOutputSample::setActualMol, enteredValueHandler());
            restore(value, patch.getActualWeight(), ReactionOutputSample::getActualWeight, ReactionOutputSample::setActualWeight, enteredValueHandler());
            restore(value, patch.getYield(), ReactionOutputSample::getYield, ReactionOutputSample::setYield, enteredValueHandler());
            restore(value, patch.getRegistrationStatus(), ReactionOutputSample::setRegistrationStatus);
            restore(value, patch.getSampleId(), ReactionOutputSample::setSampleId);
            restore(value, patch.getHandlingPrecautions(), ReactionOutputSample::setHandlingPrecautions);
            restore(value, patch.getStorageInstructions(), ReactionOutputSample::setStorageInstructions);
            restore(value, patch.getCompoundProtection(), ReactionOutputSample::setCompoundProtection);
            // TODO other attributes
            return value;
        }
    }

    private static abstract class AbstractReactionRowValueHandler<C, T extends ReactionRow, A extends Anchor, P extends AbstractReactionRowPatch<A>> extends AbstractListElementValueHandler<C, T, A, P> {

        @Override
        protected void doCompareBase(Flag updated, @Nullable T a, T b, @Nullable Optional<Integer> from, Function<T, A> anchorFn, P patch) {
            super.doCompareBase(updated, a, b, from, anchorFn, patch);
            patch.setCompound(diff(updated, a, b, ReactionRow::getCompound));
            patch.setEq(diff(updated, a, b, ReactionRow::getEq, enteredValueHandler()));
        }

        protected void doApplyBase(T value, P patch) {
            restore(value, patch.getCompound(), ReactionRow::setCompound);
            restore(value, patch.getEq(), ReactionRow::getEq, ReactionRow::setEq, enteredValueHandler());
        }
    }

    private static abstract class AbstractReactionSampleValueHandler<C, T extends ReactionSample, A extends Anchor, P extends AbstractReactionSamplePatch<A>> extends AbstractListElementValueHandler<C, T, A, P> {

        @Override
        protected void doCompareBase(Flag updated, @Nullable T a, T b, @Nullable Optional<Integer> from, Function<T, A> anchorFn, P patch) {
            super.doCompareBase(updated, a, b, from, anchorFn, patch);
            patch.setDensity(diff(updated, a, b, ReactionSample::getDensity, enteredValueHandler()));
            patch.setMolarity(diff(updated, a, b, ReactionSample::getMolarity, enteredValueHandler()));
            patch.setVolume(diff(updated, a, b, ReactionSample::getVolume, enteredValueHandler()));
            patch.setPurity(diff(updated, a, b, ReactionSample::getPurity, enteredValueHandler()));
            patch.setStrCode(diff(updated, a, b, ReactionSample::getStrCode));
            patch.setHealthHazards(diff(updated, a, b, ReactionSample::getHealthHazards));
        }

        protected void doApplyBase(T value, P patch) {
            restore(value, patch.getDensity(), ReactionSample::getDensity, ReactionSample::setDensity, enteredValueHandler());
            restore(value, patch.getMolarity(), ReactionSample::getMolarity, ReactionSample::setMolarity, enteredValueHandler());
            restore(value, patch.getVolume(), ReactionSample::getVolume, ReactionSample::setVolume, enteredValueHandler());
            restore(value, patch.getPurity(), ReactionSample::getPurity, ReactionSample::setPurity, enteredValueHandler());
            restore(value, patch.getStrCode(), ReactionSample::setStrCode);
            restore(value, patch.getHealthHazards(), ReactionSample::setHealthHazards);
        }
    }

    private static <C, T> ValueHandler<C, T, T> defaultValueHandler() {
        //noinspection unchecked
        return (ValueHandler<C, T, T>) DEFAULT_VALUE_HANDLER;
    }

    private static <C, U extends MeasurementUnit> ValueHandler<C, EnteredValue<U>, EnteredValuePatch<U>> enteredValueHandler() {
        //noinspection unchecked,rawtypes
        return (ValueHandler) ENTERED_VALUE_HANDLER;
    }
    
    private static class Comparison<T> {

        int oldIndex = -1;
        int newIndex = -1;
        @Nullable
        T oldItem = null;
        @Nullable
        T newItem = null;
    }

    private interface PatchFn<T, P> {

        void apply(P patch, @Nullable T a, T b, Flag updated);
    }

    private interface ApplyFn<C, T, P> {

        T apply(C parent, @Nullable T target, P patch);
    }

    private interface ValueHandler<C, T, P> {

        @Nullable
        Optional<P> compare(Flag updated, @Nullable T a, @Nullable T b, @Nullable Optional<Integer> from);

        @Nullable
        T apply(C container, @Nullable T value, @Nullable Optional<P> patch);
    }

    private static abstract class AbstractValueHandler<C, T, P> implements ValueHandler<C, T, P>{

        @Nullable
        @Override
        public Optional<P> compare(Flag updated, @Nullable T a, @Nullable T b, @Nullable Optional<Integer> from) {
            Flag selfUpdated = new Flag();
            if (a == null && b == null) {
                return null;
            }
            if (b == null) {
                updated.set();
                return Optional.empty();
            }
            P patch = doCompare(selfUpdated, a, b, from);
            if (selfUpdated.isSet()) {
                updated.set();
                return Optional.of(patch);
            }
            return null;
        }

        @Nullable
        @Override
        public T apply(C container, @Nullable T value, @Nullable Optional<P> patch) {
            if (patch == null) {
                return value;
            }
            //noinspection OptionalIsPresent
            if (patch.isEmpty()) {
                return null;
            }
            return doApply(container, value, patch.get());
        }

        // outcome:
        //     updated == false -> ignore result, nothing changed
        //     otherwise -> use returned patch
        protected abstract P doCompare(Flag updated, @Nullable T a, T b, @Nullable Optional<Integer> from);

        protected abstract T doApply(C container, @Nullable T value, P patch);
    }

    private static class DefaultValueHandler<C, T> implements ValueHandler<C, T, T> {

        @Nullable
        @Override
        public Optional<T> compare(Flag updated, @Nullable T a, @Nullable T b, @Nullable Optional<Integer> from) {
            if (Objects.equals(a, b)) {
                return null;
            }
            if (b == null) {
                return Optional.empty();
            }
            updated.set();
            return Optional.of(b);
        }

        @Nullable
        @Override
        public T apply(C container, @Nullable T value, @Nullable Optional<T> patch) {
            if (patch == null) {
                return value;
            }
            //noinspection OptionalIsPresent
            if (patch.isEmpty()) {
                return null;
            }
            return patch.get();
        }
    }

    private static class EnteredValueHandler<C, U extends MeasurementUnit> extends AbstractValueHandler<C, EnteredValue<U>, EnteredValuePatch<U>> {

        @Override
        public EnteredValuePatch<U> doCompare(Flag updated, @Nullable EnteredValue<U> a, EnteredValue<U> b, @Nullable Optional<Integer> from) {
            EnteredValuePatch<U> patch = new EnteredValuePatch<>();
            patch.setValue(diff(updated, a, b, EnteredValue::getValue));
            patch.setUnit(diff(updated, a, b, EnteredValue::getUnit));
            patch.setSource(diff(updated, a, b, EnteredValue::getSource));
            patch.setConflict(diff(updated, a, b, EnteredValue::isConflict));
            return patch;
        }

        @Override
        protected EnteredValue<U> doApply(C container, @Nullable EnteredValue<U> value, EnteredValuePatch<U> patch) {
            Double v = value != null ? value.getValue() : null;
            U u = value != null ? value.getUnit() : null;
            EnteredValueSource s = value != null ? value.getSource() : null;
            if (patch.getValue() != null) {
                Preconditions.checkState(patch.getValue().isPresent());
                v = patch.getValue().get();
            }
            if (patch.getUnit() != null) {
                Preconditions.checkState(patch.getUnit().isPresent());
                u = patch.getUnit().get();
            }
            if (patch.getSource() != null) {
                s = patch.getSource().orElse(null);
            }
            Preconditions.checkState(v != null && u != null && s != null);
            EnteredValue<U> result = new EnteredValue<>(v, u, s);
            if (patch.getConflict() != null) {
                Preconditions.checkState(patch.getConflict().isPresent());
                result.setConflict(patch.getConflict().get());
            }
            return result;
        }
    }

    private static abstract class AbstractListElementValueHandler<C, T, A extends Anchor, P extends AbstractListElementPatch<A>> extends AbstractValueHandler<C, T, P> {

        protected void doCompareBase(Flag updated, @Nullable T a, T b, @Nullable Optional<Integer> from, Function<T, A> anchorFn, P patch) {
            patch.setAnchor(diff(updated, a, b, anchorFn));
            if (from != null) {
                patch.setXfrom(from);
                updated.set();
            }
        }
    }

    // (no entry): doesn't change
    // index -> patch: value updated
    // index -> {repositioned: oldIndex}: item repositioned
    // index -> {repositioned: oldIndex, ...}: item repositioned and updated
    // index -> null: item deleted
    @RequiredArgsConstructor
    private static class ListValueHandler<C, T, A extends Anchor, P extends AbstractListElementPatch<A>> extends AbstractValueHandler<C, List<T>, Map<Integer, @Nullable P>> {

        private final Function<T, A> anchorFn;
        private final AbstractListElementValueHandler<C, T, A, P> itemHandler;

        @Override
        protected Map<Integer, @Nullable P> doCompare(Flag updated, @Nullable List<T> a, List<T> b, @Nullable Optional<Integer> from) {
            Map<A, Comparison<T>> map = new HashMap<>();
            for (int i = 0; i < b.size(); i++) {
                T item = b.get(i);
                A anchor = anchorFn.apply(item);
                Comparison<T> c = map.computeIfAbsent(anchor, x -> new Comparison<>());
                c.newIndex = i;
                c.newItem = item;
            }
            if (a != null) {
                for (int i = 0; i < a.size(); i++) {
                    T item = a.get(i);
                    A anchor = anchorFn.apply(item);
                    Comparison<T> c = map.computeIfAbsent(anchor, x -> new Comparison<>());
                    c.oldIndex = i;
                    c.oldItem = item;
                }
            }
            Flag collectionUpdated = new Flag();
            Set<Integer> removed = new HashSet<>();
            Map<Integer, @Nullable P> result = new TreeMap<>();
            map.forEach((anchor, c) -> {
                Optional<Integer> itemFrom;
                if (c.oldItem == null) {
                    itemFrom = Optional.empty();
                } else if (c.newIndex != c.oldIndex) {
                    itemFrom = Optional.of(c.oldIndex);
                } else {
                    itemFrom = null;
                }
                Optional<P> patch = itemHandler.compare(collectionUpdated, c.oldItem, c.newItem, itemFrom);
                if (patch != null) {
                    if (patch.isPresent()) {
                        result.put(c.newIndex, patch.get());
                    } else {
                        removed.add(c.oldIndex);
                    }
                    updated.set();
                }
            });
            for (Integer i : removed) {
                result.putIfAbsent(i, null);
            }
            return result;
        }

        @Override
        protected List<T> doApply(C container, @Nullable List<T> value, Map<Integer, @Nullable P> patch) {
            // noinspection unchecked
            T[] source = (T[]) (value != null ? value.toArray() : new Object[0]);
            T[] target = Arrays.copyOf(source, source.length + patch.size());
            for (Map.Entry<Integer, @Nullable P> entry : patch.entrySet()) {
                int position = entry.getKey();
                P itemPatch = entry.getValue();
                if (itemPatch == null) {
                    target[position] = null;
                } else {
                    T item;
                    if (itemPatch.getXfrom() == null) {
                        item = source[position];
                    } else if (itemPatch.getXfrom().isEmpty()) {
                        item = null;
                    } else {
                        item = source[itemPatch.getXfrom().get()];
                    }
                    item = itemHandler.doApply(container, item, itemPatch);
                    target[position] = item;
                }
            }
            int lastUsedPosition = target.length - 1;
            while (lastUsedPosition >= 0 && target[lastUsedPosition] == null) {
                lastUsedPosition--;
            }
            return Lists.newArrayList(Arrays.copyOf(target, lastUsedPosition + 1));
        }
    }

    private static class Flag {

        private boolean value = false;

        void set() {
            value = true;
        }

        boolean isSet() {
            return value;
        }

        @Override
        public String toString() {
            return Boolean.toString(value);
        }
    }
}

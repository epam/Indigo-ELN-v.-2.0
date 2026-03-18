package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.entity.SaltCodeInfo;
import com.epam.indigoeln.eln.mapper.DictionaryMapper;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import com.epam.indigoeln.reaction.model.units.*;
import com.epam.indigoeln.reaction.service.mutation.experiment.AbstractExperimentMutationHandler;
import com.google.common.base.Preconditions;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.epam.indigoeln.common.exception.InvalidRequestException.fail;
import static com.epam.indigoeln.reaction.model.units.EnteredValue.DEFAULT_ONE;
import static com.epam.indigoeln.reaction.model.units.EnteredValue.DEFAULT_ONE_HUNDRED;
import static com.google.common.base.MoreObjects.firstNonNull;

public abstract class ExperimentMutationHandlerBase<T extends Mutation> extends AbstractExperimentMutationHandler<T> {

    @Inject
    Instance<IndigoAPI> indigoAPI;
    @Inject
    CompoundService compoundService;
    @Inject
    DictionaryService dictionaryService;
    @Inject
    DictionaryMapper dictionaryMapper;

    // !!! only allow non-null source for undo operations
    public <U extends MeasurementUnit> EnteredValueUndo<U> setEnteredValue(Supplier<@Nullable EnteredValue<U>> getter, Consumer<@Nullable EnteredValue<U>> setter, @Nullable String stringValue, @Nullable U unit, @Nullable EnteredValueSource source, int revisionNo) {
        EnteredValue<U> ev = getter.get();
        String oldStringValue = ev != null ? ev.getStringValue() : null;
        U oldUnit = ev != null ? ev.getUnit() : null;
        EnteredValueSource oldSource = ev != null ? ev.getSource() : null;
        if (stringValue == null) { // remove old value
            ev = null;
        } else { // create or update value
            Preconditions.checkArgument(unit != null);
            double effectiveValue = Double.parseDouble(stringValue);
            ev = new EnteredValue<>(effectiveValue, stringValue, unit, source != null ? source : EnteredValueSource.userEntered(revisionNo));
        }
        setter.accept(ev);
        return new EnteredValueUndo<>(oldStringValue, oldUnit, oldSource);
    }

    public String formatSetterSummary(String what, @Nullable String value, @Nullable MeasurementUnit unit) {
        if (value == null) {
            return "Clear %s".formatted(what);
        }
        if (unit == null) {
            return "Set %s to %s".formatted(what, value);
        }
        return "Set %s to %s %s".formatted(what, value, unit);
    }

    public String formatSetterSummary(String what, @Nullable Object value) {
        if (value == null) {
            return "Clear %s".formatted(what);
        }
        return "Set %s to %s".formatted(what, StringUtils.abbreviate(value.toString(), 100));
    }

    public String formatSetterSummary(String what, @Nullable String value) {
        if (value == null || value.isEmpty()) {
            return "Clear %s".formatted(what);
        }
        return "Set %s to %s".formatted(what, StringUtils.abbreviate(value, 100));
    }

    public String formatSetterSummary(String what, @Nullable List<DictionaryItemRef> value) {
        if (value == null || value.isEmpty()) {
            return "Clear %s".formatted(what);
        }
        if (value.size() == 1) {
            return "Set %s to [%s]".formatted(what, value.getFirst());
        }
        return "Set %s to [%s, ...]".formatted(what, value.getFirst());
    }

    public String formatSetterSummaryNoDetails(String what, boolean isPresent) {
        if (!isPresent) {
            return "Clear %s".formatted(what);
        }
        return "Updated %s".formatted(what);
    }

    @Nullable
    public SaltCodeInfo saltCodeInfo(@Nullable DictionaryItemRef ref) {
        return ref != null ? dictionaryService.getSaltInfo(ref.getId()) : null;
    }

    public Object getSampleIdentifier(SampleEntity sample) {
        if (sample.getStrCode() != null) {
            return sample.getStrCode();
        }
        return "unknown sample";
    }

    public ReactionMutation.UndoResolveInputs.RowUndo setInputLineSample(ReactionInput row, SampleEntity sample, InputSampleAnchor anchor) {
        CompoundRef oldCompound = row.getCompound();
        row.setCompound(compoundService.realCompoundRef(sample.getCompound()));

        ReactionInputSample reactionInputSample = ReactionInputSample.create(row, anchor);
        reactionInputSample.setSampleId(sample.getId());
        reactionInputSample.setStrCode(sample.getStrCode());
        reactionInputSample.setDensity(EnteredValue.defaultValue(sample.getDensity(), DensityUnit.G_ML));
        reactionInputSample.setMolarity(EnteredValue.defaultValue(sample.getMolarity(), sample.getMolarityUnit()));
        reactionInputSample.setPurity(sample.getPurity() != null ? EnteredValue.defaultValue(sample.getPurity(), NoUnit.NO_UNIT) : DEFAULT_ONE_HUNDRED);
        reactionInputSample.setHealthHazards(dictionaryMapper.itemToRefList(sample.getHealthHazards()));
        reactionInputSample.setComment(sample.getBatchComment());
        reactionInputSample.setNbkBatchNumber(sample.getNbkBatchNumber());
        List<ReactionInputSample> oldSamples = row.getSamples();
        row.setSamples(List.of(reactionInputSample));
        String oldChemicalName = row.getChemicalName();
        row.setChemicalName(sample.getCompound().getChemicalName());

        schemaAffected = true;

        return new ReactionMutation.UndoResolveInputs.RowUndo(oldCompound, oldSamples, oldChemicalName);
    }

    public ReactionInput createInputLine(Reaction reaction, @Nullable IndigoMolecule molecule, ReactionRole role, InputAnchor createdInputAnchor, InputSampleAnchor createdSampleAnchor) {
        ReactionInput row = ReactionInput.create(reaction, role, createdInputAnchor);
        row.setCompound(molecule != null
                ? compoundService.virtualCompoundRef(molecule, null, null, null)
                : compoundService.unknownCompoundRef());
        row.setEq(DEFAULT_ONE);
        ReactionInputSample reactionInputSample = ReactionInputSample.create(row, createdSampleAnchor);
        reactionInputSample.setPurity(DEFAULT_ONE_HUNDRED);
        return row;
    }

    public ReactionOutput createOutputLine(Reaction reaction, IndigoMolecule molecule, boolean intended, OutputAnchor anchor) {
        return createOutputLine(reaction, compoundService.virtualCompoundRef(molecule, null, null, null), intended, anchor);
    }

    public ReactionOutput createOutputLine(Reaction reaction, CompoundRef compound, boolean intended, OutputAnchor anchor) {
        ReactionOutput row = ReactionOutput.create(reaction, reaction.getFinalOutput() != null ? ReactionOutputType.BY_PRODUCT : ReactionOutputType.FINAL, intended, anchor);
        row.setOutputName(reaction.generateNextProductName());
        row.setCompound(compound);
        row.setEq(DEFAULT_ONE);
        return row;
    }

    protected void adjustLimitingInput(Reaction reaction) {
        ReactionInput limiting = null;
        for (ReactionInput input : reaction.getInputs()) {
            if (input.isLimiting()) {
                if (limiting == null) {
                    limiting = input;
                } else {
                    input.setLimiting(false);
                }
            }
        }
        if (limiting == null && !reaction.getInputs().isEmpty()) {
            reaction.getInputs().getFirst().setLimiting(true);
        }
    }

    protected void cleanupUnintendedProducts(Reaction reaction) {
        reaction.getOutputs().removeIf(r -> !r.isIntended() && r.getSamples().isEmpty());
    }

    @SuppressWarnings("OptionalAssignedToNull")
    protected CompoundRef doUpdateCompound(ReactionRow row, @Nullable Optional<DictionaryItemRef> saltCode, @Nullable Optional<Double> saltEQ, @Nullable Optional<DictionaryItemRef> stereoisomerCode, @Nullable String molfile) {
        switch (row.getCompound()) {
            case CompoundRef.StoredOrVirtual v -> {
                DictionaryItemRef effectiveSaltCode = saltCode != null ? saltCode.orElse(null) : v.getSaltCode();
                Double effectiveSaltEQ = saltEQ != null ? saltEQ.orElse(null) : v.getSaltEQ();
                DictionaryItemRef effectiveStereoisomerCode = stereoisomerCode != null ? stereoisomerCode.orElse(null) : v.getStereoisomerCode();
                if (effectiveSaltCode == null && saltEQ != null && saltEQ.isPresent()) {
                    fail("Cannot set saltEQ because saltCode is not set");
                }
                // normalize saltEQ
                if (effectiveSaltCode != null) {
                    effectiveSaltEQ = firstNonNull(effectiveSaltEQ, 1.0);
                } else {
                    effectiveSaltEQ = null;
                }
                CompoundEntity compound = compoundService.getCompound(v.getCompoundID());
                String effectiveMolfile = molfile != null ? molfile : compound.getMolFile();
                IndigoMolecule molecule = indigoAPI.get().loadMolecule(effectiveMolfile);
                return compoundService.virtualCompoundRef(molecule, effectiveStereoisomerCode, saltCodeInfo(effectiveSaltCode), effectiveSaltEQ);
            }
            case CompoundRef.Unknown u -> throw new InvalidRequestException("Cannot set saltCode/saltEQ/stereoisomerCode/molfile for unknown compound");
        }
    }

    protected ReactionOutput findOrCreateOutputRow(Reaction reaction, CompoundRef compound, OutputAnchor createdOutputAnchor) {
        return StreamEx.of(reaction.getOutputs())
                .filter(x -> x.getCompound().compoundKeyEquals(compound))
                .findFirst()
                .orElseGet(() -> createOutputLine(reaction, compound, false, createdOutputAnchor));
    }
}

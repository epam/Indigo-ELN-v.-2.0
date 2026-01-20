package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.SaltCodeInfo;
import com.epam.indigoeln.eln.mapper.DictionaryMapper;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import com.epam.indigoeln.reaction.model.units.*;
import com.epam.indigoeln.reaction.service.mutation.experiment.AbstractExperimentMutationHandler;
import com.google.common.base.Preconditions;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.DEFAULT_ONE;

public abstract class ExperimentMutationHandlerBase<T extends Mutation, R extends MutationRedoInfo> extends AbstractExperimentMutationHandler<T, R> {

    @Inject
    Instance<IndigoAPI> indigoAPI;
    @Inject
    CompoundService compoundService;
    @Inject
    DictionaryService dictionaryService;
    @Inject
    DictionaryMapper dictionaryMapper;

    // !!! only allow non-null source for undo operations
    public <U extends MeasurementUnit> EnteredValueUndo<U> setEnteredValue(Supplier<@Nullable EnteredValue<U>> getter, Consumer<@Nullable EnteredValue<U>> setter, @Nullable Double value, @Nullable U unit, @Nullable EnteredValueSource source) {
        EnteredValue<U> ev = getter.get();
        Double oldValue = ev != null ? ev.getValue() : null;
        U oldUnit = ev != null ? ev.getUnit() : null;
        EnteredValueSource oldSource = ev != null ? ev.getSource() : null;
        if (value == null) { // remove old value
            ev = null;
        } else { // create or update value
            Preconditions.checkArgument(unit != null);
            ev = new EnteredValue<>(value, unit, source != null ? source : EnteredValueSource.USER_LAST_ENTERED);
        }
        setter.accept(ev);
        return new EnteredValueUndo<>(oldValue, oldUnit, oldSource);
    }

    public String formatSetterSummary(String what, @Nullable Double value, @Nullable MeasurementUnit unit) {
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
        reactionInputSample.setPurity(sample.getPurity() != null ? EnteredValue.defaultValue(sample.getPurity(), NoUnit.NO_UNIT) : DEFAULT_ONE);
        reactionInputSample.setHealthHazards(dictionaryMapper.itemToRefList(sample.getHealthHazards()));
        reactionInputSample.setComment(sample.getBatchComment());
        reactionInputSample.setNbkBatchNumber(sample.getNbkBatchNumber());
        List<ReactionInputSample> oldSamples = row.getSamples();
        row.setSamples(List.of(reactionInputSample));
        String oldChemicalName = row.getChemicalName();
        row.setChemicalName(sample.getCompound().getChemicalName());

        affectedRoles.add(row.getRole());

        return new ReactionMutation.UndoResolveInputs.RowUndo(oldCompound, oldSamples, oldChemicalName);
    }

    public ReactionInput createInputLine(ExperimentEntity experiment, Reaction reaction, @Nullable IndigoMolecule molecule, ReactionRole role, @Nullable Pair<InputAnchor, InputSampleAnchor> anchors) {
        ReactionInput row = ReactionInput.create(reaction, role, anchors != null ? anchors.a() : experiment.generateNextAnchor(InputAnchor.class));
        row.setCompound(molecule != null
                ? compoundService.virtualCompoundRef(molecule, null, null, null)
                : compoundService.unknownCompoundRef());
        row.setEq(DEFAULT_ONE);
        ReactionInputSample reactionInputSample = ReactionInputSample.create(row, anchors != null ? anchors.b() : experiment.generateNextAnchor(InputSampleAnchor.class));
        reactionInputSample.setPurity(DEFAULT_ONE);
        row.setSamples(List.of(reactionInputSample));
        return row;
    }

    public ReactionOutput createOutputLine(Reaction reaction, IndigoMolecule molecule, OutputAnchor anchor) {
        ReactionOutput row = ReactionOutput.create(reaction, reaction.getFinalOutput() != null ? ReactionOutputType.BY_PRODUCT : ReactionOutputType.FINAL, anchor);
        row.setOutputName(reaction.generateNextProductName());
        row.setCompound(compoundService.virtualCompoundRef(molecule, null, null, null));
        row.setEq(DEFAULT_ONE);
        row.setSamples(List.of());
        return row;
    }

    public void adjustLimitingInput(Reaction reaction) {
        ReactionInput limiting = null;
        for (@Valid ReactionInput input : reaction.getInputs()) {
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

    public CompoundRef doApplySetSaltCodeEQStereoisomerCode(ReactionRow row, @Nullable SaltCodeInfo saltCode, @Nullable Double saltEQ, @Nullable DictionaryItemRef stereoisomerCode) {
        switch (row.getCompound()) {
            case CompoundRef.Virtual v -> {
                // normalize saltEQ
                if (saltCode != null && saltEQ == null) {
                    saltEQ = 1.0;
                } else if (saltCode == null) {
                    saltEQ = null;
                }
                CompoundEntity compound = compoundService.getCompound(v.getCompoundID());
                IndigoMolecule molecule = indigoAPI.get().loadMolecule(compound.getMolFile());
                return compoundService.virtualCompoundRef(molecule, stereoisomerCode, saltCode, saltEQ);
            }
            case CompoundRef.Stored s -> throw new InvalidRequestException("Cannot modify saltCode/saltEQ/stereoisomerCode for registered compound");
            case CompoundRef.Unknown u -> throw new InvalidRequestException("Cannot set saltCode/saltEQ/stereoisomerCode for unknown compound");
        }
    }
}

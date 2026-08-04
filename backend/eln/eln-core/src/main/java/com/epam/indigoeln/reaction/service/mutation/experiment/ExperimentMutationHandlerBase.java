package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.mapper.DictionaryMapper;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.SaltCodeRef;
import com.epam.indigoeln.eln.model.StereoisomerCodeRef;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.model.units.DensityUnit;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MeasurementUnit;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.google.common.base.Preconditions;
import jakarta.inject.Inject;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

import static com.epam.indigoeln.common.exception.InvalidRequestException.fail;
import static com.epam.indigoeln.reaction.model.units.EnteredValue.DEFAULT_ONE;
import static com.epam.indigoeln.reaction.model.units.EnteredValue.DEFAULT_ONE_HUNDRED;
import static com.google.common.base.MoreObjects.firstNonNull;

public abstract class ExperimentMutationHandlerBase<T extends ExperimentMutation> extends AbstractExperimentMutationHandler<T> {

    private static final String CLEAR_SUMMARY_FORMAT = "Clear %s";
    private static final String SET_SUMMARY_FORMAT = "Set %s to %s";

    @Inject
    CompoundService compoundService;
    @Inject
    DictionaryService dictionaryService;
    @Inject
    DictionaryMapper dictionaryMapper;

    public <U extends MeasurementUnit> void setEnteredValue(Consumer<EnteredValue<U>> setter, @Nullable String stringValue, @Nullable U unit, int revisionNo) {
        doSetEnteredValue(setter, stringValue, unit, revisionNo, EnteredValue.empty());
    }

    public <U extends MeasurementUnit> void setEnteredValue(Consumer<EnteredValue<U>> setter, @Nullable String stringValue, @Nullable U unit, int revisionNo, EnteredValue<U> defaultValue) {
        doSetEnteredValue(setter, stringValue, unit, revisionNo, defaultValue);
    }

    private <U extends MeasurementUnit> void doSetEnteredValue(Consumer<EnteredValue<U>> setter, @Nullable String stringValue, @Nullable U unit, int revisionNo, EnteredValue<U> defaultValue) {
        EnteredValue<U> ev;
        if (stringValue == null) { // remove old value
            ev = defaultValue;
        } else { // create or update value
            Preconditions.checkArgument(unit != null);
            ev = EnteredValue.userEntered(stringValue, unit, revisionNo);
        }
        setter.accept(ev);
    }

    public String formatSetterSummary(String what, @Nullable String value, @Nullable MeasurementUnit unit) {
        if (value == null) {
            return CLEAR_SUMMARY_FORMAT.formatted(what);
        }
        if (unit == null) {
            return SET_SUMMARY_FORMAT.formatted(what, value);
        }
        return "Set %s to %s %s".formatted(what, value, unit);
    }

    public String formatSetterSummary(String what, @Nullable Object value) {
        if (value == null) {
            return CLEAR_SUMMARY_FORMAT.formatted(what);
        }
        return SET_SUMMARY_FORMAT.formatted(what, StringUtils.abbreviate(value.toString(), 100));
    }

    public String formatSetterSummary(String what, @Nullable String value) {
        if (value == null || value.isEmpty()) {
            return CLEAR_SUMMARY_FORMAT.formatted(what);
        }
        return SET_SUMMARY_FORMAT.formatted(what, StringUtils.abbreviate(value, 100));
    }

    public String formatSetterSummary(String what, @Nullable List<? extends DictionaryItemRef> value) {
        if (value == null || value.isEmpty()) {
            return CLEAR_SUMMARY_FORMAT.formatted(what);
        }
        if (value.size() == 1) {
            return "Set %s to [%s]".formatted(what, value.getFirst());
        }
        return "Set %s to [%s, ...]".formatted(what, value.getFirst());
    }

    public String formatSetterSummaryNoDetails(String what, boolean isPresent) {
        if (!isPresent) {
            return CLEAR_SUMMARY_FORMAT.formatted(what);
        }
        return "Updated %s".formatted(what);
    }

    public Object getSampleIdentifier(SampleEntity sample) {
        if (sample.getStrCode() != null) {
            return sample.getStrCode();
        }
        return "unknown sample";
    }

    public void setInputLineSample(ReactionInput row, SampleEntity sample, InputSampleAnchor anchor, ExperimentMutationContext context) {
        row.setSamples(List.of()); // TODO don't remove existing samples when multi-sample support is implemented on a frontend
        row.updateCompound(compoundService.realCompoundRef(sample.getCompound()));

        ReactionInputSample reactionInputSample = ReactionInputSample.create(row, anchor);
        reactionInputSample.setSampleId(sample.getId());
        reactionInputSample.setStrCode(sample.getStrCode());
        reactionInputSample.setDensity(EnteredValue.defaultValue(sample.getDensity(), DensityUnit.G_ML));
        reactionInputSample.setMolarity(EnteredValue.defaultValue(sample.getMolarity(), sample.getMolarityUnit()));
        reactionInputSample.setPurity(sample.getPurity() != null ? EnteredValue.defaultValue(sample.getPurity(), NoUnit.NO_UNIT) : DEFAULT_ONE_HUNDRED);
        reactionInputSample.setHealthHazards(dictionaryService.get(sample.getHealthHazards()));
        reactionInputSample.setComment(sample.getBatchComment());
        reactionInputSample.setNbkBatchNumber(sample.getNbkBatchNumber());
        row.setSamples(List.of(reactionInputSample));
        row.setChemicalName(sample.getCompound().getChemicalName());
    }

    public ReactionInput createInputLine(Reaction reaction, @Nullable IndigoMolecule molecule, ReactionRole role, InputAnchor createdInputAnchor, InputSampleAnchor createdSampleAnchor) {
        CompoundRef compound = molecule != null
                ? compoundService.virtualCompoundRef(molecule, null, null, null)
                : compoundService.unknownCompoundRef();
        ReactionInput row = ReactionInput.create(reaction, role, createdInputAnchor, compound);
        row.setEq(DEFAULT_ONE);
        ReactionInputSample reactionInputSample = ReactionInputSample.create(row, createdSampleAnchor);
        reactionInputSample.setPurity(DEFAULT_ONE_HUNDRED);
        return row;
    }

    public ReactionOutput createOutputLine(Reaction reaction, IndigoMolecule molecule, boolean intended, OutputAnchor anchor) {
        CompoundRef compound = compoundService.virtualCompoundRef(molecule, null, null, null);
        return ReactionOutput.create(reaction, reaction.getFinalOutput() != null ? ReactionOutputType.BY_PRODUCT : ReactionOutputType.FINAL, intended, reaction.generateNextProductName(), anchor, compound, DEFAULT_ONE);
    }

    protected void cleanupUnintendedProducts(Reaction reaction) {
        reaction.setOutputs(StreamEx.of(reaction.getOutputs()).remove(r -> !r.isIntended() && r.getSamples().isEmpty()).toImmutableList());
    }

    protected CompoundRef doUpdateSaltCode(ReactionRow row, @Nullable SaltCodeRef saltCode) {
        return doUpdateCompound(row, CompoundField.SALT_CODE, saltCode, null, null, null);
    }

    protected CompoundRef doUpdateSaltEQ(ReactionRow row, @Nullable Double saltEQ) {
        return doUpdateCompound(row, CompoundField.SALT_EQ, null, saltEQ, null, null);
    }

    protected CompoundRef doUpdateStereoisomerCode(ReactionRow row, @Nullable StereoisomerCodeRef stereoisomerCode) {
        return doUpdateCompound(row, CompoundField.STEREOISOMER_CODE, null, null, stereoisomerCode, null);
    }

    protected CompoundRef doUpdateMolfile(ReactionRow row, @Nullable String molfile) {
        return doUpdateCompound(row, CompoundField.MOLFILE, null, null, null, molfile);
    }

    private CompoundRef doUpdateCompound(ReactionRow row, CompoundField field, @Nullable SaltCodeRef saltCode, @Nullable Double saltEQ, @Nullable StereoisomerCodeRef stereoisomerCode, @Nullable String molfile) {
        switch (row.getCompound()) {
            case CompoundRef.StoredOrVirtual v -> {
                SaltCodeRef effectiveSaltCode = updatedValue(field, CompoundField.SALT_CODE, saltCode, v.getSaltCode());
                Double effectiveSaltEQ = updatedValue(field, CompoundField.SALT_EQ, saltEQ, v.getSaltEQ());
                StereoisomerCodeRef effectiveStereoisomerCode = updatedValue(field, CompoundField.STEREOISOMER_CODE, stereoisomerCode, v.getStereoisomerCode());
                if (effectiveSaltCode == null && field == CompoundField.SALT_EQ && saltEQ != null) {
                    fail("Cannot set saltEQ because saltCode is not set");
                }
                // normalize saltEQ
                if (effectiveSaltCode != null) {
                    effectiveSaltEQ = firstNonNull(effectiveSaltEQ, 1.0);
                } else {
                    effectiveSaltEQ = null;
                }
                CompoundEntity compound = compoundService.getCompound(v.getCompoundID());
                String effectiveMolfile = field == CompoundField.MOLFILE && molfile != null ? molfile : compound.getMolFile();
                IndigoMolecule molecule = indigoAPI.loadMolecule(effectiveMolfile);
                return compoundService.virtualCompoundRef(molecule, effectiveStereoisomerCode, effectiveSaltCode, effectiveSaltEQ);
            }
            case CompoundRef.Unknown u -> {
                if (molfile != null) {
                    IndigoMolecule molecule = indigoAPI.loadMolecule(molfile);
                    return compoundService.virtualCompoundRef(molecule, null, null, null);
                }
                throw new InvalidRequestException("Cannot set saltCode/saltEQ/stereoisomerCode for unknown compound");
            }
        }
    }

    private <V> @Nullable V updatedValue(CompoundField field, CompoundField updatedField, @Nullable V value, @Nullable V previousValue) {
        return field == updatedField ? value : previousValue;
    }

    private enum CompoundField {
        SALT_CODE,
        SALT_EQ,
        STEREOISOMER_CODE,
        MOLFILE
    }

    protected ReactionOutput findOrCreateOutputRow(Reaction reaction, CompoundRef compound, OutputAnchor createdOutputAnchor) {
        return StreamEx.of(reaction.getOutputs())
                .filter(x -> x.getCompound().compoundKeyEquals(compound))
                .findFirst()
                .orElseGet(() -> ReactionOutput.create(reaction, reaction.getFinalOutput() != null ? ReactionOutputType.BY_PRODUCT : ReactionOutputType.FINAL, false, reaction.generateNextProductName(), createdOutputAnchor, compound, DEFAULT_ONE));
    }
}

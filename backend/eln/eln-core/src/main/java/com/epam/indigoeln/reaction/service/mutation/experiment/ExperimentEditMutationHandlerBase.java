package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.common.model.units.DensityUnit;
import com.epam.indigoeln.common.model.units.MeasurementUnit;
import com.epam.indigoeln.common.model.units.NoUnit;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.compound.service.search.SampleSearchService;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.eln.mapper.DictionaryMapper;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.model.BuiltInDictionary;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.ExperimentStatus;
import com.epam.indigoeln.eln.model.SaltCodeRef;
import com.epam.indigoeln.eln.model.SampleSource;
import com.epam.indigoeln.eln.model.StereoisomerCodeRef;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.reaction.model.CompoundRef;
import com.epam.indigoeln.reaction.model.EnteredValue;
import com.epam.indigoeln.reaction.model.InputAnchor;
import com.epam.indigoeln.reaction.model.InputSampleAnchor;
import com.epam.indigoeln.reaction.model.OutputAnchor;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.ReactionInputSample;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.ReactionOutputType;
import com.epam.indigoeln.reaction.model.ReactionRole;
import com.epam.indigoeln.reaction.model.ReactionRow;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.google.common.base.Preconditions;
import jakarta.inject.Inject;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static com.epam.indigoeln.common.exception.InvalidRequestException.fail;
import static com.epam.indigoeln.reaction.model.EnteredValue.DEFAULT_ONE;
import static com.epam.indigoeln.reaction.model.EnteredValue.DEFAULT_ONE_HUNDRED;
import static com.google.common.base.MoreObjects.firstNonNull;

public abstract class ExperimentEditMutationHandlerBase<T extends ExperimentMutation> extends AbstractExperimentMutationHandler<T> {

    private static final String CLEAR_SUMMARY_FORMAT = "Clear %s";
    private static final String SET_SUMMARY_FORMAT = "Set %s to %s";

    @Inject
    CompoundService compoundService;
    @Inject
    SampleSearchService sampleSearchService;
    @Inject
    DictionaryService dictionaryService;
    @Inject
    DictionaryMapper dictionaryMapper;

    @Override
    protected void doValidateAccess(ExperimentEntity entity) {
        aclService.ensureAccess(entity, ApplicationPermission.EDIT_EXPERIMENTS);
    }

    @Override
    protected void doValidateStatus(ExperimentEntity entity) {
        ensureStatus(entity, ExperimentStatus.OPEN, ExperimentStatus.REOPEN);
    }

    public <U extends MeasurementUnit> void setEnteredValue(EnteredValue<U> current, Consumer<EnteredValue<U>> setter, @Nullable String stringValue, @Nullable U unit, int revisionNo) {
        doSetEnteredValue(current, setter, stringValue, unit, revisionNo, EnteredValue.empty());
    }

    public <U extends MeasurementUnit> void setEnteredValue(EnteredValue<U> current, Consumer<EnteredValue<U>> setter, @Nullable String stringValue, @Nullable U unit, int revisionNo, EnteredValue<U> defaultValue) {
        doSetEnteredValue(current, setter, stringValue, unit, revisionNo, defaultValue);
    }

    private <U extends MeasurementUnit> void doSetEnteredValue(EnteredValue<U> current, Consumer<EnteredValue<U>> setter, @Nullable String stringValue, @Nullable U unit, int revisionNo, EnteredValue<U> defaultValue) {
        EnteredValue<U> ev;
        if (stringValue == null) {
            if (!current.isEmpty() && (current.getSource().isDefault() || (current.getSource().isCalculated() && defaultValue.isEmpty()))) {
                ev = EnteredValue.cleared(revisionNo);
            } else {
                ev = defaultValue;
            }
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

    public String formatSetterSummary(String what, Collection<? extends DictionaryItemRef> value) {
        if (value.isEmpty()) {
            return CLEAR_SUMMARY_FORMAT.formatted(what);
        }
        if (value.size() == 1) {
            return "Set %s to [%s]".formatted(what, value.iterator().next());
        }
        return "Set %s to [%s, ...]".formatted(what, value.iterator().next());
    }

    public String formatSetterSummaryNoDetails(String what, boolean isPresent) {
        if (!isPresent) {
            return CLEAR_SUMMARY_FORMAT.formatted(what);
        }
        return "Updated %s".formatted(what);
    }

    public ReactionInputSample addInputSample(ReactionInput row, CompoundEntity compound, SampleDTO sample, InputSampleAnchor anchor) {
        EnteredValue<NoUnit> purity = sample.getPurity() != null ? EnteredValue.defaultValue(sample.getPurity(), NoUnit.NO_UNIT) : DEFAULT_ONE_HUNDRED;
        ReactionInputSample reactionInputSample = ReactionInputSample.create(row, anchor, sample.getSource(), sample.getSampleKey(), purity);
        reactionInputSample.setDensity(EnteredValue.defaultValue(sample.getDensity(), DensityUnit.G_ML));
        reactionInputSample.setMolarity(EnteredValue.defaultValue(sample.getMolarity(), sample.getMolarityUnit()));
        reactionInputSample.setHealthHazards(sample.getHealthHazards());
        reactionInputSample.setComment(sample.getBatchComment());
        reactionInputSample.setNbkBatchNumber(sample.getNbkBatchNumber());
        row.setChemicalName(compound.getChemicalName());
        return reactionInputSample;
    }

    public void resolveInputSample(ReactionInput row, SampleDTO sample, InputSampleAnchor anchor) {
        CompoundEntity compound = sampleSearchService.importCompound(sample);
        ReactionInputSample virtualSample = StreamEx.of(row.getSamples()).findFirst(s -> s.getSampleSource() == SampleSource.VIRTUAL).orElse(null);
        row.setSamples(row.getSamples().stream().filter(s -> s.getSampleSource() != SampleSource.VIRTUAL).toList());

        CompoundRef compoundRef = compoundService.compoundRef(compound);
        if (!row.getCompound().compoundKeyEquals(compoundRef)) {
            row.updateCompound(compoundRef);
        }

        ReactionInputSample resolvedSample = addInputSample(row, compound, sample, anchor);
        if (virtualSample != null) {
            transferUserEnteredValues(virtualSample, resolvedSample);
        }
    }

    private static void transferUserEnteredValues(ReactionInputSample from, ReactionInputSample to) {
        transferUserEnteredValue(from.getMol(), to::setMol);
        transferUserEnteredValue(from.getWeight(), to::setWeight);
        transferUserEnteredValue(from.getVolume(), to::setVolume);
        transferUserEnteredValue(from.getDensity(), to::setDensity);
        transferUserEnteredValue(from.getMolarity(), to::setMolarity);
        transferUserEnteredValue(from.getPurity(), to::setPurity);
        if (!from.getHealthHazards().isEmpty()) {
            to.setHealthHazards(from.getHealthHazards());
        }
        if (from.getComment() != null) {
            to.setComment(from.getComment());
        }
    }

    private static <U extends MeasurementUnit> void transferUserEnteredValue(EnteredValue<U> value, Consumer<EnteredValue<U>> setter) {
        if (value.getSource().isUserEntered()) {
            setter.accept(value);
        }
    }

    protected void updateInputRowCompound(Reaction reaction, ReactionInput row, CompoundRef compound) {
        StreamEx.of(reaction.getInputs())
                .findFirst(x -> x != row && x.getRole() == row.getRole() && x.getCompound().compoundKeyEquals(compound))
                .ifPresent(other -> {
                    other.getSamples().forEach(s -> s.moveInto(row));
                    if (other.isLimiting()) {
                        reaction.setLimitingAnchor(row.getAnchor());
                    }
                    other.delete();
                });
        row.updateCompound(compound);
    }

    protected void updateOutputRowCompound(Reaction reaction, ReactionOutput row, CompoundRef compound) {
        StreamEx.of(reaction.getOutputs())
                .findFirst(x -> x != row && x.getCompound().compoundKeyEquals(compound))
                .ifPresent(other -> {
                    other.getSamples().forEach(s -> s.moveInto(row));
                    row.setIntended(row.isIntended() || other.isIntended());
                    other.delete();
                });
        row.updateCompound(compound);
    }

    public ReactionInput createInputLine(Reaction reaction, @Nullable IndigoMolecule molecule, ReactionRole role, InputAnchor createdInputAnchor) {
        CompoundRef compound = molecule != null
                ? compoundService.compoundRef(molecule)
                : compoundService.unknownCompoundRef();
        ReactionInput row = ReactionInput.create(reaction, role, createdInputAnchor, compound);
        row.setEq(DEFAULT_ONE);
        return row;
    }

    public ReactionOutput createOutputLine(Reaction reaction, IndigoMolecule molecule, boolean intended, OutputAnchor anchor) {
        CompoundRef compound = compoundService.compoundRef(molecule);
        return ReactionOutput.create(reaction, reaction.getFinalOutput() != null ? ReactionOutputType.BY_PRODUCT : ReactionOutputType.FINAL, intended, reaction.generateNextProductName(), anchor, compound, DEFAULT_ONE);
    }

    protected void cleanupUnintendedProducts(Reaction reaction) {
        reaction.setOutputs(StreamEx.of(reaction.getOutputs()).remove(r -> !r.isIntended() && r.getSamples().isEmpty()).toImmutableList());
    }

    protected CompoundRef doUpdateSaltCode(ReactionRow row, SaltCodeRef saltCode) {
        return doUpdateCompound(row, CompoundField.SALT_CODE, saltCode, null, null, null);
    }

    protected CompoundRef doUpdateSaltEQ(ReactionRow row, @Nullable BigDecimal saltEQ) {
        return doUpdateCompound(row, CompoundField.SALT_EQ, null, saltEQ, null, null);
    }

    protected CompoundRef doUpdateStereoisomerCode(ReactionRow row, StereoisomerCodeRef stereoisomerCode) {
        return doUpdateCompound(row, CompoundField.STEREOISOMER_CODE, null, null, stereoisomerCode, null);
    }

    protected CompoundRef doUpdateMolfile(ReactionRow row, @Nullable String molfile) {
        return doUpdateCompound(row, CompoundField.MOLFILE, null, null, null, molfile);
    }

    private CompoundRef doUpdateCompound(ReactionRow row, CompoundField field, @Nullable SaltCodeRef saltCode, @Nullable BigDecimal saltEQ, @Nullable StereoisomerCodeRef stereoisomerCode, @Nullable String molfile) {
        CompoundRef ref = row.getCompound();
        if (row.getCompound().getCompoundID() != null) {
            SaltCodeRef effectiveSaltCode = updatedValue(field, CompoundField.SALT_CODE, saltCode, ref.getSaltCode());
            BigDecimal effectiveSaltEQ = updatedValue(field, CompoundField.SALT_EQ, saltEQ, ref.getSaltEQ());
            StereoisomerCodeRef effectiveStereoisomerCode = updatedValue(field, CompoundField.STEREOISOMER_CODE, stereoisomerCode, ref.getStereoisomerCode());
            boolean parentStructure = effectiveSaltCode.equals(dictionaryService.getDefault(BuiltInDictionary.SALT_CODE));
            if (parentStructure && field == CompoundField.SALT_EQ && saltEQ != null) {
                fail("Cannot set saltEQ because saltCode is not set");
            }
            // normalize saltEQ
            if (!parentStructure) {
                effectiveSaltEQ = firstNonNull(effectiveSaltEQ, BigDecimal.ONE);
            } else {
                effectiveSaltEQ = null;
            }
            CompoundEntity compound = compoundService.getCompound(ref.getCompoundID());
            String effectiveMolfile = field == CompoundField.MOLFILE && molfile != null ? molfile : compound.getMolFile();
            IndigoMolecule molecule = indigoAPI.loadMolecule(effectiveMolfile);
            return compoundService.compoundRef(molecule, effectiveStereoisomerCode, effectiveSaltCode, effectiveSaltEQ);
        } else {
            if (molfile != null) {
                IndigoMolecule molecule = indigoAPI.loadMolecule(molfile);
                return compoundService.compoundRef(molecule);
            }
            throw new InvalidRequestException("Cannot set saltCode/saltEQ/stereoisomerCode for unknown compound");
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

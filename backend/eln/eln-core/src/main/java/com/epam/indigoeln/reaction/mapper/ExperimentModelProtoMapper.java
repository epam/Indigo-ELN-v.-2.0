package com.epam.indigoeln.reaction.mapper;

import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.model.NbkBatchNumber;
import com.epam.indigoeln.eln.model.STRCodeCompound;
import com.epam.indigoeln.eln.model.STRCodeSample;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.EnteredValueSource;
import com.epam.indigoeln.reaction.model.units.MeasurementUnit;
import com.epam.indigoeln.reaction.proto.*;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(
        componentModel = "cdi",
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public abstract class ExperimentModelProtoMapper {

    @IgnoreProtobufFields
    @Mapping(target = "reactionsList", source = "reactions")
    @Mapping(target = "removeReactions", ignore = true)
    @Mapping(target = "reactionsOrBuilderList", ignore = true)
    @Mapping(target = "reactionsBuilderList", ignore = true)
    public abstract ExperimentModelProto.Builder toProto(ExperimentModel model, @MappingTarget ExperimentModelProto.Builder builder);

    @IgnoreProtobufFields
    @Mapping(target = "inputsList", source = "inputs")
    @Mapping(target = "inputsOrBuilderList", ignore = true)
    @Mapping(target = "inputsBuilderList", ignore = true)
    @Mapping(target = "removeInputs", ignore = true)
    @Mapping(target = "outputsList", source = "outputs")
    @Mapping(target = "removeOutputs", ignore = true)
    @Mapping(target = "outputsOrBuilderList", ignore = true)
    @Mapping(target = "outputsBuilderList", ignore = true)
    @Mapping(target = "rxnfileBytes", ignore = true)
    public abstract ReactionProto.Builder toProto(Reaction model, @MappingTarget ReactionProto.Builder builder);
    @IgnoreProtobufFields
    @Mapping(target = "samplesList", source = "samples")
    public abstract ReactionInputProto.Builder toProto(ReactionInput model, @MappingTarget ReactionInputProto.Builder builder);
    @IgnoreProtobufFields
    public abstract ReactionInputSampleProto.Builder toProto(ReactionInputSample model, @MappingTarget ReactionInputSampleProto.Builder builder);
    @IgnoreProtobufFields
    public abstract ReactionOutputProto.Builder toProto(ReactionOutput model, @MappingTarget ReactionOutputProto.Builder builder);
    @IgnoreProtobufFields
    public abstract ReactionOutputSampleProto.Builder toProto(ReactionOutputSample model, @MappingTarget ReactionOutputSampleProto.Builder builder);

    @Mapping(target = "lastUsedAnchorCached", ignore = true)
    @Mapping(target = "reactions", source = "reactionsList")
    public abstract ExperimentModel toModel(ExperimentModelProto proto);
    @Mapping(target = "model", ignore = true)
    @Mapping(target = "inputs", source = "inputsList")
    @Mapping(target = "outputs", source = "outputsList")
    public abstract Reaction toModel(ReactionProto proto);
    @Mapping(target = "reaction", ignore = true)
    @Mapping(target = "samples", source = "samplesList")
    public abstract ReactionInput toModel(ReactionInputProto proto);
    @Mapping(target = "row", ignore = true)
    @Mapping(target = "healthHazards", source = "healthHazardsList")
    public abstract ReactionInputSample toModel(ReactionInputSampleProto proto);
    @Mapping(target = "reaction", ignore = true)
    @Mapping(target = "samples", source = "samplesList")
    public abstract ReactionOutput toModel(ReactionOutputProto proto);

    @Mapping(target = "row", ignore = true)
    @Mapping(target = "compoundProtection", source = "compoundProtectionList")
    @Mapping(target = "storageInstructions", source = "storageInstructionsList")
    @Mapping(target = "solubilityInSolvents", source = "solubilityInSolventsList")
    @Mapping(target = "residualSolvents", source = "residualSolventsList")
    @Mapping(target = "purityCalculations", source = "purityCalculationsList")
    @Mapping(target = "healthHazards", source = "healthHazardsList")
    @Mapping(target = "handlingPrecautions", source = "handlingPrecautionsList")
    @Mapping(target = "precursorReactantIds", ignore = true)
    public abstract ReactionOutputSample toModel(ReactionOutputSampleProto proto);

    protected int mapAnchor(Anchor anchor) {
        return anchor.getNumber();
    }

    protected Anchor.Reaction mapReactionAnchor(int number) {
        return new Anchor.Reaction(number);
    }

    protected Anchor.Input mapReactionInputAnchor(int number) {
        return new Anchor.Input(number);
    }

    protected Anchor.InputSample mapReactionInputSampleAnchor(int number) {
        return new Anchor.InputSample(number);
    }

    protected Anchor.Output mapReactionOutputAnchor(int number) {
        return new Anchor.Output(number);
    }

    protected Anchor.OutputSample mapReactionOutputSampleAnchor(int number) {
        return new Anchor.OutputSample(number);
    }

    protected String mapSTRCodeCompound(STRCodeCompound strCode) {
        return strCode.toString();
    }

    protected STRCodeCompound mapSTRCodeCompound(String str) {
        return STRCodeCompound.parse(str);
    }

    protected String mapSTRCodeSample(STRCodeSample strCode) {
        return strCode.toString();
    }

    protected STRCodeSample mapSTRCodeSample(String str) {
        return STRCodeSample.parse(str);
    }

    protected String mapNbkBatchNumber(NbkBatchNumber nbkBatchNumber) {
        return nbkBatchNumber.toString();
    }

    protected NbkBatchNumber mapNbkBatchNumber(String nbkBatchNumber) {
        return NbkBatchNumber.parse(nbkBatchNumber);
    }

    protected CompoundRefProto mapCompoundRef(CompoundRef ref) {
        CompoundRefProto.Builder builder = CompoundRefProto.newBuilder();
        if (ref.getCompoundID() != null) {
            builder.setCompoundID(mapUUID(ref.getCompoundID()));
        }
        if (ref.getFormula() != null) {
            builder.setFormula(ref.getFormula());
        }
        if (ref.getStereoisomerCode() != null) {
            builder.setStereoisomerCode(toProto(ref.getStereoisomerCode()));
        }
        if (ref.getSaltCode() != null) {
            builder.setSaltCode(toProto(ref.getSaltCode()));
        }
        if (ref.getSaltEQ() != null) {
            builder.setSaltEQ(ref.getSaltEQ());
        }
        if (ref.getStrCode() != null) {
            builder.setStrCode(mapSTRCodeCompound(ref.getStrCode()));
        }
        if (ref.getMolWeight() != null) {
            builder.setMolWeight(mapEnteredValue(ref.getMolWeight()));
        }
        if (ref.getExactMass() != null) {
            builder.setExactMass(ref.getExactMass());
        }
        if (ref.getCasNumber() != null) {
            builder.setCasNumber(ref.getCasNumber());
        }
        switch (ref) {
            case CompoundRef.Stored stored -> {
                builder.setType(CompoundRefTypeProto.COMPOUND_TYPE_STORED);
            }
            case CompoundRef.Virtual virtual -> {
                builder.setType(CompoundRefTypeProto.COMPOUND_TYPE_VIRTUAL);
            }
            case CompoundRef.Unknown unknown -> {
                builder.setType(CompoundRefTypeProto.COMPOUND_TYPE_UNKNOWN);
            }
        };
        return builder.build();
    }

    protected CompoundRef mapCompoundRef(CompoundRefProto proto) {
        return switch (proto.getType()) {
            case COMPOUND_TYPE_STORED -> {
                yield new CompoundRef.Stored(
                        mapUUID(proto.getCompoundID()),
                        mapEnteredValue(proto.getMolWeight()),
                        proto.getExactMass(),
                        proto.getFormula(),
                        mapSTRCodeCompound(proto.getStrCode()),
                        proto.getCasNumber()
                );
            }
            case COMPOUND_TYPE_VIRTUAL -> {
                yield new CompoundRef.Virtual(
                        mapUUID(proto.getCompoundID()),
                        proto.getFormula(),
                        toModel(proto.getStereoisomerCode()),
                        toModel(proto.getSaltCode()),
                        proto.hasSaltEQ() ? proto.getSaltEQ() : null,
                        mapEnteredValue(proto.getMolWeight()),
                        proto.getExactMass(),
                        proto.getCasNumber()
                );
            }
            case COMPOUND_TYPE_UNKNOWN -> {
                CompoundRef.Unknown unknown = new CompoundRef.Unknown();
                unknown.setFormula(proto.getFormula());
                unknown.setMolWeight(mapEnteredValue(proto.getMolWeight()));
                yield unknown;
            }
            default -> {
                throw new IllegalStateException("Unknown CompoundRef type: " + proto.getType());
            }
        };
    }

    @IgnoreProtobufFields
    protected abstract DictionaryItemRefProto toProto(DictionaryItemRef ref);
    protected abstract DictionaryItemRef toModel(DictionaryItemRefProto proto);

    @IgnoreProtobufFields
    protected abstract SaltCodeRefProto toProto(SaltCodeRef ref);
    protected abstract SaltCodeRef toModel(SaltCodeRefProto proto);

    protected String mapUUID(UUID uuid) {
        return uuid.toString();
    }

    protected UUID mapUUID(String str) {
        return UUID.fromString(str);
    }

    protected EnteredValueProto mapEnteredValue(EnteredValue<?> enteredValue) {
        return EnteredValueProto.newBuilder()
                .setValue(enteredValue.getValue())
                .setUnit(MeasurementUnitProto.valueOf(enteredValue.getUnit().name()))
                .setSource(EnteredValueSourceProto.valueOf(enteredValue.getSource().name()))
                .setConflict(enteredValue.isConflict())
                .build();
    }

    protected <U extends MeasurementUnit> EnteredValue<U> mapEnteredValue(EnteredValueProto proto) {
        //noinspection unchecked
        EnteredValue<U> enteredValue = new EnteredValue<>(proto.getValue(), (U) MeasurementUnit.getUnit(proto.getUnit().name()), EnteredValueSource.valueOf(proto.getSource().name()));
        enteredValue.setConflict(proto.getConflict());
        return enteredValue;
    }

    protected ReactionRoleProto mapReactionRole(ReactionRole role) {
        return ReactionRoleProto.valueOf(role.name());
    }

    protected ReactionOutputTypeProto mapReactionOutputType(ReactionOutputType type) {
        return ReactionOutputTypeProto.valueOf(type.name());
    }
}

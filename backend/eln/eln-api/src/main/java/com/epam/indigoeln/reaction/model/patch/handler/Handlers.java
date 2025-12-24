package com.epam.indigoeln.reaction.model.patch.handler;

import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.metamodel.Metamodel;
import com.epam.indigoeln.reaction.model.metamodel.ValueHandler;
import com.epam.indigoeln.reaction.model.patch.*;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MeasurementUnit;
import com.epam.indigoeln.reaction.model.units.NoUnit;

import java.util.function.Consumer;

public class Handlers {

    private static final DefaultValueHandler<Object, Object> DEFAULT = new DefaultValueHandler<>();

    private static final EnteredValueHandler<Object, NoUnit> ENTERED_VALUE = new EnteredValueHandler<>();

    public static final Metamodel<ReactionInputSample, ReactionInputSamplePatch> INPUT_SAMPLE_METAMODEL = createMetamodel(ReactionInputSample::buildMetamodel);

    public static final MetamodelListItemValueHandler<ReactionInput, ReactionInputSample, Anchor.InputSample, ReactionInputSamplePatch> REACTION_INPUT_SAMPLE = new MetamodelListItemValueHandler<>(
            INPUT_SAMPLE_METAMODEL,
            ReactionInputSamplePatch::new,
            ReactionInputSample::createWithAnchor
    );

    public static final ListValueHandler<ReactionInput, ReactionInputSample, Anchor.InputSample, ReactionInputSamplePatch> REACTION_INPUT_SAMPLE_LIST = new ListValueHandler<>(
            ReactionInputSample::getAnchor,
            REACTION_INPUT_SAMPLE
    );

    public static final Metamodel<ReactionInput, ReactionInputPatch> INPUT_METAMODEL = createMetamodel(ReactionInput::buildMetamodel);

    public static final MetamodelListItemValueHandler<Reaction, ReactionInput, Anchor.Input, ReactionInputPatch> REACTION_INPUT = new MetamodelListItemValueHandler<>(
            INPUT_METAMODEL,
            ReactionInputPatch::new,
            ReactionInput::createWithAnchor
    );

    public static final ListValueHandler<Reaction, ReactionInput, Anchor.Input, ReactionInputPatch> REACTION_INPUT_LIST = new ListValueHandler<>(
            ReactionInput::getAnchor,
            REACTION_INPUT
    );

    public static final Metamodel<ReactionOutputSample, ReactionOutputSamplePatch> OUTPUT_SAMPLE_METAMODEL = createMetamodel(ReactionOutputSample::buildMetamodel);

    public static final MetamodelListItemValueHandler<ReactionOutput, ReactionOutputSample, Anchor.OutputSample, ReactionOutputSamplePatch> REACTION_OUTPUT_SAMPLE = new MetamodelListItemValueHandler<>(
            OUTPUT_SAMPLE_METAMODEL,
            ReactionOutputSamplePatch::new,
            ReactionOutputSample::createWithAnchor
    );

    public static final ListValueHandler<ReactionOutput, ReactionOutputSample, Anchor.OutputSample, ReactionOutputSamplePatch> REACTION_OUTPUT_SAMPLE_LIST = new ListValueHandler<>(
            ReactionOutputSample::getAnchor,
            REACTION_OUTPUT_SAMPLE
    );

    public static final Metamodel<ReactionOutput, ReactionOutputPatch> OUTPUT_METAMODEL = createMetamodel(ReactionOutput::buildMetamodel);

    public static final MetamodelListItemValueHandler<Reaction, ReactionOutput, Anchor.Output, ReactionOutputPatch> REACTION_OUTPUT = new MetamodelListItemValueHandler<>(
            OUTPUT_METAMODEL,
            ReactionOutputPatch::new,
            ReactionOutput::createWithAnchor
    );

    public static final ListValueHandler<Reaction, ReactionOutput, Anchor.Output, ReactionOutputPatch> REACTION_OUTPUT_LIST = new ListValueHandler<>(
            ReactionOutput::getAnchor,
            REACTION_OUTPUT
    );

    public static final Metamodel<Reaction, ReactionPatch> REACTION_METAMODEL = createMetamodel(Reaction::buildMetamodel);

    public static final MetamodelListItemValueHandler<ExperimentModel, Reaction, Anchor.Reaction, ReactionPatch> REACTION = new MetamodelListItemValueHandler<>(
            REACTION_METAMODEL,
            ReactionPatch::new,
            Reaction::createWithAnchor
    );

    public static final ListValueHandler<ExperimentModel, Reaction, Anchor.Reaction, ReactionPatch> REACTION_LIST = new ListValueHandler<>(
            Reaction::getAnchor,
            REACTION
    );

    public static final Metamodel<ExperimentModel, ExperimentModelPatch> EXPERIMENT_MODEL_METAMODEL = createMetamodel(ExperimentModel::buildMetamodel);

    public static final ExperimentModelValueHandler EXPERIMENT_MODEL = new ExperimentModelValueHandler(EXPERIMENT_MODEL_METAMODEL);

    public static <C, T, P> ValueHandler<C, T, P> defaultHandler() {
        //noinspection unchecked,rawtypes
        return (ValueHandler) DEFAULT;
    }

    public static <C, U extends MeasurementUnit> ValueHandler<C, EnteredValue<U>, EnteredValuePatch<U>> enteredValueHandler() {
        //noinspection unchecked,rawtypes
        return (ValueHandler) ENTERED_VALUE;
    }

    private static <C, P> Metamodel<C, P> createMetamodel(Consumer<Metamodel<C, P>> builder) {
        Metamodel<C, P> metamodel = new Metamodel<>();
        builder.accept(metamodel);
        return metamodel;
    }
}

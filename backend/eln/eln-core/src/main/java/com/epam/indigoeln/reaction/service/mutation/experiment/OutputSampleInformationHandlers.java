package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.epam.indigoeln.reaction.model.mutation.MutationRedoInfo;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import com.epam.indigoeln.reaction.model.outputsample.*;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.reaction.service.mutation.MutationHelper;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;
import com.epam.indigoeln.reaction.service.mutation.ReactionOutputSampleMutationHandler;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputComponentState.class)
class SetOutputComponentStateHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputComponentState, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputComponentState mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        DictionaryItemRef old = sample.getComponentState();
        sample.setComponentState(mutation.componentState());
        return new MutationResult(mutationHelper.formatSetterSummary("batch component state", mutation.componentState())
                , null
                , new ReactionOutputSampleMutation.SetOutputComponentState(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputHandlingPrecautions.class)
class SetOutputHandlingPrecautionsHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputHandlingPrecautions, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputHandlingPrecautions mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        List<DictionaryItemRef> old = sample.getHandlingPrecautions();
        sample.setHandlingPrecautions(ModelUtil.emptyToNull(mutation.handlingPrecautions()));
        return new MutationResult(mutationHelper.formatSetterSummary("batch handling precautions", mutation.handlingPrecautions())
                , null
                , new ReactionOutputSampleMutation.SetOutputHandlingPrecautions(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputStorageInstructions.class)
class SetOutputStorageInstructionsHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputStorageInstructions, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputStorageInstructions mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        List<DictionaryItemRef> old = sample.getStorageInstructions();
        sample.setStorageInstructions(ModelUtil.emptyToNull(mutation.storageInstructions()));
        return new MutationResult(mutationHelper.formatSetterSummary("batch storage instructions", mutation.storageInstructions())
                , null
                , new ReactionOutputSampleMutation.SetOutputStorageInstructions(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputCompoundProtection.class)
class SetOutputCompoundProtectionHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputCompoundProtection, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputCompoundProtection mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        List<DictionaryItemRef> old = sample.getCompoundProtection();
        sample.setCompoundProtection(ModelUtil.emptyToNull(mutation.compoundProtection()));
        return new MutationResult(mutationHelper.formatSetterSummary("batch compound protection", mutation.compoundProtection())
                , null
                , new ReactionOutputSampleMutation.SetOutputCompoundProtection(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputSolubilityInSolvents.class)
class SetOutputSolubilityInSolventsHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputSolubilityInSolvents, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputSolubilityInSolvents mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        List<SolubidityInSolvent> old = sample.getSolubilityInSolvents();
        sample.setSolubilityInSolvents(ModelUtil.emptyToNull(mutation.solubilityInSolvents()));
        return new MutationResult(mutationHelper.formatSetterSummaryNoDetails("batch solubidity in solvents", ModelUtil.isNotEmpty(mutation.solubilityInSolvents()))
                , null
                , new ReactionOutputSampleMutation.SetOutputSolubilityInSolvents(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputResidualSolvents.class)
class SetOutputResidualSolventsHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputResidualSolvents, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputResidualSolvents mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        List<@Valid ResidualSolvent> old = sample.getResidualSolvents();
        sample.setResidualSolvents(ModelUtil.emptyToNull(mutation.residualSolvents()));
        return new MutationResult(mutationHelper.formatSetterSummaryNoDetails("batch residual solvents", ModelUtil.isNotEmpty(mutation.residualSolvents()))
                , null
                , new ReactionOutputSampleMutation.SetOutputResidualSolvents(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputMeltingPoint.class)
class SetOutputMeltingPointHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputMeltingPoint, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputMeltingPoint mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        MeltingPoint old = sample.getMeltingPoint();
        sample.setMeltingPoint(mutation.meltingPoint());
        return new MutationResult(mutationHelper.formatSetterSummaryNoDetails("batch melting point", mutation.meltingPoint() != null)
                , null
                , new ReactionOutputSampleMutation.SetOutputMeltingPoint(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputPurityCalculations.class)
class SetOutputPurityCalculationsHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputPurityCalculations, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputPurityCalculations mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        List<PurityCalculation> old = sample.getPurityCalculations();
        sample.setPurityCalculations(ModelUtil.emptyToNull(mutation.purityCalculations()));
        return new MutationResult(mutationHelper.formatSetterSummaryNoDetails("batch purity calculations", ModelUtil.isNotEmpty(mutation.purityCalculations()))
                , null
                , new ReactionOutputSampleMutation.SetOutputPurityCalculations(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputExternalSupplier.class)
class SetOutputExternalSupplierHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputExternalSupplier, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputExternalSupplier mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        ExternalSupplier old = sample.getExternalSupplier();
        sample.setExternalSupplier(mutation.externalSupplier());
        return new MutationResult(mutationHelper.formatSetterSummaryNoDetails("batch external supplier", mutation.externalSupplier() != null)
                , null
                , new ReactionOutputSampleMutation.SetOutputExternalSupplier(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputSource.class)
class SetOutputSourceHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputSource, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputSource mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        DictionaryItemRef old = sample.getSource();
        sample.setSource(mutation.source());
        return new MutationResult(mutationHelper.formatSetterSummary("batch source", mutation.source())
                , null
                , new ReactionOutputSampleMutation.SetOutputSource(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputSourceDetails.class)
class SetOutputSourceDetailsHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputSourceDetails, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputSourceDetails mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        DictionaryItemRef old = sample.getSourceDetails();
        sample.setSourceDetails(mutation.sourceDetails());
        return new MutationResult(mutationHelper.formatSetterSummary("batch source details", mutation.sourceDetails())
                , null
                , new ReactionOutputSampleMutation.SetOutputSourceDetails(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputBatchComment.class)
class SetOutputBatchCommentHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputBatchComment, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputBatchComment mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        String old = sample.getBatchComment();
        sample.setBatchComment(mutation.batchComment());
        return new MutationResult(mutationHelper.formatSetterSummary("batch comment", mutation.batchComment())
                , null
                , new ReactionOutputSampleMutation.SetOutputBatchComment(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputStructureComment.class)
class SetOutputStructureCommentHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputStructureComment, MutationRedoInfo> {

    @Inject
    MutationHelper mutationHelper;

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputStructureComment mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        String old = sample.getStructureComment();
        sample.setStructureComment(mutation.structureComment());
        return new MutationResult(mutationHelper.formatSetterSummary("batch structure comment", mutation.structureComment())
                , null
                , new ReactionOutputSampleMutation.SetOutputStructureComment(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.RemoveProductSample.class)
class RemoveProductSampleHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.RemoveProductSample, MutationRedoInfo> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.RemoveProductSample mutation, @Nullable MutationRedoInfo redoInfo, MutationContext context) {
        row.getSamples().remove(sample);
        return new MutationResult("Remove batch"
                , null
                , new ReactionOutputMutation.UndoRemoveProductSample(row.getAnchor(), sample)
        );
    }
}

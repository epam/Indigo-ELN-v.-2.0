package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import jakarta.enterprise.context.Dependent;

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputComponentState.class)
class SetOutputComponentStateHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputComponentState> {

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputComponentState mutation, ExperimentMutationContext context) {
        sample.setComponentState(mutation.componentState());
        return formatSetterSummary("batch component state", mutation.componentState());
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputHandlingPrecautions.class)
class SetOutputHandlingPrecautionsHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputHandlingPrecautions> {

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputHandlingPrecautions mutation, ExperimentMutationContext context) {
        sample.setHandlingPrecautions(ModelUtil.emptyToNull(mutation.handlingPrecautions()));
        return formatSetterSummary("batch handling precautions", mutation.handlingPrecautions());
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputStorageInstructions.class)
class SetOutputStorageInstructionsHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputStorageInstructions> {

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputStorageInstructions mutation, ExperimentMutationContext context) {
        sample.setStorageInstructions(ModelUtil.emptyToNull(mutation.storageInstructions()));
        return formatSetterSummary("batch storage instructions", mutation.storageInstructions());
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputCompoundProtection.class)
class SetOutputCompoundProtectionHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputCompoundProtection> {

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputCompoundProtection mutation, ExperimentMutationContext context) {
        sample.setCompoundProtection(ModelUtil.emptyToNull(mutation.compoundProtection()));
        return formatSetterSummary("batch compound protection", mutation.compoundProtection());
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputSolubilityInSolvents.class)
class SetOutputSolubilityInSolventsHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputSolubilityInSolvents> {

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputSolubilityInSolvents mutation, ExperimentMutationContext context) {
        sample.setSolubilityInSolvents(ModelUtil.emptyToNull(mutation.solubilityInSolvents()));
        return formatSetterSummaryNoDetails("batch solubidity in solvents", ModelUtil.isNotEmpty(mutation.solubilityInSolvents()));
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputResidualSolvents.class)
class SetOutputResidualSolventsHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputResidualSolvents> {

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputResidualSolvents mutation, ExperimentMutationContext context) {
        sample.setResidualSolvents(ModelUtil.emptyToNull(mutation.residualSolvents()));
        return formatSetterSummaryNoDetails("batch residual solvents", ModelUtil.isNotEmpty(mutation.residualSolvents()));
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputMeltingPoint.class)
class SetOutputMeltingPointHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputMeltingPoint> {

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputMeltingPoint mutation, ExperimentMutationContext context) {
        sample.setMeltingPoint(mutation.meltingPoint());
        return formatSetterSummaryNoDetails("batch melting point", mutation.meltingPoint() != null);
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputPurityCalculations.class)
class SetOutputPurityCalculationsHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputPurityCalculations> {

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputPurityCalculations mutation, ExperimentMutationContext context) {
        sample.setPurityCalculations(ModelUtil.emptyToNull(mutation.purityCalculations()));
        return formatSetterSummaryNoDetails("batch purity calculations", ModelUtil.isNotEmpty(mutation.purityCalculations()));
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputExternalSupplier.class)
class SetOutputExternalSupplierHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputExternalSupplier> {

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputExternalSupplier mutation, ExperimentMutationContext context) {
        sample.setExternalSupplier(mutation.externalSupplier());
        return formatSetterSummaryNoDetails("batch external supplier", mutation.externalSupplier() != null);
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputSource.class)
class SetOutputSourceHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputSource> {

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputSource mutation, ExperimentMutationContext context) {
        sample.setSource(mutation.source());
        return formatSetterSummary("batch source", mutation.source());
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputSourceDetails.class)
class SetOutputSourceDetailsHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputSourceDetails> {

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputSourceDetails mutation, ExperimentMutationContext context) {
        sample.setSourceDetails(mutation.sourceDetails());
        return formatSetterSummary("batch source details", mutation.sourceDetails());
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputBatchComment.class)
class SetOutputBatchCommentHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputBatchComment> {

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputBatchComment mutation, ExperimentMutationContext context) {
        sample.setBatchComment(mutation.batchComment());
        return formatSetterSummary("batch comment", mutation.batchComment());
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputStructureComment.class)
class SetOutputStructureCommentHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputStructureComment> {

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputStructureComment mutation, ExperimentMutationContext context) {
        sample.setStructureComment(mutation.structureComment());
        return formatSetterSummary("batch structure comment", mutation.structureComment());
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.RemoveProductSample.class)
class RemoveProductSampleHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.RemoveProductSample> {

    @Override
    public String handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.RemoveProductSample mutation, ExperimentMutationContext context) {
        sample.delete();
        return "Remove batch";
    }
}

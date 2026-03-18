package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import com.epam.indigoeln.reaction.model.outputsample.*;
import com.epam.indigoeln.reaction.service.mutation.AbstractReactionOutputSampleMutationHandler;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;
import jakarta.enterprise.context.Dependent;
import jakarta.validation.Valid;

import java.util.List;

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputComponentState.class)
class SetOutputComponentStateHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputComponentState> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputComponentState mutation) {
        DictionaryItemRef old = sample.getComponentState();
        sample.setComponentState(mutation.componentState());
        return new MutationResult(formatSetterSummary("batch component state", mutation.componentState())
                , new ReactionOutputSampleMutation.SetOutputComponentState(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputHandlingPrecautions.class)
class SetOutputHandlingPrecautionsHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputHandlingPrecautions> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputHandlingPrecautions mutation) {
        List<DictionaryItemRef> old = sample.getHandlingPrecautions();
        sample.setHandlingPrecautions(ModelUtil.emptyToNull(mutation.handlingPrecautions()));
        return new MutationResult(formatSetterSummary("batch handling precautions", mutation.handlingPrecautions())
                , new ReactionOutputSampleMutation.SetOutputHandlingPrecautions(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputStorageInstructions.class)
class SetOutputStorageInstructionsHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputStorageInstructions> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputStorageInstructions mutation) {
        List<DictionaryItemRef> old = sample.getStorageInstructions();
        sample.setStorageInstructions(ModelUtil.emptyToNull(mutation.storageInstructions()));
        return new MutationResult(formatSetterSummary("batch storage instructions", mutation.storageInstructions())
                , new ReactionOutputSampleMutation.SetOutputStorageInstructions(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputCompoundProtection.class)
class SetOutputCompoundProtectionHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputCompoundProtection> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputCompoundProtection mutation) {
        List<DictionaryItemRef> old = sample.getCompoundProtection();
        sample.setCompoundProtection(ModelUtil.emptyToNull(mutation.compoundProtection()));
        return new MutationResult(formatSetterSummary("batch compound protection", mutation.compoundProtection())
                , new ReactionOutputSampleMutation.SetOutputCompoundProtection(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputSolubilityInSolvents.class)
class SetOutputSolubilityInSolventsHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputSolubilityInSolvents> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputSolubilityInSolvents mutation) {
        List<SolubidityInSolvent> old = sample.getSolubilityInSolvents();
        sample.setSolubilityInSolvents(ModelUtil.emptyToNull(mutation.solubilityInSolvents()));
        return new MutationResult(formatSetterSummaryNoDetails("batch solubidity in solvents", ModelUtil.isNotEmpty(mutation.solubilityInSolvents()))
                , new ReactionOutputSampleMutation.SetOutputSolubilityInSolvents(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputResidualSolvents.class)
class SetOutputResidualSolventsHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputResidualSolvents> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputResidualSolvents mutation) {
        List<@Valid ResidualSolvent> old = sample.getResidualSolvents();
        sample.setResidualSolvents(ModelUtil.emptyToNull(mutation.residualSolvents()));
        return new MutationResult(formatSetterSummaryNoDetails("batch residual solvents", ModelUtil.isNotEmpty(mutation.residualSolvents()))
                , new ReactionOutputSampleMutation.SetOutputResidualSolvents(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputMeltingPoint.class)
class SetOutputMeltingPointHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputMeltingPoint> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputMeltingPoint mutation) {
        MeltingPoint old = sample.getMeltingPoint();
        sample.setMeltingPoint(mutation.meltingPoint());
        return new MutationResult(formatSetterSummaryNoDetails("batch melting point", mutation.meltingPoint() != null)
                , new ReactionOutputSampleMutation.SetOutputMeltingPoint(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputPurityCalculations.class)
class SetOutputPurityCalculationsHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputPurityCalculations> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputPurityCalculations mutation) {
        List<PurityCalculation> old = sample.getPurityCalculations();
        sample.setPurityCalculations(ModelUtil.emptyToNull(mutation.purityCalculations()));
        return new MutationResult(formatSetterSummaryNoDetails("batch purity calculations", ModelUtil.isNotEmpty(mutation.purityCalculations()))
                , new ReactionOutputSampleMutation.SetOutputPurityCalculations(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputExternalSupplier.class)
class SetOutputExternalSupplierHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputExternalSupplier> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputExternalSupplier mutation) {
        ExternalSupplier old = sample.getExternalSupplier();
        sample.setExternalSupplier(mutation.externalSupplier());
        return new MutationResult(formatSetterSummaryNoDetails("batch external supplier", mutation.externalSupplier() != null)
                , new ReactionOutputSampleMutation.SetOutputExternalSupplier(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputSource.class)
class SetOutputSourceHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputSource> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputSource mutation) {
        DictionaryItemRef old = sample.getSource();
        sample.setSource(mutation.source());
        return new MutationResult(formatSetterSummary("batch source", mutation.source())
                , new ReactionOutputSampleMutation.SetOutputSource(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputSourceDetails.class)
class SetOutputSourceDetailsHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputSourceDetails> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputSourceDetails mutation) {
        DictionaryItemRef old = sample.getSourceDetails();
        sample.setSourceDetails(mutation.sourceDetails());
        return new MutationResult(formatSetterSummary("batch source details", mutation.sourceDetails())
                , new ReactionOutputSampleMutation.SetOutputSourceDetails(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputBatchComment.class)
class SetOutputBatchCommentHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputBatchComment> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputBatchComment mutation) {
        String old = sample.getBatchComment();
        sample.setBatchComment(mutation.batchComment());
        return new MutationResult(formatSetterSummary("batch comment", mutation.batchComment())
                , new ReactionOutputSampleMutation.SetOutputBatchComment(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputStructureComment.class)
class SetOutputStructureCommentHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputStructureComment> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputStructureComment mutation) {
        String old = sample.getStructureComment();
        sample.setStructureComment(mutation.structureComment());
        return new MutationResult(formatSetterSummary("batch structure comment", mutation.structureComment())
                , new ReactionOutputSampleMutation.SetOutputStructureComment(mutation.anchor(), old)
        );
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.RemoveProductSample.class)
class RemoveProductSampleHandler extends AbstractReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.RemoveProductSample> {

    @Override
    public MutationResult handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.RemoveProductSample mutation) {
        sample.delete();
        return new MutationResult("Remove batch"
                , new ReactionOutputMutation.UndoRemoveProductSample(row.getAnchor(), sample)
        );
    }
}

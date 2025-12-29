package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionOutput;
import com.epam.indigoeln.reaction.model.ReactionOutputSample;
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerFor;
import com.epam.indigoeln.reaction.service.mutation.ReactionOutputSampleMutationHandler;
import jakarta.enterprise.context.Dependent;

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputComponentState.class)
class SetOutputComponentStateHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputComponentState> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputComponentState mutation, MutationContext context) {
        sample.setComponentState(mutation.componentState());
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputHandlingPrecautions.class)
class SetOutputHandlingPrecautionsHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputHandlingPrecautions> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputHandlingPrecautions mutation, MutationContext context) {
        sample.setHandlingPrecautions(mutation.handlingPrecautions());
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputStorageInstructions.class)
class SetOutputStorageInstructionsHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputStorageInstructions> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputStorageInstructions mutation, MutationContext context) {
        sample.setStorageInstructions(mutation.storageInstructions());
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputCompoundProtection.class)
class SetOutputCompoundProtectionHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputCompoundProtection> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputCompoundProtection mutation, MutationContext context) {
        sample.setCompoundProtection(mutation.compoundProtection());
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputSolubilityInSolvents.class)
class SetOutputSolubilityInSolventsHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputSolubilityInSolvents> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputSolubilityInSolvents mutation, MutationContext context) {
        sample.setSolubilityInSolvents(mutation.solubilityInSolvents());
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputResidualSolvents.class)
class SetOutputResidualSolventsHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputResidualSolvents> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputResidualSolvents mutation, MutationContext context) {
        sample.setResidualSolvents(mutation.residualSolvents());
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputMeltingPoint.class)
class SetOutputMeltingPointHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputMeltingPoint> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputMeltingPoint mutation, MutationContext context) {
        sample.setMeltingPoint(mutation.meltingPoint());
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputPurityCalculations.class)
class SetOutputPurityCalculationsHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputPurityCalculations> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputPurityCalculations mutation, MutationContext context) {
        sample.setPurityCalculations(mutation.purityCalculations());
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputExternalSupplier.class)
class SetOutputExternalSupplierHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputExternalSupplier> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputExternalSupplier mutation, MutationContext context) {
        sample.setExternalSupplier(mutation.externalSupplier());
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputSource.class)
class SetOutputSourceHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputSource> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputSource mutation, MutationContext context) {
        sample.setSource(mutation.source());
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputSourceDetails.class)
class SetOutputSourceDetailsHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputSourceDetails> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputSourceDetails mutation, MutationContext context) {
        sample.setSourceDetails(mutation.sourceDetails());
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputBatchComment.class)
class SetOutputBatchCommentHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputBatchComment> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputBatchComment mutation, MutationContext context) {
        sample.setBatchComment(mutation.batchComment());
    }
}

@Dependent
@MutationHandlerFor(ReactionOutputSampleMutation.SetOutputStructureComment.class)
class SetOutputStructureCommentHandler implements ReactionOutputSampleMutationHandler<ReactionOutputSampleMutation.SetOutputStructureComment> {

    @Override
    public void handle(ExperimentEntity experiment, ExperimentModel model, Reaction reaction, ReactionOutput row, ReactionOutputSample sample, ReactionOutputSampleMutation.SetOutputStructureComment mutation, MutationContext context) {
        sample.setStructureComment(mutation.structureComment());
    }
}

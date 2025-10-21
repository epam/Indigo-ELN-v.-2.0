package com.epam.indigoeln.reaction.model.mutation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.SIMPLE_NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(ReactionMutation.SetScheme.class),
        @JsonSubTypes.Type(ReactionMutation.ResolveInputs.class),
        @JsonSubTypes.Type(ReactionMutation.AddEmptyInput.class),
        @JsonSubTypes.Type(ReactionMutation.AddInput.class),
        @JsonSubTypes.Type(ReactionMutation.RemoveInput.class),

        @JsonSubTypes.Type(ReactionInputMutation.SetInputRowRole.class),
        @JsonSubTypes.Type(ReactionInputMutation.SetInputRowMol.class),
        @JsonSubTypes.Type(ReactionInputMutation.SetInputRowLimiting.class),
        @JsonSubTypes.Type(ReactionInputMutation.SetInputRowSaltCode.class),
        @JsonSubTypes.Type(ReactionInputMutation.SetInputRowSaltEQ.class),
        @JsonSubTypes.Type(ReactionInputMutation.SetInputRowEQ.class),

        @JsonSubTypes.Type(ReactionInputSampleMutation.SetInputDensity.class),
        @JsonSubTypes.Type(ReactionInputSampleMutation.SetInputMolarity.class),
        @JsonSubTypes.Type(ReactionInputSampleMutation.SetInputVolume.class),
        @JsonSubTypes.Type(ReactionInputSampleMutation.SetInputPurity.class),
        @JsonSubTypes.Type(ReactionInputSampleMutation.SetInputHealthHazards.class),
        @JsonSubTypes.Type(ReactionInputSampleMutation.SetInputMol.class),
        @JsonSubTypes.Type(ReactionInputSampleMutation.SetInputWeight.class),

        @JsonSubTypes.Type(ReactionOutputMutation.AddProductSample.class),
        @JsonSubTypes.Type(ReactionOutputMutation.SetOutputRowType.class),
        @JsonSubTypes.Type(ReactionOutputMutation.SetOutputRowSaltCode.class),
        @JsonSubTypes.Type(ReactionOutputMutation.SetOutputRowSaltEQ.class),
        @JsonSubTypes.Type(ReactionOutputMutation.SetOutputRowEQ.class),

        @JsonSubTypes.Type(ReactionOutputSampleMutation.SetOutputDensity.class),
        @JsonSubTypes.Type(ReactionOutputSampleMutation.SetOutputMolarity.class),
        @JsonSubTypes.Type(ReactionOutputSampleMutation.SetOutputVolume.class),
        @JsonSubTypes.Type(ReactionOutputSampleMutation.SetOutputPurity.class),
        @JsonSubTypes.Type(ReactionOutputSampleMutation.SetOutputHealthHazards.class),
        @JsonSubTypes.Type(ReactionOutputSampleMutation.SetOutputActualMol.class),
        @JsonSubTypes.Type(ReactionOutputSampleMutation.SetOutputActualWeight.class),
        @JsonSubTypes.Type(ReactionOutputSampleMutation.RegisterSample.class),
        @JsonSubTypes.Type(ReactionOutputSampleMutation.SetOutputHandlingPrecautions.class),
        @JsonSubTypes.Type(ReactionOutputSampleMutation.SetOutputStorageInstructions.class),
        @JsonSubTypes.Type(ReactionOutputSampleMutation.SetOutputCompoundProtection.class),
        @JsonSubTypes.Type(ReactionOutputSampleMutation.SetOutputSolubilityInSolvents.class),
        @JsonSubTypes.Type(ReactionOutputSampleMutation.SetOutputResidualSolvents.class),
        @JsonSubTypes.Type(ReactionOutputSampleMutation.SetOutputMeltingPoint.class),
        @JsonSubTypes.Type(ReactionOutputSampleMutation.SetOutputPurityCalculations.class),
        @JsonSubTypes.Type(ReactionOutputSampleMutation.SetOutputExternalSupplier.class),
        @JsonSubTypes.Type(ReactionOutputSampleMutation.SetOutputSource.class),
        @JsonSubTypes.Type(ReactionOutputSampleMutation.SetOutputSourceDetails.class),
        @JsonSubTypes.Type(ReactionOutputSampleMutation.SetOutputComponentState.class),
        @JsonSubTypes.Type(ReactionOutputSampleMutation.SetOutputBatchComment.class),
        @JsonSubTypes.Type(ReactionOutputSampleMutation.SetOutputStructureComment.class),
})
public sealed interface Mutation permits
        ReactionMutation,
        ReactionInputMutation,
        ReactionInputSampleMutation,
        ReactionOutputMutation,
        ReactionOutputSampleMutation
{
}

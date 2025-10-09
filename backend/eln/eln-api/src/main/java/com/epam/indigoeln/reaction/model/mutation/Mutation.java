package com.epam.indigoeln.reaction.model.mutation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ReactionMutation.SetScheme.class, name = "SetScheme"),
        @JsonSubTypes.Type(value = ReactionMutation.ResolveInputs.class, name = "ResolveInputs"),
        @JsonSubTypes.Type(value = ReactionMutation.AddEmptyInput.class, name = "AddEmptyInput"),
        @JsonSubTypes.Type(value = ReactionMutation.RemoveInput.class, name = "RemoveInput"),
        @JsonSubTypes.Type(value = ReactionInputMutation.SetInputRole.class, name = "SetInputRole"),
        @JsonSubTypes.Type(value = ReactionInputMutation.SetLimiting.class, name = "SetLimiting"),
        @JsonSubTypes.Type(value = ReactionInputMutation.SetInputSaltCode.class, name = "SetInputSaltCode"),
        @JsonSubTypes.Type(value = ReactionInputMutation.SetInputSaltEQ.class, name = "SetInputSaltEQ"),
        @JsonSubTypes.Type(value = ReactionInputMutation.SetInputEQ.class, name = "SetInputEQ"),
        @JsonSubTypes.Type(value = ReactionInputSampleMutation.SetInputDensity.class, name = "SetInputDensity"),
        @JsonSubTypes.Type(value = ReactionInputSampleMutation.SetInputMolarity.class, name = "SetInputMolarity"),
        @JsonSubTypes.Type(value = ReactionInputSampleMutation.SetInputVolume.class, name = "SetInputVolume"),
        @JsonSubTypes.Type(value = ReactionInputSampleMutation.SetInputPurity.class, name = "SetInputPurity"),
        @JsonSubTypes.Type(value = ReactionInputSampleMutation.SetInputMol.class, name = "SetInputMol"),
        @JsonSubTypes.Type(value = ReactionInputSampleMutation.SetInputWeight.class, name = "SetInputWeight"),
        @JsonSubTypes.Type(value = ReactionOutputMutation.AddProductSample.class, name = "AddProductSample"),
        @JsonSubTypes.Type(value = ReactionOutputMutation.SetOutputType.class, name = "SetOutputType"),
        @JsonSubTypes.Type(value = ReactionOutputMutation.SetOutputSaltCode.class, name = "SetOutputSaltCode"),
        @JsonSubTypes.Type(value = ReactionOutputMutation.SetOutputSaltEQ.class, name = "SetOutputSaltEQ"),
        @JsonSubTypes.Type(value = ReactionOutputMutation.SetOutputEQ.class, name = "SetOutputEQ"),
        @JsonSubTypes.Type(value = ReactionOutputSampleMutation.SetOutputDensity.class, name = "SetOutputDensity"),
        @JsonSubTypes.Type(value = ReactionOutputSampleMutation.SetOutputMolarity.class, name = "SetOutputMolarity"),
        @JsonSubTypes.Type(value = ReactionOutputSampleMutation.SetOutputVolume.class, name = "SetOutputVolume"),
        @JsonSubTypes.Type(value = ReactionOutputSampleMutation.SetOutputPurity.class, name = "SetOutputPurity"),
        @JsonSubTypes.Type(value = ReactionOutputSampleMutation.SetOutputActualMol.class, name = "SetOutputActualMol"),
        @JsonSubTypes.Type(value = ReactionOutputSampleMutation.SetOutputActualWeight.class, name = "SetOutputActualWeight"),
        @JsonSubTypes.Type(value = ReactionOutputSampleMutation.RegisterSample.class, name = "RegisterSample"),
})
public sealed interface Mutation permits
        ReactionMutation,
        ReactionInputMutation,
        ReactionInputSampleMutation,
        ReactionOutputMutation,
        ReactionOutputSampleMutation
{
}

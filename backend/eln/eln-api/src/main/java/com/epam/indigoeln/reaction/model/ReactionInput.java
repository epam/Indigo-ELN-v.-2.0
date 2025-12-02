package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.reaction.model.metamodel.Metamodel;
import com.epam.indigoeln.reaction.model.patch.ReactionInputPatch;
import com.epam.indigoeln.reaction.model.patch.handler.Handlers;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.util.ToStringUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class ReactionInput extends ReactionRow implements ExperimentModelNode {

    public static void buildMetamodel(Metamodel<ReactionInput, ReactionInputPatch> metamodel) {
        metamodel.setName("ReactionInput");
        metamodel.anchorProperty("anchor", ReactionInput::getAnchor, ReactionInput::setAnchor, ReactionInputPatch::getAnchor, ReactionInputPatch::setAnchor);
        metamodel.accept(ReactionRow::buildMetamodelBase);
        metamodel.simpleProperty("role", ReactionInput::getRole, ReactionInput::setRole, ReactionInputPatch::getRole, ReactionInputPatch::setRole);
        metamodel.enteredValueProperty("mol", ReactionInput::getMol, ReactionInput::setMol, ReactionInputPatch::getMol, ReactionInputPatch::setMol);
        metamodel.<@Nullable String>simpleProperty("chemicalName", ReactionInput::getChemicalName, ReactionInput::setChemicalName, ReactionInputPatch::getChemicalName, ReactionInputPatch::setChemicalName);
        metamodel.simpleProperty("limiting", ReactionInput::isLimiting, ReactionInput::setLimiting, ReactionInputPatch::getLimiting, ReactionInputPatch::setLimiting, false);
        metamodel.listProperty("samples", ReactionInput::getSamples, ReactionInput::setSamples, ReactionInputPatch::getSamples, ReactionInputPatch::setSamples, Handlers.INPUT_SAMPLE_METAMODEL, Handlers.REACTION_INPUT_SAMPLE_LIST);
    }

    @NotNull
    private Anchor.Input anchor;

    @NotNull
    private ReactionRole role;

    @Nullable
    private EnteredValue<MolUnit> mol;

    @Nullable
    private String chemicalName;

    @Valid
    @NotEmpty
    @JsonManagedReference
    private List<ReactionInputSample> samples = List.of();

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private boolean limiting;

    public static ReactionInput create(Reaction reaction, ReactionRole role) {
        ReactionInput input = createWithAnchor(reaction, new Anchor.Input(reaction.getModel().generateNextAnchor()));
        input.role = role;
        return input;
    }

    public static ReactionInput createWithAnchor(Reaction reaction, Anchor.Input anchor) {
        ReactionInput input = new ReactionInput();
        input.reaction = reaction;
        input.anchor = anchor;
        return input;
    }

    @Override
    public String toString() {
        return ToStringUtil.toStringBuild(Handlers.INPUT_METAMODEL, this);
    }
}

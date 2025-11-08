package com.epam.indigoeln.reaction.model;

import com.epam.indigoeln.reaction.model.metamodel.Metamodel;
import com.epam.indigoeln.reaction.model.patch.ReactionOutputPatch;
import com.epam.indigoeln.reaction.model.patch.handler.ReactionOutputSampleValueHandler;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.WeightUnit;
import com.epam.indigoeln.reaction.util.ToStringUtil;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class ReactionOutput extends ReactionRow implements ExperimentModelNode {

    public static final Metamodel<ReactionOutput, ReactionOutputPatch> METAMODEL = new Metamodel<ReactionOutput, ReactionOutputPatch>("ReactionOutput")
            .anchorProperty("anchor", ReactionOutput::getAnchor, ReactionOutput::setAnchor, ReactionOutputPatch::getAnchor, ReactionOutputPatch::setAnchor)
            .accept(ReactionRow::addBaseProperties)
            .simpleProperty("chemicalName", ReactionOutput::getChemicalName, ReactionOutput::setChemicalName, ReactionOutputPatch::getChemicalName, ReactionOutputPatch::setChemicalName)
            .simpleProperty("type", ReactionOutput::getType, ReactionOutput::setType, ReactionOutputPatch::getType, ReactionOutputPatch::setType)
            .enteredValueProperty("theoMol", ReactionOutput::getTheoMol, ReactionOutput::setTheoMol, ReactionOutputPatch::getTheoMol, ReactionOutputPatch::setTheoMol)
            .enteredValueProperty("theoWeight", ReactionOutput::getTheoWeight, ReactionOutput::setTheoWeight, ReactionOutputPatch::getTheoWeight, ReactionOutputPatch::setTheoWeight)
            .listProperty("samples", ReactionOutput::getSamples, ReactionOutput::setSamples, ReactionOutputPatch::getSamples, ReactionOutputPatch::setSamples, ReactionOutputSample.METAMODEL, ReactionOutputSampleValueHandler.LIST_INSTANCE)
            ;

    @NotNull
    private Anchor.Output anchor;

    @NotNull
    private String chemicalName;

    @NotNull
    private ReactionOutputType type;

    @Nullable
    private EnteredValue<MolUnit> theoMol;

    @Nullable
    private EnteredValue<WeightUnit> theoWeight;

    @Valid
    @NotNull
    @JsonManagedReference
    private List<ReactionOutputSample> samples = List.of();

    public static ReactionOutput create(Reaction reaction, ReactionOutputType type) {
        ReactionOutput output = createWithAnchor(reaction, new Anchor.Output(reaction.getModel().generateNextAnchor()));
        output.type = type;
        output.chemicalName = reaction.generateNextProductName();
        return output;
    }

    public static ReactionOutput createWithAnchor(Reaction reaction, Anchor.Output anchor) {
        ReactionOutput output = new ReactionOutput();
        output.reaction = reaction;
        output.anchor = anchor;
        return output;
    }

    @Override
    public String toString() {
        return ToStringUtil.toStringBuild(METAMODEL, this);
    }
}

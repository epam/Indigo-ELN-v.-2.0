package com.epam.indigoeln.reports.api;

import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.model.units.MolWeightUnit;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

@Data
@NoArgsConstructor
public class ReagentDTO {
    private String chemicalName;
    private EnteredValue<MolUnit> mol;
    private EnteredValue<NoUnit> eq;
    private String saltCode;
    private EnteredValue<MolWeightUnit> molWeight;
    private String reactionRole;
    private String formula;

    public ReagentDTO(ReactionInput reactionInput) {
        chemicalName = reactionInput.getChemicalName() != null ? reactionInput.getChemicalName() : "";
        mol = reactionInput.getMol();
        eq = reactionInput.getEq();
        reactionRole = reactionInput.getRole().toString();
        setCompoundValues(reactionInput.getCompound());
    }

    public ReagentDTO(ReactionOutput reactionOutput) {
        chemicalName = reactionOutput.getChemicalName() != null ? reactionOutput.getChemicalName() : "";
        mol = reactionOutput.getTheoMol();
        eq = reactionOutput.getEq();
        reactionRole = reactionOutput.getType().toString();
        setCompoundValues(reactionOutput.getCompound());
    }

    private void setCompoundValues(CompoundRef compound) {
        try {
            saltCode = compound.getSaltCode().getName();
        } catch (NullPointerException e) {
            saltCode = "";
        }
        molWeight = compound.getMolWeight();
        formula = compound.getFormula();
    }

    public static Collection<ReagentDTO> allExperimentReagents(ExperimentModel model) {
        Collection<ReagentDTO> reagents = new ArrayList<>();
        for (Reaction reaction: model.getReactions()) {
            for (ReactionInput input: reaction.getInputs()) {
                reagents.add(new ReagentDTO(input));
            }
            for (ReactionOutput output: reaction.getOutputs()) {
                reagents.add(new ReagentDTO(output));
            }
        }

        return reagents;
    }
}

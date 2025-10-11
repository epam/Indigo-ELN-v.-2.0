package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.repository.SaltCodeRepository;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputMutation;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolWeightUnit;
import com.epam.indigoeln.reaction.service.calculator.MolWeightCalculator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

@ApplicationScoped
public class CompoundHandler extends AbstractMutationHandler{

    @Inject
    CompoundService compoundService;
    @Inject
    SaltCodeRepository saltCodeRepository;
    @Inject
    MolWeightCalculator molWeightCalculator;
    @Inject
    DictionaryService dictionaryService;
    @Inject
    IndigoAPI indigoAPI;

    public void handle(ExperimentModel model, ReactionInput row, ReactionInputMutation.SetInputRowSaltCode mutation) {
        SaltCodeRef saltCode = mutation.saltCode() != null ? saltCodeRef(mutation.saltCode()) : null;
        row.setCompound(doApplySetSaltCodeEQStereoisomerCode(row, saltCode, row.getCompound().getSaltEQ(), row.getCompound().getStereoisomerCode()));
    }

    public void handle(ExperimentModel model, ReactionInput row, ReactionInputMutation.SetInputRowSaltEQ mutation) {
        row.setCompound(doApplySetSaltCodeEQStereoisomerCode(row, row.getCompound().getSaltCode(), mutation.saltEQ(), row.getCompound().getStereoisomerCode()));
    }

    public void handle(ExperimentModel model, ReactionOutput row, ReactionOutputMutation.SetOutputRowSaltCode mutation) {
        SaltCodeRef saltCode = mutation.saltCode() != null ? saltCodeRef(mutation.saltCode()) : null;
        row.setCompound(doApplySetSaltCodeEQStereoisomerCode(row, saltCode, row.getCompound().getSaltEQ(), row.getCompound().getStereoisomerCode()));
    }

    public void handle(ExperimentModel model, ReactionOutput row, ReactionOutputMutation.SetOutputRowSaltEQ mutation) {
        row.setCompound(doApplySetSaltCodeEQStereoisomerCode(row, row.getCompound().getSaltCode(), mutation.saltEQ(), row.getCompound().getStereoisomerCode()));
    }

    public void handle(ExperimentModel model, ReactionInput row, ReactionInputMutation.SetInputCompoundStereoisomerCode mutation) {
        row.setCompound(doApplySetSaltCodeEQStereoisomerCode(row, row.getCompound().getSaltCode(), row.getCompound().getSaltEQ(), mutation.stereoisomerCode()));
    }

    public void handle(ExperimentModel model, ReactionOutput row, ReactionOutputMutation.SetOutputCompoundStereoisomerCode mutation) {
        row.setCompound(doApplySetSaltCodeEQStereoisomerCode(row, row.getCompound().getSaltCode(), row.getCompound().getSaltEQ(), mutation.stereoisomerCode()));
    }

    public void handle(ExperimentModel model, ReactionInput input, ReactionInputMutation.SetInputCompoundFormula mutation) {
        if (input.getCompound() instanceof CompoundRef.Unknown c) {
            c.setFormula(mutation.formula());
        } else {
            throw new InvalidRequestException("Cannot set formula for stored or virtual compound");
        }
    }

    public void handle(ExperimentModel model, ReactionOutput row, ReactionOutputMutation.SetOutputCompoundFormula mutation) {
        if (row.getCompound() instanceof CompoundRef.Unknown c) {
            c.setFormula(mutation.formula());
        } else {
            throw new InvalidRequestException("Cannot set formula for stored or virtual compound");
        }
    }

    public void handle(ExperimentModel model, ReactionInput row, ReactionInputMutation.SetInputCompoundMolWeight mutation) {
        if (row.getCompound() instanceof CompoundRef.Unknown c) {
            c.setMolWeight(EnteredValue.userLastEntered(mutation.molWeight(), MolWeightUnit.G_PER_MOL));
        } else {
            throw new InvalidRequestException("Cannot set molWeight for stored or virtual compound");
        }
    }

    public void handle(ExperimentModel model, ReactionOutput row, ReactionOutputMutation.SetOutputCompoundMolWeight mutation) {
        if (row.getCompound() instanceof CompoundRef.Unknown c) {
            c.setMolWeight(EnteredValue.userLastEntered(mutation.molWeight(), MolWeightUnit.G_PER_MOL));
        } else {
            throw new InvalidRequestException("Cannot set molWeight for stored or virtual compound");
        }
    }

    private CompoundRef doApplySetSaltCodeEQStereoisomerCode(ReactionRow row, @Nullable SaltCodeRef saltCode, @Nullable Double saltEQ, @Nullable DictionaryItemRef stereoisomerCode) {
        switch (row.getCompound()) {
            case CompoundRef.Virtual v -> {
                // normalize saltEQ
                if (v.getSaltCode() != null && v.getSaltEQ() == null) {
                    saltEQ = 1.0;
                } else if (v.getSaltCode() == null) {
                    saltEQ = null;
                }
                IndigoMolecule molecule = indigoAPI.loadMolecule(v.getMolFile());
                return compoundService.virtualCompoundRef(molecule, stereoisomerCode, saltCode, saltEQ);
            }
            case CompoundRef.Stored s -> throw new InvalidRequestException("Cannot modify saltCode/saltEQ/stereoisomerCode for registered compound");
            case CompoundRef.Unknown u -> throw new InvalidRequestException("Cannot set saltCode/saltEQ/stereoisomerCode for unknown compound");
        }
    }

    private SaltCodeRef saltCodeRef(DictionaryItemRef ref) {
        return dictionaryService.getSaltRef(ref.getId());
    }
}

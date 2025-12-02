package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.entity.SaltCodeInfo;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputMutation;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolWeightUnit;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import static com.epam.indigoeln.common.exception.InvalidRequestException.validate;

@Dependent
public class CompoundHandler extends AbstractMutationHandler {

    @Inject
    CompoundService compoundService;
    @Inject
    DictionaryService dictionaryService;
    @Inject
    IndigoAPI indigoAPI;

    public void handle(ReactionInput row, ReactionInputMutation.SetInputRowSaltCode mutation) {
        SaltCodeInfo saltCode = mutation.saltCode() != null ? saltCodeInfo(mutation.saltCode()) : null;
        row.setCompound(doApplySetSaltCodeEQStereoisomerCode(row, saltCode, row.getCompound().getSaltEQ(), row.getCompound().getStereoisomerCode()));
    }

    public void handle(ReactionInput row, ReactionInputMutation.SetInputRowSaltEQ mutation) {
        validate(row.getCompound().getSaltCode() != null, "Cannot set saltEQ because saltCode is not set");
        row.setCompound(doApplySetSaltCodeEQStereoisomerCode(row, saltCodeInfo(row.getCompound().getSaltCode()), mutation.saltEQ(), row.getCompound().getStereoisomerCode()));
    }

    public void handle(ReactionOutput row, ReactionOutputMutation.SetOutputRowSaltCode mutation) {
        SaltCodeInfo saltCode = mutation.saltCode() != null ? saltCodeInfo(mutation.saltCode()) : null;
        row.setCompound(doApplySetSaltCodeEQStereoisomerCode(row, saltCode, row.getCompound().getSaltEQ(), row.getCompound().getStereoisomerCode()));
    }

    public void handle(ReactionOutput row, ReactionOutputMutation.SetOutputRowSaltEQ mutation) {
        validate(row.getCompound().getSaltCode() != null, "Cannot set saltEQ because saltCode is not set");
        row.setCompound(doApplySetSaltCodeEQStereoisomerCode(row, saltCodeInfo(row.getCompound().getSaltCode()), mutation.saltEQ(), row.getCompound().getStereoisomerCode()));
    }

    public void handle(ReactionInput row, ReactionInputMutation.SetInputCompoundStereoisomerCode mutation) {
        row.setCompound(doApplySetSaltCodeEQStereoisomerCode(row, saltCodeInfo(row.getCompound().getSaltCode()), row.getCompound().getSaltEQ(), mutation.stereoisomerCode()));
    }

    public void handle(ReactionOutput row, ReactionOutputMutation.SetOutputCompoundStereoisomerCode mutation) {
        row.setCompound(doApplySetSaltCodeEQStereoisomerCode(row, saltCodeInfo(row.getCompound().getSaltCode()), row.getCompound().getSaltEQ(), mutation.stereoisomerCode()));
    }

    public void handle(ReactionInput row, ReactionInputMutation.SetInputCompoundMolWeight mutation) {
        if (row.getCompound() instanceof CompoundRef.Unknown c) {
            c.setMolWeight(EnteredValue.userLastEntered(mutation.molWeight(), MolWeightUnit.G_PER_MOL));
        } else {
            throw new InvalidRequestException("Cannot set molWeight for stored or virtual compound");
        }
    }

    public void handle(ReactionOutput row, ReactionOutputMutation.SetOutputCompoundMolWeight mutation) {
        if (row.getCompound() instanceof CompoundRef.Unknown c) {
            c.setMolWeight(EnteredValue.userLastEntered(mutation.molWeight(), MolWeightUnit.G_PER_MOL));
        } else {
            throw new InvalidRequestException("Cannot set molWeight for stored or virtual compound");
        }
    }

    private CompoundRef doApplySetSaltCodeEQStereoisomerCode(ReactionRow row, @Nullable SaltCodeInfo saltCode, @Nullable Double saltEQ, @Nullable DictionaryItemRef stereoisomerCode) {
        switch (row.getCompound()) {
            case CompoundRef.Virtual v -> {
                // normalize saltEQ
                if (saltCode != null && saltEQ == null) {
                    saltEQ = 1.0;
                } else if (saltCode == null) {
                    saltEQ = null;
                }
                CompoundEntity compound = compoundService.getCompound(v.getCompoundID());
                IndigoMolecule molecule = indigoAPI.loadMolecule(compound.getMolFile());
                return compoundService.virtualCompoundRef(molecule, stereoisomerCode, saltCode, saltEQ);
            }
            case CompoundRef.Stored s -> throw new InvalidRequestException("Cannot modify saltCode/saltEQ/stereoisomerCode for registered compound");
            case CompoundRef.Unknown u -> throw new InvalidRequestException("Cannot set saltCode/saltEQ/stereoisomerCode for unknown compound");
        }
    }

    private SaltCodeInfo saltCodeInfo(DictionaryItemRef ref) {
        return dictionaryService.getSaltInfo(ref.getId());
    }

    @Nullable
    private SaltCodeInfo saltCodeInfo(@Nullable SaltCodeRef ref) {
        return ref != null ? dictionaryService.getSaltInfo(ref.getId()) : null;
    }
}

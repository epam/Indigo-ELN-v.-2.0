package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.entity.SaltCodeEntity;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.repository.SaltCodeRepository;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ReactionInputMutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputMutation;
import com.epam.indigoeln.reaction.model.units.MolWeightUnit;
import com.epam.indigoeln.reaction.service.calculator.MolWeightCalculator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.fixed;

@ApplicationScoped
public class SaltCodeEQHandler extends AbstractMutationHandler{

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

    public void handle(ExperimentModel model, ReactionInputMutation.SetInputSaltCode mutation) {
        ReactionInput row = model.locate(mutation);
        SaltCodeRef saltCode = mutation.saltCode() != null ? saltCodeRef(mutation.saltCode()) : null;
        row.setCompound(doApplySetSaltCodeEQ(row, saltCode, getCurrentSaltEQ(row.getCompound())));
    }

    public void handle(ExperimentModel model, ReactionInputMutation.SetInputSaltEQ mutation) {
        ReactionInput row = model.locate(mutation);
        row.setCompound(doApplySetSaltCodeEQ(row, getCurrentSaltCode(row.getCompound()), mutation.saltEQ()));
    }

    public void handle(ExperimentModel model, ReactionOutputMutation.SetOutputSaltCode mutation) {
        ReactionOutput row = model.locate(mutation);
        SaltCodeRef saltCode = mutation.saltCode() != null ? saltCodeRef(mutation.saltCode()) : null;
        row.setCompound(doApplySetSaltCodeEQ(row, saltCode, getCurrentSaltEQ(row.getCompound())));
    }

    public void handle(ExperimentModel model, ReactionOutputMutation.SetOutputSaltEQ mutation) {
        ReactionOutput row = model.locate(mutation);
        row.setCompound(doApplySetSaltCodeEQ(row, getCurrentSaltCode(row.getCompound()), mutation.saltEQ()));
    }

    private SaltCodeRef getCurrentSaltCode(CompoundRef compound) {
        return switch (compound) {
            case CompoundRef.Virtual v -> v.getSaltCode();
            case CompoundRef.Stored s -> s.getSaltCode();
            case CompoundRef.Unknown u -> null;
        };
    }

    private Double getCurrentSaltEQ(CompoundRef compound) {
        return switch (compound) {
            case CompoundRef.Virtual v -> v.getSaltEQ();
            case CompoundRef.Stored s -> s.getSaltEQ();
            case CompoundRef.Unknown u -> null;
        };
    }

    private CompoundRef doApplySetSaltCodeEQ(ReactionRow row, @Nullable SaltCodeRef saltCode, @Nullable Double saltEQ) {
        switch (row.getCompound()) {
            case CompoundRef.Virtual v -> {
                // normalize saltEQ
                if (v.getSaltCode() != null && v.getSaltEQ() == null) {
                    saltEQ = 1.0;
                } else if (v.getSaltCode() == null) {
                    saltEQ = null;
                }
                IndigoMolecule molecule = indigoAPI.loadMolecule(v.getMolFile());
                return compoundService.virtualCompoundRef(molecule, v.getStereoisomerCode(), saltCode, saltEQ);
            }
            case CompoundRef.Stored s -> throw new InvalidRequestException("Cannot modify saltCode/saltEQ for registered compound");
            case CompoundRef.Unknown u -> throw new InvalidRequestException("Cannot set saltCode/saltEQ for unknown compound");
        }
    }

    private SaltCodeRef saltCodeRef(DictionaryItemRef ref) {
        return dictionaryService.getSaltRef(ref.getId());
    }
}

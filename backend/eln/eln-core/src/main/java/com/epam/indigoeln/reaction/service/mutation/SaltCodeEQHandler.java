package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.eln.entity.SaltCodeEntity;
import com.epam.indigoeln.eln.model.DictionaryRef;
import com.epam.indigoeln.eln.repository.SaltCodeRepository;
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
    SaltCodeRepository saltCodeRepository;
    @Inject
    MolWeightCalculator molWeightCalculator;

    public void handle(ExperimentModel model, ReactionInputMutation.SetInputSaltCode mutation) {
        ReactionInput row = model.locate(mutation);
        SaltCodeRef saltCode = mutation.saltCode() != null ? saltCodeRef(mutation.saltCode()) : null;
        doApplySetSaltCodeEQ(row, saltCode, getCurrentSaltEQ(row.getCompound()));
    }

    public void handle(ExperimentModel model, ReactionInputMutation.SetInputSaltEQ mutation) {
        ReactionInput row = model.locate(mutation);
        doApplySetSaltCodeEQ(row, getCurrentSaltCode(row.getCompound()), mutation.saltEQ());
    }

    public void handle(ExperimentModel model, ReactionOutputMutation.SetOutputSaltCode mutation) {
        ReactionOutput row = model.locate(mutation);
        SaltCodeRef saltCode = mutation.saltCode() != null ? saltCodeRef(mutation.saltCode()) : null;
        doApplySetSaltCodeEQ(row, saltCode, getCurrentSaltEQ(row.getCompound()));
    }

    public void handle(ExperimentModel model, ReactionOutputMutation.SetOutputSaltEQ mutation) {
        ReactionOutput row = model.locate(mutation);
        doApplySetSaltCodeEQ(row, getCurrentSaltCode(row.getCompound()), mutation.saltEQ());
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

    private void doApplySetSaltCodeEQ(ReactionRow row, @Nullable SaltCodeRef saltCode, @Nullable Double saltEQ) {
        switch (row.getCompound()) {
            case CompoundRef.Virtual v -> {
                v.setSaltCode(saltCode);
                v.setSaltEQ(saltEQ);
                // normalize saltEQ
                if (v.getSaltCode() != null && v.getSaltEQ() == null) {
                    v.setSaltEQ(1.0);
                } else if (v.getSaltCode() == null) {
                    v.setSaltEQ(null);
                }
                // recalculate mol weight
                double molWeight;
                if (v.getSaltCode() != null && v.getSaltEQ() != null) {
                    molWeight = molWeightCalculator.calculateMolWeightWithSalt(v.getMolFile(), v.getSaltCode(), v.getSaltEQ());
                } else {
                    molWeight = molWeightCalculator.calculateMolWeightWithoutSalt(v.getMolFile());
                }
                v.setMolWeight(fixed(molWeight, MolWeightUnit.G_PER_MOL));
            }
            case CompoundRef.Stored s -> throw new InvalidRequestException("Cannot modify saltCode/saltEQ for registered compound");
            case CompoundRef.Unknown u -> throw new InvalidRequestException("Cannot set saltCode/saltEQ for unknown compound");
        }
    }

    private SaltCodeRef saltCodeRef(DictionaryRef ref) {
        SaltCodeEntity entity = saltCodeRepository.findById(ref.getId());
        return new SaltCodeRef(entity.getName(), entity.getCharge(), entity.getMolWeight());
    }
}

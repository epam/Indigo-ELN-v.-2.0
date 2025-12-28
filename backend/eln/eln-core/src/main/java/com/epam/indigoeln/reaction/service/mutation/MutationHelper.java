package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.compound.entity.CompoundEntity;
import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.entity.SaltCodeInfo;
import com.epam.indigoeln.eln.mapper.DictionaryMapper;
import com.epam.indigoeln.eln.model.DictionaryItemRef;
import com.epam.indigoeln.eln.service.DictionaryService;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.units.DensityUnit;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.DEFAULT_ONE;

@ApplicationScoped
public class MutationHelper {

    @Inject
    Instance<IndigoAPI> indigoAPI;
    @Inject
    CompoundService compoundService;
    @Inject
    DictionaryService dictionaryService;
    @Inject
    DictionaryMapper dictionaryMapper;

    public void setInputLineSample(ReactionInput row, SampleEntity sample, MutationContext context) {
        row.setCompound(compoundService.realCompoundRef(sample.getCompound()));

        ReactionInputSample reactionInputSample = ReactionInputSample.create(row);
        reactionInputSample.setSampleId(sample.getId());
        reactionInputSample.setStrCode(sample.getStrCode());
        reactionInputSample.setDensity(EnteredValue.defaultValue(sample.getDensity(), DensityUnit.G_ML));
        reactionInputSample.setMolarity(EnteredValue.defaultValue(sample.getMolarity(), sample.getMolarityUnit()));
        reactionInputSample.setPurity(sample.getPurity() != null ? EnteredValue.defaultValue(sample.getPurity(), NoUnit.NO_UNIT) : DEFAULT_ONE);
        reactionInputSample.setHealthHazards(dictionaryMapper.itemToRefList(sample.getHealthHazards()));
        reactionInputSample.setComment(sample.getBatchComment());
        reactionInputSample.setNbkBatchNumber(sample.getNbkBatchNumber());
        row.setSamples(List.of(reactionInputSample));
        row.setChemicalName(sample.getCompound().getChemicalName());

        context.getAffectedRoles().add(row.getRole());
    }

    public ReactionInput createInputLine(Reaction reaction, @Nullable IndigoMolecule molecule, ReactionRole role) {
        ReactionInput row = ReactionInput.create(reaction, role);
        row.setCompound(molecule != null
                ? compoundService.virtualCompoundRef(molecule, null, null, null)
                : compoundService.unknownCompoundRef());
        row.setEq(DEFAULT_ONE);
        ReactionInputSample reactionInputSample = ReactionInputSample.create(row);
        reactionInputSample.setPurity(DEFAULT_ONE);
        row.setSamples(List.of(reactionInputSample));
        return row;
    }

    public ReactionOutput createOutputLine(Reaction reaction, IndigoMolecule molecule) {
        ReactionOutput row = ReactionOutput.create(reaction, reaction.getFinalOutput() != null ? ReactionOutputType.BY_PRODUCT : ReactionOutputType.FINAL);
        row.setCompound(compoundService.virtualCompoundRef(molecule, null, null, null));
        row.setEq(DEFAULT_ONE);
        row.setSamples(List.of());
        return row;
    }

    public void adjustLimitingInput(Reaction reaction) {
        if (!reaction.getInputs().isEmpty() && reaction.getLimitingInput() == null) {
            reaction.getInputs().getFirst().setLimiting(true);
        }
    }

    public CompoundRef doApplySetSaltCodeEQStereoisomerCode(ReactionRow row, @Nullable SaltCodeInfo saltCode, @Nullable Double saltEQ, @Nullable DictionaryItemRef stereoisomerCode) {
        switch (row.getCompound()) {
            case CompoundRef.Virtual v -> {
                // normalize saltEQ
                if (saltCode != null && saltEQ == null) {
                    saltEQ = 1.0;
                } else if (saltCode == null) {
                    saltEQ = null;
                }
                CompoundEntity compound = compoundService.getCompound(v.getCompoundID());
                IndigoMolecule molecule = indigoAPI.get().loadMolecule(compound.getMolFile());
                return compoundService.virtualCompoundRef(molecule, stereoisomerCode, saltCode, saltEQ);
            }
            case CompoundRef.Stored s -> throw new InvalidRequestException("Cannot modify saltCode/saltEQ/stereoisomerCode for registered compound");
            case CompoundRef.Unknown u -> throw new InvalidRequestException("Cannot set saltCode/saltEQ/stereoisomerCode for unknown compound");
        }
    }

    @Nullable
    public SaltCodeInfo saltCodeInfo(@Nullable DictionaryItemRef ref) {
        return ref != null ? dictionaryService.getSaltInfo(ref.getId()) : null;
    }
}

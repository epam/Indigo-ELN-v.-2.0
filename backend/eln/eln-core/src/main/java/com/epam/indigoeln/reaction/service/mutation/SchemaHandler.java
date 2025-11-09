package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.compound.entity.SampleEntity;
import com.epam.indigoeln.compound.service.CompoundService;
import com.epam.indigoeln.eln.mapper.DictionaryMapper;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.indigowrapper.IndigoReaction;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import com.epam.indigoeln.reaction.model.units.DensityUnit;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.epam.indigoeln.reaction.model.units.EnteredValue.DEFAULT_ONE;

@Slf4j
@Dependent
public class SchemaHandler extends AbstractMutationHandler {

    @Inject
    CompoundService compoundService;
    @Inject
    IndigoAPI indigo;
    @Inject
    DictionaryMapper dictionaryMapper;

    public void handle(Reaction reaction, ReactionMutation.SetScheme mutation) {
        // TODO match into existing inputs/outputs
        reaction.setInputs(new ArrayList<>());
        reaction.setOutputs(new ArrayList<>());

        IndigoReaction indigoReaction = indigo.loadReaction(mutation.molFile());
        for (IndigoMolecule reactant : indigoReaction.reactants()) {
            reaction.getInputs().add(createInputLine(reaction, reactant, ReactionRole.REACTANT));
        }
        for (IndigoMolecule catalyst : indigoReaction.catalysts()) {
            reaction.getInputs().add(createInputLine(reaction, catalyst, ReactionRole.CATALYST));
        }
        for (IndigoMolecule product : indigoReaction.products()) {
            reaction.getOutputs().add(createOutputLine(reaction, product));
        }
        adjustLimitingInput(reaction);
        reaction.setRxnfile(mutation.molFile());
    }

    public void handle(Reaction reaction, ReactionMutation.AddEmptyInput mutation) {
        reaction.getInputs().add(createInputLine(reaction, null, ReactionRole.REACTANT));
        adjustLimitingInput(reaction);
    }

    public void handle(Reaction reaction, ReactionMutation.AddInput mutation) {
        ReactionInput row = createInputLine(reaction, null, ReactionRole.REACTANT);
        reaction.getInputs().add(row);
        SampleEntity sample = compoundService.getSample(mutation.sampleId());
        setInputLineSample(row, sample);
        affectedRoles.add(row.getRole());
        adjustLimitingInput(reaction);
    }

    public void handle(Reaction reaction, ReactionMutation.RemoveInput mutation) {
        ReactionInput input = model.locate(mutation.input());
        reaction.getInputs().remove(input);
        affectedRoles.add(input.getRole());
        adjustLimitingInput(reaction);
    }

    public void handle(Reaction reaction, ReactionMutation.ResolveInputs mutation) {
        mutation.inputSamples().forEach((inputAnchor, sampleId) -> {
            ReactionInput row = model.locate(inputAnchor);
            SampleEntity sample = compoundService.getSample(sampleId);
            setInputLineSample(row, sample);
        });
    }

    private void setInputLineSample(ReactionInput row, SampleEntity sample) {
        row.setCompound(compoundService.realCompoundRef(sample.getCompound()));
        compoundsAffected = true;

        ReactionInputSample reactionInputSample = ReactionInputSample.create(row);
        reactionInputSample.setSampleId(sample.getId());
        reactionInputSample.setStrCode(sample.getStrCode());
        reactionInputSample.setDensity(EnteredValue.defaultValue(sample.getDensity(), DensityUnit.G_ML));
        reactionInputSample.setMolarity(EnteredValue.defaultValue(sample.getMolarity(), sample.getMolarityUnit()));
        reactionInputSample.setPurity(sample.getPurity() != null ? EnteredValue.defaultValue(sample.getPurity(), NoUnit.NO_UNIT) : DEFAULT_ONE);
        reactionInputSample.setHealthHazards(dictionaryMapper.itemToRefList(sample.getHealthHazards()));
        reactionInputSample.setComment(sample.getBatchComment());
        reactionInputSample.setChemicalName(sample.getChemicalName());
        row.setSamples(List.of(reactionInputSample));

        affectedRoles.add(row.getRole());
    }

    private ReactionInput createInputLine(Reaction reaction, @Nullable IndigoMolecule molecule, ReactionRole role) {
        ReactionInput row = ReactionInput.create(reaction, role);
        row.setCompound(molecule != null
                ? compoundService.virtualCompoundRef(molecule, null, null, null)
                : compoundService.unknownCompoundRef());
        compoundsAffected = true;
        row.setEq(DEFAULT_ONE);
        ReactionInputSample reactionInputSample = ReactionInputSample.create(row);
        reactionInputSample.setPurity(DEFAULT_ONE);
        row.setSamples(List.of(reactionInputSample));
        return row;
    }

    private ReactionOutput createOutputLine(Reaction reaction, IndigoMolecule molecule) {
        ReactionOutput row = ReactionOutput.create(reaction, reaction.getFinalOutput() != null ? ReactionOutputType.BY_PRODUCT : ReactionOutputType.FINAL);
        row.setCompound(compoundService.virtualCompoundRef(molecule, null, null, null));
        compoundsAffected = true;
        row.setEq(DEFAULT_ONE);
        row.setSamples(List.of());
        return row;
    }

    private void adjustLimitingInput(Reaction reaction) {
        if (!reaction.getInputs().isEmpty() && reaction.getLimitingInput() == null) {
            reaction.getInputs().getFirst().setLimiting(true);
        }
    }
}

package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.patch.ExperimentPatch;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.util.PatchTestUtil;
import com.epam.indigoeln.test.FeignUtil;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ExperimentModelPatchServiceTest {

    ReactionAnchor REACTION = new ReactionAnchor(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    ReactionAnchor REACTION_2 = new ReactionAnchor(UUID.fromString("00000000-0000-0000-0000-000000000002"));
    ReactionAnchor REACTION_3 = new ReactionAnchor(UUID.fromString("00000000-0000-0000-0000-000000000003"));
    InputAnchor INPUT = new InputAnchor(UUID.fromString("00000000-0000-0000-0000-000000000010"));

    ExperimentModelService service = new ExperimentModelService(FeignUtil.OBJECT_MAPPER);

    ExperimentSnapshot baseExperiment = new ExperimentSnapshot();
    ExperimentModel baseModel = new ExperimentModel();
    Reaction baseReaction = Reaction.create(baseModel, REACTION);
    ExperimentSnapshot experiment = new ExperimentSnapshot();
    ExperimentModel model = new ExperimentModel();
    Reaction reaction = Reaction.create(model, REACTION);

    @BeforeEach
    void setUp() {
        baseExperiment.setModel(baseModel);
        baseModel.setReactions(List.of(baseReaction));
        experiment.setModel(model);
        model.setReactions(List.of(reaction));
    }

    @Test
    void testEmptyPatch() throws Exception {
        makeAndVerifyPatch("""
                {}
        """);
    }

    @Test
    void testReactionAdded() throws Exception {
        Reaction reaction2 = Reaction.create(model, REACTION_2);
        model.setReactions(List.of(reaction, reaction2));
        makeAndVerifyPatch("""
                {"model": {"reactions": {">1": {"$new": {"anchor": "00000000-0000-0000-0000-000000000002", "rxnVersion": 0, "inputs": [], "outputs": [], "precursorReactantIds": []}}}}}
        """);
    }

    @Test
    void testReactionUpdated() throws Exception {
        reaction.setRxnfile("new");
        makeAndVerifyPatch("""
                {"model": {"reactions": {"0": {"rxnfile": {"$new": "new"}}}}}
        """);
    }

    @Test
    void testReactionDeleted() throws Exception {
        Reaction reaction2 = Reaction.create(model, REACTION_2);
        baseModel.setReactions(List.of(reaction, reaction2));
        model.setReactions(List.of(reaction));
        makeAndVerifyPatch("""
                {"model": {"reactions": {"1>": {"$old": {"anchor": "00000000-0000-0000-0000-000000000002", "rxnVersion": 0, "inputs": [], "outputs": [], "precursorReactantIds": []}}}}}
        """);
    }

    @Test
    void testReactionMovedAndChanged() throws Exception {
        Reaction baseReaction2 = Reaction.create(baseModel, REACTION_2);
        Reaction baseReaction3 = Reaction.create(baseModel, REACTION_3);
        Reaction reaction2 = Reaction.create(model, REACTION_2);
        Reaction reaction3 = Reaction.create(model, REACTION_3);
        reaction.setRxnfile("new");
        baseModel.setReactions(List.of(baseReaction, baseReaction2, baseReaction3));
        model.setReactions(List.of(reaction2, reaction, reaction3));
        makeAndVerifyPatch("""
                {"model": {"reactions": {"1>0": "$unchanged", "0>1": {"rxnfile": {"$new": "new"}}}}}
        """);
    }

    @Test
    void testEnteredValueCreated() throws Exception {
        ReactionInput baseInput = ReactionInput.create(reaction, ReactionRole.REACTANT, INPUT);
        baseReaction.setInputs(List.of(baseInput));
        ReactionInput input = ReactionInput.create(reaction, ReactionRole.REACTANT, INPUT);
        reaction.setInputs(List.of(input));
        input.setMol(EnteredValue.userEntered("10.0", MolUnit.MMOL, 1));
        makeAndVerifyPatch("""
                {"model": {"reactions": {"0": {"inputs": {"0": {"mol": {"$new": {"value": "10.0", "unit": "MMOL", "source": 1}}}}}}}}
        """);
    }

    @Test
    void testEnteredValueChanged() throws Exception {
        ReactionInput baseInput = ReactionInput.create(reaction, ReactionRole.REACTANT, INPUT);
        baseReaction.setInputs(List.of(baseInput));
        ReactionInput input = ReactionInput.create(reaction, ReactionRole.REACTANT, INPUT);
        reaction.setInputs(List.of(input));
        baseInput.setMol(EnteredValue.userEntered("15.0", MolUnit.MMOL, 1));
        input.setMol(EnteredValue.userEntered("10.0", MolUnit.MMOL, 1));
        makeAndVerifyPatch("""
                {"model": {"reactions": {"0": {"inputs": {"0": {"mol": {"value": {"$old": "15.0", "$new": "10.0"}}}}}}}}
        """);
    }

    @Test
    void testEnteredValueDeleted() throws Exception {
        ReactionInput baseInput = ReactionInput.create(reaction, ReactionRole.REACTANT, INPUT);
        baseReaction.setInputs(List.of(baseInput));
        ReactionInput input = ReactionInput.create(reaction, ReactionRole.REACTANT, INPUT);
        reaction.setInputs(List.of(input));
        baseInput.setMol(EnteredValue.userEntered("15.0", MolUnit.MMOL, 1));
        makeAndVerifyPatch("""
                {"model": {"reactions": {"0": {"inputs": {"0": {"mol": {"$old": {"value": "15.0", "unit": "MMOL", "source": 1}}}}}}}}
        """);
    }

    private void makeAndVerifyPatch(@Language("JSON") String expectedPatchStr) throws Exception {
        ExperimentPatch patch = service.createPatch(baseExperiment, experiment);
        String patchStr = FeignUtil.OBJECT_MAPPER.writeValueAsString(patch);
        System.out.println(expectedPatchStr.trim());
        System.out.println(patchStr);
        assertThat(patchStr).isEqualToIgnoringWhitespace(expectedPatchStr.trim());
        PatchTestUtil.verifyModelPatch(baseExperiment, patch, experiment, null);
    }
}

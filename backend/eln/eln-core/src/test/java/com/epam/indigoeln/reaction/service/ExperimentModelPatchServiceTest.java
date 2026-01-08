package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.epam.indigoeln.reaction.model.patch.ExperimentPatch;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.reaction.util.PatchTestUtil;
import com.epam.indigoeln.test.FeignUtil;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExperimentModelPatchServiceTest {

    ExperimentModelService service = new ExperimentModelService();

    ExperimentSnapshot baseExperiment = new ExperimentSnapshot();
    ExperimentModel baseModel = new ExperimentModel();
    Reaction baseReaction = Reaction.create(baseModel);
    ExperimentSnapshot experiment = new ExperimentSnapshot();
    ExperimentModel model = new ExperimentModel();
    Reaction reaction = Reaction.create(model);

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
    void testAttributeChange() throws Exception {
        model.setLastUsedAnchor(10);
        makeAndVerifyPatch("""
                {"model": {"lastUsedAnchor": {"$old": 1, "$new": 10}}}
        """);
    }

    @Test
    void testReactionAdded() throws Exception {
        Reaction reaction2 = Reaction.createWithAnchor(model, new Anchor.Reaction(10));
        model.setReactions(List.of(reaction, reaction2));
        makeAndVerifyPatch("""
                {"model": {"reactions": {">1": {"anchor": "R10", "rxnVersion": 0}}}}
        """);
    }

    @Test
    void testReactionUpdated() throws Exception {
        reaction.setRxnfile("new");
        makeAndVerifyPatch("""
                {"model": {"reactions": {"0": {"rxnfile": "new"}}}}
        """);
    }

    @Test
    void testReactionDeleted() throws Exception {
        model.setReactions(List.of());
        makeAndVerifyPatch("""
                {"model": {"reactions": {"0>": {"$old": {"anchor": "R1", "rxnVersion": 0}}}}}
        """);
    }

    @Test
    void testReactionMovedAndChanged() throws Exception {
        Reaction baseReaction2 = Reaction.createWithAnchor(baseModel, new Anchor.Reaction(2));
        Reaction baseReaction3 = Reaction.createWithAnchor(baseModel, new Anchor.Reaction(3));
        Reaction reaction2 = Reaction.createWithAnchor(model, new Anchor.Reaction(2));
        Reaction reaction3 = Reaction.createWithAnchor(model, new Anchor.Reaction(3));
        reaction.setRxnfile("new");
        baseModel.setReactions(List.of(baseReaction, baseReaction2, baseReaction3));
        model.setReactions(List.of(reaction2, reaction, reaction3));
        makeAndVerifyPatch("""
                {"model": {"reactions": {"1>0": "$unchanged", "0>1": {"rxnfile": "new"}}}}
        """);
    }

    @Test
    void testEnteredValueCreated() throws Exception {
        ReactionInput baseInput = ReactionInput.create(reaction, ReactionRole.REACTANT, new Anchor.Input(10));
        baseReaction.setInputs(List.of(baseInput));
        ReactionInput input = ReactionInput.create(reaction, ReactionRole.REACTANT, new Anchor.Input(10));
        reaction.setInputs(List.of(input));
        input.setMol(EnteredValue.userLastEntered(10.0, MolUnit.MMOL));
        makeAndVerifyPatch("""
                {"model": {"reactions": {"0": {"inputs": {"0": {"mol": {"value": 10.0, "unit": "MMOL", "source": "USER_LAST_ENTERED"}}}}}}}
        """);
    }

    @Test
    void testEnteredValueChanged() throws Exception {
        ReactionInput baseInput = ReactionInput.create(reaction, ReactionRole.REACTANT, new Anchor.Input(10));
        baseReaction.setInputs(List.of(baseInput));
        ReactionInput input = ReactionInput.create(reaction, ReactionRole.REACTANT, new Anchor.Input(10));
        reaction.setInputs(List.of(input));
        baseInput.setMol(EnteredValue.userLastEntered(15.0, MolUnit.MMOL));
        input.setMol(EnteredValue.userLastEntered(10.0, MolUnit.MMOL));
        makeAndVerifyPatch("""
                {"model": {"reactions": {"0": {"inputs": {"0": {"mol": {"value": {"$old": 15.0, "$new": 10.0}}}}}}}}
        """);
    }

    @Test
    void testEnteredValueDeleted() throws Exception {
        ReactionInput baseInput = ReactionInput.create(reaction, ReactionRole.REACTANT, new Anchor.Input(10));
        baseReaction.setInputs(List.of(baseInput));
        ReactionInput input = ReactionInput.create(reaction, ReactionRole.REACTANT, new Anchor.Input(10));
        reaction.setInputs(List.of(input));
        baseInput.setMol(EnteredValue.userLastEntered(15.0, MolUnit.MMOL));
        makeAndVerifyPatch("""
                {"model": {"reactions": {"0": {"inputs": {"0": {"mol": {"$old": {"value": 15.0, "unit": "MMOL", "source": "USER_LAST_ENTERED"}}}}}}}}
        """);
    }

    private void makeAndVerifyPatch(@Language("JSON") String expectedPatchStr) throws Exception {
        ExperimentPatch patch = service.createPatch(baseExperiment, experiment, MutationContext.createFull());
        String patchStr = FeignUtil.OBJECT_MAPPER.writeValueAsString(patch);
        System.out.println(expectedPatchStr.trim());
        System.out.println(patchStr);
        assertThat(patchStr).isEqualToIgnoringWhitespace(expectedPatchStr.trim());
        PatchTestUtil.verifyModelPatch(baseExperiment, patch, experiment, null);
    }
}

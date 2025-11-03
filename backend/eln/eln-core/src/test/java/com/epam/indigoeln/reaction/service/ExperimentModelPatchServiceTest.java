package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionInput;
import com.epam.indigoeln.reaction.model.patch.ExperimentModelPatch;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.epam.indigoeln.test.FeignUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExperimentModelPatchServiceTest {

    ExperimentModelPatchService service = new ExperimentModelPatchService();

    ExperimentModel base = new ExperimentModel();
    Reaction baseReaction = Reaction.create(base);
    ExperimentModel model = new ExperimentModel();
    Reaction reaction = Reaction.create(model);

    @BeforeEach
    void setUp() {
        base.setReactions(List.of(baseReaction));
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
                {"lastUsedAnchor": 10}
        """);
    }

    @Test
    void testReactionAdded() throws Exception {
        Reaction reaction2 = Reaction.createWithAnchor(model, new Anchor.Reaction(10));
        model.setReactions(List.of(reaction, reaction2));
        makeAndVerifyPatch("""
                {"reactions": {"1": {"xfrom": null, "anchor": "R10", "rxnFile": "", "rxnVersion": 0}}}
        """);
    }

    @Test
    void testReactionUpdated() throws Exception {
        reaction.setRxnfile("new");
        makeAndVerifyPatch("""
                {"reactions": {"0": {"rxnFile": "new"}}}
        """);
    }

    @Test
    void testReactionDeleted() throws Exception {
        model.setReactions(List.of());
        makeAndVerifyPatch("""
                {"reactions": {"0": null}}
        """);
    }

    @Test
    void testReactionMovedAndChanged() throws Exception {
        Reaction baseReaction2 = Reaction.createWithAnchor(base, new Anchor.Reaction(2));
        Reaction baseReaction3 = Reaction.createWithAnchor(base, new Anchor.Reaction(3));
        Reaction reaction2 = Reaction.createWithAnchor(model, new Anchor.Reaction(2));
        Reaction reaction3 = Reaction.createWithAnchor(model, new Anchor.Reaction(3));
        reaction.setRxnfile("new");
        base.setReactions(List.of(baseReaction, baseReaction2, baseReaction3));
        model.setReactions(List.of(reaction2, reaction, reaction3));
        makeAndVerifyPatch("""
                {"reactions": {"0": {"xfrom": 1}, "1": {"xfrom": 0, "rxnFile": "new"}}}
        """);
    }

    @Test
    void testEnteredValueCreated() throws Exception {
        ReactionInput baseInput = ReactionInput.createWithAnchor(reaction, new Anchor.Input(10));
        baseReaction.setInputs(List.of(baseInput));
        ReactionInput input = ReactionInput.createWithAnchor(reaction, new Anchor.Input(10));
        reaction.setInputs(List.of(input));
        input.setMol(EnteredValue.userLastEntered(10.0, MolUnit.MMOL));
        makeAndVerifyPatch("""
                {"reactions": {"0": {"inputs": {"0": {"mol": {"value": 10.0, "unit": "MMOL", "source": "USER_LAST_ENTERED", "conflict": false}}}}}}
        """);
    }

    @Test
    void testEnteredValueChanged() throws Exception {
        ReactionInput baseInput = ReactionInput.createWithAnchor(reaction, new Anchor.Input(10));
        baseReaction.setInputs(List.of(baseInput));
        ReactionInput input = ReactionInput.createWithAnchor(reaction, new Anchor.Input(10));
        reaction.setInputs(List.of(input));
        baseInput.setMol(EnteredValue.userLastEntered(15.0, MolUnit.MMOL));
        input.setMol(EnteredValue.userLastEntered(10.0, MolUnit.MMOL));
        makeAndVerifyPatch("""
                {"reactions": {"0": {"inputs": {"0": {"mol": {"value": 10.0}}}}}}
        """);
    }

    @Test
    void testEnteredValueDeleted() throws Exception {
        ReactionInput baseInput = ReactionInput.createWithAnchor(reaction, new Anchor.Input(10));
        baseReaction.setInputs(List.of(baseInput));
        ReactionInput input = ReactionInput.createWithAnchor(reaction, new Anchor.Input(10));
        reaction.setInputs(List.of(input));
        baseInput.setMol(EnteredValue.userLastEntered(15.0, MolUnit.MMOL));
        makeAndVerifyPatch("""
                {"reactions": {"0": {"inputs": {"0": {"mol": null}}}}}
        """);
    }

    private void makeAndVerifyPatch(@Language("JSON") String expectedPatchStr) throws JsonProcessingException {
        ExperimentModelPatch patch = service.createPatch(base, model);
        String patchStr = FeignUtil.OBJECT_MAPPER.writeValueAsString(patch);
        System.out.println(expectedPatchStr.trim());
        System.out.println(patchStr);
        assertThat(expectedPatchStr.trim()).isEqualToIgnoringWhitespace(patchStr);
        ExperimentModel appliedPatch = service.applyPatch(base, patch);
        assertThat(model).isEqualTo(appliedPatch);
    }
}

package com.epam.indigoeln.eln.util;

import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.units.EnteredValue;
import com.epam.indigoeln.reaction.model.units.MolUnit;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.intellij.lang.annotations.Language;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.epam.indigoeln.test.FeignUtil.OBJECT_MAPPER;
import static com.epam.indigoeln.test.FeignUtil.OBJECT_MAPPER_FORMATTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class JSONPatcherTest {

    ReactionAnchor REACTION = new ReactionAnchor(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    ReactionAnchor REACTION_2 = new ReactionAnchor(UUID.fromString("00000000-0000-0000-0000-000000000002"));
    ReactionAnchor REACTION_3 = new ReactionAnchor(UUID.fromString("00000000-0000-0000-0000-000000000003"));
    InputAnchor INPUT = new InputAnchor(UUID.fromString("00000000-0000-0000-0000-000000000010"));

    ExperimentSnapshot baseExperiment = new ExperimentSnapshot();
    ExperimentModel baseModel = new ExperimentModel();
    Reaction baseReaction = Reaction.create(baseModel, REACTION);
    ExperimentSnapshot experiment = new ExperimentSnapshot();
    ExperimentModel model = new ExperimentModel();
    Reaction reaction = Reaction.create(model, REACTION);

    JSONPatcher JSON_PATCHER = new JSONPatcher(Map.of(), Map.of(), Set.of(), OBJECT_MAPPER);
    JSONPatcher JSON_PATCHER_SET = new JSONPatcher(Map.of(List.of(), "anchor"), Map.of(), Set.of(), OBJECT_MAPPER);
    JSONPatcher JSON_PATCHER_LIST = new JSONPatcher(Map.of(), Map.of(List.of(), "anchor"), Set.of(), OBJECT_MAPPER);
    JSONPatcher JSON_PATCHER_LIST_INSIDE_LIST = new JSONPatcher(Map.of(), Map.of(List.of(), "key", List.of("#", "items"), "anchor"), Set.of(), OBJECT_MAPPER);
    JSONPatcher JSON_MODEL_PATCHER = new JSONPatcher(OBJECT_MAPPER);

    @BeforeEach
    void setUp() {
        baseExperiment.setModel(baseModel);
        experiment.setModel(model);
    }

    // Simple value
    //
    // Created:
    // A:  null
    // B:   "a"
    // ->   "a"
    //
    // Deleted:
    // A:  "a"
    // B:  null
    // ->  {"$old": "a"}
    //
    // Changed:
    // A:  "a"
    // B:  "b"
    // ->  {"$old": "a", "$new": "b"}

    @Test
    void testSimpleUnchanged() {
        verifySimple(
                "a",
                "a",
                """
                null
        """);
    }

    @Test
    void testSimpleNew() {
        verifySimple(
                null,
                "a",
                """
                {"$new": "a"}
        """);
    }

    @Test
    void testSimpleDeleted() {
        verifySimple(
                "a",
                null,
                """
                {"$old": "a"}
        """);
    }

    @Test
    void testSimpleUpdated() {
        verifySimple(
                "a",
                "b",
                """
                {"$old": "a", "$new": "b"}
        """);
    }

    // Object
    //
    // Created:
    // A:  null
    // B:  {"a": "b"}
    // ->  {"a": "b"}
    //
    // Deleted:
    // A:  {"a": "b"}
    // B:  null
    // ->  {"$old": {"a": "b"}}
    //
    // Updated:
    // A:  {"a": "b"}
    // B:  {"a": "c", "d": "e"}
    // ->  {"a": {"$old": "b", "$new": "c"}, "d": "e"}
    //
    // Object field updated:
    // A:  {"n": {"f": 1}}
    // B:  {"n": {"f": 2}}
    // ->  {"n": {"f": {"$old": 1, "$new": 2}}}

    @Test
    void testObjectUnchanged() {
        verifyObject(
                new TestObject("b"),
                new TestObject("b"),
                """
                    null
        """);
    }

    @Test
    void testObjectCreated() {
        verifyObject(
                null,
                new TestObject("b"),
                """
                    {"$new": {"a": "b"}}
        """);
    }

    @Test
    void testObjectDeleted() {
        verifyObject(
                new TestObject("b"),
                null,
                """
                    {"$old": {"a": "b"}}
        """);
    }

    @Test
    void testObjectUpdated() {
        verifyObject(
                new TestObject("b"),
                new TestObject("c", "e"),
                """
                    {"a": {"$old": "b", "$new": "c"}, "d": {"$new": "e"}}
        """);
    }

    @Test
    void testNestedObjectUpdated() {
        verifyObject(
                new TestObject(null, null, new NestedObject(1)),
                new TestObject(null, null, new NestedObject(2)),
                """
                    {"n": {"f": {"$old": 1, "$new": 2}}}
        """);
    }

    // Set (non-null set, where objects are identified by key; item positions are ignored)
    //
    // Unchanged:
    // A:  [{"anchor": "A1", "name": "a"}]
    // B:  [{"anchor": "A1", "name": "a"}]
    // ->  null
    //
    // Item inserted:
    // A:  []
    // B:  [{"anchor": "A1", "name": "a"}]
    // ->  {"A1": {"name": "a"}}
    //
    // Item deleted:
    // A:  [{"anchor": "A1", "name": "a"}]
    // B:  []
    // ->  {"1": {"$old": {"name": "a"}}
    //
    // Item updated:
    // A:  [{"anchor": "A2", "name": "b"}]
    // B:  [{"anchor": "A2", "name": "c"}]
    // ->  {"A2": {"$old": {"name": "b"}, "$new": {"name": "c"}}}

    @Test
    void testSetUnchanged() {
        verifySet(
                List.of(new Anchored("A1", "a")),
                List.of(new Anchored("A1", "a")),
                """
                    null
        """);
    }

    @Test
    void testSetInserted() {
        verifySet(
                List.of(new Anchored("A1", "a")),
                List.of(new Anchored("A1", "a"), new Anchored("A2", "b")),
                """
                    {"A2":{"$new":{"anchor":"A2","name":"b"}}}
        """);
    }

    @Test
    void testSetDeleted() {
        verifySet(
                List.of(new Anchored("A1", "a"), new Anchored("A2", "b")),
                List.of(new Anchored("A1", "a")),
                """
                    {"A2":{"$old":{"anchor":"A2","name":"b"}}}
        """);
    }

    @Test
    void testSetUpdated() {
        verifySet(
                List.of(new Anchored("A2", "b")),
                List.of(new Anchored("A2", "c")),
                """
                    {"A2":{"name":{"$old":"b","$new":"c"}}}
        """);
    }

    // List (non-null list, where items are identified by key; preserves item positions and detects repositions)
    //
    // Unchanged:
    // A:  [{"id": 1, "name": "a}]
    // B:  [{"id": 1, "name": "a}]
    // ->  null
    //
    // Item inserted:
    // A:  []
    // B:  [{"id": 1, "name": "a"}]
    // ->  {">0": {"name": "a"}}
    //
    // Item deleted:
    // A:  [{"id": 1, "name": "a"}]
    // B:  []
    // ->  {"0>": {"$old": {"name": "a"}}
    //
    // Item updated:
    // A:  [{"id": 2, "name": "b"}]
    // B:  [{"id": 2, "name": "c"}]
    // ->  {"2": {"$old": {"name": "b"}, "$new": {"name": "c"}}}
    //
    // Item repositioned:
    // A:  [{"id": 1, "name": "a"}, {"id": 2, "name": "b"}]
    // B:  [{"id": 2, "name": "b"}, {"id": 1, "name": "a"}]
    // ->  {"2>1": "$unchanged", "1>2": "$unchanged"}
    //
    // Item repositioned and updated:
    // A:  [{"id": 1, "name": "a"}, {"id": 2, "name": "b"}]
    // B:  [{"id": 2, "name": "b"}, {"id": 1, "name": "aa"}]
    // ->  {"2>1": {"name": {"$old": "a", "$new": "aa"}}, "1>2": "$unchanged"}

    @Test
    void testListUnchanged() {
        this.verifyList(
                List.of(new Anchored("A1", "a")),
                List.of(new Anchored("A1", "a")),
                """
                    null
                """
        );
    }

    @Test
    void testListInserted() {
        this.verifyList(
                List.of(new Anchored("A1", "a")),
                List.of(new Anchored("A1", "a"), new Anchored("A2", "b")),
                """
                    {">1":{"$new":{"anchor":"A2","name":"b"}}}
                """
        );
    }

    @Test
    void testListDeleted() {
        this.verifyList(
                List.of(new Anchored("A1", "a"), new Anchored("A2", "b")),
                List.of(new Anchored("A1", "a")),
                """
                    {"1>":{"$old":{"anchor":"A2","name":"b"}}}
                """
        );
    }

    @Test
    void testListUpdated() {
        this.verifyList(
                List.of(new Anchored("A1", "a")),
                List.of(new Anchored("A1", "b")),
                """
                    {"0":{"name":{"$old":"a","$new":"b"}}}
                """
        );
    }

    @Test
    void testListRepositioned() {
        this.verifyList(
                List.of(new Anchored("A1", "a"), new Anchored("A2", "b")),
                List.of(new Anchored("A2", "b"), new Anchored("A1", "a")),
                """
                    {"1>0":"$unchanged","0>1":"$unchanged"}
                """
        );
    }

    @Test
    void testListRepositionedAndUpdated() {
        this.verifyList(
                List.of(new Anchored("A1", "a"), new Anchored("A2", "b")),
                List.of(new Anchored("A2", "b"), new Anchored("A1", "aa")),
                """
                    {"1>0":"$unchanged","0>1":{"name":{"$old":"a","$new":"aa"}}}
                """
        );
    }

    @Test
    void testListInsideListInserted() {
        this.doVerify(
                List.of(new InnerList("K1", List.of(new Anchored("A1", "a")))),
                List.of(new InnerList("K1", List.of(new Anchored("A1", "a"), new Anchored("A2", "b")))),
                JSON_PATCHER_LIST_INSIDE_LIST,
                """
                        {"0":{"items":{">1":{"$new":{"anchor":"A2","name":"b"}}}}}
                """
        );
    }

    // Experiment Model

    @Test
    void testEmptyPatch() throws Exception {
        verifyModel("""
                {}
        """);
    }

    @Test
    void testReactionAdded() throws Exception {
        Reaction.create(model, REACTION_2);
        verifyModel("""
                {"model":{"reactions":{">1":{"$new":{"anchor":"00000000-0000-0000-0000-000000000002","inputs":[],"outputs":[],"precursorReactantIds":[]}}}}}
        """);
    }

    @Test
    void testReactionUpdated() throws Exception {
        reaction.setRxnfile("new");
        verifyModel("""
                {"model":{"reactions":{"0":{"rxnfile":{"$new":"new"}}}}}
        """);
    }

    @Test
    void testReactionDeleted() throws Exception {
        Reaction.create(baseModel, REACTION_2);
        model.setReactions(List.of(reaction));
        verifyModel("""
                {"model":{"reactions":{"1>":{"$old":{"anchor":"00000000-0000-0000-0000-000000000002","inputs":[],"outputs":[],"precursorReactantIds":[]}}}}}
        """);
    }

    @Test
    void testReactionMovedAndChanged() throws Exception {
        Reaction.create(baseModel, REACTION_2);
        Reaction.create(baseModel, REACTION_3);
        Reaction reaction2 = Reaction.create(model, REACTION_2);
        Reaction reaction3 = Reaction.create(model, REACTION_3);
        reaction.setRxnfile("new");
        model.setReactions(List.of(reaction2, reaction, reaction3));
        verifyModel("""
                {"model":{"reactions":{"1>0":"$unchanged","0>1":{"rxnfile":{"$new":"new"}}}}}
        """);
    }

    @Test
    void testEnteredValueCreated() throws Exception {
        ReactionInput baseInput = ReactionInput.create(reaction, ReactionRole.REACTANT, INPUT, new CompoundRef.Unknown());
        baseReaction.setInputs(List.of(baseInput));
        ReactionInput input = ReactionInput.create(reaction, ReactionRole.REACTANT, INPUT, new CompoundRef.Unknown());
        reaction.setInputs(List.of(input));
        input.setMol(EnteredValue.userEntered("10.0", MolUnit.MMOL, 1));
        verifyModel("""
                {"model":{"reactions":{"0":{"inputs":{"0":{"mol":{"$new":{"value":"10.0","unit":"MMOL","source":1}}}}}}}}
        """);
    }

    @Test
    void testEnteredValueChanged() throws Exception {
        ReactionInput baseInput = ReactionInput.create(reaction, ReactionRole.REACTANT, INPUT, new CompoundRef.Unknown());
        baseReaction.setInputs(List.of(baseInput));
        ReactionInput input = ReactionInput.create(reaction, ReactionRole.REACTANT, INPUT, new CompoundRef.Unknown());
        reaction.setInputs(List.of(input));
        baseInput.setMol(EnteredValue.userEntered("15.0", MolUnit.MMOL, 1));
        input.setMol(EnteredValue.userEntered("10.0", MolUnit.MMOL, 1));
        verifyModel("""
                {"model":{"reactions":{"0":{"inputs":{"0":{"mol":{"value":{"$old":"15.0","$new":"10.0"}}}}}}}}
        """);
    }

    @Test
    void testEnteredValueDeleted() throws Exception {
        ReactionInput baseInput = ReactionInput.create(reaction, ReactionRole.REACTANT, INPUT, new CompoundRef.Unknown());
        baseReaction.setInputs(List.of(baseInput));
        ReactionInput input = ReactionInput.create(reaction, ReactionRole.REACTANT, INPUT, new CompoundRef.Unknown());
        reaction.setInputs(List.of(input));
        baseInput.setMol(EnteredValue.userEntered("15.0", MolUnit.MMOL, 1));
        verifyModel("""
                {"model":{"reactions":{"0":{"inputs":{"0":{"mol":{"$old":{"value":"15.0","unit":"MMOL","source":1}}}}}}}}
        """);
    }

    @Test
    void testEnteredValueOverwritten() throws Exception {
        ReactionInput baseInput = ReactionInput.create(reaction, ReactionRole.REACTANT, INPUT, new CompoundRef.Unknown());
        baseReaction.setInputs(List.of(baseInput));
        ReactionInput input = ReactionInput.create(reaction, ReactionRole.REACTANT, INPUT, new CompoundRef.Unknown());
        reaction.setInputs(List.of(input));
        baseInput.setMol(EnteredValue.userEntered("15.0", MolUnit.MMOL, 1));
        input.setMol(EnteredValue.userEntered("10.0", MolUnit.MMOL, 1).withOverwritten(true));
        verifyModel("""
                {"model":{"reactions":{"0":{"inputs":{"0":{"mol":{"value":{"$old":"15.0","$new":"10.0"},"overwritten":{"$new":true}}}}}}}}
        """, false);
    }

    @Test
    void testEnteredValueOverwrittenWithEmpty() throws Exception {
        ReactionInput baseInput = ReactionInput.create(reaction, ReactionRole.REACTANT, INPUT, new CompoundRef.Unknown());
        baseReaction.setInputs(List.of(baseInput));
        ReactionInput input = ReactionInput.create(reaction, ReactionRole.REACTANT, INPUT, new CompoundRef.Unknown());
        reaction.setInputs(List.of(input));
        baseInput.setMol(EnteredValue.userEntered("15.0", MolUnit.MMOL, 1));
        input.setMol(EnteredValue.<MolUnit>empty().withOverwritten(true));
        verifyModel("""
                {"model":{"reactions":{"0":{"inputs":{"0":{"mol":{"value":{"$old":"15.0"},"unit":{"$old":"MMOL"},"source":{"$old":1},"overwritten":{"$new":true}}}}}}}}
        """, false);
    }

    private void verifySimple(@Nullable String oldValue, @Nullable String newValue, @Language("JSON") String expectedStr) {
        doVerify(oldValue, newValue, JSON_PATCHER, expectedStr);
    }

    private void verifyObject(TestObject oldValue, TestObject newValue, @Language("JSON") String expectedStr) {
        doVerify(oldValue, newValue, JSON_PATCHER, expectedStr);
    }

    private void verifySet(List<Anchored> oldValue, List<Anchored> newValue, @Language("JSON") String expectedStr) {
        doVerify(oldValue, newValue, JSON_PATCHER_SET, expectedStr);
    }

    private void verifyList(List<Anchored> oldValue, List<Anchored> newValue, @Language("JSON") String expectedStr) {
        doVerify(oldValue, newValue, JSON_PATCHER_LIST, expectedStr);
    }

    private void verifyModel(@Language("JSON") String expectedPatchStr) throws Exception {
        verifyModel(expectedPatchStr, true);
    }

    private void verifyModel(@Language("JSON") String expectedPatchStr, boolean verifyPatchApplication) throws Exception {
        JsonNode before = OBJECT_MAPPER.valueToTree(baseExperiment);
        JsonNode after = OBJECT_MAPPER.valueToTree(experiment);
        JsonNode patch = JSON_MODEL_PATCHER.createTopLevel(before, after);
        String patchStr = OBJECT_MAPPER.writeValueAsString(patch);
        assertThat(patchStr).isEqualToIgnoringWhitespace(expectedPatchStr.trim());
        if (verifyPatchApplication) {
            JsonNode applied = JSON_MODEL_PATCHER.apply(before, patch);
            assertJSONEquals(applied, after);
            JsonNode reversed = JSON_MODEL_PATCHER.reverse(after, patch);
            assertJSONEquals(reversed, before);
        }
    }

    @SneakyThrows
    private <T> void doVerify(T oldValue, T newValue, JSONPatcher jsonPatcher, @Language("JSON") String expectedStr) {
        JsonNode oldJSON = OBJECT_MAPPER.valueToTree(oldValue);
        JsonNode newJSON = OBJECT_MAPPER.valueToTree(newValue);
        JsonNode patchJSON = jsonPatcher.create(oldJSON, newJSON);
        String patchStr = OBJECT_MAPPER.writeValueAsString(patchJSON);
        String expectedStr1 = OBJECT_MAPPER.writeValueAsString(OBJECT_MAPPER.readValue(expectedStr, JsonNode.class));
        assertThat(patchStr).isEqualTo(expectedStr1);
        doVerifyJSON(patchJSON, expectedStr1);
        doVerifyPatchApplication(oldValue, newValue, patchJSON, jsonPatcher);
    }

    @SneakyThrows
    private void doVerifyJSON(JsonNode patchJSON, String expectedJSON) {
        String json = OBJECT_MAPPER.writeValueAsString(patchJSON);
        assertThat(json).isEqualToIgnoringWhitespace(expectedJSON.trim());
    }

    @SneakyThrows
    private <T> void doVerifyPatchApplication(T oldValue, T newValue, JsonNode patchJSON, JSONPatcher jsonPatcher) {
        JsonNode oldValueJSON = OBJECT_MAPPER.valueToTree(oldValue);
        JsonNode newValueJSON = OBJECT_MAPPER.valueToTree(newValue);
        JsonNode restoredJSON = jsonPatcher.apply(oldValueJSON, patchJSON);
        assertJSONEquals(restoredJSON, newValueJSON);
        JsonNode revertedJSON = jsonPatcher.reverse(newValueJSON, patchJSON);
        assertJSONEquals(revertedJSON, oldValueJSON);
    }

    @SneakyThrows
    private static void assertJSONEquals(JsonNode actual, JsonNode expected) {
        if (!actual.equals(expected)) {
            fail("expected:\n%s\n but was:\n%s", OBJECT_MAPPER_FORMATTED.writeValueAsString(expected), OBJECT_MAPPER_FORMATTED.writeValueAsString(actual));
        }
    }
}

@Data
@NoArgsConstructor(onConstructor_ = @JsonCreator)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
class TestObject {

    private String a;
    private String d;
    private NestedObject n;

    TestObject(String a) {
        this(a, null, null);
    }

    TestObject(String a, String d) {
        this(a, d, null);
    }
}

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
class NestedObject {

    private Integer f;
}

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
class Anchored {

    private String anchor;
    private String name;
}

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
class InnerList {

    private String key;
    private List<Anchored> items;
}

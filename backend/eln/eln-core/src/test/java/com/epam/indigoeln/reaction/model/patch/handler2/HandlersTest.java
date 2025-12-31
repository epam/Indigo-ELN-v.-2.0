package com.epam.indigoeln.reaction.model.patch.handler2;

import com.epam.indigoeln.reaction.model.patch.ListPatch;
import com.epam.indigoeln.reaction.util.JSONPatcher;
import com.epam.indigoeln.reaction.util.PatchTestUtil;
import com.epam.indigoeln.reaction.util.SerializerUtils;
import com.epam.indigoeln.test.FeignUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.intellij.lang.annotations.Language;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class HandlersTest {

    DiffHandler<String, String> stringHandler = new DefaultDiffHandler<>();
    DiffHandler<TestObject, TestObjectDiff> objectHandler = new TestObjectDiffHandler();
    DiffHandler<List<Anchored>, Map<String, Patched<AnchoredDiff>>> setHandler = new SetDiffHandler<>(Anchored::getAnchor, new AnchoredDiffHandler());
    DiffHandler<List<Anchored>, ListPatch<AnchoredDiffForList>> listHandler = new ListDiffHandler<>(Anchored::getAnchor, new AnchoredDiffForListHandler());

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
                null,
                """
                null
        """);
    }

    @Test
    void testSimpleNew() {
        verifySimple(
                null,
                "a",
                Patched.created("a"),
                """
                "a"
        """);
    }

    @Test
    void testSimpleDeleted() {
        verifySimple(
                "a",
                null,
                Patched.deleted("a"),
                """
                {"$old": "a"}
        """);
    }

    @Test
    void testSimpleUpdated() {
        verifySimple(
                "a",
                "b",
                Patched.updated("a", "b"),
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
                null,
                """
                    null
        """);
    }

    @Test
    void testObjectCreated() {
        verifyObject(
                null,
                new TestObject("b"),
                Patched.created(new TestObjectDiff(Patched.verbatim("b"), null, null)),
                """
                    {"a": "b"}
        """);
    }

    @Test
    void testObjectDeleted() {
        verifyObject(
                new TestObject("b"),
                null,
                Patched.deleted(new TestObjectDiff(Patched.verbatim("b"), null, null)),
                """
                    {"$old": {"a": "b"}}
        """);
    }

    @Test
    void testObjectUpdated() {
        verifyObject(
                new TestObject("b"),
                new TestObject("c", "e"),
                Patched.verbatim(new TestObjectDiff(Patched.updated("b", "c"), Patched.created("e"), null)),
                """
                    {"a": {"$old": "b", "$new": "c"}, "d": "e"}
        """);
    }

    @Test
    void testNestedObjectUpdated() {
        verifyObject(
                new TestObject(null, null, new NestedObject(1)),
                new TestObject(null, null, new NestedObject(2)),
                Patched.verbatim(new TestObjectDiff(null, null, Patched.verbatim(new NestedObjectDiff(Patched.updated(1, 2))))),
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
                null,
                """
                    null
        """);
    }

    @Test
    void testSetInserted() {
        verifySet(
                List.of(),
                List.of(new Anchored("A1", "a")),
                Patched.verbatim(Map.of("A1", Patched.verbatim(new AnchoredDiff(Patched.verbatim("a"))))),
                """
                    {"A1": {"name": "a"}}
        """);
    }

    @Test
    void testSetDeleted() {
        verifySet(
                List.of(new Anchored("A1", "a")),
                List.of(),
                Patched.verbatim(Map.of("A1", Patched.deleted(new AnchoredDiff(Patched.verbatim("a"))))),
                """
                    {"A1": {"$old": {"name": "a"}}}
        """);
    }

    @Test
    void testSetUpdated() {
        verifySet(
                List.of(new Anchored("A2", "b")),
                List.of(new Anchored("A2", "c")),
                Patched.verbatim(Map.of("A2", Patched.verbatim(new AnchoredDiff(Patched.updated("b", "c"))))),
                """
                    {"A2": {"name": {"$old": "b", "$new": "c"}}}
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
                null,
                """
                    null
                """
        );
    }

    @Test
    void testListInserted() {
        this.verifyList(
                List.of(),
                List.of(new Anchored("A1", "a")),
                Patched.verbatim(new ListPatch<>(List.of(
                        new ListPatch.Item<>(
                                null, 0,
                                Patched.verbatim(new AnchoredDiffForList(Patched.verbatim("A1"), Patched.verbatim("a")))
                        )
                ))),
                """
                    {">0": {"anchor": "A1", "name": "a"}}
                """
        );
    }

    @Test
    void testListDeleted() {
        this.verifyList(
                List.of(new Anchored("A1", "a")),
                List.of(),
                Patched.verbatim(new ListPatch<>(List.of(
                        new ListPatch.Item<>(
                                0, null,
                                Patched.deleted(new AnchoredDiffForList(Patched.verbatim("A1"), Patched.verbatim("a")))
                        )
                ))),
                """
                    {"0>": {"$old": {"anchor": "A1", "name": "a"}}}
                """
        );
    }

    @Test
    void testListUpdated() {
        this.verifyList(
                List.of(new Anchored("A1", "a")),
                List.of(new Anchored("A1", "b")),
                Patched.verbatim(new ListPatch<>(List.of(
                        new ListPatch.Item<>(
                                0, 0,
                                Patched.verbatim(new AnchoredDiffForList(null, Patched.updated("a", "b")))
                        )
                ))),
                """
                    {"0": {"name": {"$old": "a", "$new": "b"}}}
                """
        );
    }

    @Test
    void testListRepositioned() {
        this.verifyList(
                List.of(new Anchored("A1", "a"), new Anchored("A2", "b")),
                List.of(new Anchored("A2", "b"), new Anchored("A1", "a")),
                Patched.verbatim(new ListPatch<>(List.of(
                        new ListPatch.Item<>(
                                1, 0,
                                null
                        ),
                        new ListPatch.Item<>(
                                0, 1,
                                null
                        )
                ))),
                """
                    {"1>0": "$unchanged", "0>1": "$unchanged"}
                """
        );
    }

    @Test
    void testListRepositionedAndUpdated() {
        this.verifyList(
                List.of(new Anchored("A1", "a"), new Anchored("A2", "b")),
                List.of(new Anchored("A2", "b"), new Anchored("A1", "aa")),
                Patched.verbatim(new ListPatch<>(List.of(
                        new ListPatch.Item<>(
                                1, 0,
                                null
                        ),
                        new ListPatch.Item<>(
                                0, 1,
                                Patched.verbatim(new AnchoredDiffForList(null, Patched.updated("a", "aa")))
                        )
                ))),
                """
                    {"1>0": "$unchanged", "0>1": {"name": {"$old": "a", "$new": "aa"}}}
                """
        );
    }

    private void verifySimple(@Nullable String oldValue, @Nullable String newValue, Patched<String> expected, @Language("JSON") String expectedJSON) {
        doVerify(oldValue, newValue, stringHandler, new TypeReference<>() {}, new TypeReference<>() {}, new JSONPatcher(Map.of(), Map.of()), expected, expectedJSON);
    }

    private void verifyObject(TestObject oldValue, TestObject newValue, Patched<TestObjectDiff> expected, @Language("JSON") String expectedJSON) {
        doVerify(oldValue, newValue, objectHandler, new TypeReference<>() {}, new TypeReference<>() {}, new JSONPatcher(Map.of(), Map.of()), expected, expectedJSON);
    }

    private void verifySet(List<Anchored> oldValue, List<Anchored> newValue, Patched<Map<String, Patched<AnchoredDiff>>> expected, @Language("JSON") String expectedJSON) {
        doVerify(oldValue, newValue, setHandler, new TypeReference<>() {}, new TypeReference<>() {}, new JSONPatcher(Map.of("$", "anchor"), Map.of()), expected, expectedJSON);
    }

    private void verifyList(List<Anchored> oldValue, List<Anchored> newValue, Patched<ListPatch<AnchoredDiffForList>> expected, @Language("JSON") String expectedJSON) {
        doVerify(oldValue, newValue, listHandler, new TypeReference<>() {}, new TypeReference<>() {}, new JSONPatcher(Map.of(), Map.of("$", "anchor")), expected, expectedJSON);
    }

    @SneakyThrows
    private <T, P, PP extends Patched<P>> void doVerify(T oldValue, T newValue, DiffHandler<T, P> handler, TypeReference<T> typeReference, TypeReference<PP> patchTypeReference, JSONPatcher jsonPatcher, Patched<P> expected, @Language("JSON") String expectedJSON) {
        Patched<P> patch = handler.compare(oldValue, newValue);
        System.out.println("patch = " + patch);
        System.out.println("expected = " + expected);
        assertThat(patch).isEqualTo(expected);
        JavaType patchType = FeignUtil.OBJECT_MAPPER.getTypeFactory().constructType(patchTypeReference);
        SerializerUtils.ROOT_TYPE.set(patchType);
        try {
            ObjectWriter patchWriter = FeignUtil.OBJECT_MAPPER.writerFor(patchType);
            ObjectReader patchReader = FeignUtil.OBJECT_MAPPER.readerFor(patchType);
            JavaType type = FeignUtil.OBJECT_MAPPER.getTypeFactory().constructType(typeReference);
            SerializerUtils.ROOT_TYPE.set(type);
            ObjectWriter writer = FeignUtil.OBJECT_MAPPER.writerFor(type);
            ObjectReader reader = FeignUtil.OBJECT_MAPPER.readerFor(type);
            doVerifyJSON(patch, expectedJSON, patchWriter, patchReader);
            doVerifyPatchApplication(oldValue, newValue, patch, writer, patchWriter, jsonPatcher);
        } finally {
            SerializerUtils.ROOT_TYPE.remove();
        }
    }

    @SneakyThrows
    private <P> void doVerifyJSON(P patch, String expectedJSON, ObjectWriter patchWriter, ObjectReader patchReader) {
        String json = patchWriter.writeValueAsString(patch);
        assertThat(json).isEqualToIgnoringWhitespace(expectedJSON.trim());
        P restored = patchReader.readValue(json);
        assertThat(restored).isEqualTo(patch);
    }

    @SneakyThrows
    private <T, P> void doVerifyPatchApplication(T oldValue, T newValue, Patched<P> patch, ObjectWriter writer, ObjectWriter patchWriter, JSONPatcher jsonPatcher) {
        JsonNode oldValueJSON = FeignUtil.OBJECT_MAPPER.readTree(writer.writeValueAsString(oldValue));
        JsonNode newValueJSON = FeignUtil.OBJECT_MAPPER.readTree(writer.writeValueAsString(newValue));
        JsonNode patchJSON = FeignUtil.OBJECT_MAPPER.readTree(patchWriter.writeValueAsString(patch));
        System.out.println("doVerifyPatchApplication: oldValue = " + oldValueJSON + ", is null = " + (oldValueJSON == null));
        System.out.println("doVerifyPatchApplication: newValue = " + newValueJSON + ", is null = " + (newValueJSON == null));
        System.out.println("doVerifyPatchApplication: patch = " + patchJSON + ", is null = " + (patchJSON == null));
        JsonNode restoredJSON = jsonPatcher.restoreWithJSON(oldValueJSON, patchJSON);
        System.out.println("doVerifyPatchApplication: restored = " + restoredJSON + ", is null = " + (restoredJSON == null));
        assertThat(PatchTestUtil.minimizeJSON(restoredJSON)).isEqualTo(PatchTestUtil.minimizeJSON(newValueJSON));
    }
}

@Data
@AllArgsConstructor
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
class NestedObject {

    private Integer f;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
class TestObjectDiff {

    private Patched<String> a;
    private Patched<String> d;
    private Patched<NestedObjectDiff> n;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
class NestedObjectDiff {

    private Patched<Integer> f;
}

class TestObjectDiffHandler extends AbstractDiffHandler<TestObject, TestObjectDiff> {

    private static final DefaultDiffHandler<String> STRING_HANDLER = DefaultDiffHandler.instance();
    private static final NestedObjectDiffHandler NESTED_HANDLER = new NestedObjectDiffHandler();

    @Override
    @Nullable
    protected Patched<TestObjectDiff> doCompare(@Nullable TestObject a, TestObject b) {
        TestObjectDiff diff = new TestObjectDiff();
        diff.setA(STRING_HANDLER.compare(a != null ? a.getA() : null, b.getA()));
        diff.setD(STRING_HANDLER.compare(a != null ? a.getD(): null, b.getD()));
        diff.setN(NESTED_HANDLER.compare(a != null ? a.getN(): null, b.getN()));
        return diff.getA() != null || diff.getD() != null || diff.getN() != null ? Patched.verbatim(diff) : null;
    }
}

class NestedObjectDiffHandler extends AbstractDiffHandler<NestedObject, NestedObjectDiff> {

    private static final DefaultDiffHandler<Integer> INTEGER_HANDLER = DefaultDiffHandler.instance();

    @Override
    protected @Nullable Patched<NestedObjectDiff> doCompare(@Nullable NestedObject a, NestedObject b) {
        NestedObjectDiff diff = new NestedObjectDiff();
        diff.setF(INTEGER_HANDLER.compare(a != null ? a.getF() : null, b.getF()));
        return diff.getF() != null ? Patched.verbatim(diff) : null;
    }
}

@Data
@AllArgsConstructor
class Anchored {

    private String anchor;
    private String name;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
class AnchoredDiff {

    private Patched<String> name;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
class AnchoredDiffForList {

    private Patched<String> anchor;
    private Patched<String> name;
}

class AnchoredDiffHandler extends AbstractDiffHandler<Anchored, AnchoredDiff> {

    private static final DefaultDiffHandler<String> STRING_HANDLER = DefaultDiffHandler.instance();

    @Override
    protected @Nullable Patched<AnchoredDiff> doCompare(@Nullable Anchored a, Anchored b) {
        AnchoredDiff diff = new AnchoredDiff();
        diff.setName(STRING_HANDLER.compare(a != null ? a.getName() : null, b.getName()));
        return diff.getName() != null ? Patched.verbatim(diff) : null;
    }
}

class AnchoredDiffForListHandler extends AbstractDiffHandler<Anchored, AnchoredDiffForList> {

    private static final DefaultDiffHandler<String> STRING_HANDLER = DefaultDiffHandler.instance();

    @Override
    protected @Nullable Patched<AnchoredDiffForList> doCompare(@Nullable Anchored a, Anchored b) {
        AnchoredDiffForList diff = new AnchoredDiffForList();
        diff.setAnchor(STRING_HANDLER.compare(a != null ? a.getAnchor() : null, b.getAnchor()));
        diff.setName(STRING_HANDLER.compare(a != null ? a.getName() : null, b.getName()));
        return diff.getName() != null ? Patched.verbatim(diff) : null;
    }
}

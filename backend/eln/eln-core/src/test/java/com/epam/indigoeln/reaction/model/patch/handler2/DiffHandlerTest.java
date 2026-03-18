package com.epam.indigoeln.reaction.model.patch.handler2;

import com.epam.indigoeln.reaction.model.patch.ListPatch;
import com.epam.indigoeln.reaction.util.JSONPatcher;
import com.epam.indigoeln.reaction.util.PatchTestUtil;
import com.epam.indigoeln.reaction.util.SerializerUtils;
import com.epam.indigoeln.test.FeignUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.*;
import org.intellij.lang.annotations.Language;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DiffHandlerTest {

    DiffHandler<String, String> stringHandler = DefaultDiffHandler.instance();
    DiffHandler<TestObject, TestObjectDiff> objectHandler = new TestObjectDiffHandler();
    DiffHandler<List<Anchored>, Map<String, Patched<Anchored, AnchoredDiff>>> setHandler = new SetDiffHandler<>(Anchored::getAnchor, new AnchoredDiffHandler());
    DiffHandler<List<Anchored>, ListPatch<Anchored, AnchoredDiffForList>> listHandler = new ListDiffHandler<>(Anchored::getAnchor, new AnchoredDiffForListHandler());
    DiffHandler<List<InnerList>, ListPatch<InnerList, InnerListPatch>> listInsideListHandler = new ListDiffHandler<>(InnerList::getKey, new InnerListHandler(new ListDiffHandler<>(Anchored::getAnchor, new AnchoredDiffForListHandler())));

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
                {"$new": "a"}
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
                Patched.replaced("a", "b"),
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
                Patched.created(new TestObject("b")),
                """
                    {"$new": {"a": "b"}}
        """);
    }

    @Test
    void testObjectDeleted() {
        verifyObject(
                new TestObject("b"),
                null,
                Patched.deleted(new TestObject("b")),
                """
                    {"$old": {"a": "b"}}
        """);
    }

    @Test
    void testObjectUpdated() {
        verifyObject(
                new TestObject("b"),
                new TestObject("c", "e"),
                Patched.updated(new TestObjectDiff(Patched.replaced("b", "c"), Patched.created("e"), null)),
                """
                    {"a": {"$old": "b", "$new": "c"}, "d": {"$new": "e"}}
        """);
    }

    @Test
    void testNestedObjectUpdated() {
        verifyObject(
                new TestObject(null, null, new NestedObject(1)),
                new TestObject(null, null, new NestedObject(2)),
                Patched.updated(new TestObjectDiff(null, null, Patched.updated(new NestedObjectDiff(Patched.replaced(1, 2))))),
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
                List.of(new Anchored("A1", "a")),
                List.of(new Anchored("A1", "a"), new Anchored("A2", "b")),
                Patched.updated(
                        Map.of("A2", Patched.created(new Anchored("A2", "b")))
                ),
                """
                    {"A2": {"$new": {"anchor": "A2", "name": "b"}}}
        """);
    }

    @Test
    void testSetDeleted() {
        verifySet(
                List.of(new Anchored("A1", "a"), new Anchored("A2", "b")),
                List.of(new Anchored("A1", "a")),
                Patched.updated(
                        Map.of("A2", Patched.deleted(new Anchored("A2", "b")))
                ),
                """
                    {"A2": {"$old": {"anchor": "A2", "name": "b"}}}
        """);
    }

    @Test
    void testSetUpdated() {
        verifySet(
                List.of(new Anchored("A2", "b")),
                List.of(new Anchored("A2", "c")),
                Patched.updated(Map.of("A2", Patched.updated(new AnchoredDiff(Patched.replaced("b", "c"))))),
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
                List.of(new Anchored("A1", "a")),
                List.of(new Anchored("A1", "a"), new Anchored("A2", "b")),
                Patched.updated(ListPatch.of(
                        new ListPatch.Item<>(null, 1, Patched.created(new Anchored("A2", "b")))
                )),
                """
                    {">1": {"$new": {"anchor": "A2", "name": "b"}}}
                """
        );
    }

    @Test
    void testListDeleted() {
        this.verifyList(
                List.of(new Anchored("A1", "a"), new Anchored("A2", "b")),
                List.of(new Anchored("A1", "a")),
                Patched.updated(ListPatch.of(
                        new ListPatch.Item<>(1, null, Patched.deleted(new Anchored("A2", "b")))
                )),
                """
                    {"1>": {"$old": {"anchor": "A2", "name": "b"}}}
                """
        );
    }

    @Test
    void testListUpdated() {
        this.verifyList(
                List.of(new Anchored("A1", "a")),
                List.of(new Anchored("A1", "b")),
                Patched.updated(ListPatch.of(
                        new ListPatch.Item<>(
                                0, 0,
                                Patched.updated(new AnchoredDiffForList(null, Patched.replaced("a", "b")))
                        )
                )),
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
                Patched.updated(ListPatch.of(
                        new ListPatch.Item<>(
                                1, 0,
                                null
                        ),
                        new ListPatch.Item<>(
                                0, 1,
                                null
                        )
                )),
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
                Patched.updated(ListPatch.of(
                        new ListPatch.Item<>(
                                1, 0,
                                null
                        ),
                        new ListPatch.Item<>(
                                0, 1,
                                Patched.updated(new AnchoredDiffForList(null, Patched.replaced("a", "aa")))
                        )
                )),
                """
                    {"1>0": "$unchanged", "0>1": {"name": {"$old": "a", "$new": "aa"}}}
                """
        );
    }

    @Test
    void testListInsideListInserted() {
        this.doVerify(
                List.of(new InnerList("K1", List.of(new Anchored("A1", "a")))),
                List.of(new InnerList("K1", List.of(new Anchored("A1", "a"), new Anchored("A2", "b")))),
                listInsideListHandler,
                new TypeReference<>() {},
                new TypeReference<>() {},
                new JSONPatcher(Map.of(), Map.of("$", "K1", "$.#.items", "anchor")),
                Patched.updated(ListPatch.of(
                        new ListPatch.Item<>(
                                0, 0,
                                Patched.updated(new InnerListPatch(
                                        null,
                                        Patched.updated(ListPatch.of(
                                                new ListPatch.Item<>(null, 1, Patched.created(new Anchored("A2", "b")))
                                        ))
                                ))
                        )
                )),
                """
                        {"0": {"items": {">1": {"$new": {"anchor": "A2", "name": "b"}}}}}
                """
        );
    }

    private void verifySimple(@Nullable String oldValue, @Nullable String newValue, Patched<String, String> expected, @Language("JSON") String expectedJSON) {
        doVerify(oldValue, newValue, stringHandler, new TypeReference<>() {}, new TypeReference<>() {}, new JSONPatcher(Map.of(), Map.of()), expected, expectedJSON);
    }

    private void verifyObject(TestObject oldValue, TestObject newValue, Patched<TestObject, TestObjectDiff> expected, @Language("JSON") String expectedJSON) {
        doVerify(oldValue, newValue, objectHandler, new TypeReference<>() {}, new TypeReference<>() {}, new JSONPatcher(Map.of(), Map.of()), expected, expectedJSON);
    }

    private void verifySet(List<Anchored> oldValue, List<Anchored> newValue, Patched<List<Anchored>, Map<String, Patched<Anchored, AnchoredDiff>>> expected, @Language("JSON") String expectedJSON) {
        doVerify(oldValue, newValue, setHandler, new TypeReference<>() {}, new TypeReference<>() {}, new JSONPatcher(Map.of("$", "anchor"), Map.of()), expected, expectedJSON);
    }

    private void verifyList(List<Anchored> oldValue, List<Anchored> newValue, Patched<List<Anchored>, ListPatch<Anchored, AnchoredDiffForList>> expected, @Language("JSON") String expectedJSON) {
        doVerify(oldValue, newValue, listHandler, new TypeReference<>() {}, new TypeReference<>() {}, new JSONPatcher(Map.of(), Map.of("$", "anchor")), expected, expectedJSON);
    }

    @SneakyThrows
    private <T, P, PP extends Patched<T, P>> void doVerify(T oldValue, T newValue, DiffHandler<T, P> handler, TypeReference<T> typeReference, TypeReference<PP> patchTypeReference, JSONPatcher jsonPatcher, Patched<T, P> expected, @Language("JSON") String expectedJSON) {
        Patched<T, P> patch = handler.compare(oldValue, newValue);
        System.out.println("patch    = " + patch);
        System.out.println("expected = " + expected);
        assertThat(patch).isEqualTo(expected);
        JavaType patchType = FeignUtil.OBJECT_MAPPER.getTypeFactory().constructType(patchTypeReference);
        SerializerUtils.withRootType(patchType, () -> {
            ObjectWriter patchWriter = FeignUtil.OBJECT_MAPPER.writerFor(patchType);
            ObjectReader patchReader = FeignUtil.OBJECT_MAPPER.readerFor(patchType);
            JavaType type = FeignUtil.OBJECT_MAPPER.getTypeFactory().constructType(typeReference);
            SerializerUtils.withRootType(type, () -> {
                ObjectWriter writer = FeignUtil.OBJECT_MAPPER.writerFor(type);
                doVerifyJSON(patch, expectedJSON, patchWriter, patchReader);
                doVerifyPatchApplication(oldValue, newValue, patch, writer, patchWriter, jsonPatcher);
            });
        });
    }

    @SneakyThrows
    private <P> void doVerifyJSON(P patch, String expectedJSON, ObjectWriter patchWriter, ObjectReader patchReader) {
        String json = patchWriter.writeValueAsString(patch);
        assertThat(json).isEqualToIgnoringWhitespace(expectedJSON.trim());
        P restored = patchReader.readValue(json);
        assertThat(restored).isEqualTo(patch);
    }

    @SneakyThrows
    private <T, P> void doVerifyPatchApplication(T oldValue, T newValue, Patched<T, P> patch, ObjectWriter writer, ObjectWriter patchWriter, JSONPatcher jsonPatcher) {
        JsonNode oldValueJSON = FeignUtil.OBJECT_MAPPER.readTree(writer.writeValueAsString(oldValue));
        JsonNode newValueJSON = FeignUtil.OBJECT_MAPPER.readTree(writer.writeValueAsString(newValue));
        JsonNode patchJSON = FeignUtil.OBJECT_MAPPER.readTree(patchWriter.writeValueAsString(patch));
        System.out.println("doVerifyPatchApplication: oldValue = " + oldValueJSON);
        System.out.println("doVerifyPatchApplication: newValue = " + newValueJSON);
        System.out.println("doVerifyPatchApplication: patch = " + patchJSON);

        JsonNode restoredJSON = jsonPatcher.apply(oldValueJSON, patchJSON);
        System.out.println("doVerifyPatchApplication: restored = " + restoredJSON);
        assertThat(PatchTestUtil.minimizeJSON(restoredJSON)).isEqualTo(PatchTestUtil.minimizeJSON(newValueJSON));

        JsonNode revertedJSON = jsonPatcher.reverse(newValueJSON, patchJSON);
        System.out.println("doVerifyPatchApplication: revered = " + revertedJSON);
        assertThat(PatchTestUtil.minimizeJSON(revertedJSON)).isEqualTo(PatchTestUtil.minimizeJSON(oldValueJSON));
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
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
class TestObjectDiff {

    private Patched<String, String> a;
    private Patched<String, String> d;
    private Patched<NestedObject, NestedObjectDiff> n;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
class NestedObjectDiff {

    private Patched<Integer, Integer> f;
}

class TestObjectDiffHandler extends AbstractDiffHandler<TestObject, TestObjectDiff> {

    private static final DefaultDiffHandler<String> STRING_HANDLER = DefaultDiffHandler.instance();
    private static final NestedObjectDiffHandler NESTED_HANDLER = new NestedObjectDiffHandler();

    @Override
    @Nullable
    protected Patched<TestObject, TestObjectDiff> doCompare(@Nullable TestObject a, TestObject b) {
        TestObjectDiff diff = new TestObjectDiff();
        diff.setA(STRING_HANDLER.compare(a != null ? a.getA() : null, b.getA()));
        diff.setD(STRING_HANDLER.compare(a != null ? a.getD(): null, b.getD()));
        diff.setN(NESTED_HANDLER.compare(a != null ? a.getN(): null, b.getN()));
        return diff.getA() != null || diff.getD() != null || diff.getN() != null ? Patched.updated(diff) : null;
    }
}

class NestedObjectDiffHandler extends AbstractDiffHandler<NestedObject, NestedObjectDiff> {

    private static final DefaultDiffHandler<Integer> INTEGER_HANDLER = DefaultDiffHandler.instance();

    @Override
    protected @Nullable Patched<NestedObject, NestedObjectDiff> doCompare(@Nullable NestedObject a, NestedObject b) {
        NestedObjectDiff diff = new NestedObjectDiff();
        diff.setF(INTEGER_HANDLER.compare(a != null ? a.getF() : null, b.getF()));
        return diff.getF() != null ? Patched.updated(diff) : null;
    }
}

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
class Anchored {

    private String anchor;
    private String name;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
class AnchoredDiff {

    private Patched<String, String> name;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
class AnchoredDiffForList {

    private Patched<String, String> anchor;
    private Patched<String, String> name;
}

class AnchoredDiffHandler extends AbstractDiffHandler<Anchored, AnchoredDiff> {

    private static final DefaultDiffHandler<String> STRING_HANDLER = DefaultDiffHandler.instance();

    @Override
    @Nullable
    protected Patched<Anchored, AnchoredDiff> doCompare(@Nullable Anchored a, Anchored b) {
        AnchoredDiff diff = new AnchoredDiff();
        diff.setName(STRING_HANDLER.compare(a != null ? a.getName() : null, b.getName()));
        return diff.getName() != null ? Patched.updated(diff) : null;
    }
}

class AnchoredDiffForListHandler extends AbstractDiffHandler<Anchored, AnchoredDiffForList> {

    private static final DefaultDiffHandler<String> STRING_HANDLER = DefaultDiffHandler.instance();

    @Override
    @Nullable
    protected Patched<Anchored, AnchoredDiffForList> doCompare(@Nullable Anchored a, Anchored b) {
        AnchoredDiffForList diff = new AnchoredDiffForList();
        diff.setAnchor(STRING_HANDLER.compare(a != null ? a.getAnchor() : null, b.getAnchor()));
        diff.setName(STRING_HANDLER.compare(a != null ? a.getName() : null, b.getName()));
        return diff.getName() != null ? Patched.updated(diff) : null;
    }
}

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
class InnerList {

    private String key;
    private List<Anchored> items;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
class InnerListPatch {

    private Patched<String, String> key;
    private Patched<List<Anchored>, ListPatch<Anchored, AnchoredDiffForList>> items;
}

@RequiredArgsConstructor
class InnerListHandler extends AbstractDiffHandler<InnerList, InnerListPatch> {

    private final DiffHandler<List<Anchored>, ListPatch<Anchored, AnchoredDiffForList>> nestedHandler;

    @Override
    @Nullable
    protected Patched<InnerList, InnerListPatch> doCompare(@Nullable InnerList a, InnerList b) {
        InnerListPatch diff = new InnerListPatch();
        diff.setKey(DefaultDiffHandler.<String>instance().compare(a != null ? a.getKey() : null, b.getKey()));
        diff.setItems(nestedHandler.compare(a != null ? a.getItems() : null, b.getItems()));
        return diff.getItems() != null ? Patched.updated(diff) : null;
    }
}

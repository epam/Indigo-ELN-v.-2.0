package com.epam.indigoeln.eln.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import com.google.common.base.Preconditions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.util.*;

@Slf4j
@ApplicationScoped
public class JSONPatcher {

    private static final String FIELD_NEW = "$new";
    private static final String FIELD_OLD = "$old";
    private static final String UNCHANGED = "$unchanged";
    private static final String MODEL = "model";
    private static final String REACTIONS = "reactions";
    private static final String ANCHOR = "anchor";

    private static final Map<List<String>, String> EXPERIMENT_SET_PATHS = Map.of(
            List.of("attachments"), "id",
            List.of("acl"), "username"
    );
    public static final Map<List<String>, String> EXPERIMENT_LIST_PATHS = Map.of(
            List.of(MODEL, REACTIONS), ANCHOR,
            List.of(MODEL, REACTIONS, "#", "inputs"), ANCHOR,
            List.of(MODEL, REACTIONS, "#", "inputs", "#", "samples"), ANCHOR,
            List.of(MODEL, REACTIONS, "#", "outputs"), ANCHOR,
            List.of(MODEL, REACTIONS, "#", "outputs", "#", "samples"), ANCHOR
    );
    private static final Set<List<String>> EXPERIMENT_IGNORED_PATHS = Set.of(
            List.of("revision")
    );

    private static final Comparator<ListComparison> LIST_ITEM_COMPARATOR = Comparator
            // first, deleted items (-1); second, order by newIndex
            .comparing((ListComparison c) -> c.newIndex != -1 ? c.newIndex : -1)
            // for deleted items, sort by old index
            .thenComparing(c -> c.oldIndex != -1 ? c.oldIndex : -1);

    private final Map<List<String>, String> setPaths;
    private final Map<List<String>, String> listPaths;
    private final Set<List<String>> ignoredPaths;

    private final JsonNodeFactory nodeFactory;

    @Inject
    public JSONPatcher(ObjectMapper objectMapper) {
        this(EXPERIMENT_SET_PATHS, EXPERIMENT_LIST_PATHS, EXPERIMENT_IGNORED_PATHS, objectMapper);
    }

    public JSONPatcher(Map<List<String>, String> setPaths, Map<List<String>, String> listPaths, Set<List<String>> ignoredPaths, ObjectMapper objectMapper) {
        this.setPaths = setPaths;
        this.listPaths = listPaths;
        this.ignoredPaths = ignoredPaths;
        this.nodeFactory = objectMapper.getNodeFactory();
    }

    public JsonNode createTopLevel(JsonNode base, JsonNode updated) {
        JsonNode diff = doCreate(base, updated, new ArrayList<>());
        return diff != null ? diff : nodeFactory.objectNode();
    }

    public JsonNode create(JsonNode base, JsonNode updated) {
        JsonNode diff = doCreate(base, updated, new ArrayList<>());
        return diff != null ? diff : nodeFactory.nullNode();
    }

    @Nullable
    public JsonNode doCreate(@Nullable JsonNode base, @Nullable JsonNode updated, List<String> path) {
        base = base instanceof NullNode ? null : base;
        updated = updated instanceof NullNode ? null : updated;
        if (base == null && updated == null || ignoredPaths.contains(path)) {
            return null;
        }
        if (base == null || updated == null) {
            return makePatched(base, updated);
        }
        if (base.isObject()) {
            Preconditions.checkState(updated.isObject());
            return doCreateObject((ObjectNode) base, (ObjectNode) updated, path);
        }
        if (base.isArray() && setPaths.containsKey(path)) {
            Preconditions.checkState(updated.isArray());
            return doCreateSet((ArrayNode) base, (ArrayNode) updated, path);
        }
        if (base.isArray() && listPaths.containsKey(path)) {
            Preconditions.checkState(updated.isArray());
            return doCreateList((ArrayNode) base, (ArrayNode) updated, path);
        }
        if (base.equals(updated)) {
            return null;
        }
        return makePatched(base, updated);
    }

    @Nullable
    private JsonNode doCreateObject(ObjectNode base, ObjectNode updated, List<String> path) {
        ObjectNode diff = null;
        for (Map.Entry<String, JsonNode> baseEntry : base.properties()) {
            String key = baseEntry.getKey();
            path.add(key);
            JsonNode propertyDiff = doCreate(baseEntry.getValue(), updated.get(key), path);
            path.removeLast();
            if (propertyDiff != null) {
                if (diff == null) {
                    diff = nodeFactory.objectNode();
                }
                diff.set(key, propertyDiff);
            }
        }
        for (Map.Entry<String, JsonNode> updatedEntry : updated.properties()) {
            String key = updatedEntry.getKey();
            if (!base.has(key)) {
                path.add(key);
                JsonNode propertyDiff = doCreate(null, updatedEntry.getValue(), path);
                path.removeLast();
                if (propertyDiff != null) {
                    if (diff == null) {
                        diff = nodeFactory.objectNode();
                    }
                    diff.set(key, propertyDiff);
                }
            }
        }
        return diff;
    }

    @Nullable
    private JsonNode doCreateSet(ArrayNode base, ArrayNode updated, List<String> path) {
        String anchorKey = setPaths.get(path);
        Map<String, SetComparison> map = new HashMap<>();
        for (JsonNode item : base) {
            String anchor = item.get(anchorKey).textValue();
            SetComparison c = map.computeIfAbsent(anchor, k -> new SetComparison());
            c.oldItem = item;
        }
        for (JsonNode item : updated) {
            String anchor = item.get(anchorKey).textValue();
            SetComparison c = map.computeIfAbsent(anchor, k -> new SetComparison());
            c.newItem = item;
        }
        ObjectNode diff = null;
        path.add("#");
        for (Map.Entry<String, SetComparison> entry : map.entrySet()) {
            JsonNode itemDiff = doCreate(entry.getValue().oldItem, entry.getValue().newItem, path);
            if (itemDiff != null) {
                if (diff == null) {
                    diff = nodeFactory.objectNode();
                }
                diff.set(entry.getKey(), itemDiff);
            }
        }
        path.removeLast();
        return diff;
    }

    @Nullable
    private JsonNode doCreateList(ArrayNode base, ArrayNode updated, List<String> path) {
        String anchorKey = listPaths.get(path);
        Map<String, ListComparison>  map = new HashMap<>();
        for (int i = 0; i < updated.size(); i++) {
            JsonNode item = updated.get(i);
            String anchor = item.get(anchorKey).textValue();
            ListComparison c = map.computeIfAbsent(anchor, k -> new ListComparison());
            c.newIndex = i;
            c.newItem = item;
        }
        for (int i = 0; i < base.size(); i++) {
            JsonNode item = base.get(i);
            String anchor = item.get(anchorKey).textValue();
            ListComparison c = map.computeIfAbsent(anchor, k -> new ListComparison());
            c.oldIndex = i;
            c.oldItem = item;
        }
        List<ListComparison> modified = new ArrayList<>();
        path.add("#");
        for (ListComparison c : map.values()) {
            c.diff = doCreate(c.oldItem, c.newItem, path);
            if (c.diff != null || c.oldIndex != c.newIndex) {
                modified.add(c);
            }
        }
        path.removeLast();
        if (modified.isEmpty()) {
            return null;
        }
        modified.sort(LIST_ITEM_COMPARATOR);
        ObjectNode diff = nodeFactory.objectNode();
        for (ListComparison c : modified) {
            String key = listKeyToString(c);
            diff.set(key, c.diff != null ? c.diff : nodeFactory.textNode(UNCHANGED));
        }
        return diff;
    }

    private ObjectNode makePatched(@Nullable JsonNode base, @Nullable JsonNode updated) {
        ObjectNode patched = nodeFactory.objectNode();
        if (base != null) {
            patched.set("$old", base);
        }
        if (updated != null) {
            patched.set("$new", updated);
        }
        return patched;
    }

    public JsonNode apply(JsonNode base, JsonNode patch) {
        return doApply(base, patch, new ArrayList<>(), false);
    }

    public JsonNode reverse(JsonNode base, JsonNode patch) {
        return doApply(base, patch, new ArrayList<>(), true);
    }

    private JsonNode doApply(JsonNode base, JsonNode patch, List<String> path, boolean reverse) {
        if (patch.isNull()) { // unchanged
            return base;
        }

        switch (patch) {
            case ObjectNode patchObject when (patchObject.size() <= 2 && (patchObject.has(FIELD_OLD) || patchObject.has(FIELD_NEW)))-> { // created or deleted value
                if (reverse) {
                    if (patchObject.has(FIELD_OLD)) {
                        return patchObject.get(FIELD_OLD);
                    }
                    return nodeFactory.nullNode();
                } else {
                    if (patchObject.has(FIELD_NEW)) {
                        return patchObject.get(FIELD_NEW);
                    }
                    return nodeFactory.nullNode();
                }
            }
            case ObjectNode patchObject when setPaths.containsKey(path) -> { // set diff
                return doRestoreSet(base, patchObject, path, reverse);
            }
            case ObjectNode patchObject when listPaths.containsKey(path) -> { // list diff
                return doRestoreList(base, patchObject, path, reverse);
            }
            case ObjectNode patchObject -> { // updated object
                return doRestoreObject(base, patchObject, path, reverse);
            }
            default -> {
                throw new IllegalStateException("Unexpected patch state: base is " + base.getNodeType() + ", patch is " + patch.getNodeType());
            }
        }
    }

    private ObjectNode doRestoreObject(JsonNode base, ObjectNode patchObject, List<String> path, boolean reverse) {
        ObjectNode targetObject = nodeFactory.objectNode();
        switch (base) {
            case ObjectNode object -> targetObject.setAll(object);
            case NullNode nullNode -> {}
            default -> throw new IllegalStateException("Unexpected base: " + base);
        }
        for (Map.Entry<String, JsonNode> entry : patchObject.properties()) {
            String key = entry.getKey();
            JsonNode oldValue = base.has(key) ? base.get(key) : nodeFactory.nullNode();
            path.add(key);
            JsonNode newValue = doApply(oldValue, entry.getValue(), path, reverse);
            path.removeLast();
            if (newValue.isNull()) {
                targetObject.remove(key);
            } else {
                targetObject.set(key, newValue);
            }
        }
        return targetObject;
    }

    private ArrayNode doRestoreSet(JsonNode base, ObjectNode patch, List<String> path, boolean reverse) {
        ArrayNode targetArray = copyArray(base);
        String keyProperty = setPaths.get(path);

        Map<String, Integer> keyIndices = new HashMap<>();
        for (int i = 0; i < targetArray.size(); i++) {
            keyIndices.put(targetArray.get(i).get(keyProperty).textValue(), i);
        }

        path.add("#");
        for (Map.Entry<String, JsonNode> entry : patch.properties()) {
            Integer index = keyIndices.get(entry.getKey());
            ObjectNode itemPatch = (ObjectNode) entry.getValue();
            JsonNode newValue = doApply(index != null ? targetArray.get(index) : nodeFactory.nullNode(), itemPatch, path, reverse);
            if (index != null) {
                targetArray.set(index, newValue);
            } else {
                targetArray.add(newValue);
            }
        }
        path.removeLast();
        targetArray.removeIf(JsonNode::isNull);
        return targetArray;
    }

    private ArrayNode doRestoreList(JsonNode base, ObjectNode patch, List<String> path, boolean reverse) {
        ArrayNode sourceArray = base instanceof ArrayNode baseArray ? baseArray : nodeFactory.arrayNode();
        ArrayNode targetArray = copyArray(base);

        int[] referenceCount = new int[sourceArray.size() + patch.size()];
        for (int i = 0; i < sourceArray.size(); i++) {
            referenceCount[i]++;
        }
        path.add("#");
        for (Map.Entry<String, JsonNode> entry : patch.properties()) {
            int[] index = parseListKey(entry.getKey());
            if (reverse) {
                index = new int[]{index[1], index[0]};
            }
            if (index[0] == -1) { // new item
                Preconditions.checkState(index[1] != -1);
                safeSet(targetArray, index[1], doApply(nodeFactory.nullNode(), entry.getValue(), path, reverse));
                referenceCount[index[1]]++;
            } else if (index[1] == -1) { // deleted item
                referenceCount[index[0]]--;
            } else { // updated and/or repositioned item
                JsonNode oldValue = sourceArray.get(index[0]);
                JsonNode newValue = entry.getValue() instanceof TextNode patchText && UNCHANGED.equals(patchText.textValue())
                        ? oldValue
                        : doApply(oldValue, entry.getValue(), path, reverse);
                safeSet(targetArray, index[1], newValue);
                referenceCount[index[0]]--;
                referenceCount[index[1]]++;
            }
        }
        path.removeLast();

        int lastReferencedIndex = -1;
        for (int i = referenceCount.length - 1; i >= 0; i--) {
            if (referenceCount[i] > 0) {
                lastReferencedIndex = i;
                break;
            }
        }
        int targetSize = lastReferencedIndex + 1;
        while (targetArray.size() > targetSize) {
            targetArray.remove(targetArray.size() - 1);
        }
        return targetArray;
    }

    private ArrayNode copyArray(JsonNode source) {
        ArrayNode targetArray = nodeFactory.arrayNode();
        switch (source) {
            case ArrayNode array -> targetArray.addAll(array);
            case NullNode nullNode -> {}
            default -> throw new IllegalStateException("Expecting array or null as base value, got " + source.getNodeType());
        }
        return targetArray;
    }

    private void safeSet(ArrayNode array, int index, JsonNode value) {
        while (array.size() < index + 1) {
            array.add(nodeFactory.nullNode());
        }
        array.set(index, value);
    }

    private static String listKeyToString(ListComparison c) {
        if (c.newIndex != -1 && c.newIndex == c.oldIndex) {
            return String.valueOf(c.newIndex);
        }
        StringBuilder str = new StringBuilder();
        if (c.oldIndex != -1) {
            str.append(c.oldIndex);
        }
        str.append('>');
        if (c.newIndex != -1) {
            str.append(c.newIndex);
        }
        return str.toString();
    }

    public static int[] parseListKey(String str) {
        int p = str.indexOf('>');
        if (p == -1) {
            int v = Integer.parseInt(str);
            return new int[]{v, v};
        }
        int[] indices = new int[]{-1, -1};
        if (p > 0) {
            indices[0] = Integer.parseInt(str.substring(0, p));
        }
        if (p < str.length() - 1) {
            indices[1] = Integer.parseInt(str.substring(p + 1));
        }
        return indices;
    }

    private static class SetComparison {

        @Nullable
        JsonNode oldItem;
        @Nullable
        JsonNode newItem;
    }

    @RequiredArgsConstructor
    private static class ListComparison {

        int oldIndex = -1;
        int newIndex = -1;
        @Nullable
        JsonNode oldItem;
        @Nullable
        JsonNode newItem;
        @Nullable
        JsonNode diff;
    }
}

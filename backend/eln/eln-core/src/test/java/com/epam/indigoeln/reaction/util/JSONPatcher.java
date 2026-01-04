package com.epam.indigoeln.reaction.util;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.reaction.config.ListPatchSerializers;
import com.epam.indigoeln.test.FeignUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.util.*;

import static com.epam.indigoeln.reaction.config.PatchedSerializers.FIELD_NEW;
import static com.epam.indigoeln.reaction.config.PatchedSerializers.FIELD_OLD;

@Slf4j
@RequiredArgsConstructor
public class JSONPatcher {

    public static final JSONPatcher EXPERIMENT_INSTANCE = new JSONPatcher(
            Map.of(
                "$.attachments", "id",
                "$.acl", "username"
            ),
            Map.of(
                "$.model.reactions", "anchor",
                "$.model.reactions.#.inputs", "anchor",
                "$.model.reactions.#.inputs.#.samples", "anchor",
                "$.model.reactions.#.outputs", "anchor",
                "$.model.reactions.#.outputs.#.samples", "anchor"
            )
    );

    private final Map<String, String> setPaths;
    private final Map<String, String> listPaths;

    private final JsonNodeFactory nodeFactory = FeignUtil.OBJECT_MAPPER.getNodeFactory();

    public JsonNode apply(JsonNode base, JsonNode patch) {
        return apply(base, patch, "$");
    }

    private JsonNode apply(JsonNode base, JsonNode patch, String path) {
        System.out.printf("path=%s, base=%s, patch=%s\n", path, base, patch);
        JsonNode result = doApply(base, patch, path);
        System.out.printf("result=%s\n", result);
        return result;
    }

    private JsonNode doApply(JsonNode base, JsonNode patch, String path) {
        if (patch.isNull()) { // unchanged
            return base;
        }

        if (setPaths.containsKey(path)) { // set
            return doRestoreSet(base, (ObjectNode) patch, path);
        }

        if (listPaths.containsKey(path)) { // list
            return doRestoreList(base, (ObjectNode) patch, path);
        }

        if (patch instanceof ObjectNode patchObject && (patchObject.has(FIELD_OLD) || patchObject.has(FIELD_NEW))) { // updated or deleted simple value
            return patchObject.has(FIELD_NEW) ? patchObject.get(FIELD_NEW) : nodeFactory.nullNode();
        }

        if (patch instanceof ObjectNode patchObject) {
            return doRestoreObject(path, base, patchObject);
        }

        if (base.isNull() && !patch.isNull()) { // new value
            return patch;
        }

        throw new IllegalStateException("Unexpected patch state: base is " + base.getNodeType() + ", patch is " + patch.getNodeType());
    }

    private ObjectNode doRestoreObject(String path, JsonNode base, ObjectNode patchObject) {
        ObjectNode targetObject = nodeFactory.objectNode();
        switch (base) {
            case ObjectNode object -> targetObject.setAll(object);
            case NullNode nullNode -> {}
            default -> throw new IllegalStateException("Unexpected base: " + base);
        }
        for (Map.Entry<String, JsonNode> entry : patchObject.properties()) {
            String key = entry.getKey();
            JsonNode oldValue = base.has(key) ? base.get(key) : nodeFactory.nullNode();
            JsonNode newValue = apply(oldValue, entry.getValue(), path + '.' + key);
            if (newValue.isNull()) {
                targetObject.remove(key);
            } else {
                targetObject.set(key, newValue);
            }
        }
        return targetObject;
    }

    private ArrayNode doRestoreSet(JsonNode base, ObjectNode patch, String path) {
        ArrayNode targetArray = copyArray(base);
        String keyProperty = setPaths.get(path);

        Map<String, Integer> keyIndices = new HashMap<>();
        for (int i = 0; i < targetArray.size(); i++) {
            keyIndices.put(targetArray.get(i).get(keyProperty).textValue(), i);
        }

        for (Map.Entry<String, JsonNode> entry : patch.properties()) {
            Integer index = keyIndices.get(entry.getKey());
            ObjectNode itemPatch = (ObjectNode) entry.getValue();
            if (index == null) { // item inserted
                ObjectNode newValue = nodeFactory.objectNode();
                newValue.setAll(itemPatch);
                newValue.set(keyProperty, nodeFactory.textNode(entry.getKey()));
                targetArray.add(newValue);
            } else if (itemPatch.has(FIELD_OLD)) { // item deleted
                Preconditions.checkState(!itemPatch.has(FIELD_NEW));
                targetArray.set(index, nodeFactory.nullNode());
            } else {
                JsonNode newValue = apply(targetArray.get(index), itemPatch, path + ".#");
                targetArray.set(index, newValue);
            }
        }
        targetArray.removeIf(JsonNode::isNull);
        return targetArray;
    }

    private ArrayNode doRestoreList(JsonNode base, ObjectNode patch, String path) {
        ArrayNode sourceArray = base instanceof ArrayNode baseArray ? baseArray : nodeFactory.arrayNode();
        ArrayNode targetArray = copyArray(base);

        int[] referenceCount = new int[sourceArray.size() + patch.size()];
        for (int i = 0; i < sourceArray.size(); i++) {
            referenceCount[i]++;
        }
        for (Map.Entry<String, JsonNode> entry : patch.properties()) {
            Pair<@Nullable Integer, @Nullable Integer> index = ListPatchSerializers.parseKey(entry.getKey());
            if (index.a() == null) { // new item
                Preconditions.checkState(index.b() != null);
                safeSet(targetArray, index.b(), doApply(nodeFactory.nullNode(), entry.getValue(), path + ".#"));
                referenceCount[index.b()]++;
            } else if (index.b() == null) { // deleted item
                referenceCount[index.a()]--;
            } else { // updated item
                JsonNode oldValue = sourceArray.get(index.a());
                JsonNode newValue = entry.getValue() instanceof TextNode patchText && ListPatchSerializers.UNCHANGED.equals(patchText.textValue())
                        ? oldValue
                        : apply(oldValue, entry.getValue(), path + ".#");
                safeSet(targetArray, index.b(), newValue);
                referenceCount[index.b()]++;
            }
        }

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
}

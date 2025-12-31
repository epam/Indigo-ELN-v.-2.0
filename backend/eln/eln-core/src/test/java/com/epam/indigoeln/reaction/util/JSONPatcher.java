package com.epam.indigoeln.reaction.util;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.reaction.config.ListPatchSerializers;
import com.epam.indigoeln.test.FeignUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;

import java.util.*;

import static com.epam.indigoeln.reaction.config.PatchedSerializers.FIELD_NEW;
import static com.epam.indigoeln.reaction.config.PatchedSerializers.FIELD_OLD;

@RequiredArgsConstructor
public class JSONPatcher {

    public static final JSONPatcher EXPERIMENT_INSTANCE = new JSONPatcher(
            Map.of(
                "$.attachments.#", "id",
                "$.acl.#", "username"
            ),
            Map.of(
                "$.model.reactions.#", "anchor",
                "$.model.reactions.#.inputs.#", "anchor",
                "$.model.reactions.#.inputs.#.samples.#", "anchor",
                "$.model.reactions.#.outputs.#", "anchor",
                "$.model.reactions.#.outputs.#.samples.#", "anchor"
            )
    );

    private final Map<String, String> setPaths;
    private final Map<String, String> listPaths;

    private final JsonNodeFactory nodeFactory = FeignUtil.OBJECT_MAPPER.getNodeFactory();

    public JsonNode restoreWithJSON(JsonNode baseJSON, JsonNode patchJSON) {
        return restoreWithJSON(baseJSON, patchJSON, "$");
    }

    protected JsonNode restoreWithJSON(JsonNode baseJSON, JsonNode patchJSON, String path) {
        if (patchJSON.isNull()) { // unchanged
            return baseJSON;
        }

        if (setPaths.containsKey(path)) { // set
            return doRestoreSet(baseJSON, (ObjectNode) patchJSON, path);
        }

        if (listPaths.containsKey(path)) { // list
            return doRestoreList(baseJSON, (ObjectNode) patchJSON, path);
        }

        if (baseJSON.isNull() && !patchJSON.isNull()) { // new value
            return patchJSON;
        }

        if (patchJSON instanceof ObjectNode patchObject && (patchObject.has(FIELD_OLD) || patchObject.has(FIELD_NEW))) { // updated or deleted simple value
            return patchObject.has(FIELD_NEW) ? patchObject.get(FIELD_NEW) : nodeFactory.nullNode();
        }

        if (baseJSON instanceof ObjectNode baseObject && patchJSON instanceof ObjectNode patchObject) { // object updated
            return doRestoreObject(path, baseObject, patchObject);
        }

        throw new IllegalStateException("Unexpected patch state: base is " + baseJSON.getNodeType() + ", patch is " + patchJSON.getNodeType());
    }

    private ObjectNode doRestoreObject(String path, ObjectNode baseObject, ObjectNode patchObject) {
        ObjectNode targetObject = nodeFactory.objectNode();
        targetObject.setAll(baseObject);
        for (Map.Entry<String, JsonNode> entry : patchObject.properties()) {
            String key = entry.getKey();
            JsonNode oldValue = baseObject.has(key) ? baseObject.get(key) : nodeFactory.nullNode();
            JsonNode newValue = restoreWithJSON(oldValue, entry.getValue(), path + '.' + key);
            if (newValue.isNull()) {
                targetObject.remove(key);
            } else {
                targetObject.set(key, newValue);
            }
        }
        return targetObject;
    }

    private ArrayNode doRestoreSet(JsonNode baseJSON, ObjectNode patchJSON, String path) {
        ArrayNode targetArray = copyArray(baseJSON);
        String keyProperty = setPaths.get(path);

        Map<String, Integer> keyIndices = new HashMap<>();
        for (int i = 0; i < targetArray.size(); i++) {
            keyIndices.put(targetArray.get(i).get(keyProperty).textValue(), i);
        }

        for (Map.Entry<String, JsonNode> entry : patchJSON.properties()) {
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
                JsonNode newValue = restoreWithJSON(targetArray.get(index), itemPatch, path + ".#");
                targetArray.set(index, newValue);
            }
        }
        targetArray.removeIf(JsonNode::isNull);
        return targetArray;
    }

    private ArrayNode doRestoreList(JsonNode baseJSON, ObjectNode patchJSON, String path) {
        ArrayNode sourceArray = baseJSON instanceof ArrayNode baseArray ? baseArray : nodeFactory.arrayNode();
        ArrayNode targetArray = copyArray(baseJSON);

        int[] referenceCount = new int[sourceArray.size() + patchJSON.size()];
        for (int i = 0; i < sourceArray.size(); i++) {
            referenceCount[i]++;
        }
        for (Map.Entry<String, JsonNode> entry : patchJSON.properties()) {
            Pair<Integer, Integer> index = ListPatchSerializers.parseKey(entry.getKey());
            if (index.a() == null) { // new item
                Preconditions.checkState(index.b() != null);
                safeSet(targetArray, index.b(), entry.getValue());
                referenceCount[index.b()]++;
            } else if (index.b() == null) { // deleted item
                referenceCount[index.a()]--;
            } else { // updated item
                JsonNode oldValue = sourceArray.get(index.a());
                JsonNode newValue = entry.getValue() instanceof TextNode patchText && ListPatchSerializers.UNCHANGED.equals(patchText.textValue())
                        ? oldValue
                        : restoreWithJSON(oldValue, entry.getValue(), path + ".#");
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

    private ArrayNode copyArray(JsonNode baseJSON) {
        ArrayNode targetArray = nodeFactory.arrayNode();
        switch (baseJSON) {
            case ArrayNode array -> targetArray.addAll(array);
            case NullNode nullNode -> {}
            default -> throw new IllegalStateException("Expecting array or null as base value for set patch, got " + baseJSON.getNodeType());
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

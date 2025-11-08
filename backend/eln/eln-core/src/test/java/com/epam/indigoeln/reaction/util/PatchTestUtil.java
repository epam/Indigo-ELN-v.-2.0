package com.epam.indigoeln.reaction.util;

import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.patch.ExperimentModelPatch;
import com.epam.indigoeln.reaction.model.patch.handler.ExperimentModelValueHandler;
import com.epam.indigoeln.test.FeignUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class PatchTestUtil {

    public static ExperimentModel verifyModelPatch(ExperimentModel initial, ExperimentModelPatch patch, ExperimentModel updated) throws Exception {
        byte[] initialBytes = FeignUtil.OBJECT_MAPPER.writeValueAsBytes(initial);
        ExperimentModel initialCopy = FeignUtil.OBJECT_MAPPER.readValue(initialBytes, ExperimentModel.class);
        JsonNode initialJSON = FeignUtil.OBJECT_MAPPER.readTree(initialBytes);

        byte[] updatedBytes = FeignUtil.OBJECT_MAPPER.writeValueAsBytes(updated);
        JsonNode updatedJSON = FeignUtil.OBJECT_MAPPER.readTree(updatedBytes);

        ExperimentModel reapplied = ExperimentModelValueHandler.INSTANCE.apply(null, initialCopy, Optional.of(patch));
        assertThat(reapplied).isNotNull().isEqualTo(updated);

        byte[] reappliedBytes = FeignUtil.OBJECT_MAPPER.writeValueAsBytes(reapplied);
        JsonNode reappliedJSON = FeignUtil.OBJECT_MAPPER.readTree(reappliedBytes);

        assertThat(reappliedJSON).isEqualTo(updatedJSON);

        JsonNode appliedWithJSON = restoreWithJSON(new ArrayList<>(), initialJSON.deepCopy(), FeignUtil.OBJECT_MAPPER.readTree(FeignUtil.OBJECT_MAPPER.writeValueAsBytes(patch)));
        assertThat(minimizeJSON(appliedWithJSON.deepCopy())).isEqualTo(minimizeJSON(updatedJSON.deepCopy()));

        return reapplied;
    }

    private static final Map<List<String>, JsonNode> DEFAULT_JSON;

    static {
        try {
            DEFAULT_JSON = Map.of(
                    List.of(), FeignUtil.OBJECT_MAPPER.readTree("""
                            {"reactions": []}
                            """),
                    List.of("reactions", "#"), FeignUtil.OBJECT_MAPPER.readTree("""
                            {"inputs": [], "outputs": []}
                            """),
                    List.of("reactions", "#", "inputs", "#"), FeignUtil.OBJECT_MAPPER.readTree("""
                            {"samples": []}
                            """),
                    List.of("reactions", "#", "outputs", "#"), FeignUtil.OBJECT_MAPPER.readTree("""
                            {"samples": []}
                            """)
            );
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    // !!! instead of minification, use "default object" (with empty arrays and null values) for each model node
    // !!! implement spread operator to simplify code
    @Nullable
    private static JsonNode restoreWithJSON(List<String> path, @Nullable JsonNode baseJSON, JsonNode patchJSON) {
        System.out.println("restoreWithJSON: " + path);
        if (baseJSON == null && DEFAULT_JSON.containsKey(path)) {
            baseJSON = DEFAULT_JSON.get(path).deepCopy();
        }
        if (baseJSON instanceof ArrayNode array && patchJSON instanceof ObjectNode patch) {
            JsonNode[] source = StreamEx.of(array.elements()).toArray(JsonNode[]::new);
            @Nullable JsonNode[] target = Arrays.copyOf(source, source.length + patch.size());
            for (Map.Entry<String, @Nullable JsonNode> entry : patch.properties()) {
                int targetIndex = Integer.parseInt(entry.getKey());
                if (entry.getValue().isNull()) {
                    target[targetIndex] = null;
                } else {
                    Preconditions.checkArgument(entry.getValue() instanceof ObjectNode);
                    JsonNode sourceNode;
                    JsonNode xfrom = entry.getValue().get("xfrom");
                    if (xfrom == null) {
                        sourceNode = source[targetIndex];
                    } else if (xfrom.isNull()) {
                        sourceNode = null;
                    } else {
                        Preconditions.checkArgument(xfrom instanceof NumericNode);
                        int sourceIndex = xfrom.intValue();
                        sourceNode = source[sourceIndex];
                    }
                    ((ObjectNode) entry.getValue()).remove("xfrom");
                    path.add("#");
                    target[targetIndex] = restoreWithJSON(path, sourceNode, entry.getValue());
                    path.removeLast();
                }
            }
            array.removeAll();
            for (JsonNode jsonNode : target) {
                if (jsonNode == null) {
                    break;
                }
                array.add(jsonNode);
            }
        } else if (baseJSON instanceof ObjectNode object && patchJSON instanceof ObjectNode patch) {
            for (Map.Entry<String, JsonNode> entry : patch.properties()) {
                path.add(entry.getKey());
                JsonNode updated = restoreWithJSON(path, baseJSON.get(entry.getKey()), entry.getValue());
                path.removeLast();
                if (updated != null) {
                    object.set(entry.getKey(), updated);
                } else {
                    object.remove(entry.getKey());
                }
            }
        } else {
            return patchJSON;
        }
        return baseJSON;
    }

    @Nullable
    private static JsonNode minimizeJSON(JsonNode json) {
        if (json instanceof ObjectNode object) {
            List<String> deletedKeys = new ArrayList<>();
            for (Map.Entry<String, JsonNode> entry : object.properties()) {
                JsonNode minimized = minimizeJSON(entry.getValue());
                if (isEmpty(entry.getValue())) {
                    deletedKeys.add(entry.getKey());
                } else {
                    entry.setValue(minimized);
                }
            }
            object.remove(deletedKeys);
            return isEmpty(object) ? null : object;
        } else if (json instanceof ArrayNode array) {
            if (array.isEmpty()) {
                return null;
            }
            for (int i = 0; i < array.size(); i++) {
                array.set(i, minimizeJSON(array.get(i)));
            }
            return array;
        }
        return json;
    }

    private static boolean isEmpty(@Nullable JsonNode json) {
        return json == null
                || json.isNull()
                || json instanceof ObjectNode object && object.isEmpty()
                || json instanceof ArrayNode array && array.isEmpty();
    }
}

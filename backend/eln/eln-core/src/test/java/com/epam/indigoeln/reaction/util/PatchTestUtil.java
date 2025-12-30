package com.epam.indigoeln.reaction.util;

import com.epam.indigoeln.eln.model.ExperimentDetailsDTO;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.patch.ExperimentPatch;
import com.epam.indigoeln.reaction.model.patch.ListPatch;
import com.epam.indigoeln.test.FeignUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class PatchTestUtil {

    public static void verifyModelPatch(ExperimentDetailsDTO initial, ExperimentPatch patch, ExperimentDetailsDTO updated, @Nullable CalculationReportBuilder reportBuilder) throws Exception {
        doVerifyModelPatch(initial, patch, updated, reportBuilder);
    }

    public static void verifyModelPatch(ExperimentSnapshot initial, ExperimentPatch patch, ExperimentSnapshot updated, @Nullable CalculationReportBuilder reportBuilder) throws Exception {
        doVerifyModelPatch(initial, patch, updated, reportBuilder);
    }

    private static void doVerifyModelPatch(Object initial, ExperimentPatch patch, Object updated, @Nullable CalculationReportBuilder reportBuilder) throws Exception {
        byte[] initialBytes = FeignUtil.OBJECT_MAPPER.writeValueAsBytes(initial);
        JsonNode initialJSON = cleanupJSON(FeignUtil.OBJECT_MAPPER.readTree(initialBytes));

        byte[] updatedBytes = FeignUtil.OBJECT_MAPPER.writeValueAsBytes(updated);
        JsonNode updatedJSON = cleanupJSON(FeignUtil.OBJECT_MAPPER.readTree(updatedBytes));

        JsonNode appliedWithJSON = restoreWithJSON(initialJSON.deepCopy(), FeignUtil.OBJECT_MAPPER.readTree(FeignUtil.OBJECT_MAPPER.writeValueAsBytes(patch)));

        String expected = FeignUtil.OBJECT_MAPPER_FORMATTED.writeValueAsString(minimizeJSON(updatedJSON.deepCopy()));
        String applied = FeignUtil.OBJECT_MAPPER_FORMATTED.writeValueAsString(minimizeJSON(appliedWithJSON.deepCopy()));
        try {
            assertThat(applied).isEqualTo(expected);
        } catch (AssertionError e) {
            if (reportBuilder != null) {
                reportBuilder.addFailedComparison("Model with applied patch not equals to expected", expected, applied);
            }
            throw e;
        }
    }

    private static JsonNode restoreWithJSON(JsonNode baseJSON, JsonNode patchJSON) {
        log.trace("restoreWithJSON:\n\tbaseJSON: {}\n\tpatchJSON: {}", baseJSON, patchJSON);
        if (patchJSON instanceof ObjectNode patch && patchJSON.has(ListPatch.SIZE_FIELD)) {
            log.trace("performing list merge");
            JsonNode[] source;
            if (baseJSON instanceof ArrayNode array) {
                source = StreamEx.of(array.elements()).toArray(JsonNode[]::new);
            } else if (baseJSON.isNull()) {
                source = new JsonNode[0];
            } else {
                throw new IllegalArgumentException();
            }
            Preconditions.checkArgument(patch.get(ListPatch.SIZE_FIELD).isInt());
            JsonNode[] target = Arrays.copyOf(source, patch.get(ListPatch.SIZE_FIELD).intValue());
            patch.remove(ListPatch.SIZE_FIELD);
            for (Map.Entry<String, JsonNode> entry : patch.properties()) {
                int targetIndex = Integer.parseInt(entry.getKey());
                if (entry.getValue().isNull()) { // deleted
                    target[targetIndex] = FeignUtil.OBJECT_MAPPER.nullNode();
                } else {
                    Preconditions.checkArgument(entry.getValue() instanceof ObjectNode);
                    JsonNode sourceNode;
                    JsonNode xfrom = entry.getValue().get("$from");
                    if (xfrom == null) { // not repositioned
                        sourceNode = source[targetIndex];
                    } else if (xfrom.isNull()) { // new
                        sourceNode = FeignUtil.OBJECT_MAPPER.nullNode();
                    } else { // repositioned
                        Preconditions.checkArgument(xfrom instanceof NumericNode);
                        int sourceIndex = xfrom.intValue();
                        sourceNode = source[sourceIndex];
                    }
                    ((ObjectNode) entry.getValue()).remove("$from");
                    target[targetIndex] = restoreWithJSON(sourceNode, entry.getValue());
                    log.trace("updated index {}: {}", targetIndex, target[targetIndex]);
                }
            }
            ArrayNode targetNode = FeignUtil.OBJECT_MAPPER.createArrayNode();
            targetNode.addAll(Arrays.asList(target));
            log.trace("list merge result: {}", targetNode);
            return targetNode;
        } else if (patchJSON instanceof ObjectNode patch) {
            log.trace("performing object merge");
            ObjectNode target = switch (baseJSON) {
                case ObjectNode object -> object;
                case NullNode nullNode -> FeignUtil.OBJECT_MAPPER.createObjectNode();
                default -> throw new IllegalArgumentException();
            };
            for (Map.Entry<String, JsonNode> entry : patch.properties()) {
                JsonNode baseProperty = baseJSON.has(entry.getKey()) ? baseJSON.get(entry.getKey()) : FeignUtil.OBJECT_MAPPER.nullNode();
                JsonNode updatedProperty = restoreWithJSON(baseProperty, entry.getValue());
                log.trace("updated property {}: {}", entry.getKey(), updatedProperty);
                if (!updatedProperty.isNull()) {
                    target.set(entry.getKey(), updatedProperty);
                } else {
                    target.remove(entry.getKey());
                }
            }
            log.trace("object merge result: {}", target);
            return target;
        }
        return patchJSON;
    }

    // make ExperimentDetailsDTO same shape as ExperimentSnapshot
    private static JsonNode cleanupJSON(JsonNode json) {
        ((ObjectNode) json).remove("modifiedAt");
        ((ObjectNode) json).remove("revision");
        return json;
    }

    // remove empty objects and arrays; sorts object keys for comparison
    @Nullable
    private static JsonNode minimizeJSON(JsonNode json) {
        return switch (json) {
            case ObjectNode object -> {
                Map<String, JsonNode> content = EntryStream.of(object.propertyStream())
                        .mapValues(PatchTestUtil::minimizeJSON)
                        .nonNullValues()
                        .toCustomMap(TreeMap::new);
                object.removeAll();
                object.setAll(content);
                yield isEmpty(object) ? null : object;
            }
            case ArrayNode array -> {
                if (array.isEmpty()) {
                    yield null;
                }
                for (int i = 0; i < array.size(); i++) {
                    array.set(i, minimizeJSON(array.get(i)));
                }
                yield array;
            }
            case NullNode nullNode -> null;
            default -> json;
        };
    }

    private static boolean isEmpty(@Nullable JsonNode json) {
        return json == null
                || json.isNull()
                || json instanceof ObjectNode object && object.isEmpty()
                || json instanceof ArrayNode array && array.isEmpty();
    }
}

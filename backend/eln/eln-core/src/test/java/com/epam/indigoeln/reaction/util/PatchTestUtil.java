package com.epam.indigoeln.reaction.util;

import com.epam.indigoeln.eln.model.ExperimentDetailsDTO;
import com.epam.indigoeln.flyway.util.JsonLocator;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.patch.ExperimentPatch;
import com.epam.indigoeln.test.FeignUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.EntryStream;
import org.apache.commons.math3.util.Precision;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class PatchTestUtil {

    private static ObjectNode prepareForComparison(ObjectNode root) {
        JsonNodeFactory nodeFactory = FeignUtil.OBJECT_MAPPER.getNodeFactory();
        root.set("revision", nodeFactory.textNode("..."));
        JsonLocator.<ObjectNode>findNodes(root, "model/reactions/*").forEach(reactionJSON -> {
            reactionJSON.set("rxnVersion", nodeFactory.textNode("..."));
            reactionJSON.set("rxnfile", nodeFactory.textNode("..."));
        });
        JsonLocator.findNodes(root, "model/reactions/**").forEach(node -> {
           if (node instanceof ObjectNode objectJSON
                   && objectJSON.get("source") instanceof NumericNode sourceJSON
                   && sourceJSON.isIntegralNumber()
//                   && sourceJSON.intValue() == latestRevision
           ) {
                objectJSON.set("source", nodeFactory.textNode("$source"));
           }
        });
        return (ObjectNode) cleanupNumbers(nodeFactory, root);
    }

    private static JsonNode cleanupNumbers(JsonNodeFactory nodeFactory, JsonNode node) {
        return switch (node) {
            case NumericNode number when number.isFloatingPointNumber() -> nodeFactory.numberNode(Precision.round(number.doubleValue(), 6));
            case ObjectNode object -> {
                ObjectNode cleaned = nodeFactory.objectNode();
                object.properties().forEach(entry -> {
                    cleaned.set(entry.getKey(), cleanupNumbers(nodeFactory, entry.getValue()));
                });
                yield cleaned;
            }
            case ArrayNode array -> {
                ArrayNode cleaned = nodeFactory.arrayNode();
                array.forEach(item -> cleaned.add(cleanupNumbers(nodeFactory, item)));
                yield cleaned;
            }
            default -> node;
        };
    }

    public static void verifyModel(ExperimentSnapshot actual, ExperimentSnapshot expected, @Nullable CalculationReportBuilder reportBuilder, Supplier<String> messageFn) throws Exception {
        JsonNode actualJSON = FeignUtil.OBJECT_MAPPER.readTree(FeignUtil.OBJECT_MAPPER.writeValueAsBytes(actual));
        JsonNode expectedJSON = FeignUtil.OBJECT_MAPPER.readTree(FeignUtil.OBJECT_MAPPER.writeValueAsBytes(expected));
        actualJSON = prepareForComparison((ObjectNode) actualJSON);
        expectedJSON = prepareForComparison((ObjectNode) expectedJSON);
        assertObjectsEqual(reportBuilder
                , null
                , actualJSON
                , expectedJSON
                , "failure"
                , "FAILURE: " + messageFn.get()
        );
    }

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

        String patchStr = FeignUtil.OBJECT_MAPPER_FORMATTED.writeValueAsString(patch);
        JsonNode appliedWithJSON = JSONPatcher.EXPERIMENT_INSTANCE.apply(initialJSON.deepCopy(), FeignUtil.OBJECT_MAPPER.readTree(patchStr));

        assertObjectsEqual(reportBuilder, patchStr, prepareForComparison((ObjectNode) minimizeJSON(appliedWithJSON)), prepareForComparison((ObjectNode) minimizeJSON(updatedJSON)), "patched", "Model (right) with applied patch (left) not equals to expected (middle)");
    }

    private static void assertObjectsEqual(@Nullable CalculationReportBuilder reportBuilder, @Nullable String patch, JsonNode actualJSON, JsonNode expectedJSON, String reportClass, String message) throws JsonProcessingException {
        String expected = FeignUtil.OBJECT_MAPPER_FORMATTED.writeValueAsString(minimizeJSON(expectedJSON.deepCopy()));
        String actual = FeignUtil.OBJECT_MAPPER_FORMATTED.writeValueAsString(minimizeJSON(actualJSON.deepCopy()));
        try {
            assertThat(actual).isEqualTo(expected);
        } catch (AssertionError e) {
            if (reportBuilder != null) {
                reportBuilder.addFailedComparison(reportClass, message, patch, expected, actual);
            }
            throw e;
        }
    }

    // make ExperimentDetailsDTO same shape as ExperimentSnapshot
    private static JsonNode cleanupJSON(JsonNode json) {
        ((ObjectNode) json).remove("modifiedAt");
        ((ObjectNode) json).remove("revision");
        return json;
    }

    // remove empty objects and arrays; sorts object keys for comparison
    @Nullable
    public static JsonNode minimizeJSON(JsonNode json) {
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

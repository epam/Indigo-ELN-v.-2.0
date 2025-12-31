package com.epam.indigoeln.reaction.util;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.eln.model.ExperimentDetailsDTO;
import com.epam.indigoeln.reaction.config.ListPatchSerializers;
import com.epam.indigoeln.reaction.config.PatchedSerializers;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.patch.ExperimentPatch;
import com.epam.indigoeln.reaction.model.patch.ListPatch;
import com.epam.indigoeln.test.FeignUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.*;

import static com.epam.indigoeln.reaction.config.PatchedSerializers.FIELD_NEW;
import static com.epam.indigoeln.reaction.config.PatchedSerializers.FIELD_OLD;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class PatchTestUtil {

    private static final Set<String> LIST_DIFF_PATHS = Set.of(".reactions", ".reactions.inputs", ".reactions.inputs.samples", ".reactions.outputs", ".reactions.outputs.samples");

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

        JsonNode appliedWithJSON = JSONPatcher.EXPERIMENT_INSTANCE.restoreWithJSON(initialJSON.deepCopy(), FeignUtil.OBJECT_MAPPER.readTree(FeignUtil.OBJECT_MAPPER.writeValueAsBytes(patch)));

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

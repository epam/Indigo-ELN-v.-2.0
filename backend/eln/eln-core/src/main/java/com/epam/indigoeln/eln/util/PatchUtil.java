package com.epam.indigoeln.eln.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public class PatchUtil {

    public static String formatJSONDiff(JsonNode json) {
        ToStringUtil.Builder builder = new ToStringUtil.Builder();
        formatJSONDiff(json, "", builder, null);
        return builder.toString();
    }

    private static void formatJSONDiff(JsonNode json, String prefix, ToStringUtil.Builder builder, @Nullable Boolean newOrOld) {
        switch (json) {
            case ObjectNode object -> {
                JsonNode oldNode = object.get("$old"), newNode = object.get("$new");
                if (oldNode != null && !oldNode.isContainerNode() || newNode != null && !newNode.isContainerNode()) {
                    String oldStr = oldNode != null ? "<span class='old'>%s</span>".formatted(oldNode) : "";
                    String newStr = newNode != null ? "<span class='new'>%s</span>".formatted(newNode) : "";
                    String delimiter = !oldStr.isEmpty() && !newStr.isEmpty() ? " -> " : "";
                    builder.property(prefix, oldStr + delimiter + newStr);
                    return;
                }
                if (!prefix.isEmpty()) {
                    builder.open2(prefix);
                }
                for (Map.Entry<String, JsonNode> entry : object.properties()) {
                    switch (entry.getKey()) {
                        case "$new" -> {
                            builder.setPrefixAndSuffix2("<span class='new'>", "</span>");
                            formatJSONDiff(entry.getValue(), "", builder, true);
                            builder.setPrefixAndSuffix2("", "");
                        }
                        case "$old" -> {
                            builder.setPrefixAndSuffix2("<span class='old'>", "</span>");
                            formatJSONDiff(entry.getValue(), "", builder, false);
                            builder.setPrefixAndSuffix2("", "");
                        }
                        default -> {
                            formatJSONDiff(entry.getValue(), entry.getKey(), builder, newOrOld);
                        }
                    }
                }
                if (!prefix.isEmpty()) {
                    builder.close2();
                }
            }
            case ArrayNode array -> {
                if (!prefix.isEmpty()) {
                    builder.open2(prefix);
                }
                for (int i = 0; i < array.size(); i++) {
                    formatJSONDiff(array.get(i), String.valueOf(i), builder, newOrOld);
                }
                if (!prefix.isEmpty()) {
                    builder.close2();
                }
            }
            default -> {
                if (!prefix.isEmpty()) {
                    builder.property(prefix, json.toPrettyString());
                } else {
                    builder.text(json.toPrettyString());
                }
            }
        }
    }
}

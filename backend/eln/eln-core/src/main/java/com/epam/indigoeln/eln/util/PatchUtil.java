package com.epam.indigoeln.eln.util;

import com.epam.indigoeln.reaction.model.patch.handler2.DefaultDiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.DiffHandler;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.epam.indigoeln.reaction.util.Flag;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

public class PatchUtil {

    @Nullable
    public static <C, T> Patched<T, T> diff(Flag updated, @Nullable C a, C b, Function<C, @Nullable T> valueFn) {
        return diff(updated, a, b, valueFn, DefaultDiffHandler.instance());
    }

    @Nullable
    public static <C, T, P> Patched<T, P> diff(Flag updated, @Nullable C a, C b, Function<C, @Nullable T> valueFn, DiffHandler<T, P> handler) {
        Patched<T, P> result = handler.compare(a != null ? valueFn.apply(a) : null, valueFn.apply(b));
        if (result != null) {
            updated.set();
        }
        return result;
    }

    public static String formatJSONDiff(JsonNode json) {
        ToStringUtil.Builder builder = new ToStringUtil.Builder();
        formatJSONDiff(json, "", builder, null);
        return builder.toString();
    }

    private static void formatJSONDiff(JsonNode json, String prefix, ToStringUtil.Builder builder, @Nullable Boolean newOrOld) {
        if (newOrOld != null) {
        }
        switch (json) {
            case ObjectNode object -> {
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

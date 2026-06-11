package com.epam.indigoeln.eln.util;

import com.epam.indigoeln.reaction.model.units.MeasurementUnit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PatchUtil {

    public static String formatJSONDiff(JsonNode before, JsonNode patch, Function<String, String> rxnfileFn, Function<UUID, String> molfileFn) {
        GridBuilder grid = new GridBuilder(16);
        formatJSONDiff(before, patch, grid, new ArrayList<>(), null, rxnfileFn, molfileFn);
        return grid.build();
    }

    private static void formatJSONDiff(@Nullable JsonNode before, @Nullable JsonNode patch, GridBuilder grid, List<String> path, @Nullable Boolean newOrOld, Function<String, String> rxnfileFn, Function<UUID, String> molfileFn) {
        String nestedClass = newOrOld == null ? "" : newOrOld ? " new" : " old";
        switch (patch) {
            case ObjectNode objectPatch when (objectPatch.get("$old") instanceof ValueNode || objectPatch.get("$new") instanceof ValueNode) -> {
                // patch: old-new if old/new are scalars -> show inline
                String oldValue = objectPatch.has("$old") ? objectPatch.get("$old").asText() : null;
                String newValue = objectPatch.has("$new") ? objectPatch.get("$new").asText() : null;
                if (path.equals(List.of("model", "reactions", "#", "rxnfile"))) {
                    oldValue = oldValue != null ? rxnfileFn.apply(oldValue) : null;
                    newValue = newValue != null ? rxnfileFn.apply(newValue) : null;
                } else if (path.equals(List.of("model", "reactions", "#", "inputs", "#", "compound", "compoundID"))) {
                    oldValue = oldValue != null ? molfileFn.apply(UUID.fromString(oldValue)) : null;
                    newValue = newValue != null ? molfileFn.apply(UUID.fromString(newValue)) : null;
                } else if (path.equals(List.of("model", "reactions", "#", "outputs", "#", "compound", "compoundID"))) {
                    oldValue = oldValue != null ? molfileFn.apply(UUID.fromString(oldValue)) : null;
                    newValue = newValue != null ? molfileFn.apply(UUID.fromString(newValue)) : null;
                }
                String s = "<span class='old'>%s</span> → <span class='new'>%s</span>".formatted(oldValue, newValue);
                grid.right(s).left().newRow();
            }
            case ObjectNode objectPatch when (objectPatch.has("$old") || objectPatch.has("$new")) -> {
                // patch: old-new if old/new are not scalars -> set oldOrNew, nest into old and new
                if (objectPatch.has("$old")) {
                    formatJSONDiff(before, objectPatch.get("$old"), grid, path, false, rxnfileFn, molfileFn);
                }
                if (objectPatch.has("$new")) {
                    formatJSONDiff(before, objectPatch.get("$new"), grid, path, true, rxnfileFn, molfileFn);
                }
            }
            case ObjectNode objectPatch when (objectPatch.get("value") instanceof ValueNode value && objectPatch.get("unit") instanceof ValueNode unit && objectPatch.get("source") instanceof ValueNode source) -> {
                // EnteredValue created or deleted
                String s = formatEnteredValue(before == null, value.asText(), unit.asText(), source.asText());
                grid.right(s).left().newRow();
            }
            case ObjectNode objectPatch when (before instanceof ObjectNode objectBefore && objectBefore.has("value") && objectBefore.has("unit") && objectBefore.has("source")) -> {
                // EnteredValue changed
                String oldSource = objectBefore.get("source").asText();
                String oldValue = objectBefore.get("value").asText();
                String oldUnit = objectBefore.get("unit").asText();
                String newSource = objectPatch.get("source") instanceof ObjectNode s && s.get("$new") instanceof ValueNode n ? n.asText() : oldSource;
                String newValue = objectPatch.get("value") instanceof ObjectNode v && v.get("$new") instanceof ValueNode n ? n.asText() : oldValue;
                String newUnit = objectPatch.get("unit") instanceof ObjectNode u && u.get("$new") instanceof ValueNode n ? n.asText() : oldUnit;
                String s = "%s → %s".formatted(
                        formatEnteredValue(false, oldValue, oldUnit, oldSource),
                        formatEnteredValue(true, newValue, newUnit, newSource)
                );
                grid.right(s).left().newRow();
            }
            case ObjectNode objectPatch when (objectPatch.size() == 2 && objectPatch.get("id") instanceof ValueNode id && objectPatch.get("name") instanceof ValueNode name) -> {
                // DictionaryRef or ExperimentRef
                String s = "<span class='%s'>%s</span>".formatted(nestedClass, name);
                grid.right(s).left().newRow();
            }
            case ObjectNode objectPatch when (objectPatch.size() == 2 && objectPatch.get("username") instanceof ValueNode username && objectPatch.get("displayName") instanceof ValueNode displayName) -> {
                // UserRef
                String s = "<span class='%s'>%s (%s)</span>".formatted(nestedClass, displayName, username);
                grid.right(s).left().newRow();
            }
            case ObjectNode objectPatch when (JSONPatcher.EXPERIMENT_LIST_PATHS.containsKey(path)) -> {
                // list format, iterate indices
                objectPatch.forEachEntry((key, value) -> {
                    int[] indices = JSONPatcher.parseListKey(key);
                    String displayPath = path.getLast();
                    displayPath = displayPath.substring(0, displayPath.length() - 1); // remove plural "s"; should have displayName for every field instead
                    String s = indices[0] == indices[1] ? "%d:".formatted(indices[0] + 1) // index didn't change
                            : indices[1] == -1 ? "<span class='old'>%d</span>:<br/><span class='comment'>removed</span>".formatted(indices[0] + 1) // deleted
                            : indices[0] == -1 ? "<span class='new'>%d</span>:<br/><span class='comment'>inserted</span>".formatted(indices[1] + 1) // inserted
                            : "<span class='old'>%d</span> → <span class='new'>%d</span>:<br/><span class='comment'>repositioned</span>".formatted(indices[0] + 1, indices[1] + 1); // repositioned
                    grid.left(); // overwrite collection name
                    grid.right("<span class='key%s'>%s %s</span>".formatted(nestedClass, displayPath, s));
                    path.add("#");
                    JsonNode node = before != null ? before.get(indices[0]) : null;
                    formatJSONDiff(node, value, grid, path, newOrOld, rxnfileFn, molfileFn);
                    path.removeLast();
                    grid.newRow();
                });
            }
            case ObjectNode objectPatch -> {
                // set format or properties, iterate keys
                objectPatch.forEachEntry((key, value) -> {
                    path.add(key);
                    JsonNode node = before != null ? before.get(key) : null;
                    grid.right("<span class='key%s'>%s:</span>".formatted(nestedClass, key));
                    formatJSONDiff(node, value, grid, path, newOrOld, rxnfileFn, molfileFn);
                    grid.left().newRow();
                    path.removeLast();
                });
            }
            case ArrayNode arrayPatch when (JSONPatcher.EXPERIMENT_LIST_PATHS.containsKey(path)) -> {
                for (int i = 0; i < arrayPatch.size(); i++) {
                    JsonNode node = arrayPatch.get(i);
                    String displayPath = path.getLast();
                    displayPath = displayPath.substring(0, displayPath.length() - 1); // remove plural "s"; should have displayName for every field instead
                    grid.left(); // overwrite collection name
                    grid.right("<span class='key%s'>%s %d<br/><span class='comment'>%s</span>".formatted(nestedClass, displayPath, i + 1, newOrOld == Boolean.TRUE ? "inserted" : "removed"));
                    path.add("#");
                    formatJSONDiff(null, node, grid, path, newOrOld, rxnfileFn, molfileFn);
                    path.removeLast();
                    grid.newRow();
                }
            }
            case ArrayNode arrayPatch -> {
                if (arrayPatch.isEmpty()) {
                    grid.right("<span class='%s'>[ ]</span>".formatted(nestedClass)).left().newRow();
                } else {
                    grid.right("<span class='%s'>[</span>".formatted(nestedClass)).left().newRow();
                    path.add("#");
                    for (JsonNode item : arrayPatch) {
                        formatJSONDiff(null, item, grid, path, newOrOld, rxnfileFn, molfileFn);
                    }
                    path.removeLast();
                    grid.right("<span class='%s'>]</span>".formatted(nestedClass)).left().newRow();
                }
            }
            default -> {
                grid.right("<span class='%s'>%s</span>".formatted(nestedClass, patch.asText())).left().newRow();
            }
        }
    }

    private static String formatEnteredValue(boolean newOrOld, String value, String unit, String source) {
        unit = MeasurementUnit.ALL_UNITS.get(unit).getDisplayName();
        source = Character.isDigit(source.charAt(0)) ? "user-entered" : source;
        return "<span class='%s ev-%s'>%s %s</span>&ensp;<span class='comment'>[%s]</span>".formatted(newOrOld ? "new" : "old", source, value, unit, source);
    }

    private static class GridBuilder {

        private final int columns;
        private final List<@Nullable String[]> rows = new ArrayList<>();
        private @Nullable String[] currentRow;
        private int columnNo;
        private int offset;

        public GridBuilder(int columns) {
            this.columns = columns;
            newRow();
        }

        GridBuilder newRow() {
            currentRow = new String[columns];
            rows.add(currentRow);
            columnNo = offset;
            return this;
        }

        GridBuilder right(String value) {
            currentRow[columnNo] = value;
            Preconditions.checkState(++columnNo < columns);
            offset++;
            return this;
        }

        GridBuilder left() {
            Preconditions.checkState(--columnNo >= 0);
            Preconditions.checkState(--offset >= 0);
            return this;
        }

        String build() {
            StringBuilder sb = new StringBuilder();
            String columnsCss = "repeat(%d, fit-content(200px)) 1fr".formatted(columns - 1);
            sb.append("<div style='display: grid; grid-template-columns: %s; gap: 4px; font-size: small'>\n".formatted(columnsCss));
            rows.removeIf(row -> StreamEx.of(row).nonNull().findAny().isEmpty());
            for (int rowNo = 0; rowNo < rows.size(); rowNo++) {
                String[] row = rows.get(rowNo);
                int last = columns - 1;
                while (last >= 0 && row[last] == null) {
                    last--;
                }
                if (last == 0) {
                    continue;
                }
                for (int i = 0; i <= last; i++) {
                    if (row[i] != null) {
                        int xSpan = i == last ? columns - i : 1;
                        int ySpan = 1;
                        while (rowNo + ySpan < rows.size() && rows.get(rowNo + ySpan)[i] == null) {
                            ySpan++;
                        }
                        String css = "grid-row: %d / span %d; grid-column: %d / span %d".formatted(rowNo + 1, ySpan, i + 1, xSpan);
                        sb.append("<div style='%s'>%s</div>\n".formatted(css, row[i]));
                    }
                }
            }
            sb.append("</div>\n");
            return sb.toString();
        }
    }
}

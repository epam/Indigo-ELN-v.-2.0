package com.epam.indigoeln.flyway.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

// We need something like JsonPath for model migrations, but existing JsonPath implementations like Jayway seems to be only focused on read-only queries.
// So here is a simple JSONPath-like tool that returns real Jackson model nodes that can be modified and serialized back
public class JsonLocator {

    private static final String ANY = "*";
    private static final String ANY_PATH = "**";

    public static <N extends JsonNode> List<N> findNodes(JsonNode root, String path) {
        return findNodes(root, path, false, true);
    }

    public static List<ObjectNode> findObjects(JsonNode root, String path) {
        return findNodes(root, path, true, true);
    }

    public static <N extends JsonNode> List<N> findNodes(JsonNode root, String path, boolean onlyObjects, boolean skipNulls) {
        Stream<JsonNode> stream = doFindNodes(Stream.of(root), Arrays.asList(path.split("/")));
        if (skipNulls) {
            stream = stream.filter(node -> !node.isNull());
        }
        if (onlyObjects) {
            stream = stream.filter(node -> node instanceof ObjectNode);
        }
        //noinspection unchecked
        return (List<N>) stream.toList();
    }

    private static Stream<JsonNode> doFindNodes(Stream<JsonNode> stream, List<String> path) {
        List<JsonNode> temp = stream.toList();
        stream = temp.stream();
        if (path.isEmpty()) {
            return stream;
        }
        String pathElement = path.getFirst();
        List<String> remainingPath = path.subList(1, path.size());
        return switch (pathElement) {
            case ANY -> doFindNodes(
                    stream.flatMap(node -> childrenNodes(node, ANY)),
                    remainingPath
            );
            case ANY_PATH -> doFindNodes(
                    stream.flatMap(JsonLocator::recurseNodes),
                    remainingPath
            );
            default -> doFindNodes(
                    stream.flatMap(node -> childrenNodes(node, pathElement)),
                    remainingPath
            );
        };
    }

    private static Stream<JsonNode> childrenNodes(JsonNode node, String fieldName) {
        if (node instanceof ArrayNode array) {
            if (fieldName.equals(ANY)) {
                return StreamSupport.stream(array.spliterator(), false);
            }
            try {
                int index = Integer.parseInt(fieldName);
                JsonNode item = array.get(index);
                return item != null ? Stream.of(item) : Stream.empty();
            } catch (NumberFormatException e) {
                return Stream.empty();
            }
        } else if (node instanceof ObjectNode object) {
            return object.propertyStream()
                    .filter(e -> fieldName.equals(ANY) || e.getKey().equals(fieldName))
                    .map(Map.Entry::getValue);
        } else {
            return Stream.empty();
        }
    }

    private static Stream<JsonNode> recurseNodes(JsonNode node) {
        Stream<JsonNode> directChildren = childrenNodes(node, ANY);
        return Stream.concat(
                Stream.of(node),
                directChildren.flatMap(JsonLocator::recurseNodes)
        );
    }
}

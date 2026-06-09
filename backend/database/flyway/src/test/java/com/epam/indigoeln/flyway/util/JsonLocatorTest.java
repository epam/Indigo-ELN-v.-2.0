package com.epam.indigoeln.flyway.util;

import com.epam.indigoeln.test.FeignUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class JsonLocatorTest {

    @Test
    void findSimple() throws IOException {
        JsonNode root = FeignUtil.OBJECT_MAPPER.readTree("""
                {"one": 1, "two": 2}
                """);
        assertThat(JsonLocator.<JsonNode>findNodes(root, "two")).singleElement().extracting(JsonNode::intValue).isEqualTo(2);
    }

    @Test
    void findAny() throws IOException {
        JsonNode root = FeignUtil.OBJECT_MAPPER.readTree("""
                {"one": 1, "two": 2}
                """);
        assertThat(JsonLocator.<JsonNode>findNodes(root, "*")).extracting(JsonNode::intValue).containsExactly(1, 2);
    }

    @Test
    void findNone() throws IOException {
        JsonNode root = FeignUtil.OBJECT_MAPPER.readTree("""
                {"one": 1, "two": 2}
                """);
        assertThat(JsonLocator.<JsonNode>findNodes(root, "three")).isEmpty();
    }

    @Test
    void findTree() throws IOException {
        JsonNode root = FeignUtil.OBJECT_MAPPER.readTree("""
                {"a": {"one": 1, "two": 2}, "b": {"three": 3}}
                """);
        assertThat(JsonLocator.<JsonNode>findNodes(root, "**")).extracting(JsonNode::intValue).containsExactly(0, 0, 1, 2, 0, 3); // 0 for object nodes: root, a, b
    }

    @Test
    void findNested() throws IOException {
        JsonNode root = FeignUtil.OBJECT_MAPPER.readTree("""
                {"a": {"one": 1, "two": 2}, "b": {"three": 3}}
                """);
        assertThat(JsonLocator.<JsonNode>findNodes(root, "a/one")).extracting(JsonNode::intValue).containsExactly(1);
    }

    @Test
    void findNestedTree() throws IOException {
        JsonNode root = FeignUtil.OBJECT_MAPPER.readTree("""
                {"a": {"one": 1, "two": 2, "key": 10}, "b": {"three": 3, "key": 11}}
                """);
        assertThat(JsonLocator.<JsonNode>findNodes(root, "**/key")).extracting(JsonNode::intValue).containsExactly(10, 11);
    }

    @Test
    void findInArray() throws IOException {
        JsonNode root = FeignUtil.OBJECT_MAPPER.readTree("""
                [{"key": 10}, {"x": 1}, {"key": 11}]
                """);
        assertThat(JsonLocator.<JsonNode>findNodes(root, "*/key")).extracting(JsonNode::intValue).containsExactly(10, 11);
    }

    @Test
    void findTreeInArray() throws IOException {
        JsonNode root = FeignUtil.OBJECT_MAPPER.readTree("""
                [{"key": 10}, {"x": 1}, {"key": 11}]
                """);
        assertThat(JsonLocator.<JsonNode>findNodes(root, "**/key")).extracting(JsonNode::intValue).containsExactly(10, 11);
    }

    @Test
    void findArrayIndex() throws IOException {
        JsonNode root = FeignUtil.OBJECT_MAPPER.readTree("""
                {"parts": [{"key": 1}, {"key": 2}]}
                """);
        assertThat(JsonLocator.<JsonNode>findNodes(root, "parts/1/key")).extracting(JsonNode::intValue).containsExactly(2);
    }

    @Test
    void findCombined() throws IOException {
        JsonNode root = FeignUtil.OBJECT_MAPPER.readTree("""
                {"head": [{"x": 1}, {"x": 2, "y": {"tail": 10}, "z": null}, {"x": 3, "y": {"tail": 11}}], "tail": 12}
                """);
        assertThat(JsonLocator.<JsonNode>findNodes(root, "head/**/tail")).extracting(JsonNode::intValue).containsExactly(10, 11);
    }

    @Test
    void findSkipNulls() throws IOException {
        JsonNode root = FeignUtil.OBJECT_MAPPER.readTree("""
                [{"key": 1}, {"key": null}, {"a": {"key": 2}}, {"a": null}, {"a": {"key": null}}]
                """);
        assertThat(JsonLocator.<JsonNode>findNodes(root, "**/key", false, true)).extracting(JsonNode::intValue).containsExactly(1, 2);
        assertThat(JsonLocator.<JsonNode>findNodes(root, "**/key", false, false)).extracting(JsonNode::intValue).containsExactly(1, 0, 2, 0);
    }
}

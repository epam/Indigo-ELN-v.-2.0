package com.epam.indigoeln.flyway.migrations;

import com.epam.indigoeln.test.FeignUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class R__300_migrate_experiment_modelsTest {

    R__300_migrate_experiment_models migration = new R__300_migrate_experiment_models();

    @Test
    void testRemoveIDFromUserRef() {
        ObjectNode model = readJSON("""
                {"fake": [{"id": "xxx", "displayName": "Alice Smith", "username": "alice"}]}
                """);
        migration.updateModel(model);
        verifyJSON(model, """
                {"fake": [{"displayName": "Alice Smith", "username": "alice"}]}
                """);
    }

    @Test
    void testUpdateCalculated() {
        ObjectNode model = readJSON("""
                {"fake": [{"value": 14, "source": -1}]}
                """);
        migration.updateModel(model);
        verifyJSON(model, """
                {"fake": [{"value": 14, "source": "calculated"}]}
                """);
    }

    @Test
    void testUpdateExactMass() {
        ObjectNode model = readJSON("""
                {"fake": [{"exactMass": 10}]}
                """);
        migration.updateModel(model);
        verifyJSON(model, """
                {"fake": [{"exactMass": {"value": "10", "unit": "NO_UNIT", "source": "fixed"}}]}
                """);
    }

    @Test
    void testReactionLimiting() {
        ObjectNode model = readJSON("""
                {"reactions": [{"inputs": [{"anchor": "a"}, {"anchor": "b", "limiting": true}]}]}
                """);
        migration.updateModel(model);
        verifyJSON(model, """
                {"reactions": [{"inputs": [{"anchor": "a"}, {"anchor": "b"}], "limitingAnchor": "b"}]}
                """);
    }

    @SneakyThrows
    private ObjectNode readJSON(@Language("JSON") String json) {
        return (ObjectNode) FeignUtil.OBJECT_MAPPER.readTree(json);
    }

    @SneakyThrows
    private void verifyJSON(ObjectNode node, String expected) {
        String actualJSON = FeignUtil.OBJECT_MAPPER.writeValueAsString(node);
        assertThat(actualJSON).isEqualToIgnoringWhitespace(expected);
    }
}

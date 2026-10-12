package com.epam.indigoeln.flyway.migrations;

import com.epam.indigoeln.test.FeignUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import org.intellij.lang.annotations.Language;

import static org.assertj.core.api.Assertions.assertThat;

class R__300_migrate_experiment_modelsTest {

    R__300_migrate_experiment_models migration = new R__300_migrate_experiment_models();

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

package com.epam.indigoeln.flyway.migrations;

import com.epam.indigoeln.test.FeignUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

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

    @Test
    void testMigrateToExperimentUnits() {
        ObjectNode model = readJSON("""
                {
                  "reactions": [{
                    "anchor": "r1",
                    "weightUnit": "MG",
                    "inputs": [{
                      "mol": {"value": "4.9", "unit": "MMOL", "source": "calculated"},
                      "samples": [{
                        "weight": {"value": "676.5", "unit": "MG", "source": 3},
                        "volume": {"value": "1.5", "unit": "L", "source": 4},
                        "molarity": {"value": "250", "unit": "MM", "source": 5},
                        "density": {"value": "1.1", "unit": "G_ML", "source": 6},
                        "purity": {"value": "100", "unit": "NO_UNIT", "source": "default"}
                      }]
                    }],
                    "outputs": [{
                      "theoWeight": {"value": "2", "exactValue": 2.0, "unit": "KG", "source": "calculated"},
                      "samples": [{"actualMol": {"value": "1", "unit": "UMOL", "source": 7}}]
                    }]
                  }],
                  "significantFigures": 5
                }
                """);
        migration.updateModel(model);
        String expected = """
                {
                  "reactions": [{
                    "anchor": "r1",
                    "inputs": [{
                      "mol": {"value": "0.0049", "unit": "MOL", "source": "calculated"},
                      "samples": [{
                        "weight": {"value": "0.6765", "unit": "G", "source": 3},
                        "volume": {"value": "1500", "unit": "ML", "source": 4},
                        "molarity": {"value": "0.25", "unit": "M", "source": 5},
                        "density": {"value": "1.1", "unit": "G_ML", "source": 6},
                        "purity": {"value": "100", "unit": "NO_UNIT", "source": "default"}
                      }]
                    }],
                    "outputs": [{
                      "theoWeight": {"value": "2000", "exactValue": 2000.0, "unit": "G", "source": "calculated"},
                      "samples": [{"actualMol": {"value": "0.000001", "unit": "MOL", "source": 7}}]
                    }]
                  }],
                  "significantFigures": 5,
                  "weightUnit": "G",
                  "volumeUnit": "ML",
                  "molUnit": "MOL",
                  "molarityUnit": "M"
                }
                """;
        verifyJSON(model, expected);

        // idempotent: a migrated model may already hold units the user chose
        migration.updateModel(model);
        verifyJSON(model, expected);
    }

    @Test
    void testExperimentUnitsAlreadySet() {
        String json = """
                {
                  "reactions": [{"anchor": "r1", "inputs": [{"mol": {"value": "4.9", "unit": "MMOL", "source": 3}}], "outputs": []}],
                  "significantFigures": 5,
                  "weightUnit": "MG",
                  "volumeUnit": "L",
                  "molUnit": "MMOL",
                  "molarityUnit": "MM"
                }
                """;
        ObjectNode model = readJSON(json);
        migration.updateModel(model);
        verifyJSON(model, json);
    }
}

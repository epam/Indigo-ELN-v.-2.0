package com.epam.indigoeln.flyway.migrations;

import com.epam.indigoeln.common.model.units.MeasurementUnit;
import com.epam.indigoeln.common.model.units.MolUnit;
import com.epam.indigoeln.common.model.units.MolarityUnit;
import com.epam.indigoeln.common.model.units.VolumeUnit;
import com.epam.indigoeln.common.model.units.WeightUnit;
import com.epam.indigoeln.flyway.util.JsonLocator;
import com.epam.indigoeln.reaction.util.UnitUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

@Slf4j
public class R__300_migrate_experiment_models extends BaseJavaMigration {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String VALUE = "value";
    private static final String EXACT_VALUE = "exactValue";
    private static final String UNIT = "unit";
    private static final String SOURCE = "source";
    private static final String WEIGHT_UNIT = "weightUnit";
    private static final String VOLUME_UNIT = "volumeUnit";
    private static final String MOL_UNIT = "molUnit";
    private static final String MOLARITY_UNIT = "molarityUnit";

    private static final List<MeasurementUnit> DEFAULT_UNITS = List.of(WeightUnit.G, VolumeUnit.ML, MolUnit.MOL, MolarityUnit.M);

    // Java migrations have no checksum of their own; change it whenever updateModel changes, so the repeatable migration re-runs
    @Override
    public Integer getChecksum() {
        return 1;
    }

    @Override
    public void migrate(Context context) throws Exception {
        try (
                Statement stList = context.getConnection().createStatement();
                ResultSet rsList = stList.executeQuery("SELECT id, model FROM Experiment");
                PreparedStatement stUpdate = context.getConnection().prepareStatement("UPDATE Experiment SET model = ?::JSONB WHERE id = ?")
        ) {
            int count = 0;
            while (rsList.next()) {
                UUID experimentId = rsList.getObject("id", UUID.class);
                String modelStr = rsList.getString("model");
                ObjectNode model = (ObjectNode) OBJECT_MAPPER.readTree(modelStr);
                try {
                    updateModel(model);
                } catch (RuntimeException e) {
                    log.error("Failed to migrate model for experiment {}", experimentId, e);
                    throw new RuntimeException(e);
                }
                String modelStr2 = OBJECT_MAPPER.writeValueAsString(model);
                if (!modelStr2.equals(modelStr)) {
                    stUpdate.setString(1, modelStr2);
                    stUpdate.setObject(2, experimentId);
                    stUpdate.addBatch();
                    count++;
                }
            }
            if (count > 0) {
                stUpdate.executeBatch();
                log.info("{} experiment models updated", count);
            }
        }
    }

    void updateModel(ObjectNode model) {
        migrateToExperimentUnits(model);
    }

    // units were moved from each value to the experiment: set the default experiment units and convert every value to them
    private void migrateToExperimentUnits(ObjectNode model) {
        if (model.has(WEIGHT_UNIT)) {
            return;
        }
        for (ObjectNode reaction : JsonLocator.findObjects(model, "reactions/*")) {
            reaction.remove(List.of(WEIGHT_UNIT, VOLUME_UNIT, MOL_UNIT, MOLARITY_UNIT));
        }
        for (ObjectNode node : JsonLocator.findObjects(model, "reactions/**")) {
            if (node.get(VALUE) instanceof JsonNode value && value.isTextual()
                    && node.get(UNIT) instanceof JsonNode unitNode && unitNode.isTextual()
                    && node.has(SOURCE)) {
                convertToDefaultUnit(node, value.textValue(), UnitUtils.ALL_UNITS.get(unitNode.textValue()));
            }
        }
        model.put(WEIGHT_UNIT, WeightUnit.G.name());
        model.put(VOLUME_UNIT, VolumeUnit.ML.name());
        model.put(MOL_UNIT, MolUnit.MOL.name());
        model.put(MOLARITY_UNIT, MolarityUnit.M.name());
    }

    private void convertToDefaultUnit(ObjectNode node, String value, @Nullable MeasurementUnit unit) {
        if (unit == null) {
            return;
        }
        MeasurementUnit target = DEFAULT_UNITS.stream().filter(u -> u.getClass() == unit.getClass()).findFirst().orElse(null);
        if (target == null || target == unit) {
            return;
        }
        BigDecimal factor = BigDecimal.valueOf(unit.getMultiplier()).divide(BigDecimal.valueOf(target.getMultiplier()), RoundingMode.UNNECESSARY);
        node.put(VALUE, new BigDecimal(value).multiply(factor).stripTrailingZeros().toPlainString());
        if (node.get(EXACT_VALUE) instanceof JsonNode exact && exact.isNumber()) {
            node.put(EXACT_VALUE, exact.decimalValue().multiply(factor).doubleValue());
        }
        node.put(UNIT, target.name());
    }
}

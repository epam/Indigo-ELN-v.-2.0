package com.epam.indigoeln.flyway.migrations;

import com.epam.indigoeln.flyway.util.JsonLocator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

@Slf4j
public class R__300_migrate_experiment_models extends BaseJavaMigration {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void migrate(Context context) throws Exception {
        try (
                Statement stList = context.getConnection().createStatement();
                ResultSet rsList = stList.executeQuery("SELECT id, model FROM Experiment");
                PreparedStatement stUpdate = context.getConnection().prepareStatement("UPDATE Experiment SET model = ?::JSONB WHERE id = ?")
        ) {
            while (rsList.next()) {
                UUID experimentId = rsList.getObject("id", UUID.class);
                String modelStr = rsList.getString("model");
                ObjectNode model = (ObjectNode) OBJECT_MAPPER.readTree(modelStr);
                updateModel(experimentId, model);
                stUpdate.setString(1, OBJECT_MAPPER.writeValueAsString(model));
                stUpdate.setObject(2, experimentId);
                stUpdate.addBatch();
            }
            int updatedCount = stUpdate.executeBatch().length;
            log.info("{} experiment models updated", updatedCount);
        }
    }

    private void updateModel(UUID experimentId, ObjectNode model) {
        try {
            model.remove("schemaVersion");
            for (ObjectNode node : JsonLocator.<ObjectNode>findNodes(model, "//*", true)) {
                node.remove("rxnVersion");
                node.remove("conflict");
                node.remove("overwritten");
            }
        } catch (RuntimeException e) {
            log.error("Failed to migrate model for experiment {}",  experimentId, e);
            throw new RuntimeException(e);
        }
    }
}

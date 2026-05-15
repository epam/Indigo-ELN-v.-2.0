package com.epam.indigoeln.flyway.migrations;

import com.epam.indigoeln.flyway.util.JsonLocator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NumericNode;
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
            int userRefsFixed = 0;
            int enteredValuesFixed = 0;
            for (ObjectNode node : JsonLocator.<ObjectNode>findNodes(model, "//*", true)) {
                if (node.has("id") && node.has("username") && node.has("displayName")) {
                    // UserRef, remove ID
                    node.remove("id");
                    userRefsFixed++;
                } else if (node.has("value") && node.get("source") instanceof NumericNode n && n.intValue() < 0) {
                    node.set("source", OBJECT_MAPPER.getNodeFactory().textNode("calculated"));
                    enteredValuesFixed++;
                }
            }
            if (userRefsFixed != 0) {
                log.info("UserRefs fixed: {}", userRefsFixed);
            }
            if (enteredValuesFixed != 0) {
                log.info("EnteredValues fixed: {}", enteredValuesFixed);
            }
        } catch (RuntimeException e) {
            log.error("Failed to migrate model for experiment {}",  experimentId, e);
            throw new RuntimeException(e);
        }
    }
}

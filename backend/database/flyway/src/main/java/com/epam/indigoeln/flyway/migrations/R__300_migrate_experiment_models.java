package com.epam.indigoeln.flyway.migrations;

import com.epam.indigoeln.flyway.util.JsonLocator;
import com.epam.indigoeln.reaction.model.units.NoUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
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
    private static final String SOURCE = "source";

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
                try {
                    updateModel(model);
                } catch (RuntimeException e) {
                    log.error("Failed to migrate model for experiment {}", experimentId, e);
                    throw new RuntimeException(e);
                }
                stUpdate.setString(1, OBJECT_MAPPER.writeValueAsString(model));
                stUpdate.setObject(2, experimentId);
                stUpdate.addBatch();
            }
            int updatedCount = stUpdate.executeBatch().length;
            log.info("{} experiment models updated", updatedCount);
        }
    }

    void updateModel(ObjectNode model) {
        JsonNodeFactory nodeFactory = OBJECT_MAPPER.getNodeFactory();
        for (ObjectNode node : JsonLocator.findObjects(model, "**")) {
            if (node.has("id") && node.has("username") && node.has("displayName")) {
                // UserRef, remove ID
                node.remove("id");
            } else if (node.has("value") && node.get(SOURCE) instanceof NumericNode n && n.intValue() < 0) {
                // update negative EnteredValue.source to word "calculated"
                node.set(SOURCE, nodeFactory.textNode("calculated"));
            } else if (node.get("exactMass") instanceof NumericNode n) {
                // update numeric exactMass
                ObjectNode obj = nodeFactory.objectNode();
                obj.set("value", nodeFactory.textNode(n.decimalValue().toString()));
                obj.set("unit", nodeFactory.textNode(NoUnit.NO_UNIT.name()));
                obj.set(SOURCE, nodeFactory.textNode("fixed"));
                node.set("exactMass", obj);
            }
        }
        for (ObjectNode reaction : JsonLocator.findObjects(model, "reactions/*")) {
            for (ObjectNode input : JsonLocator.findObjects(reaction, "inputs/*")) {
                if (input.get("limiting") instanceof BooleanNode limiting && limiting.booleanValue()) {
                    reaction.set("limitingAnchor", input.get("anchor"));
                }
                input.remove("limiting");
            }
        }
    }
}

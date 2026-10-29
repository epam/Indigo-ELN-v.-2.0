package com.epam.indigoeln.flyway.util;

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
public abstract class AbstractModelMigration extends BaseJavaMigration {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

    protected abstract void updateModel(ObjectNode model);
}

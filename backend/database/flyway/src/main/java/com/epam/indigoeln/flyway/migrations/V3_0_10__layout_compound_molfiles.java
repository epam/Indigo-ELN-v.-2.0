package com.epam.indigoeln.flyway.migrations;

import com.epam.indigoeln.eln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.eln.indigowrapper.IndigoMolecule;
import com.epam.indigoeln.eln.indigowrapper.IndigoRendererAPI;
import io.quarkus.arc.Arc;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

/**
 * Lays out the stored molfiles that have no 2D coordinates, as {@code CompoundService} now does
 * for every new compound, and re-renders their pictures from the result.
 */
@Slf4j
public class V3_0_10__layout_compound_molfiles extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        try (
                Statement stList = context.getConnection().createStatement();
                ResultSet rsList = stList.executeQuery("SELECT id, mol_file FROM Compound");
                PreparedStatement stUpdate = context.getConnection().prepareStatement("UPDATE Compound SET mol_file = ?, picture = ? WHERE id = ?")
        ) {
            // client proxies: native Indigo starts on the first call, so an empty database never needs it
            IndigoAPI indigo = Arc.container().instance(IndigoAPI.class).get();
            IndigoRendererAPI indigoRenderer = Arc.container().instance(IndigoRendererAPI.class).get();
            int count = 0;
            while (rsList.next()) {
                UUID compoundId = rsList.getObject("id", UUID.class);
                IndigoMolecule molecule = indigo.loadMolecule(rsList.getString("mol_file"));
                if (molecule.hasLayout()) {
                    continue;
                }
                molecule.layout();
                indigoRenderer.setRenderOptions("svg", 300, 200);
                stUpdate.setString(1, molecule.molfile());
                stUpdate.setBytes(2, indigoRenderer.renderToBuffer(molecule));
                stUpdate.setObject(3, compoundId);
                stUpdate.addBatch();
                count++;
            }
            if (count > 0) {
                stUpdate.executeBatch();
                log.info("{} compound molfiles laid out", count);
            }
        }
    }
}

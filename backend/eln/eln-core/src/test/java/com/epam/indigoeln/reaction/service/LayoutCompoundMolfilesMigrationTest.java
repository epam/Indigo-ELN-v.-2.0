package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.flyway.migrations.V3_0_10__layout_compound_molfiles;
import com.epam.indigoeln.reaction.model.CompoundRef;
import com.epam.indigoeln.reaction.model.OutputSampleAnchor;
import com.epam.indigoeln.reaction.model.mutation.ReactionOutputSampleMutation;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import one.util.streamex.StreamEx;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;
import java.util.UUID;

import static com.epam.indigoeln.test.MolfileTestUtil.atomCoordinates;
import static com.epam.indigoeln.test.MolfileTestUtil.withoutCoordinates;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestSecurity(user = ELNBaseTest.JOHN_USERNAME)
public class LayoutCompoundMolfilesMigrationTest extends MutationsTestBase {

    @BeforeEach
    void setUp() {
        initExperiment("LayoutCompoundMolfilesMigrationTest");
    }

    @Test
    void testLaysOutStoredMolfileWithoutCoordinates() throws Exception {
        experiment.mutateSetSchemeFromResource("/reaction.rxn");
        experiment.mutateAddProductSample(1);
        OutputSampleAnchor anchor = experiment.outputSample(1, 1).getAnchor();
        experiment.mutate(new ReactionOutputSampleMutation.SetOutputMolfile(anchor, "OC(=O)c1ccc(I)cc1F"), false);
        CompoundRef compound = StreamEx.of(experiment.reaction().getOutputs())
                .findFirst(output -> StreamEx.of(output.getSamples()).anyMatch(sample -> sample.getAnchor().equals(anchor)))
                .orElseThrow()
                .getCompound();
        UUID compoundId = Objects.requireNonNull(compound.getCompoundID());

        // read and written over JDBC: the molfile endpoint is cached
        try (Connection connection = databasePool.get().getConnection()) {
            String canSmiles = select(connection, compoundId, "can_smiles");
            // as stored before compounds were laid out on creation
            update(connection, compoundId, withoutCoordinates(select(connection, compoundId, "mol_file")));
            assertThat(atomCoordinates(select(connection, compoundId, "mol_file"))).hasSize(11).containsOnly("0.0000 0.0000");

            new V3_0_10__layout_compound_molfiles().migrate(context(connection));

            assertThat(atomCoordinates(select(connection, compoundId, "mol_file"))).hasSize(11).doesNotHaveDuplicates();
            assertThat(select(connection, compoundId, "can_smiles")).isEqualTo(canSmiles);
        }
    }

    private static String select(Connection connection, UUID compoundId, String column) throws Exception {
        try (PreparedStatement st = connection.prepareStatement("SELECT " + column + " FROM Compound WHERE id = ?")) {
            st.setObject(1, compoundId);
            try (ResultSet rs = st.executeQuery()) {
                assertThat(rs.next()).isTrue();
                return rs.getString(1);
            }
        }
    }

    private static void update(Connection connection, UUID compoundId, String molfile) throws Exception {
        try (PreparedStatement st = connection.prepareStatement("UPDATE Compound SET mol_file = ? WHERE id = ?")) {
            st.setString(1, molfile);
            st.setObject(2, compoundId);
            st.executeUpdate();
        }
    }

    static Context context(Connection connection) {
        return new Context() {
            @Override
            public Configuration getConfiguration() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Connection getConnection() {
                return connection;
            }
        };
    }
}

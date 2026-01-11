package com.epam.indigoeln.flyway.service;

import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.vertx.core.json.jackson.VertxModule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@Slf4j
@ApplicationScoped
public class DatabaseInitializationService {

    @Inject
    Flyway flyway;
    @Inject
    DataSource dataSource;

    private final CsvMapper mapper = new CsvMapper();
    private final CsvSchema schema = CsvSchema.emptySchema().withHeader();

    private final AtomicReference<UUID> adminID = new AtomicReference<>();

    DatabaseInitializationService() {
        mapper.registerModule(new VertxModule());
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new ParameterNamesModule());
    }

    @SneakyThrows
    public Map<String, String> migrate() {
        MigrateResult flywayResult = flyway.migrate();
        Map<String, String> statistics = new LinkedHashMap<>();
        statistics.put("migrationsExecuted", "" + flywayResult.migrationsExecuted);
        initDictionaries(statistics);
        log.info("Database migration completed: {}", statistics);
        return statistics;
    }

    @Transactional
    @SneakyThrows
    void initDictionaries(Map<String, String> statistics) {
        try (Connection conn = dataSource.getConnection()) {
            // roles and users
            statistics.put("rolesInserted", "" + insertRoles(conn, readCSV(RoleSpec.class, "/db/data/roles.csv")));
            List<UserSpec> users = readCSV(UserSpec.class, "/db/data/users.csv");
            adminID.set(users.getFirst().id());
            statistics.put("usersInserted", "" + insertUsers(conn, users));
            // dictionaries and items
            List<DictionarySpec> dictionaries = readCSV(DictionarySpec.class, "/db/data/dictionaries.csv");
            statistics.put("dictionariesInserted", "" + insertDictionaries(conn, dictionaries));
            Map<String, DictionarySpec> dictionaryMap = StreamEx.of(dictionaries)
                    .toMap(DictionarySpec::code, Function.identity());
            statistics.put("itemsInserted", "" + insertDictionaryItems(conn, readCSV(DictionaryItemSpec.class, "/db/data/dictionary_items.csv"), dictionaryMap));
            statistics.put("saltCodesInserted", "" + insertSaltCodes(conn, readCSV(SaltCodeSpec.class, "/db/data/salt_codes.csv")));
            // templates
            statistics.put("templatesInserted", "" + insertTemplates(conn, readCSV(TemplateSpec.class, "/db/data/templates.csv")));
        }
    }

    private <T> List<T> readCSV(Class<T> klass, String resourceName) throws IOException {
        try (MappingIterator<T> it = mapper.readerFor(klass).with(schema).readValues(ModelUtil.loadResource(getClass(), resourceName))) {
            return it.readAll();
        }
    }

    private int insertRoles(Connection conn, List<RoleSpec> roles) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement("""
                INSERT INTO Application_Role (id, name, permissions)
                VALUES (?, ?, ?)
                ON CONFLICT (id) DO UPDATE
                SET name=?, permissions=?
                """)) {
            for (RoleSpec role : roles) {
                Set<String> permissions = new TreeSet<>(role.permissions);
                if (permissions.contains("*")) {
                    permissions.remove("*");
                    for (ApplicationPermission permission : ApplicationPermission.values()) {
                        permissions.add(permission.name());
                    }
                }
                int parameterNo = 0;
                st.setObject(++parameterNo, role.id);
                for (int i = 0; i < 2; i++) {
                    st.setString(++parameterNo, role.name);
                    st.setObject(++parameterNo, conn.createArrayOf("VARCHAR", permissions.toArray()));
                }
                st.addBatch();
            }
            return st.executeBatch().length;
        }
    }

    private int insertUsers(Connection conn, List<UserSpec> users) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement("""
                INSERT INTO User_Account (id, created_by_id, created_at, modified_by_id, modified_at, username, first_name, last_name, display_name)
                VALUES (?, ?, now(), ?, now(), ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE
                SET username=?, first_name=?, last_name=?, display_name=?, modified_by_id=?, modified_at=now()
                """)) {
            for (UserSpec user : users) {
                int parameterNo = 0;
                st.setObject(++parameterNo, user.id);
                st.setObject(++parameterNo, adminID.get());
                st.setObject(++parameterNo, adminID.get());
                for (int i = 0; i < 2; i++) {
                    st.setString(++parameterNo, user.username);
                    st.setString(++parameterNo, user.firstName);
                    st.setString(++parameterNo, user.lastName);
                    st.setString(++parameterNo, user.displayName);
                }
                st.setObject(++parameterNo, adminID.get());
                st.addBatch();
            }
            return st.executeBatch().length;
        }
    }

    private int insertDictionaries(Connection conn, List<DictionarySpec> dictionaries) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement("""
                INSERT INTO Dictionary (id, created_by_id, created_at, modified_by_id, modified_at, code, name, user_editable, description)
                VALUES (?, ?, now(), ?, now(), ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE
                SET code=?, name=?, user_editable=?, description=?, modified_by_id=?, modified_at=now()
                """)) {
            for (DictionarySpec dict : dictionaries) {
                int parameterNo = 0;
                st.setObject(++parameterNo, dict.id);
                st.setObject(++parameterNo, adminID.get());
                st.setObject(++parameterNo, adminID.get());
                for (int i = 0; i < 2; i++) {
                    st.setString(++parameterNo, dict.code);
                    st.setString(++parameterNo, dict.name);
                    st.setBoolean(++parameterNo, dict.userEditable);
                    st.setString(++parameterNo, dict.description);
                }
                st.setObject(++parameterNo, adminID.get());
                st.addBatch();
            }
            return st.executeBatch().length;
        }
    }

    private int insertDictionaryItems(Connection conn, List<DictionaryItemSpec> items, Map<String, DictionarySpec> dictionaryMap) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement("""
                INSERT INTO Dictionary_Item (id, created_by_id, created_at, modified_by_id, modified_at, dictionary_id, ordinal, name, description, active)
                VALUES (?, ?, now(), ?, now(), ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE 
                SET ordinal=?, name=?, description=?, active=?, modified_by_id=?, modified_at=now()
                """)) {
            for (DictionaryItemSpec item : items) {
                DictionarySpec dictionary = dictionaryMap.get(item.dictionary);
                if (dictionary == null) {
                    throw new IllegalStateException("Dictionary with code " + item.dictionary + " not found");
                }
                int parameterNo = 0;
                st.setObject(++parameterNo, item.id);
                st.setObject(++parameterNo, adminID.get());
                st.setObject(++parameterNo, adminID.get());
                st.setObject(++parameterNo, dictionary.id);
                for (int i = 0; i < 2; i++) {
                    st.setInt(++parameterNo, item.ordinal());
                    st.setString(++parameterNo, item.name());
                    st.setString(++parameterNo, item.description());
                    st.setBoolean(++parameterNo, item.active());
                }
                st.setObject(++parameterNo, adminID.get());
                st.addBatch();
            }
            return st.executeBatch().length;
        }
    }

    private int insertSaltCodes(Connection conn, List<SaltCodeSpec> saltCodes) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement("""
                INSERT INTO Salt_Code (id, code, name, formula, charge, mol_weight)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE
                SET code=?, name=?, formula=?, charge=?, mol_weight=?
                """)) {
            for (SaltCodeSpec salt : saltCodes) {
                int parameterNo = 0;
                st.setObject(++parameterNo, salt.id);
                for (int i = 0; i < 2; i++) {
                    st.setString(++parameterNo, salt.code);
                    st.setString(++parameterNo, salt.name);
                    st.setString(++parameterNo, salt.formula);
                    st.setInt(++parameterNo, salt.charge);
                    st.setDouble(++parameterNo, salt.molWeight);
                }
                st.addBatch();
            }
            return st.executeBatch().length;
        }
    }

    private int insertTemplates(Connection conn, List<TemplateSpec> templates) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement("""
                INSERT INTO Template (id, created_by_id, created_at, modified_by_id, modified_at, name, template_tabs)
                VALUES (?, ?, now(), ?, now(), ?, ?::jsonb)
                ON CONFLICT (id) DO UPDATE
                SET name=?, template_tabs=?::jsonb, modified_by_id=?, modified_at=now()
                """)) {
            for (TemplateSpec template : templates) {
                int parameterNo = 0;
                st.setObject(++parameterNo, template.id);
                st.setObject(++parameterNo, adminID.get());
                st.setObject(++parameterNo, adminID.get());
                for (int i = 0; i < 2; i++) {
                    st.setString(++parameterNo, template.name());
                    st.setString(++parameterNo, template.content());
                }
                st.setObject(++parameterNo, adminID.get());
                st.addBatch();
            }
            return st.executeBatch().length;
        }
    }

    private long count(Connection conn, String table) throws SQLException {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    @RegisterForReflection
    public record RoleSpec (
            UUID id,
            String name,
            List<String> permissions
    ) {}

    @RegisterForReflection
    public record UserSpec (
            UUID id,
            String username,
            String firstName,
            String lastName,
            String displayName
    ) {}

    @RegisterForReflection
    public record DictionarySpec (
        UUID id,
        String code,
        String name,
        boolean userEditable,
        String description
    ) {}

    @RegisterForReflection
    public record DictionaryItemSpec (
        UUID id,
        String dictionary,
        int ordinal,
        String name,
        String description,
        boolean active
    ) {}

    @RegisterForReflection
    public record SaltCodeSpec (
        UUID id,
        String code,
        String name,
        String formula,
        int charge,
        double molWeight
    ) {}

    @RegisterForReflection
    public record TemplateSpec (
        UUID id,
        String name,
        String content
    ) {}
}

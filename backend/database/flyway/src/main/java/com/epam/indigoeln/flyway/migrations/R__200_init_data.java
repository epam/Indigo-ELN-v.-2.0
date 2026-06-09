package com.epam.indigoeln.flyway.migrations;

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
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.base.Strings.emptyToNull;

@Slf4j
public class R__200_init_data extends BaseJavaMigration {

    private final Supplier<byte[]> ROLES_CSV = () -> ModelUtil.loadResource(getClass(), "/db/data/roles.csv");
    private final Supplier<byte[]> USERS_CSV = () -> ModelUtil.loadResource(getClass(), "/db/data/users.csv");
    private final Supplier<byte[]> DICTIONARIES_CSV = () -> ModelUtil.loadResource(getClass(), "/db/data/dictionaries.csv");
    private final Supplier<byte[]> DICTIONARY_ITEMS_CSV = () -> ModelUtil.loadResource(getClass(), "/db/data/dictionary_items.csv");
    private final Supplier<byte[]> TEMPLATES_CSV = () -> ModelUtil.loadResource(getClass(), "/db/data/templates.csv");

    private final CsvMapper mapper = new CsvMapper();
    private final CsvSchema schema = CsvSchema.emptySchema().withHeader();

    private final AtomicReference<UUID> adminID = new AtomicReference<>();

    {
        mapper.registerModule(new VertxModule());
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new ParameterNamesModule());
    }

    @Override
    public void migrate(Context context) throws Exception {
        // roles and users
        List<RoleSpec> roles = readCSV(RoleSpec.class, ROLES_CSV);
        Map<String, RoleSpec> rolesMap = StreamEx.of(roles)
                .toMap(RoleSpec::name, Function.identity());
        log.info("roles: {}", insertRoles(context.getConnection(), roles));
        List<UserSpec> users = readCSV(UserSpec.class, USERS_CSV);
        adminID.set(users.getFirst().id());
        log.info("users: {}", insertUsers(context.getConnection(), users, rolesMap));
        // dictionaries and items
        List<DictionarySpec> dictionaries = readCSV(DictionarySpec.class, DICTIONARIES_CSV);
        log.info("dictionaries: {}", insertDictionaries(context.getConnection(), dictionaries));
        Map<String, DictionarySpec> dictionaryMap = StreamEx.of(dictionaries)
                .toMap(DictionarySpec::code, Function.identity());
        log.info("items: {}", insertDictionaryItems(context.getConnection(), readCSV(DictionaryItemSpec.class, DICTIONARY_ITEMS_CSV), dictionaryMap));
        // templates
        log.info("templates: {}", insertTemplates(context.getConnection(), readCSV(TemplateSpec.class, TEMPLATES_CSV)));
    }

    @Override
    public Integer getChecksum() {
        int checksum = 0;
        checksum |= Arrays.hashCode(ROLES_CSV.get());
        checksum |= Arrays.hashCode(USERS_CSV.get());
        checksum |= Arrays.hashCode(DICTIONARIES_CSV.get());
        checksum |= Arrays.hashCode(DICTIONARY_ITEMS_CSV.get());
        checksum |= Arrays.hashCode(TEMPLATES_CSV.get());
        return checksum;
    }

    private <T> List<T> readCSV(Class<T> klass, Supplier<byte[]> bytes) throws IOException {
        try (MappingIterator<T> it = mapper.readerFor(klass).with(schema).readValues(bytes.get())) {
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

    private int insertUsers(Connection conn, List<UserSpec> users, Map<String, RoleSpec> rolesMap) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement("""
                INSERT INTO User_Account (id, created_by_id, created_at, modified_by_id, modified_at, username, first_name, last_name, display_name)
                VALUES (?, ?, now(), ?, now(), ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE
                SET username=?, first_name=?, last_name=?, display_name=?, modified_by_id=?, modified_at=now()
                """);
             PreparedStatement stRoles = conn.prepareStatement("""
                INSERT INTO User_Account_Application_Role (user_id, role_id)
                VALUES (?, ?)
                ON CONFLICT (user_id, role_id) DO NOTHING
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
                for (String role : user.roles) {
                    stRoles.setObject(1, user.id);
                    RoleSpec roleSpec = rolesMap.get(role);
                    if (roleSpec == null) {
                        throw new IllegalStateException("Role " + role + " not found");
                    }
                    stRoles.setObject(2, roleSpec.id);
                    stRoles.addBatch();
                }
            }
            int count = st.executeBatch().length;
            stRoles.executeBatch();
            return count;
        }
    }

    private int insertDictionaries(Connection conn, List<DictionarySpec> dictionaries) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement("""
                INSERT INTO Dictionary (id, created_by_id, created_at, modified_by_id, modified_at, code, name, user_editable, description, deleted)
                VALUES (?, ?, now(), ?, now(), ?, ?, ?, ?, false)
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
                INSERT INTO Dictionary_Item (id, created_by_id, created_at, modified_by_id, modified_at, dictionary_id, ordinal, name, description, details, active, deleted)
                VALUES (?, ?, now(), ?, now(), ?, ?, ?, ?, cast(? as jsonb), ?, false)
                ON CONFLICT (id) DO UPDATE
                SET ordinal=?, name=?, description=?, details=cast(? as jsonb), active=?, modified_by_id=?, modified_at=now()
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
                    String description = emptyToNull(item.description());
                    String details = emptyToNull(item.details());
                    st.setInt(++parameterNo, item.ordinal());
                    st.setString(++parameterNo, item.name());
                    st.setString(++parameterNo, description);
                    st.setString(++parameterNo, details);
                    st.setBoolean(++parameterNo, item.active());
                }
                st.setObject(++parameterNo, adminID.get());
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
            String displayName,
            List<String> roles
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
            boolean active,
            @Nullable String details
    ) {}

    @RegisterForReflection
    public record TemplateSpec (
            UUID id,
            String name,
            String content
    ) {}
}

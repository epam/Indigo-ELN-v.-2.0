package com.epam.indigoeln.flyway.service;

import com.epam.indigoeln.common.util.ModelUtil;
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
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@ApplicationScoped
public class DatabaseInitializationService {

    private static final UUID ADMIN = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Inject
    Flyway flyway;
    @Inject
    DataSource dataSource;

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
        CsvMapper mapper = new CsvMapper();
        mapper.registerModule(new VertxModule());
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new ParameterNamesModule());
        List<DictionarySpec> dictionaries;
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        try (MappingIterator<DictionarySpec> it = mapper.readerFor(DictionarySpec.class).with(schema).readValues(ModelUtil.loadResource(getClass(), "/db/data/dictionaries.csv"))) {
            dictionaries = it.readAll();
        }
        Map<String, DictionarySpec> dictionaryMap = StreamEx.of(dictionaries)
                .toMap(DictionarySpec::code, Function.identity());
        List<DictionaryItemSpec> items;
        try (MappingIterator<DictionaryItemSpec> it = mapper.readerFor(DictionaryItemSpec.class).with(schema).readValues(ModelUtil.loadResource(getClass(), "/db/data/dictionary_items.csv"))) {
            items = it.readAll();
        }
        List<SaltCodeSpec> saltCodes;
        try (MappingIterator<SaltCodeSpec> it = mapper.readerFor(SaltCodeSpec.class).with(schema).readValues(ModelUtil.loadResource(getClass(), "/db/data/salt_codes.csv"))) {
            saltCodes = it.readAll();
        }
        List<TemplateSpec> templates;
        try (MappingIterator<TemplateSpec> it = mapper.readerFor(TemplateSpec.class).with(schema).readValues(ModelUtil.loadResource(getClass(), "/db/data/templates.csv"))) {
            templates = it.readAll();
        }
        try (Connection conn = dataSource.getConnection()) {
            if (count(conn, "Dictionary") > 0) {
                log.info("Dictionaries already exist, skipping");
                return;
            }
            statistics.put("dictionariesInserted", "" + insertDictionaries(conn, dictionaries));
            statistics.put("itemsInserted", "" + insertDictionaryItems(conn, items, dictionaryMap));
            statistics.put("saltCodesInserted", "" + insertSaltCodes(conn, saltCodes));
            statistics.put("templatesInserted", "" + insertTemplates(conn, templates));
        }
    }

    private int insertDictionaries(Connection conn, List<DictionarySpec> dictionaries) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement("""
                INSERT INTO Dictionary (id, created_by_id, created_at, modified_by_id, modified_at, code, name, user_editable, description)
                VALUES (?, ?, now(), ?, now(), ?, ?, ?, ?)
                """)) {
            for (DictionarySpec dict : dictionaries) {
                st.setObject(1, dict.id);
                st.setObject(2, ADMIN);
                st.setObject(3, ADMIN);
                st.setString(4, dict.code);
                st.setString(5, dict.name);
                st.setBoolean(6, dict.userEditable);
                st.setString(7, dict.description);
                st.addBatch();
            }
            return st.executeBatch().length;
        }
    }

    private int insertDictionaryItems(Connection conn, List<DictionaryItemSpec> items, Map<String, DictionarySpec> dictionaryMap) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement("""
                INSERT INTO Dictionary_Item (id, created_by_id, created_at, modified_by_id, modified_at, dictionary_id, ordinal, name, description, active)
                VALUES (?, ?, now(), ?, now(), ?, ?, ?, ?, ?)
                """)) {
            for (DictionaryItemSpec item : items) {
                DictionarySpec dictionary = dictionaryMap.get(item.dictionary);
                if (dictionary == null) {
                    throw new IllegalStateException("Dictionary with code " + item.dictionary + " not found");
                }
                st.setObject(1, UUID.randomUUID());
                st.setObject(2, ADMIN);
                st.setObject(3, ADMIN);
                st.setObject(4, dictionary.id);
                st.setInt(5, item.ordinal());
                st.setString(6, item.name());
                st.setString(7, item.description());
                st.setBoolean(8, item.active());
                st.addBatch();
            }
            return st.executeBatch().length;
        }
    }

    private int insertSaltCodes(Connection conn, List<SaltCodeSpec> saltCodes) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement("""
                INSERT INTO Salt_Code (id, code, name, formula, charge, mol_weight)
                VALUES (?, ?, ?, ?, ?, ?)
                """)) {
            for (SaltCodeSpec salt : saltCodes) {
                st.setObject(1, UUID.randomUUID());
                st.setString(2, salt.code);
                st.setString(3, salt.name);
                st.setString(4, salt.formula);
                st.setInt(5, salt.charge);
                st.setDouble(6, salt.molWeight);
                st.addBatch();
            }
            return st.executeBatch().length;
        }
    }

    private int insertTemplates(Connection conn, List<TemplateSpec> templates) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement("""
                INSERT INTO Template (id, created_by_id, created_at, modified_by_id, modified_at, name, template_tabs)
                VALUES (?, ?, now(), ?, now(), ?, ?::jsonb)
                """)) {
            for (TemplateSpec template : templates) {
                st.setObject(1, UUID.randomUUID());
                st.setObject(2, ADMIN);
                st.setObject(3, ADMIN);
                st.setString(4, template.name());
                st.setString(5, template.content());
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
        String code,
        String name,
        String formula,
        int charge,
        double molWeight
    ) {}

    @RegisterForReflection
    public record TemplateSpec (
        String name,
        String content
    ) {}
}

package com.epam.indigoeln.config.dbchangelogs;

import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import com.mongodb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;

@ChangeLog(order = "001")
public class ChangeLogVersion10 {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeLogVersion10.class);

    @ChangeSet(order = "01", author = "indigoeln", id = "01-initIndexes")
    public void initIndexes(DB db) {
        db.getCollection("role").createIndex(BasicDBObjectBuilder.start().add("name", 1).get(), BasicDBObjectBuilder.start().add("unique", true).get());

        db.getCollection("user").createIndex(BasicDBObjectBuilder.start().add("login", 1).get(), BasicDBObjectBuilder.start().add("unique", true).get());
        db.getCollection("user").createIndex(BasicDBObjectBuilder.start().add("email", 1).get());
        db.getCollection("user").createIndex(BasicDBObjectBuilder.start().add("roles", 1).get());

        db.getCollection("project").createIndex(BasicDBObjectBuilder.start().add("name", 1).get(), BasicDBObjectBuilder.start().add("unique", true).get());
        db.getCollection("project").createIndex(BasicDBObjectBuilder.start().add("accessList.user", 1).get());
        db.getCollection("project").createIndex(BasicDBObjectBuilder.start().add("notebooks", 1).get());
        db.getCollection("project").createIndex(BasicDBObjectBuilder.start().add("fileIds", 1).get());
        db.getCollection("project").createIndex(BasicDBObjectBuilder.start().add("sequenceId", 1).get());

        db.getCollection("notebook").createIndex(BasicDBObjectBuilder.start().add("name", 1).get(), BasicDBObjectBuilder.start().add("unique", true).get());
        db.getCollection("notebook").createIndex(BasicDBObjectBuilder.start().add("experiments", 1).get());
        db.getCollection("notebook").createIndex(BasicDBObjectBuilder.start().add("sequenceId", 1).get());

        db.getCollection("experiment").createIndex(BasicDBObjectBuilder.start().add("fileIds", 1).get());
        db.getCollection("experiment").createIndex(BasicDBObjectBuilder.start().add("sequenceId", 1).get());
    }

    @ChangeSet(order = "02", author = "indigoeln", id = "02-initRoles")
    public void initRoles(DB db) {
        db.getCollection("role").insert(BasicDBObjectBuilder.start()
                .add("_id", "role-0")
                .add("name", "All Permissions")
                .add("system", true)
                .add("authorities", Arrays.asList(
                        "USER_EDITOR", "ROLE_EDITOR", "CONTENT_EDITOR",
                        "PROJECT_READER", "NOTEBOOK_READER", "EXPERIMENT_READER",
                        "PROJECT_CREATOR", "NOTEBOOK_CREATOR", "EXPERIMENT_CREATOR",
                        "PROJECT_REMOVER", "NOTEBOOK_REMOVER", "EXPERIMENT_REMOVER",
                        "TEMPLATE_EDITOR", "DICTIONARY_EDITOR", "GLOBAL_SEARCH"
                ))
                .get());
    }

    @ChangeSet(order = "03", author = "indigoeln", id = "03-initUsers")
    public void initUsers(DB db) {
        db.getCollection("user").insert(BasicDBObjectBuilder.start()
                .add("_id", "admin")
                .add("login", "admin")
                .add("password", getDefaultAdminPassword())
                .add("first_name", "admin")
                .add("last_name", "Administrator")
                .add("email", "admin@localhost")
                .add("activated", true)
                .add("system", true)
                .add("lang_key", "en")
                .add("created_by", "system")
                .add("created_date", new Date())
                .add("roles", Collections.singletonList(new DBRef("role", "role-0")))
                .get());
    }

    @ChangeSet(order = "04", author = "indigoeln", id = "04-initDataDictionaries")
    public void initDataDictionaries(DB db) {
        DBCollection dictionary = db.getCollection("dictionary");

        dictionary.insert(
                createDictionary("therapeuticArea", "Therapeutic Area", "Therapeutic Area", Arrays.asList(
                        createDictionaryWord("Obesity", null, true, 0),
                        createDictionaryWord("Diabet", null, true, 1),
                        createDictionaryWord("Pulmonology", null, true, 2),
                        createDictionaryWord("Cancer", null, true, 3)
                )));

        dictionary.insert(
                createDictionary("projectCode", "Project Code & Name", "Project Code for experiment details", Arrays.asList(
                        createDictionaryWord("Code 1", null, true, 0),
                        createDictionaryWord("Code 2", null, true, 1),
                        createDictionaryWord("Code 3", null, true, 2)
                )));

        dictionary.insert(
                createDictionary("source", "Source", "Source", Arrays.asList(
                        createDictionaryWord("Source1", null, true, 0),
                        createDictionaryWord("Source2", null, true, 1)
                )));

        dictionary.insert(
                createDictionary("sourceDetail", "Source Details", "Source Details", Arrays.asList(
                        createDictionaryWord("Source Details1", null, true, 0),
                        createDictionaryWord("Source Details2", null, true, 1),
                        createDictionaryWord("Source Details3", null, true, 2)
                )));

        dictionary.insert(
                createDictionary("stereoisomerCode", "Stereoisomer Code", "Stereoisomer Code", Arrays.asList(
                        createDictionaryWord("NOSTC", "Achiral - No Stereo Centers", true, 0),
                        createDictionaryWord("AMESO", "Achiral - Meso Stereomers", true, 1),
                        createDictionaryWord("CISTR", "Achiral - Cis/Trans Stereomers", true, 2),
                        createDictionaryWord("SNENK", "Single Enantiomer (chirality known)", true, 3),
                        createDictionaryWord("RMCMX", "Racemic (stereochemistry known)", true, 4),
                        createDictionaryWord("ENENK", "Enantio-Enriched (chirality known)", true, 5),
                        createDictionaryWord("DSTRK", "Diastereomers (stereochemistry known)", true, 6),
                        createDictionaryWord("SNENU", "Other - Single Enantiomer (chirality unknown)", true, 7),
                        createDictionaryWord("LRCMX", "Other - Racemic (relative stereochemistry unknown)", true, 8),
                        createDictionaryWord("ENENU", "Other - Enantio-Enriched (chirality unknown)", true, 9),
                        createDictionaryWord("DSTRU", "Other - Diastereomers (relative stereochemistry unknown)", true, 10),
                        createDictionaryWord("UNKWN", "Other - Unknown Stereomer/Mixture", true, 11),
                        createDictionaryWord("HSREG", "Flag for automatic stereoisomer code assignment for multi-registration", true, 12),
                        createDictionaryWord("ACHIR", "ACHIRAL", true, 13),
                        createDictionaryWord("HOMO", "HOMO-CHIRAL", true, 14),
                        createDictionaryWord("MESO", "MESO", true, 15),
                        createDictionaryWord("RACEM", "RACEMIC", true, 16),
                        createDictionaryWord("SCALE", "SCALEMIC", true, 17)
                )));

        dictionary.insert(
                createDictionary("compoundState", "Compound State", "Compound State", Arrays.asList(
                        createDictionaryWord("Solid", null, true, 0),
                        createDictionaryWord("Gas", null, true, 1),
                        createDictionaryWord("oil", null, true, 2),
                        createDictionaryWord("liquid", null, true, 3)
                )));

        dictionary.insert(
                createDictionary("compoundProtection", "Compound Protection", "Compound Protection", Arrays.asList(
                        createDictionaryWord("Compound Protection1", null, true, 0),
                        createDictionaryWord("Compound Protection2", null, true, 1),
                        createDictionaryWord("Compound Protection3", null, true, 2)
                )));

        dictionary.insert(
                createDictionary("solventName", "Solvent Name", "Solvent Name", Arrays.asList(
                        createDictionaryWord("Acetic acid", null, true, 0),
                        createDictionaryWord("Hydrochloric acid", null, true, 1),
                        createDictionaryWord("Fumaric acid", null, true, 2),
                        createDictionaryWord("Formaldehyde", null, true, 3),
                        createDictionaryWord("Sulfuric acid", null, true, 4)
                )));

        dictionary.insert(
                createDictionary("purity", "Purity", "Purity definition methods", Arrays.asList(
                        createDictionaryWord("NMR", null, true, 0),
                        createDictionaryWord("HPLC", null, true, 1),
                        createDictionaryWord("LCMS", null, true, 2),
                        createDictionaryWord("CHN", null, true, 3),
                        createDictionaryWord("MS", null, true, 4)
                )));

        dictionary.insert(
                createDictionary("healthHazards", "Health Hazards", "Health Hazards", Arrays.asList(
                        createDictionaryWord("Very Toxic", null, true, 0),
                        createDictionaryWord("Explosive, Potential", null, true, 1),
                        createDictionaryWord("Carcinogen", null, true, 2),
                        createDictionaryWord("Corrosive - Acid", null, true, 3),
                        createDictionaryWord("Mutagen", null, true, 4),
                        createDictionaryWord("Flammable", null, true, 5)
                )));

        dictionary.insert(
                createDictionary("handlingPrecautions", "Handling Precautions", "Handling Precautions", Arrays.asList(
                        createDictionaryWord("Electrostatic", null, true, 0),
                        createDictionaryWord("Hygroscopic", null, true, 1),
                        createDictionaryWord("Oxidiser", null, true, 2),
                        createDictionaryWord("Air Sensitive", null, true, 3),
                        createDictionaryWord("Moisture Sensitive", null, true, 4)
                )));

        dictionary.insert(
                createDictionary("storageInstructions", "Storage Instructions", "Storage Instructions", Arrays.asList(
                        createDictionaryWord("No Special Storage Required", null, true, 0),
                        createDictionaryWord("Store in Refrigerator", null, true, 1),
                        createDictionaryWord("Store Under Argon", null, true, 2),
                        createDictionaryWord("Keep tightly sealed", null, true, 3)
                )));
    }

    private DBObject createDictionary(String id, String name, String description, List<DBObject> words) {
        return BasicDBObjectBuilder.start()
                .add("_id", id)
                .add("name", name)
                .add("description", description)
                .add("words", words)
                .add("accessList", Collections.emptyList())
                .get();
    }

    private DBObject createDictionaryWord(String name, String description, boolean enable, int rank) {
        return BasicDBObjectBuilder.start()
                .add("_id", null)
                .add("name", name)
                .add("description", description)
                .add("enable", enable)
                .add("rank", rank)
                .add("accessList", Collections.emptyList())
                .get();
    }

    private String getDefaultAdminPassword() {
        try (InputStream is = ChangeLogVersion10.class.getClassLoader().getResourceAsStream("application.properties")) {
            Properties properties = new Properties();
            properties.load(is);

            return properties.getProperty("default-admin-password");
        } catch (Exception e) {
            LOGGER.warn("Cannot read default admin password from properties", e);
        }

        return null;
    }
}

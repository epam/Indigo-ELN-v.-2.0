package com.epam.indigoeln.config.dbchangelogs;

import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import com.mongodb.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@ChangeLog(order = "001")
public final class ChangeLogVersion10 {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeLogVersion10.class);

    private static final String UNIQUE_KEY = "unique";
    private static final String PROJECT_COLLECTION_NAME = "project";
    private static final String NOTEBOOK_COLLECTION_NAME = "notebook";
    private static final String EXPERIMENT_COLLECTION_NAME = "experiment";
    private static final String ADMIN = "admin";
    private static final String SYSTEM = "system";
    private static final String SEQUENCE_ID = "sequenceId";
    private static final String ID_KEY = "_id";
    private static final String ROLE_ID = "role-0";
    private static final String THERAPEUTIC_AREA_ID = "therapeuticArea";
    private static final String PROJECT_CODE_ID = "projectCode";
    private static final String SOURCE_ID = "source";
    private static final String SOURCE_DETAIL_ID = "sourceDetail";
    private static final String STEREOISOMER_CODE_ID = "stereoisomerCode";
    private static final String COMPOUND_STATE_ID = "compoundState";
    private static final String COMPOUND_PROTECTION_ID = "compoundProtection";
    private static final String SOLVENT_NAME_ID = "solventName";
    private static final String PURITY_ID = "purity";
    private static final String HEALTH_HAZARDS_ID = "healthHazards";
    private static final String HANDLING_PRECAUTIONS_ID = "handlingPrecautions";
    private static final String STORAGE_INSTRUCTIONS = "storageInstructions";

    @ChangeSet(order = "01", author = "indigoeln", id = "01-initIndexes")
    public void initIndexes(DB db) {
        db.getCollection("role").createIndex(BasicDBObjectBuilder.start().add("name", 1).get(),
                BasicDBObjectBuilder.start().add(UNIQUE_KEY, true).get());

        db.getCollection("user").createIndex(BasicDBObjectBuilder.start().add("login", 1).get(),
                BasicDBObjectBuilder.start().add(UNIQUE_KEY, true).get());
        db.getCollection("user").createIndex(BasicDBObjectBuilder.start().add("email", 1).get());
        db.getCollection("user").createIndex(BasicDBObjectBuilder.start().add("roles", 1).get());

        db.getCollection(PROJECT_COLLECTION_NAME).createIndex(BasicDBObjectBuilder.start().add("name", 1).get(),
                BasicDBObjectBuilder.start().add(UNIQUE_KEY, true).get());
        db.getCollection(PROJECT_COLLECTION_NAME).createIndex(BasicDBObjectBuilder.start()
                .add("accessList.user", 1).get());
        db.getCollection(PROJECT_COLLECTION_NAME).createIndex(BasicDBObjectBuilder.start()
                .add("notebooks", 1).get());
        db.getCollection(PROJECT_COLLECTION_NAME).createIndex(BasicDBObjectBuilder.start()
                .add("fileIds", 1).get());
        db.getCollection(PROJECT_COLLECTION_NAME).createIndex(BasicDBObjectBuilder.start()
                .add(SEQUENCE_ID, 1).get());

        db.getCollection(NOTEBOOK_COLLECTION_NAME).createIndex(BasicDBObjectBuilder.start()
                .add("name", 1).get(), BasicDBObjectBuilder.start().add(UNIQUE_KEY, true).get());
        db.getCollection(NOTEBOOK_COLLECTION_NAME).createIndex(BasicDBObjectBuilder.start()
                .add("experiments", 1)
                .get());
        db.getCollection(NOTEBOOK_COLLECTION_NAME).createIndex(BasicDBObjectBuilder.start().add(SEQUENCE_ID, 1)
                .get());

        db.getCollection(EXPERIMENT_COLLECTION_NAME).createIndex(BasicDBObjectBuilder.start()
                .add("fileIds", 1).get());
        db.getCollection(EXPERIMENT_COLLECTION_NAME).createIndex(BasicDBObjectBuilder.start()
                .add(SEQUENCE_ID, 1)
                .get());
        db.getCollection(EXPERIMENT_COLLECTION_NAME).createIndex(BasicDBObjectBuilder.start()
                .add("experimentFullName", 1)
                .get());

        db.getCollection("component").createIndex(BasicDBObjectBuilder.start()
                .add(EXPERIMENT_COLLECTION_NAME, 1)
                .get());
    }

    @ChangeSet(order = "02", author = "indigoeln", id = "02-initRoles")
    public void initRoles(DB db) {
        final DBCollection collection = db.getCollection("role");
        if (collection.findOne(objectId(ROLE_ID)) == null) {
            collection.insert(BasicDBObjectBuilder.start()
                    .add(ID_KEY, objectId(ROLE_ID))
                    .add("name", "All Permissions")
                    .add(SYSTEM, true)
                    .add("authorities", Arrays.asList(
                            "USER_EDITOR", "ROLE_EDITOR", "CONTENT_EDITOR",
                            "PROJECT_READER", "NOTEBOOK_READER", "EXPERIMENT_READER",
                            "PROJECT_CREATOR", "NOTEBOOK_CREATOR", "EXPERIMENT_CREATOR",
                            "PROJECT_REMOVER", "NOTEBOOK_REMOVER", "EXPERIMENT_REMOVER",
                            "TEMPLATE_EDITOR", "DICTIONARY_EDITOR", "GLOBAL_SEARCH"
                    ))
                    .get());
        } else {
            LOGGER.warn(String.format("Role with %s = %s already exists", ID_KEY, ROLE_ID));
        }

    }

    @ChangeSet(order = "03", author = "indigoeln", id = "03-initUsers")
    public void initUsers(DB db) {
        final DBCollection collection = db.getCollection("user");
        if (collection.findOne(ADMIN) == null) {
            collection.insert(BasicDBObjectBuilder.start()
                    .add(ID_KEY, ADMIN)
                    .add("login", ADMIN)
                    .add("password", getDefaultAdminPassword())
                    .add("first_name", ADMIN)
                    .add("last_name", "Administrator")
                    .add("email", "admin@localhost")
                    .add("activated", true)
                    .add(SYSTEM, true)
                    .add("lang_key", "en")
                    .add("created_by", SYSTEM)
                    .add("created_date", new Date())
                    .add("roles", Collections.singletonList(new DBRef("role", objectId(ROLE_ID))))
                    .get());
        } else {
            LOGGER.warn(String.format("User with %s = %s already exists", ID_KEY, ADMIN));
        }
    }

    @ChangeSet(order = "04", author = "indigoeln", id = "04-initDataDictionaries")
    public void initDataDictionaries(DB db) {

        List<DBObject> therapeuticAreaList = Arrays.asList(
                createDictionaryWord("Obesity", null, true, 0),
                createDictionaryWord("Diabet", null, true, 1),
                createDictionaryWord("Pulmonology", null, true, 2),
                createDictionaryWord("Cancer", null, true, 3)
        );
        createDictionary(THERAPEUTIC_AREA_ID, "Therapeutic Area", "Therapeutic Area",
                therapeuticAreaList, db);

        final List<DBObject> projectCodeList = Arrays.asList(
                createDictionaryWord("Code 1", null, true, 0),
                createDictionaryWord("Code 2", null, true, 1),
                createDictionaryWord("Code 3", null, true, 2)
        );

        createDictionary(PROJECT_CODE_ID, "Project Code & Name", "Project Code for "
                + "experiment details", projectCodeList, db);

        final List<DBObject> sourceList = Arrays.asList(
                createDictionaryWord("Source1", null, true, 0),
                createDictionaryWord("Source2", null, true, 1)
        );

        createDictionary(SOURCE_ID, "Source", "Source", sourceList, db);

        final List<DBObject> sourceDetailsList = Arrays.asList(
                createDictionaryWord("Source Details1", null, true, 0),
                createDictionaryWord("Source Details2", null, true, 1),
                createDictionaryWord("Source Details3", null, true, 2)
        );

        createDictionary(SOURCE_DETAIL_ID, "Source Details", "Source Details", sourceDetailsList, db);

        final List<DBObject> stereoisomerCodeList = Arrays.asList(
                createDictionaryWord("NOSTC", "Achiral - No Stereo Centers",
                        true, 0),
                createDictionaryWord("AMESO", "Achiral - Meso Stereomers",
                        true, 1),
                createDictionaryWord("CISTR", "Achiral - Cis/Trans Stereomers",
                        true, 2),
                createDictionaryWord("SNENK", "Single Enantiomer (chirality known)",
                        true, 3),
                createDictionaryWord("RMCMX", "Racemic (stereochemistry known)",
                        true, 4),
                createDictionaryWord("ENENK", "Enantio-Enriched (chirality known)",
                        true, 5),
                createDictionaryWord("DSTRK", "Diastereomers (stereochemistry known)",
                        true, 6),
                createDictionaryWord("SNENU",
                        "Other - Single Enantiomer (chirality unknown)",
                        true, 7),
                createDictionaryWord("LRCMX", "Other - Racemic (relative "
                        + "stereochemistry unknown)", true, 8),
                createDictionaryWord("ENENU",
                        "Other - Enantio-Enriched (chirality unknown)",
                        true, 9),
                createDictionaryWord("DSTRU", "Other - Diastereomers "
                        + "(relative stereochemistry unknown)", true, 10),
                createDictionaryWord("UNKWN", "Other - Unknown Stereomer/Mixture",
                        true, 11),
                createDictionaryWord("HSREG", "Flag for automatic stereoisomer "
                        + "code assignment for multi-registration", true, 12),
                createDictionaryWord("ACHIR", "ACHIRAL", true, 13),
                createDictionaryWord("HOMO", "HOMO-CHIRAL", true, 14),
                createDictionaryWord("MESO", "MESO", true, 15),
                createDictionaryWord("RACEM", "RACEMIC", true, 16),
                createDictionaryWord("SCALE", "SCALEMIC", true, 17)
        );

        createDictionary(STEREOISOMER_CODE_ID, "Stereoisomer Code",
                "Stereoisomer Code", stereoisomerCodeList, db);

        final List<DBObject> compoundStateList = Arrays.asList(
                createDictionaryWord("Solid", null, true, 0),
                createDictionaryWord("Gas", null, true, 1),
                createDictionaryWord("oil", null, true, 2),
                createDictionaryWord("liquid", null, true, 3)
        );

        createDictionary(COMPOUND_STATE_ID, "Compound State", "Compound State", compoundStateList, db);

        final List<DBObject> compoundProtectionList = Arrays.asList(
                createDictionaryWord("Compound Protection1", null, true, 0),
                createDictionaryWord("Compound Protection2", null, true, 1),
                createDictionaryWord("Compound Protection3", null, true, 2)
        );

        createDictionary(COMPOUND_PROTECTION_ID, "Compound Protection",
                "Compound Protection", compoundProtectionList, db);

        final List<DBObject> solventNameList = Arrays.asList(
                createDictionaryWord("Acetic acid", null, true, 0),
                createDictionaryWord("Hydrochloric acid", null, true, 1),
                createDictionaryWord("Fumaric acid", null, true, 2),
                createDictionaryWord("Formaldehyde", null, true, 3),
                createDictionaryWord("Sulfuric acid", null, true, 4)
        );

        createDictionary(SOLVENT_NAME_ID, "Solvent Name", "Solvent Name", solventNameList, db);

        final List<DBObject> purityList = Arrays.asList(
                createDictionaryWord("NMR", null, true, 0),
                createDictionaryWord("HPLC", null, true, 1),
                createDictionaryWord("LCMS", null, true, 2),
                createDictionaryWord("CHN", null, true, 3),
                createDictionaryWord("MS", null, true, 4)
        );

        createDictionary(PURITY_ID, "Purity", "Purity definition methods", purityList, db);

        final List<DBObject> healthHazardsList = Arrays.asList(
                createDictionaryWord("Very Toxic", null, true, 0),
                createDictionaryWord("Explosive, Potential", null, true, 1),
                createDictionaryWord("Carcinogen", null, true, 2),
                createDictionaryWord("Corrosive - Acid", null, true, 3),
                createDictionaryWord("Mutagen", null, true, 4),
                createDictionaryWord("Flammable", null, true, 5)
        );

        createDictionary(HEALTH_HAZARDS_ID, "Health Hazards", "Health Hazards", healthHazardsList, db);

        final List<DBObject> handlingPrecautionsList = Arrays.asList(
                createDictionaryWord("Electrostatic", null, true, 0),
                createDictionaryWord("Hygroscopic", null, true, 1),
                createDictionaryWord("Oxidiser", null, true, 2),
                createDictionaryWord("Air Sensitive", null, true, 3),
                createDictionaryWord("Moisture Sensitive", null, true, 4)
        );

        createDictionary(HANDLING_PRECAUTIONS_ID, "Handling Precautions",
                "Handling Precautions", handlingPrecautionsList, db);

        final List<DBObject> storageInstructionsList = Arrays.asList(
                createDictionaryWord("No Special Storage Required", null,
                        true, 0),
                createDictionaryWord("Store in Refrigerator", null, true, 1),
                createDictionaryWord("Store Under Argon", null, true, 2),
                createDictionaryWord("Keep tightly sealed", null, true, 3)
        );

        createDictionary(STORAGE_INSTRUCTIONS, "Storage Instructions",
                "Storage Instructions", storageInstructionsList, db);

    }

    private void createDictionary(String id, String name, String description, List<DBObject> words, DB db) {
        DBCollection dictionary = db.getCollection("dictionary");
        String message = "Dictionary with %s = %s already exists";

        if (dictionary.findOne(objectId(id)) == null) {
            dictionary.insert(BasicDBObjectBuilder.start()
                    .add(ID_KEY, objectId(id))
                    .add("name", name)
                    .add("description", description)
                    .add("words", words)
                    .add("accessList", Collections.emptyList())
                    .get());
        } else {
            LOGGER.warn(String.format(message, ID_KEY, id));
        }
    }

    private DBObject createDictionaryWord(String name, String description, boolean enable, int rank) {
        return BasicDBObjectBuilder.start()
                .add(ID_KEY, null)
                .add("name", name)
                .add("description", description)
                .add("enable", enable)
                .add("rank", rank)
                .add("accessList", Collections.emptyList())
                .get();
    }

    private String getDefaultAdminPassword() {
        return ChangeLogBase.getEnvironment().getProperty("default-admin-password");
    }

    private static ObjectId objectId(String id) {
        return new ObjectId(DigestUtils.md5Hex(id).substring(0, 24));
    }
}

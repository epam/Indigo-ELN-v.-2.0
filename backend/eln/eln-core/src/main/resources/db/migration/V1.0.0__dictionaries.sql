CREATE TYPE Dictionary AS ENUM ('THERAPEUTIC_AREA', 'PROJECT_CODE', 'STEREOISOMER_CODE');

CREATE TABLE Dictionary_Item (
    id UUID PRIMARY KEY,
    dictionary Dictionary NOT NULL,
    ordinal INT NOT NULL,
    name VARCHAR(1000) NOT NULL,
    description VARCHAR(1000),
    deleted BOOL NOT NULL,
    CONSTRAINT dictionary_dictionary_ordinal_uq UNIQUE (dictionary, ordinal) DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT dictionary_dictionary_name_uq UNIQUE (dictionary, name) DEFERRABLE INITIALLY DEFERRED
);

INSERT INTO Dictionary_Item (id, dictionary, ordinal, name, deleted) VALUES
    (gen_random_uuid(), 'THERAPEUTIC_AREA', 1, 'Obesity', false),
    (gen_random_uuid(), 'THERAPEUTIC_AREA', 2, 'Diabetes', false),
    (gen_random_uuid(), 'THERAPEUTIC_AREA', 3, 'Pulmonology', false),
    (gen_random_uuid(), 'THERAPEUTIC_AREA', 4, 'Cancer', false)
;

INSERT INTO Dictionary_Item (id, dictionary, ordinal, name, deleted) VALUES
    (gen_random_uuid(), 'PROJECT_CODE', 1, 'Code 1', false),
    (gen_random_uuid(), 'PROJECT_CODE', 2, 'Code 2', false),
    (gen_random_uuid(), 'PROJECT_CODE', 3, 'Code 3', false)
;

INSERT INTO Dictionary_Item (id, dictionary, ordinal, name, description, deleted) VALUES
    (gen_random_uuid(), 'STEREOISOMER_CODE', 1, 'NOSTC', 'Achiral - No Stereo Centers', false),
    (gen_random_uuid(), 'STEREOISOMER_CODE', 2, 'AMESO', 'Achiral - Meso Stereomers', false),
    (gen_random_uuid(), 'STEREOISOMER_CODE', 3, 'CISTR', 'Achiral - Cis/Trans Stereomers', false),
    (gen_random_uuid(), 'STEREOISOMER_CODE', 4, 'SNENK', 'Single Enantiomer (chirality known)', false),
    (gen_random_uuid(), 'STEREOISOMER_CODE', 5, 'RMCMX', 'Racemic (stereochemistry known)', false),
    (gen_random_uuid(), 'STEREOISOMER_CODE', 6, 'ENENK', 'Enantio-Enriched (chirality known)', false),
    (gen_random_uuid(), 'STEREOISOMER_CODE', 7, 'DSTRK', 'Diastereomers (stereochemistry known)', false),
    (gen_random_uuid(), 'STEREOISOMER_CODE', 8, 'SNENU', 'Other - Single Enantiomer (chirality unknown)', false),
    (gen_random_uuid(), 'STEREOISOMER_CODE', 9, 'LRCMX', 'Other - Racemic (relative stereochemistry unknown)', false),
    (gen_random_uuid(), 'STEREOISOMER_CODE', 10, 'ENENU', 'Other - Enantio-Enriched (chirality unknown)', false),
    (gen_random_uuid(), 'STEREOISOMER_CODE', 11, 'DSTRU', 'Other - Diastereomers (relative stereochemistry unknown)', false),
    (gen_random_uuid(), 'STEREOISOMER_CODE', 12, 'UNKWN', 'Other - Unknown Stereomer/Mixture', false),
    (gen_random_uuid(), 'STEREOISOMER_CODE', 13, 'HSREG', 'Flag for automatic stereoisomer code assignment for multi-registration', false),
    (gen_random_uuid(), 'STEREOISOMER_CODE', 14, 'ACHIR', 'ACHIRAL', false),
    (gen_random_uuid(), 'STEREOISOMER_CODE', 15, 'HOMO', 'HOMO-CHIRAL', false),
    (gen_random_uuid(), 'STEREOISOMER_CODE', 16, 'MESO', 'MESO', false),
    (gen_random_uuid(), 'STEREOISOMER_CODE', 17, 'RACEM', 'RACEMIC', false),
    (gen_random_uuid(), 'STEREOISOMER_CODE', 18, 'SCALE', 'SCALEMIC', false)
;

CREATE TABLE Salt_Code (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE,
    charge INT NOT NULL,
    mol_weight FLOAT8 NOT NULL
);

INSERT INTO Salt_Code (id, name, charge, mol_weight) VALUES
    (gen_random_uuid(), 'Sulphate', -2, 96.063);

-- Remaining dictionaries from Indigo 2.0:
-- Source: Source 1, Source 2
-- Source Details: Source Details 1, Source Details 2, Source Details 3
-- Compound State: Solid, Gas, Oil, Liquid
-- Compound Protection: Compound Protection 1, Compound Protection 2, Compound Protection 3
-- Solvent Name: Acetic acid, Hydrochloric acid, Fumaric acid, Formaldehyde, Sulfuric acid
-- Purity definition methods: NMR, HPLC, LCMS, CHN, MS
-- Health Hazards: Very Toxic, Explosive, Potential, Carcinogen, Corrosive - Acid, Mutagen, Flammable
-- Handling Precautions: Electrostatic, Hygroscopic, Oxidiser, Air Sensitive, Moisture Sensitive
-- Storage Instructions: No Special Storage Required, Store in Refrigerator, Store Under Argon, Keep tightly sealed

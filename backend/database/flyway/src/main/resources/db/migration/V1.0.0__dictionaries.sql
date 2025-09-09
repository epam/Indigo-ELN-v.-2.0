CREATE TYPE Dictionary AS ENUM (
    'THERAPEUTIC_AREA',
    'PROJECT_CODE',
    'PROJECT_KEYWORD',
    'STEREOISOMER_CODE',
    'HEALTH_HAZARD',
    'HANDLING_PRECAUTIONS',
    'STORAGE_INSTRUCTIONS',
    'COMPOUND_PROTECTION',
    'SOLVENT',
    'EXTERNAL_SUPPLIER',
    'SAMPLE_SOURCE',
    'SAMPLE_SOURCE_DETAILS',
    'COMPONENT_STATE',
    'TEST');

CREATE TABLE Dictionary_Item (
    id UUID PRIMARY KEY,
    created_by_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    modified_by_id UUID NOT NULL,
    modified_at TIMESTAMPTZ NOT NULL,
    dictionary Dictionary NOT NULL,
    ordinal INT NOT NULL,
    name VARCHAR(1000) NOT NULL,
    description VARCHAR(1000),
    active BOOL NOT NULL,
    CONSTRAINT dictionary_dictionary_ordinal_uq UNIQUE (dictionary, ordinal) DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT dictionary_dictionary_name_uq UNIQUE (dictionary, name) DEFERRABLE INITIALLY DEFERRED
);

CREATE TEMPORARY TABLE Dictionary_Item_Temp (
    dictionary Dictionary NOT NULL,
    ordinal INT NOT NULL,
    name VARCHAR(1000) NOT NULL,
    description VARCHAR(1000)
);

INSERT INTO Dictionary_Item_Temp (dictionary, ordinal, name, description) VALUES
    ('THERAPEUTIC_AREA', 1, 'Obesity', null),
    ('THERAPEUTIC_AREA', 2, 'Diabetes', null),
    ('THERAPEUTIC_AREA', 3, 'Pulmonology', null),
    ('THERAPEUTIC_AREA', 4, 'Cancer', null),

    ('PROJECT_CODE', 1, 'Code 1', null),
    ('PROJECT_CODE', 2, 'Code 2', null),
    ('PROJECT_CODE', 3, 'Code 3', null),

    ('STEREOISOMER_CODE', 1, 'NOSTC', 'Achiral - No Stereo Centers'),
    ('STEREOISOMER_CODE', 2, 'AMESO', 'Achiral - Meso Stereomers'),
    ('STEREOISOMER_CODE', 3, 'CISTR', 'Achiral - Cis/Trans Stereomers'),
    ('STEREOISOMER_CODE', 4, 'SNENK', 'Single Enantiomer (chirality known)'),
    ('STEREOISOMER_CODE', 5, 'RMCMX', 'Racemic (stereochemistry known)'),
    ('STEREOISOMER_CODE', 6, 'ENENK', 'Enantio-Enriched (chirality known)'),
    ('STEREOISOMER_CODE', 7, 'DSTRK', 'Diastereomers (stereochemistry known)'),
    ('STEREOISOMER_CODE', 8, 'SNENU', 'Other - Single Enantiomer (chirality unknown)'),
    ('STEREOISOMER_CODE', 9, 'LRCMX', 'Other - Racemic (relative stereochemistry unknown)'),
    ('STEREOISOMER_CODE', 10, 'ENENU', 'Other - Enantio-Enriched (chirality unknown)'),
    ('STEREOISOMER_CODE', 11, 'DSTRU', 'Other - Diastereomers (relative stereochemistry unknown)'),
    ('STEREOISOMER_CODE', 12, 'UNKWN', 'Other - Unknown Stereomer/Mixture'),
    ('STEREOISOMER_CODE', 13, 'HSREG', 'Flag for automatic stereoisomer code assignment for multi-registration'),
    ('STEREOISOMER_CODE', 14, 'ACHIR', 'ACHIRAL'),
    ('STEREOISOMER_CODE', 15, 'HOMO', 'HOMO-CHIRAL'),
    ('STEREOISOMER_CODE', 16, 'MESO', 'MESO'),
    ('STEREOISOMER_CODE', 17, 'RACEM', 'RACEMIC'),
    ('STEREOISOMER_CODE', 18, 'SCALE', 'SCALEMIC'),

    ('HEALTH_HAZARD', 1, 'Health Hazard 1', null),
    ('HEALTH_HAZARD', 2, 'Health Hazard 2', null),

    ('HANDLING_PRECAUTIONS', 1, 'Handling Precautions 1', null),
    ('HANDLING_PRECAUTIONS', 2, 'Handling Precautions 2', null),

    ('STORAGE_INSTRUCTIONS', 1, 'Storage Instructions 1', null),
    ('STORAGE_INSTRUCTIONS', 2, 'Storage Instructions 2', null),

    ('COMPOUND_PROTECTION', 1, 'Compound Protection 1', null),
    ('COMPOUND_PROTECTION', 2, 'Compound Protection 2', null),

    ('SOLVENT', 1, 'Solvent 1', null),
    ('SOLVENT', 2, 'Solvent 2', null),
    ('EXTERNAL_SUPPLIER', 1, 'External Supplier 1', null),
    ('EXTERNAL_SUPPLIER', 2, 'External Supplier 2', null),
    ('SAMPLE_SOURCE', 1, 'Sample Source 1', null),
    ('SAMPLE_SOURCE', 2, 'Sample Source 2', null),
    ('SAMPLE_SOURCE_DETAILS', 1, 'Sample Source Details 1', null),
    ('SAMPLE_SOURCE_DETAILS', 2, 'Sample Source Details 2', null),
    ('COMPONENT_STATE', 1, 'Oil', null),
    ('COMPONENT_STATE', 2, 'Gas', null),
    ('COMPONENT_STATE', 3, 'Solid', null),
    ('COMPONENT_STATE', 4, 'Liquid', null)
;

INSERT INTO Dictionary_Item (id, dictionary, created_by_id, created_at, modified_by_id, modified_at, ordinal, name, description, active)
SELECT gen_random_uuid(), dictionary, '00000000-0000-0000-0000-000000000001', now(), '00000000-0000-0000-0000-000000000001', now(), ordinal, name, description, true
FROM Dictionary_Item_Temp;

DROP TABLE Dictionary_Item_Temp;

CREATE TABLE Salt_Code (
    id UUID PRIMARY KEY,
    code VARCHAR(2) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL UNIQUE,
    formula VARCHAR(200) NOT NULL,
    charge INT NOT NULL,
    mol_weight FLOAT8 NOT NULL
);

INSERT INTO Salt_Code (id, code, name, formula, charge, mol_weight) VALUES
    (gen_random_uuid(), '01', '01 - Sulphate', 'SO4²⁻', -2, 96.063);

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

CREATE TYPE Dictionary AS ENUM ('THERAPEUTIC_AREA', 'PROJECT_CODE', 'STEREOISOMER_CODE', 'TEST');

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

INSERT INTO Dictionary_Item (id, dictionary, created_by_id, created_at, modified_by_id, modified_at, ordinal, name, description, active) VALUES
    (gen_random_uuid(), 'THERAPEUTIC_AREA', '00000000-0000-0000-0000-000000000001', now(), '00000000-0000-0000-0000-000000000001', now(), 1, 'Obesity', null, true),
    (gen_random_uuid(), 'THERAPEUTIC_AREA', '00000000-0000-0000-0000-000000000001', now(), '00000000-0000-0000-0000-000000000001', now(), 2, 'Diabetes', null, true),
    (gen_random_uuid(), 'THERAPEUTIC_AREA', '00000000-0000-0000-0000-000000000001', now(), '00000000-0000-0000-0000-000000000001', now(), 3, 'Pulmonology', null, true),
    (gen_random_uuid(), 'THERAPEUTIC_AREA', '00000000-0000-0000-0000-000000000001', now(), '00000000-0000-0000-0000-000000000001', now(), 4, 'Cancer', null, true),
    (gen_random_uuid(), 'PROJECT_CODE', '00000000-0000-0000-0000-000000000001', now(), '00000000-0000-0000-0000-000000000001', now(), 1, 'Code 1', null, true),
    (gen_random_uuid(), 'PROJECT_CODE', '00000000-0000-0000-0000-000000000001', now(), '00000000-0000-0000-0000-000000000001', now(), 2, 'Code 2', null, true),
    (gen_random_uuid(), 'PROJECT_CODE', '00000000-0000-0000-0000-000000000001', now(), '00000000-0000-0000-0000-000000000001', now(), 3, 'Code 3', null, true),
    (gen_random_uuid(), 'STEREOISOMER_CODE', '00000000-0000-0000-0000-000000000001', now(), '00000000-0000-0000-0000-000000000001', now(), 1, 'NOSTC', 'Achiral - No Stereo Centers', true),
    (gen_random_uuid(), 'STEREOISOMER_CODE', '00000000-0000-0000-0000-000000000001', now(), '00000000-0000-0000-0000-000000000001', now(), 2, 'AMESO', 'Achiral - Meso Stereomers', true),
    (gen_random_uuid(), 'STEREOISOMER_CODE', '00000000-0000-0000-0000-000000000001', now(), '00000000-0000-0000-0000-000000000001', now(), 3, 'CISTR', 'Achiral - Cis/Trans Stereomers', true),
    (gen_random_uuid(), 'STEREOISOMER_CODE', '00000000-0000-0000-0000-000000000001', now(), '00000000-0000-0000-0000-000000000001', now(), 4, 'SNENK', 'Single Enantiomer (chirality known)', true),
    (gen_random_uuid(), 'STEREOISOMER_CODE', '00000000-0000-0000-0000-000000000001', now(), '00000000-0000-0000-0000-000000000001', now(), 5, 'RMCMX', 'Racemic (stereochemistry known)', true),
    (gen_random_uuid(), 'STEREOISOMER_CODE', '00000000-0000-0000-0000-000000000001', now(), '00000000-0000-0000-0000-000000000001', now(), 6, 'ENENK', 'Enantio-Enriched (chirality known)', true),
    (gen_random_uuid(), 'STEREOISOMER_CODE', '00000000-0000-0000-0000-000000000001', now(), '00000000-0000-0000-0000-000000000001', now(), 7, 'DSTRK', 'Diastereomers (stereochemistry known)', true),
    (gen_random_uuid(), 'STEREOISOMER_CODE', '00000000-0000-0000-0000-000000000001', now(), '00000000-0000-0000-0000-000000000001', now(), 8, 'SNENU', 'Other - Single Enantiomer (chirality unknown)', true),
    (gen_random_uuid(), 'STEREOISOMER_CODE', '00000000-0000-0000-0000-000000000001', now(), '00000000-0000-0000-0000-000000000001', now(), 9, 'LRCMX', 'Other - Racemic (relative stereochemistry unknown)', true),
    (gen_random_uuid(), 'STEREOISOMER_CODE', '00000000-0000-0000-0000-000000000001', now(), '00000000-0000-0000-0000-000000000001', now(), 10, 'ENENU', 'Other - Enantio-Enriched (chirality unknown)', true),
    (gen_random_uuid(), 'STEREOISOMER_CODE', '00000000-0000-0000-0000-000000000001', now(), '00000000-0000-0000-0000-000000000001', now(), 11, 'DSTRU', 'Other - Diastereomers (relative stereochemistry unknown)', true),
    (gen_random_uuid(), 'STEREOISOMER_CODE', '00000000-0000-0000-0000-000000000001', now(), '00000000-0000-0000-0000-000000000001', now(), 12, 'UNKWN', 'Other - Unknown Stereomer/Mixture', true),
    (gen_random_uuid(), 'STEREOISOMER_CODE', '00000000-0000-0000-0000-000000000001', now(), '00000000-0000-0000-0000-000000000001', now(), 13, 'HSREG', 'Flag for automatic stereoisomer code assignment for multi-registration', true),
    (gen_random_uuid(), 'STEREOISOMER_CODE', '00000000-0000-0000-0000-000000000001', now(), '00000000-0000-0000-0000-000000000001', now(), 14, 'ACHIR', 'ACHIRAL', true),
    (gen_random_uuid(), 'STEREOISOMER_CODE', '00000000-0000-0000-0000-000000000001', now(), '00000000-0000-0000-0000-000000000001', now(), 15, 'HOMO', 'HOMO-CHIRAL', true),
    (gen_random_uuid(), 'STEREOISOMER_CODE', '00000000-0000-0000-0000-000000000001', now(), '00000000-0000-0000-0000-000000000001', now(), 16, 'MESO', 'MESO', true),
    (gen_random_uuid(), 'STEREOISOMER_CODE', '00000000-0000-0000-0000-000000000001', now(), '00000000-0000-0000-0000-000000000001', now(), 17, 'RACEM', 'RACEMIC', true),
    (gen_random_uuid(), 'STEREOISOMER_CODE', '00000000-0000-0000-0000-000000000001', now(), '00000000-0000-0000-0000-000000000001', now(), 18, 'SCALE', 'SCALEMIC', true)
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

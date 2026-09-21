CREATE TYPE Molarity_Unit AS ENUM (
    'MM',
    'M'
);

CREATE TABLE SRS_Compound (
    id UUID PRIMARY KEY,
    can_smiles VARCHAR(1000) NOT NULL,
    stereoisomer_code UUID,
    salt_code UUID,
    salt_eq_100 INT,
    external_number VARCHAR(1000),
    chemical_name VARCHAR(1000),
    compound_key VARCHAR(1000),
    str_code VARCHAR(1000),
    formula VARCHAR(1000) NOT NULL,
    mol_file TEXT NOT NULL,
    mol_weight DOUBLE PRECISION NOT NULL,
    exact_mass DOUBLE PRECISION NOT NULL,
    cas_number VARCHAR(1000),
    picture BYTEA NOT NULL,
    CONSTRAINT compound_uq UNIQUE (can_smiles, stereoisomer_code, salt_code, salt_eq_100),
    CONSTRAINT compound_str_code_salt_eq_uq UNIQUE (str_code, salt_eq_100) -- str_code is shared across saltEQ
);
CREATE INDEX ix_srs_compound_mol_file ON SRS_Compound USING bingo_idx (mol_file bingo.molecule);

CREATE TABLE SRS_Sample (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    compound_id UUID NOT NULL,
    str_code VARCHAR(64),
    external_number VARCHAR(1000),
    nbk_batch_number VARCHAR(64),
    density NUMERIC,
    molarity NUMERIC,
    molarity_unit Molarity_Unit,
    purity NUMERIC,
    compound_state UUID,
    health_hazards UUID[],
    batch_comment TEXT,
    search_vector TSVECTOR NOT NULL,
    CONSTRAINT sample_compound_id_fk FOREIGN KEY (compound_id) REFERENCES SRS_Compound(id),
    CONSTRAINT sample_str_code_uq UNIQUE (str_code)
);
CREATE INDEX ix_srs_sample_compound_id ON SRS_Sample (compound_id);
CREATE INDEX ix_srs_sample_search_vector ON SRS_Sample USING GIN (search_vector);

CREATE SEQUENCE srs_compound_str_code_compound_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;

CREATE TYPE Compound_External_Source AS ENUM ('PUBCHEM');

CREATE TABLE Compound (
    id UUID PRIMARY KEY,
    can_smiles VARCHAR(1000) NOT NULL,
    stereoisomer_code_id UUID,
    salt_code_id UUID,
    salt_eq_100 INT,
    external_source Compound_External_Source,
    external_number VARCHAR(1000),
    chemical_name VARCHAR(1000),
    compound_key VARCHAR(1000),
    str_code VARCHAR(1000),
    formula VARCHAR(1000) NOT NULL,
    mol_file TEXT NOT NULL,
    mol_weight NUMERIC NOT NULL,
    exact_mass NUMERIC NOT NULL,
    cas_number VARCHAR(1000),
    picture BYTEA NOT NULL,
    CONSTRAINT compound_stereoisomer_code_fk FOREIGN KEY (stereoisomer_code_id) REFERENCES Dictionary_Item (id),
    CONSTRAINT compound_salt_code_fk FOREIGN KEY (salt_code_id) REFERENCES Dictionary_Item (id),
    CONSTRAINT compound_uq UNIQUE (can_smiles, stereoisomer_code_id, salt_code_id, salt_eq_100),
    CONSTRAINT compound_str_code_salt_eq_uq UNIQUE (str_code, salt_eq_100) -- str_code is shared across saltEQ
);

CREATE INDEX ix_compound_mol_file ON Compound USING bingo_idx (mol_file bingo.molecule) ;

CREATE TABLE Sample (
    id UUID PRIMARY KEY,
    created_by_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    modified_by_id UUID NOT NULL,
    modified_at TIMESTAMPTZ NOT NULL,
    compound_id UUID NOT NULL,
    str_code VARCHAR(64),
    external_number VARCHAR(1000),
    nbk_batch_number VARCHAR(64),
    density NUMERIC,
    molarity NUMERIC,
    molarity_unit Molarity_Unit,
    purity NUMERIC,
    compound_state_id UUID,
    batch_comment TEXT,
    search_vector TSVECTOR,
    CONSTRAINT sample_created_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT sample_modified_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT sample_compound_id_fk FOREIGN KEY (compound_id) REFERENCES Compound(id),
    CONSTRAINT sample_compound_state_id_fk FOREIGN KEY (compound_state_id) REFERENCES dictionary_item(id),
    CONSTRAINT sample_str_code_uq UNIQUE (str_code)
);

CREATE TABLE Sample_Health_Hazard (
    sample_id UUID NOT NULL,
    health_hazard_id UUID NOT NULL,
    CONSTRAINT sample_health_hazard_pk PRIMARY KEY (sample_id, health_hazard_id),
    CONSTRAINT sample_health_hazard_sample_id_fk FOREIGN KEY (sample_id) REFERENCES Sample(id) ON DELETE CASCADE,
    CONSTRAINT sample_health_hazard_health_hazard_id_fk FOREIGN KEY (health_hazard_id) REFERENCES dictionary_item(id)
);

-- CREATE TABLE Sample_Compound (
--     sample_id UUID NOT NULL,
--     compound_id UUID NOT NULL,
--     percentage DOUBLE PRECISION NOT NULL,
--     ordinal INT NOT NULL,
--     CONSTRAINT sample_compound_pk PRIMARY KEY (sample_id, compound_id),
--     CONSTRAINT sample_compound_sample_id_fk FOREIGN KEY (sample_id) REFERENCES Sample(id) ON DELETE CASCADE,
--     CONSTRAINT sample_compound_compound_id_fk FOREIGN KEY (compound_id) REFERENCES Compound(id) ON DELETE CASCADE
-- );

CREATE SEQUENCE compound_str_code_compound_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;

CREATE TABLE Sample_Mark (
    sample_id UUID NOT NULL,
    user_id UUID NOT NULL,
    CONSTRAINT sample_mark_pk PRIMARY KEY (sample_id, user_id),
    CONSTRAINT sample_mark_sample_id_fk FOREIGN KEY (sample_id) REFERENCES Sample (id) ON DELETE CASCADE,
    CONSTRAINT sample_mark_user_id_fk FOREIGN KEY (user_id) REFERENCES User_Account (id) ON DELETE CASCADE
);

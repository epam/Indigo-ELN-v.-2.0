CREATE TYPE Sample_Source AS ENUM ('ELN', 'SRS', 'PUBCHEM');

CREATE TABLE Compound (
    id UUID PRIMARY KEY,
    can_smiles VARCHAR(1000) NOT NULL,
    stereoisomer_code_id UUID,
    salt_code_id UUID,
    salt_eq_100 INT,
    source Sample_Source NOT NULL,
    compound_key VARCHAR(1000),
    chemical_name VARCHAR(1000),
    formula VARCHAR(1000) NOT NULL,
    mol_file TEXT NOT NULL,
    mol_weight DOUBLE PRECISION NOT NULL,
    exact_mass DOUBLE PRECISION NOT NULL,
    cas_number VARCHAR(1000),
    picture BYTEA NOT NULL,
    CONSTRAINT compound_stereoisomer_code_fk FOREIGN KEY (stereoisomer_code_id) REFERENCES Dictionary_Item (id),
    CONSTRAINT compound_salt_code_fk FOREIGN KEY (salt_code_id) REFERENCES Dictionary_Item (id),
    CONSTRAINT compound_uq UNIQUE (can_smiles, stereoisomer_code_id, salt_code_id, salt_eq_100)
);
CREATE INDEX ix_compound_mol_file ON Compound USING bingo_idx (mol_file bingo.molecule);
CREATE UNIQUE INDEX ix_compound_uq_2 ON Compound (source, compound_key) WHERE compound_key IS NOT NULL;

CREATE TABLE Sample (
    id UUID PRIMARY KEY,
    created_by_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    modified_by_id UUID NOT NULL,
    modified_at TIMESTAMPTZ NOT NULL,
    compound_id UUID NOT NULL,
    source Sample_Source NOT NULL,
    sample_key VARCHAR(1000),
    nbk_batch_number VARCHAR(64),
    density NUMERIC,
    molarity NUMERIC,
    molarity_unit Molarity_Unit,
    purity NUMERIC,
    compound_state_id UUID,
    batch_comment TEXT,
    search_vector TSVECTOR NOT NULL,
    CONSTRAINT sample_created_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT sample_modified_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT sample_compound_id_fk FOREIGN KEY (compound_id) REFERENCES Compound(id),
    CONSTRAINT sample_compound_state_id_fk FOREIGN KEY (compound_state_id) REFERENCES dictionary_item(id)
);
CREATE INDEX ix_sample_compound_id ON Sample (compound_id);
CREATE INDEX ix_sample_search_vector ON Sample USING GIN (search_vector);
CREATE UNIQUE INDEX ix_sample_uq_2 ON Sample (source, sample_key) WHERE sample_key IS NOT NULL;

CREATE TABLE Sample_Health_Hazard (
    sample_id UUID NOT NULL,
    health_hazard_id UUID NOT NULL,
    CONSTRAINT sample_health_hazard_pk PRIMARY KEY (sample_id, health_hazard_id),
    CONSTRAINT sample_health_hazard_sample_id_fk FOREIGN KEY (sample_id) REFERENCES Sample(id) ON DELETE CASCADE,
    CONSTRAINT sample_health_hazard_health_hazard_id_fk FOREIGN KEY (health_hazard_id) REFERENCES dictionary_item(id)
);

CREATE TABLE Sample_Mark (
    sample_id UUID NOT NULL,
    user_id UUID NOT NULL,
    CONSTRAINT sample_mark_pk PRIMARY KEY (sample_id, user_id),
    CONSTRAINT sample_mark_sample_id_fk FOREIGN KEY (sample_id) REFERENCES Sample (id) ON DELETE CASCADE,
    CONSTRAINT sample_mark_user_id_fk FOREIGN KEY (user_id) REFERENCES User_Account (id) ON DELETE CASCADE
);
CREATE INDEX ix_sample_mark_user_id ON Sample_Mark (user_id);

CREATE TYPE Compound_Source AS ENUM ('ELN');

CREATE TABLE Compound (
    id UUID PRIMARY KEY,
    can_smiles VARCHAR(1000) NOT NULL,
    stereoisomer_code_id UUID,
    salt_code_id UUID,
    salt_eq_100 INT,
    source Compound_Source NOT NULL,
    str_code VARCHAR(1000),
    formula VARCHAR(1000) NOT NULL,
    name VARCHAR(1000),
    mol_file TEXT NOT NULL,
    mol_weight DOUBLE PRECISION NOT NULL,
    CONSTRAINT compound_stereoisomer_code_fk FOREIGN KEY (stereoisomer_code_id) REFERENCES dictionary_item (id),
    CONSTRAINT compound_salt_code_fk FOREIGN KEY (salt_code_id) REFERENCES salt_code (id),
    CONSTRAINT compound_uq UNIQUE (can_smiles, stereoisomer_code_id, salt_code_id, salt_eq_100),
    CONSTRAINT compound_str_code_salt_eq_uq UNIQUE (str_code, salt_eq_100) -- str_code is shared across saltEQ
);

CREATE INDEX ix_compound_mol_file ON Compound USING bingo_idx (mol_file bingo.molecule) ;

CREATE TABLE Compound_Experiment (
    compound_id UUID NOT NULL,
    experiment_id UUID NOT NULL,
    CONSTRAINT compound_experiment_pk PRIMARY KEY (compound_id, experiment_id),
    CONSTRAINT compound_experiment_compound_id_fk FOREIGN KEY (compound_id) REFERENCES Compound(id) ON DELETE CASCADE,
    CONSTRAINT compound_experiment_experiment_id_fk FOREIGN KEY (experiment_id) REFERENCES Experiment(id) ON DELETE CASCADE
);

CREATE TABLE Sample (
    id UUID PRIMARY KEY,
    compound_id UUID NOT NULL,
    str_code VARCHAR(64),
    notebook_batch_number VARCHAR(64),
    density DOUBLE PRECISION,
    molarity DOUBLE PRECISION,
    molarity_unit Molarity_Unit,
    purity DOUBLE PRECISION,
    compound_state_id UUID,
    batch_comment TEXT,
    search_vector TSVECTOR,
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

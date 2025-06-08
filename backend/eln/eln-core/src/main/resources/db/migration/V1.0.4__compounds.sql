CREATE TYPE Compound_Source AS ENUM ('ELN');

CREATE TABLE Compound (
    id UUID PRIMARY KEY,
    source Compound_Source NOT NULL,
    compound_key VARCHAR(1000),
    canonical_smiles VARCHAR(1000) NOT NULL,
    formula VARCHAR(1000) NOT NULL,
    name VARCHAR(1000),
    mol_file TEXT NOT NULL,
    mol_weight DOUBLE PRECISION NOT NULL,
--     CONSTRAINT compound_compound_key UNIQUE (compound_key),
    CONSTRAINT compound_canonical_smiles_uq UNIQUE (canonical_smiles)
);

CREATE INDEX ix_compound_mol_file ON Compound USING bingo_idx (mol_file bingo.molecule) ;

CREATE TABLE Sample (
    id UUID PRIMARY KEY,
    compound_id UUID NOT NULL,
    batch_number VARCHAR(1000),
    CONSTRAINT sample_compound_id_fk FOREIGN KEY (compound_id) REFERENCES Compound(id)
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

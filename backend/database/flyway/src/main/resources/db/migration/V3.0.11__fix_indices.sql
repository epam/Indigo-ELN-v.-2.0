DROP INDEX ix_compound_uq_2;

ALTER TABLE Compound DROP CONSTRAINT compound_uq;
ALTER TABLE Compound ADD CONSTRAINT compound_uq UNIQUE NULLS NOT DISTINCT (can_smiles, stereoisomer_code_id, salt_code_id, salt_eq_100);

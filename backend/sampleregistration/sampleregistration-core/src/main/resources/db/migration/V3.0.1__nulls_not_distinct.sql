-- salt_eq_100 is nullable, and NULLs are distinct by default, so the constraints did not cover parent structures
ALTER TABLE SRS_Compound DROP CONSTRAINT srs_compound_uq;
ALTER TABLE SRS_Compound ADD CONSTRAINT srs_compound_uq UNIQUE NULLS NOT DISTINCT (can_smiles, stereoisomer_code, salt_code, salt_eq_100);

ALTER TABLE SRS_Compound DROP CONSTRAINT srs_compound_str_code_salt_eq_uq;
ALTER TABLE SRS_Compound ADD CONSTRAINT srs_compound_str_code_salt_eq_uq UNIQUE NULLS NOT DISTINCT (str_code, salt_eq_100);

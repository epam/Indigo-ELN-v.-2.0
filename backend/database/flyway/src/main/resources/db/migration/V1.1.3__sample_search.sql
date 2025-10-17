ALTER TABLE Compound ADD COLUMN cas_number VARCHAR(1000);
ALTER TABLE Compound DROP COLUMN name;

ALTER TABLE Sample ADD COLUMN external_number VARCHAR(1000);
ALTER TABLE Sample RENAME COLUMN notebook_batch_number TO nbk_batch_number;
ALTER TABLE Sample ADD COLUMN chemical_name VARCHAR(1000);

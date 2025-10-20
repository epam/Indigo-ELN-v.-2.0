ALTER TABLE Compound ADD COLUMN cas_number VARCHAR(1000);
ALTER TABLE Compound DROP COLUMN name;
ALTER TABLE Compound ADD COLUMN exact_mass DOUBLE PRECISION;

-- TODO delete this after DB is recreated
UPDATE Compound SET exact_mass = mol_weight;
ALTER TABLE Compound ALTER COLUMN exact_mass SET NOT NULL;

ALTER TABLE Sample ADD COLUMN external_number VARCHAR(1000);
ALTER TABLE Sample RENAME COLUMN notebook_batch_number TO nbk_batch_number;
ALTER TABLE Sample ADD COLUMN chemical_name VARCHAR(1000);

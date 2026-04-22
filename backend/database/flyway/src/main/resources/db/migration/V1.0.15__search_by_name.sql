ALTER TABLE Compound DROP COLUMN source;
DROP TYPE Compound_Source;

CREATE TYPE Compound_External_Source AS ENUM ('PUBCHEM');
ALTER TABLE Compound ADD COLUMN external_source Compound_External_Source;
ALTER TABLE Compound ADD COLUMN external_number VARCHAR(1000);

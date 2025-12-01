ALTER TABLE Compound ADD COLUMN chemical_name VARCHAR(1000);
ALTER TABLE Sample DROP COLUMN chemical_name;

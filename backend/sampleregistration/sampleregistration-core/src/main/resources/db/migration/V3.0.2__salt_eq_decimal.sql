ALTER TABLE SRS_Compound ALTER COLUMN salt_eq_100 TYPE DECIMAL USING trim_scale(salt_eq_100 / 100.0);
ALTER TABLE SRS_Compound RENAME COLUMN salt_eq_100 TO salt_eq;

ALTER TABLE Project ADD COLUMN full_acl ACL_Entry[];
ALTER TABLE Project ADD COLUMN short_acl ACL_Entry[];
UPDATE Project SET full_acl = '{}', short_acl = '{}' WHERE true;
ALTER TABLE Project ALTER COLUMN full_acl SET NOT NULL;
ALTER TABLE Project ALTER COLUMN short_acl SET NOT NULL;

ALTER TABLE Notebook ADD COLUMN full_acl ACL_Entry[];
ALTER TABLE Notebook ADD COLUMN short_acl ACL_Entry[];
UPDATE Notebook SET full_acl = '{}', short_acl = '{}' WHERE true;
ALTER TABLE Notebook ALTER COLUMN full_acl SET NOT NULL;
ALTER TABLE Notebook ALTER COLUMN short_acl SET NOT NULL;

ALTER TABLE Experiment ADD COLUMN full_acl ACL_Entry[];
ALTER TABLE Experiment ADD COLUMN short_acl ACL_Entry[];
UPDATE Experiment SET full_acl = '{}', short_acl = '{}' WHERE true;
ALTER TABLE Experiment ALTER COLUMN full_acl SET NOT NULL;
ALTER TABLE Experiment ALTER COLUMN short_acl SET NOT NULL;

ALTER TABLE Notebook_ACL DROP COLUMN inherited;
ALTER TABLE Experiment_ACL DROP COLUMN inherited;

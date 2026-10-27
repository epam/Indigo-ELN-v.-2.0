-- Effective ACL is now computed from the *_ACL tables (Java for lists/details, *_Access_View for RLS)
DROP VIEW Project_Access_View;
DROP VIEW Notebook_Access_View;
DROP VIEW Experiment_Access_View;

DROP INDEX ix_project_acl_gin;
DROP INDEX ix_notebook_acl_gin;
DROP INDEX ix_experiment_acl_gin;

ALTER TABLE Project DROP COLUMN full_acl, DROP COLUMN short_acl;
ALTER TABLE Notebook DROP COLUMN full_acl, DROP COLUMN short_acl;
ALTER TABLE Experiment DROP COLUMN full_acl, DROP COLUMN short_acl;

DROP FUNCTION acl_user_ids(ACL_Entry[]);
DROP TYPE ACL_Entry;

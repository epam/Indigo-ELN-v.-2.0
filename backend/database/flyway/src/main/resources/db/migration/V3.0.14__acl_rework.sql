-- Effective ACL is computed from the *_ACL and *_Implicit_Access tables (Java for lists/details, *_Access_View for RLS)
DROP VIEW IF EXISTS Project_Access_View;
DROP VIEW IF EXISTS Notebook_Access_View;
DROP VIEW IF EXISTS Experiment_Access_View;

DROP INDEX ix_project_acl_gin;
DROP INDEX ix_notebook_acl_gin;
DROP INDEX ix_experiment_acl_gin;

ALTER TABLE Project DROP COLUMN full_acl, DROP COLUMN short_acl;
ALTER TABLE Notebook DROP COLUMN full_acl, DROP COLUMN short_acl;
ALTER TABLE Experiment DROP COLUMN full_acl, DROP COLUMN short_acl;

DROP FUNCTION acl_user_ids(ACL_Entry[]);
DROP TYPE ACL_Entry;

-- Users who created or have explicit access to a nested entity, kept apart from explicit ACL
CREATE TABLE Project_Implicit_Access (
    project_id UUID NOT NULL,
    user_id UUID NOT NULL,
    CONSTRAINT project_implicit_access_pk PRIMARY KEY (project_id, user_id),
    CONSTRAINT project_implicit_access_project_id_fk FOREIGN KEY (project_id) REFERENCES Project (id) ON DELETE CASCADE,
    CONSTRAINT project_implicit_access_user_id_fk FOREIGN KEY (user_id) REFERENCES User_Account (id) ON DELETE CASCADE
);

CREATE TABLE Notebook_Implicit_Access (
    notebook_id UUID NOT NULL,
    user_id UUID NOT NULL,
    CONSTRAINT notebook_implicit_access_pk PRIMARY KEY (notebook_id, user_id),
    CONSTRAINT notebook_implicit_access_notebook_id_fk FOREIGN KEY (notebook_id) REFERENCES Notebook (id) ON DELETE CASCADE,
    CONSTRAINT notebook_implicit_access_user_id_fk FOREIGN KEY (user_id) REFERENCES User_Account (id) ON DELETE CASCADE
);

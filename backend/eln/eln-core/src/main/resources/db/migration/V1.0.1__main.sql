CREATE TYPE Experiment_Status AS ENUM ('OPEN', 'WAITING_FOR_SIGNATURE', 'COMPLETED', 'REJECTED');

CREATE TYPE Access_Level AS ENUM ('IMPLICIT_VIEW', 'VIEW', 'EDIT', 'ADMIN', 'AUTHOR');

CREATE TYPE ACL_Entry AS (user_id UUID, display_name TEXT, level Access_Level, inherited BOOLEAN);

CREATE TYPE Experiment_Count AS (status Experiment_Status, count INT);

CREATE TYPE Application_Permission AS ENUM (
    'MANAGE_USERS',
    'MANAGE_ROLES',
    'MANAGE_DICTIONARIES',
    'MANAGE_TEMPLATES',

    'VIEW_PROJECTS',
    'CREATE_PROJECTS',
    'EDIT_PROJECTS',
    'DELETE_PROJECTS',

    'VIEW_NOTEBOOKS',
    'CREATE_NOTEBOOKS',
    'EDIT_NOTEBOOKS',
    'DELETE_NOTEBOOKS',

    'VIEW_EXPERIMENTS',
    'CREATE_EXPERIMENTS',
    'EDIT_EXPERIMENTS',
    'DELETE_EXPERIMENTS'
);

CREATE TABLE Application_Role (
    id UUID PRIMARY KEY,
    name VARCHAR(256) NOT NULL,
--     permissions Application_Permission[] NOT NULL,
    permissions VARCHAR[] NOT NULL,
    CONSTRAINT application_role_name_uq UNIQUE (name)
);

CREATE TABLE User_Account (
    id UUID PRIMARY KEY,
    created_by_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    modified_by_id UUID NOT NULL,
    modified_at TIMESTAMPTZ NOT NULL,
    username VARCHAR(256) NOT NULL,
    first_name VARCHAR(256),
    last_name VARCHAR(256),
    display_name VARCHAR(256) NOT NULL,
    CONSTRAINT user_account_created_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT user_account_modified_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT user_account_username_uq UNIQUE (username)
);
CREATE UNIQUE INDEX ix_user_account_display_name ON User_Account (lower(display_name));

CREATE TABLE User_Account_Application_Role (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    CONSTRAINT user_account_role_pk PRIMARY KEY (user_id, role_id),
    CONSTRAINT user_account_role_user_id_fk FOREIGN KEY (user_id) REFERENCES User_Account (id) ON DELETE CASCADE,
    CONSTRAINT user_account_role_role_id_fk FOREIGN KEY (role_id) REFERENCES Application_Role (id) ON DELETE CASCADE
);

INSERT INTO Application_Role (id, name, permissions) VALUES
    ('00000000-0000-0000-0000-000000000002', 'Administrators', ARRAY[
        'MANAGE_USERS',
        'MANAGE_ROLES',
        'MANAGE_DICTIONARIES',
        'MANAGE_TEMPLATES',

        'VIEW_PROJECTS',
        'CREATE_PROJECTS',
        'EDIT_PROJECTS',
        'DELETE_PROJECTS',

        'VIEW_NOTEBOOKS',
        'CREATE_NOTEBOOKS',
        'EDIT_NOTEBOOKS',
        'DELETE_NOTEBOOKS',

        'VIEW_EXPERIMENTS',
        'CREATE_EXPERIMENTS',
        'EDIT_EXPERIMENTS',
        'DELETE_EXPERIMENTS'
    ]),
    ('00000000-0000-0000-0000-000000000003', 'Content Editor', ARRAY[
        'VIEW_PROJECTS',
        'CREATE_PROJECTS',
        'EDIT_PROJECTS',
        'DELETE_PROJECTS',

        'VIEW_NOTEBOOKS',
        'CREATE_NOTEBOOKS',
        'EDIT_NOTEBOOKS',
        'DELETE_NOTEBOOKS',

        'VIEW_EXPERIMENTS',
        'CREATE_EXPERIMENTS',
        'EDIT_EXPERIMENTS',
        'DELETE_EXPERIMENTS'
    ]),
    ('00000000-0000-0000-0000-000000000004', 'Template Editor', ARRAY[
        'MANAGE_TEMPLATES'
    ])
;

INSERT INTO User_Account (id
        , created_by_id, created_at, modified_by_id, modified_at
        , username, last_name, display_name)
VALUES ('00000000-0000-0000-0000-000000000001'
        , '00000000-0000-0000-0000-000000000001', NOW(), '00000000-0000-0000-0000-000000000001', NOW()
        , 'admin', 'Administrator', 'Administrator'
);

INSERT INTO User_Account_Application_Role (user_id, role_id)
VALUES ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002');

CREATE TABLE Attachment (
    id UUID PRIMARY KEY,
    created_by_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    modified_by_id UUID NOT NULL,
    modified_at TIMESTAMPTZ NOT NULL,
    name VARCHAR(256) NOT NULL,
    size BIGINT NOT NULL,
    content BYTEA NOT NULL,
    CONSTRAINT attachment_created_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT attachment_modified_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id)
);

CREATE TABLE Project (
    id UUID PRIMARY KEY,
    created_by_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    modified_by_id UUID NOT NULL,
    modified_at TIMESTAMPTZ NOT NULL,
    name VARCHAR(256) NOT NULL,
    literature TEXT,
    description TEXT,
    search_vector TSVECTOR,
    CONSTRAINT project_created_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT project_modified_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT project_name_uq UNIQUE (name)
);
CREATE INDEX ix_project_search_vector ON Project USING GIN(search_vector);

CREATE TABLE Project_Keyword (
    project_id UUID NOT NULL,
    keyword_id UUID NOT NULL,
    ordinal INT NOT NULL,
    CONSTRAINT project_keyword_pk PRIMARY KEY (project_id, keyword_id),
    CONSTRAINT project_keyword_project_id_fk FOREIGN KEY (project_id) REFERENCES Project (id) ON DELETE CASCADE,
    CONSTRAINT project_keyword_keyword_id_fk FOREIGN KEY (keyword_id) REFERENCES Dictionary_Item (id),
    CONSTRAINT project_keyword_ordinal_uq UNIQUE (project_id, ordinal) DEFERRABLE INITIALLY DEFERRED
);

CREATE TABLE Project_Attachment (
    project_id UUID NOT NULL,
    attachment_id UUID NOT NULL,
    CONSTRAINT project_attachment_pk PRIMARY KEY (project_id, attachment_id),
    CONSTRAINT project_attachment_project_id_fk FOREIGN KEY (project_id) REFERENCES Project (id) ON DELETE CASCADE,
    CONSTRAINT project_attachment_attachment_id_fk FOREIGN KEY (attachment_id) REFERENCES Attachment (id) ON DELETE CASCADE
);

CREATE TABLE Project_ACL (
    project_id UUID NOT NULL,
    user_id UUID NOT NULL,
    level Access_Level NOT NULL,
    CONSTRAINT project_acl_pk PRIMARY KEY (project_id, user_id),
    CONSTRAINT project_acl_project_id_fk FOREIGN KEY (project_id) REFERENCES Project (id) ON DELETE CASCADE,
    CONSTRAINT project_acl_user_id_fk FOREIGN KEY (user_id) REFERENCES User_Account (id) ON DELETE CASCADE
);

CREATE TABLE Notebook (
    id UUID PRIMARY KEY,
    created_by_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    modified_by_id UUID NOT NULL,
    modified_at TIMESTAMPTZ NOT NULL,
    project_id UUID NOT NULL,
    name VARCHAR(256) NOT NULL,
    description TEXT,
    search_vector TSVECTOR,
    CONSTRAINT notebook_created_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT notebook_modified_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT notebook_project_id_fk FOREIGN KEY (project_id) REFERENCES Project (id),
    CONSTRAINT notebook_name_uq UNIQUE (name)
);
CREATE INDEX ix_notebook_search_vector ON Notebook USING GIN(search_vector);

CREATE TABLE Notebook_Attachment (
    notebook_id UUID NOT NULL,
    attachment_id UUID NOT NULL,
    CONSTRAINT notebook_attachment_pk PRIMARY KEY (notebook_id, attachment_id),
    CONSTRAINT notebook_attachment_notebook_id_fk FOREIGN KEY (notebook_id) REFERENCES Notebook (id) ON DELETE CASCADE,
    CONSTRAINT notebook_attachment_attachment_id_fk FOREIGN KEY (attachment_id) REFERENCES Attachment (id) ON DELETE CASCADE
);

CREATE TABLE Notebook_ACL (
    notebook_id UUID NOT NULL,
    user_id UUID NOT NULL,
    level Access_Level NOT NULL,
    inherited BOOLEAN NOT NULL,
    CONSTRAINT notebook_acl_pk PRIMARY KEY (notebook_id, user_id),
    CONSTRAINT notebook_acl_notebook_id_fk FOREIGN KEY (notebook_id) REFERENCES Notebook (id) ON DELETE CASCADE,
    CONSTRAINT notebook_acl_user_id_fk FOREIGN KEY (user_id) REFERENCES User_Account (id) ON DELETE CASCADE
);

CREATE TABLE Template (
    id UUID PRIMARY KEY,
    created_by_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    modified_by_id UUID NOT NULL,
    modified_at TIMESTAMPTZ NOT NULL,
    name VARCHAR(256) NOT NULL,
    components JSONB NOT NULL,
    CONSTRAINT template_created_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT template_modified_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id)
);

CREATE TABLE Experiment (
    id UUID PRIMARY KEY,
    created_by_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    modified_by_id UUID NOT NULL,
    modified_at TIMESTAMPTZ NOT NULL,
    project_id UUID NOT NULL,
    notebook_id UUID NOT NULL,
    name VARCHAR(256) NOT NULL,
    status Experiment_Status NOT NULL,
    therapeutic_area_id UUID,
    project_code_id UUID,
    description TEXT,
    model JSONB NOT NULL,
    picture BYTEA,
    search_vector TSVECTOR,
    CONSTRAINT experiment_created_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT experiment_modified_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT experiment_project_id_fk FOREIGN KEY (project_id) REFERENCES Project (id),
    CONSTRAINT experiment_notebook_id_fk FOREIGN KEY (notebook_id) REFERENCES Notebook (id),
    CONSTRAINT experiment_therapeutic_area_id_fk FOREIGN KEY (therapeutic_area_id) REFERENCES Dictionary_Item (id),
    CONSTRAINT experiment_project_code_id FOREIGN KEY (project_code_id) REFERENCES Dictionary_Item (id),
    CONSTRAINT experiment_name_uq UNIQUE (name)
);
CREATE INDEX ix_experiment_search_vector ON Experiment USING GIN(search_vector);

CREATE TABLE Experiment_Attachment (
    experiment_id UUID NOT NULL,
    attachment_id UUID NOT NULL,
    CONSTRAINT experiment_attachment_pk PRIMARY KEY (experiment_id, attachment_id),
    CONSTRAINT experiment_attachment_experiment_id_fk FOREIGN KEY (experiment_id) REFERENCES Experiment (id) ON DELETE CASCADE,
    CONSTRAINT experiment_attachment_attachment_id_fk FOREIGN KEY (attachment_id) REFERENCES Attachment (id) ON DELETE CASCADE
);

CREATE TABLE Experiment_Mark (
    experiment_id UUID NOT NULL,
    user_id UUID NOT NULL,
    CONSTRAINT experiment_mark_pk PRIMARY KEY (experiment_id, user_id),
    CONSTRAINT experiment_mark_experiment_id_fk FOREIGN KEY (experiment_id) REFERENCES Experiment (id) ON DELETE CASCADE,
    CONSTRAINT experiment_mark_user_id_fk FOREIGN KEY (user_id) REFERENCES User_Account (id) ON DELETE CASCADE
);

CREATE TABLE Experiment_ACL (
    experiment_id UUID NOT NULL,
    user_id UUID NOT NULL,
    level Access_Level NOT NULL,
    inherited BOOLEAN NOT NULL,
    CONSTRAINT experiment_acl_pk PRIMARY KEY (experiment_id, user_id),
    CONSTRAINT experiment_acl_experiment_id_fk FOREIGN KEY (experiment_id) REFERENCES Experiment (id) ON DELETE CASCADE,
    CONSTRAINT experiment_acl_user_id_fk FOREIGN KEY (user_id) REFERENCES User_Account (id) ON DELETE CASCADE
);

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
    first_name VARCHAR(256) NOT NULL,
    last_name VARCHAR(256) NOT NULL,
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
        'SYSTEM_OPERATIONS',
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
    ]),
    ('00000000-0000-0000-0000-000000000005', 'Project Creator', ARRAY[
        'CREATE_PROJECTS'
    ])
;

INSERT INTO User_Account (id
    , created_by_id, created_at, modified_by_id, modified_at
    , username, first_name, last_name, display_name)
VALUES ('00000000-0000-0000-0000-000000000001'
    , '00000000-0000-0000-0000-000000000001', NOW(), '00000000-0000-0000-0000-000000000001', NOW()
    , 'admin', 'Administrator', 'Administrator', 'Administrator'
);

INSERT INTO User_Account_Application_Role (user_id, role_id)
VALUES ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002');

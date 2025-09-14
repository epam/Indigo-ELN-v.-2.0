CREATE TYPE Experiment_Status AS ENUM (
    'OPEN',
    'REOPEN',
    'COMPLETED',
    'SUBMITTED',
    'SIGNING',
    'REJECTED',
    'SIGNED',
    'ARCHIVED',
    'CANCELLED'
);

CREATE TYPE Access_Level AS ENUM (
    'IMPLICIT_VIEW',
    'VIEW',
    'EDIT',
    'ADMIN',
    'AUTHOR'
);

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
    'DELETE_EXPERIMENTS',
    'SUBMIT_EXPERIMENTS'
);

CREATE TYPE Signature_Reason AS ENUM (
    'AUTHOR',
    'WITNESS'
);

CREATE TYPE Signature_Status AS ENUM (
    'APPROVED',
    'REJECTED'
);

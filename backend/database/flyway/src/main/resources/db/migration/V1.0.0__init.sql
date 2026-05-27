CREATE EXTENSION IF NOT EXISTS pg_stat_statements;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

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

CREATE TYPE ACL_Entry AS (user_id UUID, display_name TEXT, username TEXT, level Access_Level, inherited BOOLEAN);

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

CREATE TYPE Mol_Unit AS ENUM (
    'UMOL',
    'MMOL',
    'MOL'
);
CREATE TYPE Mol_Weight_Unit AS ENUM (
    'G_PER_MOL'
);
CREATE TYPE Volume_Unit AS ENUM (
    'ML',
    'L'
);
CREATE TYPE Weight_Unit AS ENUM (
    'MG',
    'G',
    'KG'
);
CREATE TYPE Molarity_Unit AS ENUM (
    'MM',
    'M'
);
CREATE TYPE Density_Unit AS ENUM (
    'G_ML'
);

CREATE TYPE Reaction_Role AS ENUM (
    'REACTANT',
    'CATALYST',
    'SOLVENT',
    'OUTPUT'
);

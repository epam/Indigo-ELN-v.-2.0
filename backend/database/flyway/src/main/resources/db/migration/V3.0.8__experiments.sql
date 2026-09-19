CREATE TABLE Experiment (
    id UUID PRIMARY KEY,
    revision INT NOT NULL,
    deleted BOOL NOT NULL,
    created_by_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    modified_by_id UUID NOT NULL,
    modified_at TIMESTAMPTZ NOT NULL,
    project_id UUID NOT NULL,
    notebook_id UUID NOT NULL,
    template_id UUID NOT NULL,
    name VARCHAR(256) NOT NULL,
    status Experiment_Status NOT NULL,
    therapeutic_area_id UUID,
    project_code_id UUID,
    title VARCHAR(1024),
    description TEXT,
    literature TEXT,
    batch_creator_id UUID NOT NULL,
    model JSONB NOT NULL,
    picture BYTEA,
    search_vector TSVECTOR NOT NULL,
    full_acl ACL_Entry[] NOT NULL,
    short_acl ACL_Entry[] NOT NULL,
    signature_number VARCHAR(1024),
    signature_attachment_id UUID,
    version INT,
    linked_experiments UUID[] NOT NULL,
    continued_from UUID[] NOT NULL,
    continued_to UUID[] NOT NULL,
    CONSTRAINT experiment_created_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT experiment_modified_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT experiment_project_id_fk FOREIGN KEY (project_id) REFERENCES Project (id),
    CONSTRAINT experiment_notebook_id_fk FOREIGN KEY (notebook_id) REFERENCES Notebook (id),
    CONSTRAINT experiment_template_id_fk FOREIGN KEY (template_id) REFERENCES Template (id),
    CONSTRAINT experiment_therapeutic_area_id_fk FOREIGN KEY (therapeutic_area_id) REFERENCES Dictionary_Item (id),
    CONSTRAINT experiment_project_code_id FOREIGN KEY (project_code_id) REFERENCES Dictionary_Item (id),
    CONSTRAINT experiment_name_uq UNIQUE (name)
);
CREATE INDEX ix_experiment_search_vector ON Experiment USING GIN(search_vector);
CREATE INDEX ix_experiment_name ON Experiment USING GIN (name gin_trgm_ops);
CREATE INDEX ix_experiment_acl_gin ON Experiment USING GIN (acl_user_ids(full_acl));
CREATE INDEX ix_experiment_notebook_id ON Experiment (notebook_id);
CREATE INDEX ix_experiment_project_id ON Experiment (project_id);
CREATE INDEX ix_experiment_created_by_id ON Experiment (created_by_id);
CREATE INDEX ix_experiment_therapeutic_area_id ON Experiment (therapeutic_area_id);
CREATE INDEX ix_experiment_project_code_id ON Experiment (project_code_id);

CREATE TABLE Experiment_Mark (
    experiment_id UUID NOT NULL,
    user_id UUID NOT NULL,
    CONSTRAINT experiment_mark_pk PRIMARY KEY (experiment_id, user_id),
    CONSTRAINT experiment_mark_experiment_id_fk FOREIGN KEY (experiment_id) REFERENCES Experiment (id) ON DELETE CASCADE,
    CONSTRAINT experiment_mark_user_id_fk FOREIGN KEY (user_id) REFERENCES User_Account (id) ON DELETE CASCADE
);
CREATE INDEX ix_experiment_mark_user_id ON Experiment_Mark (user_id);

CREATE TABLE Experiment_ACL (
    experiment_id UUID NOT NULL,
    user_id UUID NOT NULL,
    level Access_Level NOT NULL,
    CONSTRAINT experiment_acl_pk PRIMARY KEY (experiment_id, user_id),
    CONSTRAINT experiment_acl_experiment_id_fk FOREIGN KEY (experiment_id) REFERENCES Experiment (id) ON DELETE CASCADE,
    CONSTRAINT experiment_acl_user_id_fk FOREIGN KEY (user_id) REFERENCES User_Account (id) ON DELETE CASCADE
);

CREATE TABLE Experiment_Referenced_Compound (
    experiment_id UUID NOT NULL,
    compound_id UUID NOT NULL,
    reaction_role Reaction_Role NOT NULL,
    CONSTRAINT experiment_referenced_compound_pk PRIMARY KEY (compound_id, experiment_id, reaction_role),
    CONSTRAINT experiment_referenced_compound_experiment_id_fk FOREIGN KEY (experiment_id) REFERENCES Experiment(id) ON DELETE CASCADE,
    CONSTRAINT experiment_referenced_compound_compound_id_fk FOREIGN KEY (compound_id) REFERENCES Compound(id)
);
CREATE INDEX ix_experiment_referenced_compound_experiment_id ON Experiment_Referenced_Compound (experiment_id);

CREATE TABLE Experiment_Rxnfile (
    experiment_id UUID NOT NULL,
    ordinal INT NOT NULL,
    rxnfile TEXT NOT NULL,
    CONSTRAINT experiment_rxnfile_pk PRIMARY KEY (experiment_id, ordinal),
    CONSTRAINT experiment_rxnfile_experiment_id_fk FOREIGN KEY (experiment_id) REFERENCES Experiment(id) ON DELETE CASCADE
);
CREATE INDEX ix_experiment_rxnfile_rxnfile ON Experiment_Rxnfile USING bingo_idx (rxnfile bingo.reaction);

CREATE TABLE Experiment_Search_Batch (
    experiment_id UUID NOT NULL,
    batch_purity DOUBLE PRECISION,
    batch_yield DOUBLE PRECISION,
    CONSTRAINT experiment_search_batch_parent_id_fk FOREIGN KEY (experiment_id) REFERENCES Experiment (id) ON DELETE CASCADE
);

CREATE TABLE Experiment_Revision (
    experiment_id UUID NOT NULL,
    revision INT NOT NULL,
    user_id UUID NOT NULL,
    datetime TIMESTAMPTZ NOT NULL,
    summary VARCHAR(1000) NOT NULL,
    mutation JSONB NOT NULL,
    diff JSONB NOT NULL,
    undo_for INT,
    redo_for INT,
    version INT,
    snapshot JSONB,
    messages VARCHAR(1024)[],
    debug_messages VARCHAR(1024)[],
    CONSTRAINT experiment_revision_pk PRIMARY KEY (experiment_id, revision),
    CONSTRAINT experiment_revision_experiment_id_fk FOREIGN KEY (experiment_id) REFERENCES Experiment (id),
    CONSTRAINT experiment_revision_experiment_id_version_uq UNIQUE (experiment_id, version)
);

ALTER TABLE Experiment ADD CONSTRAINT experiment_id_revision_fk FOREIGN KEY (id, revision) REFERENCES Experiment_Revision (experiment_id, revision) DEFERRABLE INITIALLY DEFERRED;

CREATE TABLE Experiment_Attachment (
    id UUID PRIMARY KEY,
    created_by_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    modified_by_id UUID NOT NULL,
    modified_at TIMESTAMPTZ NOT NULL,
    name VARCHAR(256) NOT NULL,
    size BIGINT NOT NULL,
    deleted BOOL NOT NULL,
    content BYTEA NOT NULL,
    experiment_id UUID,
    CONSTRAINT experiment_attachment_created_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT experiment_attachment_modified_by_id_fk FOREIGN KEY (modified_by_id) REFERENCES User_Account (id),
    CONSTRAINT experiment_attachment_experiment_id_fk FOREIGN KEY (experiment_id) REFERENCES Experiment (id) ON DELETE CASCADE
);
CREATE INDEX ix_experiment_attachment_experiment_id ON Experiment_Attachment (experiment_id);

ALTER TABLE Experiment ADD CONSTRAINT experiment_signature_attachment_id_fk FOREIGN KEY (signature_attachment_id) REFERENCES Experiment_Attachment (id) ON DELETE SET NULL;

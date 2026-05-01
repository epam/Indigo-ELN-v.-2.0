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
    search_vector TSVECTOR,
    full_acl ACL_Entry[] NOT NULL,
    short_acl ACL_Entry[] NOT NULL,
    report_for_signature_id UUID,
    CONSTRAINT experiment_created_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT experiment_modified_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT experiment_project_id_fk FOREIGN KEY (project_id) REFERENCES Project (id),
    CONSTRAINT experiment_notebook_id_fk FOREIGN KEY (notebook_id) REFERENCES Notebook (id),
    CONSTRAINT experiment_template_id_fk FOREIGN KEY (template_id) REFERENCES Template (id),
    CONSTRAINT experiment_therapeutic_area_id_fk FOREIGN KEY (therapeutic_area_id) REFERENCES Dictionary_Item (id),
    CONSTRAINT experiment_project_code_id FOREIGN KEY (project_code_id) REFERENCES Dictionary_Item (id),
    CONSTRAINT experiment_name_uq UNIQUE (name),
    CONSTRAINT experiment_report_for_signature_id_fk FOREIGN KEY (report_for_signature_id) REFERENCES Attachment (id) ON DELETE SET NULL
);

CREATE INDEX ix_experiment_search_vector ON Experiment USING GIN(search_vector);
CREATE INDEX ix_experiment_name ON Experiment USING GIN (name gin_trgm_ops);

CREATE TABLE Experiment_Linked_Experiment (
    parent_id UUID NOT NULL,
    experiment_id UUID NOT NULL,
    CONSTRAINT experiment_linked_experiment_pk PRIMARY KEY (parent_id, experiment_id),
    CONSTRAINT experiment_linked_experiment_parent_id_fk FOREIGN KEY (parent_id) REFERENCES Experiment (id) ON DELETE CASCADE,
    CONSTRAINT experiment_linked_experiment_experiment_id_fk FOREIGN KEY (experiment_id) REFERENCES Experiment (id) ON DELETE CASCADE
);

CREATE TABLE Experiment_Continued_From (
    parent_id UUID NOT NULL,
    experiment_id UUID NOT NULL,
    CONSTRAINT experiment_continued_from_pk PRIMARY KEY (parent_id, experiment_id),
    CONSTRAINT experiment_continued_from_parent_id_fk FOREIGN KEY (parent_id) REFERENCES Experiment (id) ON DELETE CASCADE,
    CONSTRAINT experiment_continued_from_experiment_id_fk FOREIGN KEY (experiment_id) REFERENCES Experiment (id) ON DELETE CASCADE
);

CREATE TABLE Experiment_Continued_To (
    parent_id UUID NOT NULL,
    experiment_id UUID NOT NULL,
    CONSTRAINT experiment_continued_to_pk PRIMARY KEY (parent_id, experiment_id),
    CONSTRAINT experiment_continued_to_parent_id_fk FOREIGN KEY (parent_id) REFERENCES Experiment (id) ON DELETE CASCADE,
    CONSTRAINT experiment_continued_to_experiment_id_fk FOREIGN KEY (experiment_id) REFERENCES Experiment (id) ON DELETE CASCADE
);

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

CREATE TABLE Experiment_Signature (
    id UUID PRIMARY KEY,
    experiment_id UUID NOT NULL,
    ordinal INT, -- should be NOT NULL, but Hibernate initially inserts NULL
    user_id UUID NOT NULL,
    reason Signature_Reason NOT NULL,
    status Signature_Status,
    signed_at TIMESTAMPTZ,
    CONSTRAINT experiment_signature_experiment_id FOREIGN KEY (experiment_id) REFERENCES Experiment (id) ON DELETE CASCADE,
    CONSTRAINT experiment_signature_user_id FOREIGN KEY (user_id) REFERENCES User_Account (id)
);

CREATE TABLE Experiment_Rxnfile (
    experiment_id UUID NOT NULL,
    ordinal INT NOT NULL,
    rxnfile TEXT NOT NULL,
    CONSTRAINT experiment_rxnfile_pk PRIMARY KEY (experiment_id, ordinal),
    CONSTRAINT experiment_rxnfile_experiment_id_fk FOREIGN KEY (experiment_id) REFERENCES Experiment(id) ON DELETE CASCADE
);

CREATE INDEX ix_experiment_rxnfile_rxnfile ON Experiment_Rxnfile USING bingo_idx (rxnfile bingo.reaction);

CREATE TABLE Experiment_Edit_Session (
    id UUID PRIMARY KEY,
    experiment_id UUID NOT NULL,
    user_id UUID NOT NULL,
    started TIMESTAMPTZ NOT NULL,
    last_active TIMESTAMPTZ NOT NULL,
    finished TIMESTAMPTZ,
    CONSTRAINT experiment_edit_session_experiment_id_fk FOREIGN KEY (experiment_id) REFERENCES Experiment (id),
    CONSTRAINT experiment_edit_session_user_id_fk FOREIGN KEY (user_id) REFERENCES User_Account (id)
);

CREATE TABLE Experiment_Revision (
    experiment_id UUID NOT NULL,
    revision INT NOT NULL,
    user_id UUID NOT NULL,
    datetime TIMESTAMPTZ NOT NULL,
    edit_session_id UUID,
    summary VARCHAR(1000) NOT NULL,
    mutation JSONB NOT NULL,
    diff JSONB NOT NULL,
    undo_for INT,
    redo_for INT,
    version INT,
    snapshot JSONB,
    CONSTRAINT experiment_revision_pk PRIMARY KEY (experiment_id, revision),
    CONSTRAINT experiment_revision_experiment_id_fk FOREIGN KEY (experiment_id) REFERENCES Experiment (id),
    CONSTRAINT experiment_revision_edit_session_id_fk FOREIGN KEY (edit_session_id) REFERENCES Experiment_Edit_Session (id),
    CONSTRAINT experiment_revision_experiment_id_version_uq UNIQUE (experiment_id, version)
);

ALTER TABLE Experiment ADD CONSTRAINT experiment_id_revision_fk FOREIGN KEY (id, revision) REFERENCES Experiment_Revision (experiment_id, revision) DEFERRABLE INITIALLY DEFERRED;

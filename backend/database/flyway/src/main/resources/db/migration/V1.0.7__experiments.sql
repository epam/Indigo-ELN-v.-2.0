CREATE TABLE Template (
    id UUID PRIMARY KEY,
    created_by_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    modified_by_id UUID NOT NULL,
    modified_at TIMESTAMPTZ NOT NULL,
    name VARCHAR(256) NOT NULL,
    template_tabs JSONB NOT NULL,
    CONSTRAINT template_created_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT template_modified_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT template_name_uq UNIQUE (name)
);

CREATE TABLE Experiment (
    id UUID PRIMARY KEY,
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
    description TEXT,
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

CREATE TABLE Experiment_Referenced_Dictionary_Item (
    experiment_id UUID NOT NULL,
    dictionary_item_id UUID NOT NULL,
    CONSTRAINT experiment_dictionary_item_pk PRIMARY KEY (experiment_id, dictionary_item_id),
    CONSTRAINT experiment_dictionary_item_experiment_id_fk FOREIGN KEY (experiment_id) REFERENCES Experiment(id) ON DELETE CASCADE,
    CONSTRAINT experiment_dictionary_item_dictionary_item_id_fk FOREIGN KEY (dictionary_item_id) REFERENCES dictionary_item(id)
);

CREATE TABLE Experiment_Referenced_Compound (
    experiment_id UUID NOT NULL,
    compound_id UUID NOT NULL,
    reaction_role Reaction_Role NOT NULL,
    CONSTRAINT experiment_referenced_compound_pk PRIMARY KEY (compound_id, experiment_id),
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

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
    template_tabs JSONB NOT NULL,
    CONSTRAINT template_created_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT template_modified_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT template_name_uq UNIQUE (name)
);

CREATE TABLE Signature_Template (
    id UUID PRIMARY KEY,
    created_by_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    modified_by_id UUID NOT NULL,
    modified_at TIMESTAMPTZ NOT NULL,
    name VARCHAR(256) NOT NULL,
    CONSTRAINT template_created_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT template_modified_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id)
);

CREATE TABLE Signature_Template_Block (
    signature_template_id UUID NOT NULL,
    ordinal INT NOT NULL,
    user_id UUID,
    reason Signature_Reason NOT NULL,
    CONSTRAINT signature_template_block_pk PRIMARY KEY (signature_template_id, ordinal),
    CONSTRAINT signature_template_block_signature_template_id_fk FOREIGN KEY (signature_template_id) REFERENCES Signature_Template (id) ON DELETE CASCADE,
    CONSTRAINT signature_template_block_user_id_fk FOREIGN KEY (user_id) REFERENCES User_Account (id)
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
    inherited BOOLEAN NOT NULL,
    CONSTRAINT experiment_acl_pk PRIMARY KEY (experiment_id, user_id),
    CONSTRAINT experiment_acl_experiment_id_fk FOREIGN KEY (experiment_id) REFERENCES Experiment (id) ON DELETE CASCADE,
    CONSTRAINT experiment_acl_user_id_fk FOREIGN KEY (user_id) REFERENCES User_Account (id) ON DELETE CASCADE
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

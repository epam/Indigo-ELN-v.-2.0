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
    full_acl ACL_Entry[] NOT NULL,
    short_acl ACL_Entry[] NOT NULL,
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

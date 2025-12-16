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
    full_acl ACL_Entry[] NOT NULL,
    short_acl ACL_Entry[] NOT NULL,
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
    CONSTRAINT notebook_acl_pk PRIMARY KEY (notebook_id, user_id),
    CONSTRAINT notebook_acl_notebook_id_fk FOREIGN KEY (notebook_id) REFERENCES Notebook (id) ON DELETE CASCADE,
    CONSTRAINT notebook_acl_user_id_fk FOREIGN KEY (user_id) REFERENCES User_Account (id) ON DELETE CASCADE
);

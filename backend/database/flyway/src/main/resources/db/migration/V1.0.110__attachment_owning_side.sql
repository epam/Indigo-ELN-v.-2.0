ALTER TABLE Project_Attachment RENAME TO Project_Attachment_Link;
ALTER TABLE Notebook_Attachment RENAME TO Notebook_Attachment_Link;
ALTER TABLE Experiment_Attachment RENAME TO Experiment_Attachment_Link;

CREATE TABLE Project_Attachment (
    id UUID PRIMARY KEY,
    created_by_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    modified_by_id UUID NOT NULL,
    modified_at TIMESTAMPTZ NOT NULL,
    name VARCHAR(256) NOT NULL,
    size BIGINT NOT NULL,
    deleted BOOL NOT NULL,
    content BYTEA NOT NULL,
    -- nullable: deleting an attachment detaches it from its parent so that undo can restore it
    project_id UUID,
    CONSTRAINT project_attachment_created_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT project_attachment_modified_by_id_fk FOREIGN KEY (modified_by_id) REFERENCES User_Account (id),
    CONSTRAINT project_attachment_project_id_fk FOREIGN KEY (project_id) REFERENCES Project (id) ON DELETE CASCADE
);

CREATE TABLE Notebook_Attachment (
    id UUID PRIMARY KEY,
    created_by_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    modified_by_id UUID NOT NULL,
    modified_at TIMESTAMPTZ NOT NULL,
    name VARCHAR(256) NOT NULL,
    size BIGINT NOT NULL,
    deleted BOOL NOT NULL,
    content BYTEA NOT NULL,
    notebook_id UUID,
    CONSTRAINT notebook_attachment_created_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT notebook_attachment_modified_by_id_fk FOREIGN KEY (modified_by_id) REFERENCES User_Account (id),
    CONSTRAINT notebook_attachment_notebook_id_fk FOREIGN KEY (notebook_id) REFERENCES Notebook (id) ON DELETE CASCADE
);

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

CREATE INDEX ix_project_attachment_project_id ON Project_Attachment (project_id);
CREATE INDEX ix_notebook_attachment_notebook_id ON Notebook_Attachment (notebook_id);
CREATE INDEX ix_experiment_attachment_experiment_id ON Experiment_Attachment (experiment_id);

INSERT INTO Project_Attachment (id, created_by_id, created_at, modified_by_id, modified_at, name, size, deleted, content, project_id)
SELECT a.id, a.created_by_id, a.created_at, a.modified_by_id, a.modified_at, a.name, a.size, a.deleted, a.content, l.project_id
FROM Attachment a JOIN Project_Attachment_Link l ON l.attachment_id = a.id;

INSERT INTO Notebook_Attachment (id, created_by_id, created_at, modified_by_id, modified_at, name, size, deleted, content, notebook_id)
SELECT a.id, a.created_by_id, a.created_at, a.modified_by_id, a.modified_at, a.name, a.size, a.deleted, a.content, l.notebook_id
FROM Attachment a JOIN Notebook_Attachment_Link l ON l.attachment_id = a.id;

INSERT INTO Experiment_Attachment (id, created_by_id, created_at, modified_by_id, modified_at, name, size, deleted, content, experiment_id)
SELECT a.id, a.created_by_id, a.created_at, a.modified_by_id, a.modified_at, a.name, a.size, a.deleted, a.content, l.experiment_id
FROM Attachment a JOIN Experiment_Attachment_Link l ON l.attachment_id = a.id;

DROP TABLE Project_Attachment_Link;
DROP TABLE Notebook_Attachment_Link;
DROP TABLE Experiment_Attachment_Link;

ALTER TABLE Experiment DROP CONSTRAINT experiment_signature_attachment_id_fk;
ALTER TABLE Experiment ADD CONSTRAINT experiment_signature_attachment_id_fk FOREIGN KEY (signature_attachment_id) REFERENCES Experiment_Attachment (id) ON DELETE SET NULL;

DROP TABLE Attachment;

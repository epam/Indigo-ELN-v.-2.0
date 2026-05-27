CREATE TABLE User_Account
(
    id UUID NOT NULL PRIMARY KEY,
    username VARCHAR(1024) NOT NULL,
    first_name VARCHAR(1024),
    last_name VARCHAR(1024),
    display_name VARCHAR(1024) NOT NULL,
    CONSTRAINT user_account_uq1 UNIQUE (username)
);

CREATE TYPE Signature_Reason AS ENUM ('AUTHOR', 'WITNESS');

CREATE TYPE Document_Status AS ENUM ('SUBMITTED', 'SIGNING', 'SIGNED', 'REJECTED', 'CANCELLED');

CREATE TYPE Signature_Status AS ENUM ('WAITING', 'APPROVED', 'REJECTED');

CREATE TABLE Signature_Template
(
    id UUID PRIMARY KEY,
    created_by_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    modified_by_id UUID NOT NULL,
    modified_at TIMESTAMPTZ NOT NULL,
    name VARCHAR(256) NOT NULL,
    CONSTRAINT signature_template_fk1 FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT signature_template_fk2 FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT signature_template_uq1 UNIQUE (name)
);

CREATE TABLE Signature_Template_Block
(
    id UUID PRIMARY KEY,
    template_id UUID NOT NULL,
    ordinal INT, -- should be NOT NULL, but Hibernate first inserts null here
    user_id UUID,
    reason Signature_Reason NOT NULL,
    CONSTRAINT signature_template_block_uq1 UNIQUE (template_id, ordinal),
    CONSTRAINT signature_template_block_fk1 FOREIGN KEY (template_id) REFERENCES Signature_Template (id) ON DELETE CASCADE,
    CONSTRAINT signature_template_block_fk2 FOREIGN KEY (user_id) REFERENCES User_Account (id)
);

CREATE TABLE Document
(
    id UUID PRIMARY KEY,
    name VARCHAR(1024) NOT NULL,
    template_id UUID NOT NULL,
    author_id UUID NOT NULL,
    status Document_Status NOT NULL,
    created_date TIMESTAMPTZ NOT NULL,
    last_modified_date TIMESTAMPTZ NOT NULL,
    filename VARCHAR(1024) NOT NULL,
    content BYTEA NOT NULL,
    CONSTRAINT document_fk1 FOREIGN KEY (template_id) REFERENCES Signature_Template (id),
    CONSTRAINT document_fk2 FOREIGN KEY (author_id) REFERENCES User_Account (id)
);

CREATE TABLE Document_Signature
(
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL,
    ordinal INT, -- should be NOT NULL, but Hibernate initially inserts NULL
    template_block_id UUID NOT NULL,
    user_id UUID NOT NULL,
    reason Signature_Reason NOT NULL,
    action_date TIMESTAMPTZ,
    status Signature_Status NOT NULL,
    comment TEXT,
    CONSTRAINT document_signature_fk1 FOREIGN KEY (user_id) REFERENCES User_Account (id),
    CONSTRAINT document_signature_fk2 FOREIGN KEY (document_id) REFERENCES Document (id) ON DELETE CASCADE,
    CONSTRAINT document_signature_fk3 FOREIGN KEY (template_block_id) REFERENCES Signature_Template_Block (id),
    CONSTRAINT document_signature_uq1 UNIQUE (document_id, ordinal)
);

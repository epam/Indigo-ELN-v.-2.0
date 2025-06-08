CREATE TABLE UserAccount
(
    id SERIAL NOT NULL PRIMARY KEY,
    userName VARCHAR(1024) NOT NULL,
    firstName VARCHAR(1024),
    lastName VARCHAR(1024),
    CONSTRAINT UserName_Uq UNIQUE (userName)
);

CREATE TYPE Reason AS ENUM ('AUTHOR', 'WITNESS');

CREATE TYPE Status AS ENUM ('SUBMITTED', 'SIGNING', 'SIGNED', 'REJECTED', 'WAITING', 'CANCELLED', 'ARCHIVING', 'ARCHIVED');

CREATE TYPE SignatureStatus AS ENUM ('WAITING', 'SIGNED', 'REJECTED');

CREATE TABLE Template
(
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    authorId INT NOT NULL,
    createdDate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    lastModifiedDate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT UserId_FK FOREIGN KEY (authorId) REFERENCES UserAccount (id)
);

CREATE TABLE TemplateSignatureBlock
(
    id SERIAL PRIMARY KEY,
    templateId INT NOT NULL,
    index INT NOT NULL,
    userId INT,
    reason Reason NOT NULL,
    templateSignatureBlockIndex SMALLINT,
    CONSTRAINT UserId_Fk FOREIGN KEY (userId) REFERENCES UserAccount (id),
    CONSTRAINT TemplateId_Fk FOREIGN KEY (templateId) REFERENCES Template (id) ON DELETE CASCADE,
    CONSTRAINT TemplateId_Index_Uq UNIQUE (templateId, index)
);

CREATE TABLE Document
(
    id SERIAL PRIMARY KEY,
    name VARCHAR(1024) NOT NULL,
    templateId INT NOT NULL,
    authorId INT NOT NULL,
    status Status NOT NULL,
    createdDate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    lastModifiedDate TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    content BYTEA NOT NULL,
    CONSTRAINT TemplateId_Fk FOREIGN KEY (templateId) REFERENCES Template (id),
    CONSTRAINT AuthorId_Fk FOREIGN KEY (authorId) REFERENCES UserAccount (id)
);

CREATE TABLE DocumentSignatureBlock
(
    id SERIAL PRIMARY KEY,
    documentId INT NOT NULL,
    index INT NOT NULL,
    templateBlockId INT NOT NULL,
    userId INT NOT NULL,
    reason Reason NOT NULL,
    actionDate TIMESTAMP WITHOUT TIME ZONE,
    status SignatureStatus NOT NULL,
    comment TEXT,
    CONSTRAINT UserId_Fk FOREIGN KEY (userId) REFERENCES UserAccount (id),
    CONSTRAINT DocumentId_Fk FOREIGN KEY (documentId) REFERENCES Document (id) ON DELETE CASCADE,
    CONSTRAINT TemplateBlockId_Fk FOREIGN KEY (templateBlockId) REFERENCES TemplateSignatureBlock (id),
    CONSTRAINT DocumentId_Index_Uq UNIQUE (documentId, index)
);

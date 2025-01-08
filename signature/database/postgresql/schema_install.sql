-- user

CREATE ROLE signature_service LOGIN PASSWORD 'sigpass' ;

-- schema

CREATE SCHEMA signature_service AUTHORIZATION signature_service ;

SET search_path TO signature_service ;

ALTER ROLE signature_service SET search_path TO signature_service ;

-- tables

-- table: UserAccount

CREATE TABLE UserAccount
(
  UserId VARCHAR(36) NOT NULL,
  Active VARCHAR(5) NOT NULL,
  Admin VARCHAR(5) NOT NULL,
  CreationDate timestamp without time zone,
  CreatedBy VARCHAR(100),
  Username VARCHAR(100),
  Password VARCHAR(100),
  FirstName VARCHAR(1024),
  LastName VARCHAR(1024),
  Email VARCHAR(1024),
  CONSTRAINT User_Id_Pk PRIMARY KEY (UserId),
  CONSTRAINT Username_Unique UNIQUE (Username)
) ;

ALTER TABLE UserAccount OWNER TO signature_service ;

-- table: Reason

CREATE TABLE Reason
(
  ReasonId SMALLINT NOT NULL,
  Text VARCHAR(100),
  CONSTRAINT Reason_Id_Pk PRIMARY KEY (ReasonId)
);

ALTER TABLE Reason OWNER TO signature_service ;

-- table: Status
CREATE TABLE Status
(
  StatusId SMALLINT NOT NULL,
  Name VARCHAR(100),
  CONSTRAINT Status_Id_Pk PRIMARY KEY (StatusId)
) ;

ALTER TABLE Status OWNER TO signature_service ;

-- table: SignatureTemplate

CREATE TABLE SignatureTemplate
(
  TemplateId VARCHAR(36) NOT NULL,
  TemplateName VARCHAR(100),
  UserId VARCHAR(36),
  CONSTRAINT Signature_Template_Id_Pk PRIMARY KEY (TemplateId),
  CONSTRAINT Signature_Template_User_Id_Fk FOREIGN KEY (UserId) REFERENCES UserAccount (UserId) ON DELETE CASCADE
) ;

ALTER TABLE SignatureTemplate OWNER TO signature_service ;

-- table: Document

CREATE TABLE Document
(
  DocumentId VARCHAR(36) NOT NULL,
  Name VARCHAR(1024),
  Status SMALLINT,
  CreationDate timestamp without time zone,
  LastUpdateDate timestamp without time zone,
  UserId VARCHAR(36),
  DocumentFile BYTEA,
  CONSTRAINT Document_Id_Pk PRIMARY KEY (DocumentId),
  CONSTRAINT Document_User_Id_Fk FOREIGN KEY (UserId) REFERENCES UserAccount (UserId) ON DELETE CASCADE,
  CONSTRAINT Document_Status_Fk FOREIGN KEY (Status) REFERENCES Status (StatusId) ON DELETE CASCADE
) ;

ALTER TABLE Document OWNER TO signature_service ;

-- table: DocumentSignatureBlock

CREATE TABLE DocumentSignatureBlock
(
  DocumentSignatureBlockId VARCHAR(36) NOT NULL,
  DocumentId VARCHAR(36),
  DocumentSignatureBlockIndex SMALLINT,
  UserId VARCHAR(36),
  ReasonId SMALLINT,
  ActionDate timestamp without time zone,
  Status SMALLINT,
  Comments TEXT,
  Inspected VARCHAR(5),
  CONSTRAINT Doc_Sig_Block_Id_Pk PRIMARY KEY (DocumentSignatureBlockId),
  CONSTRAINT Doc_Sig_Block_Document_Id_Fk FOREIGN KEY (DocumentId) REFERENCES Document (DocumentId) ON DELETE CASCADE,
  CONSTRAINT Doc_Sig_Block_User_Id_Fk FOREIGN KEY (UserId) REFERENCES UserAccount (UserId) ON DELETE CASCADE,
  CONSTRAINT Doc_Sig_Block_Reason_Id_Fk FOREIGN KEY (ReasonId) REFERENCES Reason (ReasonId) ON DELETE CASCADE,
  CONSTRAINT Doc_Sig_Block_Status_Fk FOREIGN KEY (Status) REFERENCES Status (StatusId) ON DELETE CASCADE
) ;

ALTER TABLE DocumentSignatureBlock OWNER TO signature_service ;

-- table: TemplateSignatureBlock

CREATE TABLE TemplateSignatureBlock
(
  TemplateSignatureBlockId VARCHAR(36) NOT NULL,
  UserId VARCHAR(36),
  ReasonId SMALLINT,
  TemplateSignatureBlockIndex SMALLINT,
  TemplateId VARCHAR(36),
  CONSTRAINT Tem_Sig_Block_Id_Pk PRIMARY KEY (TemplateSignatureBlockId),
  CONSTRAINT Tem_Sig_Block_User_Id_Fk FOREIGN KEY (UserId) REFERENCES UserAccount (UserId) ON DELETE CASCADE,
  CONSTRAINT Tem_Sig_Block_Reason_Id_Fk FOREIGN KEY (ReasonId) REFERENCES Reason (ReasonId) ON DELETE CASCADE,
  CONSTRAINT Tem_Sig_Block_Template_Id_Fk FOREIGN KEY (TemplateId) REFERENCES SignatureTemplate (TemplateId) ON DELETE CASCADE
) ;

ALTER TABLE TemplateSignatureBlock OWNER TO signature_service ;

-- table: FtpQueue

CREATE TABLE FtpQueue
(
  Id VARCHAR(36) NOT NULL,
  DocumentId VARCHAR(36) NOT NULL,
  FtpStatus VARCHAR(100),
  CONSTRAINT FtpQueue_Pk PRIMARY KEY (Id),
  CONSTRAINT FtpQueue_DocumentId_Fk FOREIGN KEY (DocumentId) REFERENCES Document (DocumentId) ON DELETE CASCADE
) ;

ALTER TABLE FtpQueue OWNER TO signature_service ;

-- insert initial data into tables

-- Reason

INSERT INTO Reason (ReasonId, Text) VALUES (1, 'I am the author') ;
INSERT INTO Reason (ReasonId, Text) VALUES (2, 'I am the Witness') ;

-- Status

INSERT INTO Status (StatusId, Name) VALUES (1, 'SUBMITTED') ;
INSERT INTO Status (StatusId, Name) VALUES (2, 'SIGNING') ;
INSERT INTO Status (StatusId, Name) VALUES (3, 'SIGNED') ;
INSERT INTO Status (StatusId, Name) VALUES (4, 'REJECTED') ;
INSERT INTO Status (StatusId, Name) VALUES (5, 'WAITING') ;
INSERT INTO Status (StatusId, Name) VALUES (6, 'CANCELLED') ;
INSERT INTO Status (StatusId, Name) VALUES (7, 'ARCHIVING') ;
INSERT INTO Status (StatusId, Name) VALUES (8, 'ARCHIVED') ;

-- Admin user with username 'admin' and temporary password '1234'

INSERT INTO UserAccount (UserId, Active, Admin, Username, Password, FirstName, LastName, Email) 
        VALUES('d8fd0f74-33ac-4c84-b8ec-af5863dea126', 'TRUE', 'TRUE', 'admin', '{IjFrTA9lv20QsyGhWIYfU3gTnwI/yOdveAWHYhnihQA=}754864deaddc7e325ece00ab6a9ad972', 'System', 'Administrator', 'admin@e.mail') ;

-- commit changes

COMMIT ;

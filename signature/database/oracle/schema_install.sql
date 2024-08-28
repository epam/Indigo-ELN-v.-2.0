-- User

CREATE TABLESPACE Indigo_Signature_Service_Data datafile 'Indigo_Signature_Service_Data.ora' SIZE 5M REUSE AUTOEXTEND ON NEXT 5M MAXSIZE UNLIMITED EXTENT MANAGEMENT LOCAL ;
CREATE USER signature_service IDENTIFIED BY sigpass DEFAULT TABLESPACE Indigo_Signature_Service_Data ;
GRANT CONNECT,RESOURCE TO signature_service ;

-- Tables

-- table: UserAccount

CREATE TABLE signature_service.UserAccount
(
  UserId VARCHAR(36) NOT NULL,
  Active VARCHAR(5) NOT NULL,
  Admin VARCHAR(5) NOT NULL,
  CreationDate TIMESTAMP,
  CreatedBy VARCHAR(100),
  Username VARCHAR(100),
  Password VARCHAR(100),
  FirstName VARCHAR(1024),
  LastName VARCHAR(1024),
  Email VARCHAR(1024),
  CONSTRAINT User_Id_Pk PRIMARY KEY (UserId),
  CONSTRAINT Username_Unique UNIQUE (Username)
) ;

-- table: Reason

CREATE TABLE signature_service.Reason
(
  ReasonId SMALLINT NOT NULL,
  Text VARCHAR(100),
  CONSTRAINT Reason_Id_Pk PRIMARY KEY (ReasonId)
);

-- table: Status

CREATE TABLE signature_service.Status
(
  StatusId SMALLINT NOT NULL,
  Name VARCHAR(100),
  CONSTRAINT Status_Id_Pk PRIMARY KEY (StatusId)
) ;

-- table: SignatureTemplate

CREATE TABLE signature_service.SignatureTemplate
(
  TemplateId VARCHAR(36) NOT NULL,
  TemplateName VARCHAR(100),
  UserId VARCHAR(36),
  CONSTRAINT Signature_Template_Id_Pk PRIMARY KEY (TemplateId),
  CONSTRAINT Signature_Template_User_Id_Fk FOREIGN KEY (UserId) REFERENCES signature_service.UserAccount (UserId) ON DELETE CASCADE
) ;

-- table: Document

CREATE TABLE signature_service.Document
(
  DocumentId VARCHAR(36) NOT NULL,
  Name VARCHAR(1024),
  Status SMALLINT,
  CreationDate TIMESTAMP,
  LastUpdateDate TIMESTAMP,
  UserId VARCHAR(36),
  DocumentFile BLOB,
  CONSTRAINT Document_Id_Pk PRIMARY KEY (DocumentId),
  CONSTRAINT Document_User_Id_Fk FOREIGN KEY (UserId) REFERENCES signature_service.UserAccount (UserId) ON DELETE CASCADE,
  CONSTRAINT Document_Status_Fk FOREIGN KEY (Status) REFERENCES signature_service.Status (StatusId) ON DELETE CASCADE
) ;

-- table: DocumentSignatureBlock

CREATE TABLE signature_service.DocumentSignatureBlock
(
  DocumentSignatureBlockId VARCHAR(36) NOT NULL,
  DocumentId VARCHAR(36),
  DocumentSignatureBlockIndex SMALLINT,
  UserId VARCHAR(36),
  ReasonId SMALLINT,
  ActionDate TIMESTAMP,
  Status SMALLINT,
  Comments CLOB,
  Inspected VARCHAR(5),
  CONSTRAINT Doc_Sig_Block_Id_Pk PRIMARY KEY (DocumentSignatureBlockId),
  CONSTRAINT Doc_Sig_Block_Document_Id_Fk FOREIGN KEY (DocumentId) REFERENCES signature_service.Document (DocumentId) ON DELETE CASCADE,
  CONSTRAINT Doc_Sig_Block_User_Id_Fk FOREIGN KEY (UserId) REFERENCES signature_service.UserAccount (UserId) ON DELETE CASCADE,
  CONSTRAINT Doc_Sig_Block_Reason_Id_Fk FOREIGN KEY (ReasonId) REFERENCES signature_service.Reason (ReasonId) ON DELETE CASCADE,
  CONSTRAINT Doc_Sig_Block_Status_Fk FOREIGN KEY (Status) REFERENCES signature_service.Status (StatusId) ON DELETE CASCADE
) ;

-- table: TemplateSignatureBlock

CREATE TABLE signature_service.TemplateSignatureBlock
(
  TemplateSignatureBlockId VARCHAR(36) NOT NULL,
  UserId VARCHAR(36),
  ReasonId SMALLINT,
  TemplateSignatureBlockIndex SMALLINT,
  TemplateId VARCHAR(36),
  CONSTRAINT Tem_Sig_Block_Id_Pk PRIMARY KEY (TemplateSignatureBlockId),
  CONSTRAINT Tem_Sig_Block_User_Id_Fk FOREIGN KEY (UserId) REFERENCES signature_service.UserAccount (UserId) ON DELETE CASCADE,
  CONSTRAINT Tem_Sig_Block_Reason_Id_Fk FOREIGN KEY (ReasonId) REFERENCES signature_service.Reason (ReasonId) ON DELETE CASCADE,
  CONSTRAINT Tem_Sig_Block_Template_Id_Fk FOREIGN KEY (TemplateId) REFERENCES signature_service.SignatureTemplate (TemplateId) ON DELETE CASCADE
) ;

-- table: FtpQueue

CREATE TABLE signature_service.FtpQueue
(
  Id VARCHAR(36) NOT NULL,
  DocumentId VARCHAR(36) NOT NULL,
  FtpStatus VARCHAR(100),
  CONSTRAINT FtpQueue_Pk PRIMARY KEY (Id),
  CONSTRAINT FtpQueue_DocumentId_Fk FOREIGN KEY (DocumentId) REFERENCES signature_service.Document (DocumentId) ON DELETE CASCADE
) ;

-- insert initial data into tables

-- Reason

INSERT INTO signature_service.Reason (ReasonId, Text) VALUES (1, 'I am the author') ;
INSERT INTO signature_service.Reason (ReasonId, Text) VALUES (2, 'I am the Witness') ;

-- Status

INSERT INTO signature_service.Status (StatusId, Name) VALUES (1, 'SUBMITTED') ;
INSERT INTO signature_service.Status (StatusId, Name) VALUES (2, 'SIGNING') ;
INSERT INTO signature_service.Status (StatusId, Name) VALUES (3, 'SIGNED') ;
INSERT INTO signature_service.Status (StatusId, Name) VALUES (4, 'REJECTED') ;
INSERT INTO signature_service.Status (StatusId, Name) VALUES (5, 'WAITING') ;
INSERT INTO signature_service.Status (StatusId, Name) VALUES (6, 'CANCELLED') ;
INSERT INTO signature_service.Status (StatusId, Name) VALUES (7, 'ARCHIVING') ;
INSERT INTO signature_service.Status (StatusId, Name) VALUES (8, 'ARCHIVED') ;

-- Admin user with username 'admin' and temporary password '1234'

INSERT INTO signature_service.UserAccount (UserId, Active, Admin, Username, Password, FirstName, LastName, Email) 
        VALUES('d8fd0f74-33ac-4c84-b8ec-af5863dea126', 'TRUE', 'TRUE', 'admin', '81dc9bdb52d04dc20036dbd8313ed055', 'System', 'Administrator', 'admin@e.mail') ;

-- commit changes

COMMIT ;

CREATE TABLE User_Account
(
    id SERIAL NOT NULL PRIMARY KEY,
    userName VARCHAR(1024) NOT NULL,
    first_Name VARCHAR(1024),
    last_Name VARCHAR(1024),
    CONSTRAINT UserName_Uq UNIQUE (userName)
);

CREATE TYPE Reason AS ENUM ('AUTHOR', 'WITNESS');

CREATE TYPE Status AS ENUM ('SUBMITTED', 'SIGNING', 'SIGNED', 'REJECTED', 'WAITING', 'CANCELLED', 'ARCHIVING', 'ARCHIVED');

CREATE TYPE Signature_Status AS ENUM ('WAITING', 'SIGNED', 'REJECTED');

CREATE TABLE Template
(
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    author_Id INT NOT NULL,
    created_Date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_Modified_Date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT User_Id_FK FOREIGN KEY (author_Id) REFERENCES User_Account (id)
);

CREATE TABLE Template_Signature_Block
(
    id SERIAL PRIMARY KEY,
    template_Id INT NOT NULL,
    index INT NOT NULL,
    user_Id INT,
    reason Reason NOT NULL,
    template_Signature_Block_Index SMALLINT,
    CONSTRAINT UserId_Fk FOREIGN KEY (user_Id) REFERENCES User_Account (id),
    CONSTRAINT Template_Id_Fk FOREIGN KEY (template_Id) REFERENCES Template (id) ON DELETE CASCADE,
    CONSTRAINT Template_Id_Index_Uq UNIQUE (template_Id, index)
);

CREATE TABLE Document
(
    id SERIAL PRIMARY KEY,
    name VARCHAR(1024) NOT NULL,
    template_Id INT NOT NULL,
    author_Id INT NOT NULL,
    status Status NOT NULL,
    created_Date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_Modified_Date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    content BYTEA NOT NULL,
    CONSTRAINT Template_Id_Fk FOREIGN KEY (template_Id) REFERENCES Template (id),
    CONSTRAINT Author_Id_Fk FOREIGN KEY (author_Id) REFERENCES User_Account (id)
);

CREATE TABLE Document_Signature_Block
(
    id SERIAL PRIMARY KEY,
    document_Id INT NOT NULL,
    index INT NOT NULL,
    template_Block_Id INT NOT NULL,
    user_Id INT NOT NULL,
    reason Reason NOT NULL,
    action_Date TIMESTAMP WITHOUT TIME ZONE,
    status Signature_Status NOT NULL,
    comment TEXT,
    CONSTRAINT User_Id_Fk FOREIGN KEY (user_Id) REFERENCES User_Account (id),
    CONSTRAINT Document_Id_Fk FOREIGN KEY (document_Id) REFERENCES Document (id) ON DELETE CASCADE,
    CONSTRAINT Template_Block_Id_Fk FOREIGN KEY (template_Block_Id) REFERENCES Template_Signature_Block (id),
    CONSTRAINT Document_Id_Index_Uq UNIQUE (document_Id, index)
);

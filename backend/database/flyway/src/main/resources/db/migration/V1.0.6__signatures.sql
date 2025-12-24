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

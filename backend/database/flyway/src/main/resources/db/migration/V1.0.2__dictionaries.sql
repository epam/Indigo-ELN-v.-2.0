CREATE TABLE Dictionary (
    id UUID PRIMARY KEY,
    created_by_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    modified_by_id UUID NOT NULL,
    modified_at TIMESTAMPTZ NOT NULL,
    code VARCHAR(200) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    user_editable BOOL NOT NULL,
    CONSTRAINT dictionary_created_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT dictionary_modified_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT dictionary_code_uq UNIQUE (code) DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT dictionary_name_uq UNIQUE (name) DEFERRABLE INITIALLY DEFERRED
);

CREATE TABLE Dictionary_Item (
    id UUID PRIMARY KEY,
    created_by_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    modified_by_id UUID NOT NULL,
    modified_at TIMESTAMPTZ NOT NULL,
    dictionary_id UUID NOT NULL,
    ordinal INT NOT NULL,
    name VARCHAR(1000) NOT NULL,
    description VARCHAR(1000),
    active BOOL NOT NULL,
    CONSTRAINT dictionary_item_created_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT dictionary_item_modified_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT dictionary_item_dictionary_id_fk FOREIGN KEY (dictionary_id) REFERENCES Dictionary (id),
    CONSTRAINT dictionary_item_dictionary_ordinal_uq UNIQUE (dictionary_id, ordinal) DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT dictionary_item_dictionary_name_uq UNIQUE (dictionary_id, name) DEFERRABLE INITIALLY DEFERRED
);

CREATE TABLE Salt_Code (
    id UUID PRIMARY KEY,
    code VARCHAR(2) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL UNIQUE,
    formula VARCHAR(200) NOT NULL,
    charge INT NOT NULL,
    mol_weight FLOAT8 NOT NULL
);

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
    deleted BOOL NOT NULL,
    CONSTRAINT dictionary_created_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT dictionary_modified_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id)
);
CREATE UNIQUE INDEX ix_dictionary_code ON Dictionary (code) WHERE NOT deleted;
CREATE UNIQUE INDEX ix_dictionary_name ON Dictionary (name) WHERE NOT deleted;

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
    deleted BOOL NOT NULL,
    CONSTRAINT dictionary_item_created_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT dictionary_item_modified_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id),
    CONSTRAINT dictionary_item_dictionary_id_fk FOREIGN KEY (dictionary_id) REFERENCES Dictionary (id)
);
CREATE UNIQUE INDEX ix_dictionary_item_ordinal ON Dictionary_Item (dictionary_id, ordinal) WHERE active AND NOT deleted;
CREATE UNIQUE INDEX ix_dictionary_item_name ON Dictionary_Item (dictionary_id, name) WHERE active AND NOT deleted;

CREATE TABLE Salt_Code (
    id UUID PRIMARY KEY,
    code VARCHAR(2) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL UNIQUE,
    formula VARCHAR(200) NOT NULL,
    charge INT NOT NULL,
    mol_weight FLOAT8 NOT NULL
);

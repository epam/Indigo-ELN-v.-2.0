-- TODO merge into compounds.sql
ALTER TABLE Sample ADD COLUMN created_by_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000001';
ALTER TABLE Sample ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
ALTER TABLE Sample ADD COLUMN modified_by_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000001';;
ALTER TABLE Sample ADD COLUMN modified_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

ALTER TABLE Sample ALTER COLUMN created_by_id DROP DEFAULT;
ALTER TABLE Sample ALTER COLUMN created_at DROP DEFAULT;
ALTER TABLE Sample ALTER COLUMN modified_by_id DROP DEFAULT;
ALTER TABLE Sample ALTER COLUMN modified_at DROP DEFAULT;

ALTER TABLE Sample ADD CONSTRAINT sample_created_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id);
ALTER TABLE Sample ADD CONSTRAINT sample_modified_by_id_fk FOREIGN KEY (created_by_id) REFERENCES User_Account (id);

CREATE TABLE Sample_Mark (
    sample_id UUID NOT NULL,
    user_id UUID NOT NULL,
    CONSTRAINT sample_mark_pk PRIMARY KEY (sample_id, user_id),
    CONSTRAINT sample_mark_sample_id_fk FOREIGN KEY (sample_id) REFERENCES Sample (id) ON DELETE CASCADE,
    CONSTRAINT sample_mark_user_id_fk FOREIGN KEY (user_id) REFERENCES User_Account (id) ON DELETE CASCADE
);

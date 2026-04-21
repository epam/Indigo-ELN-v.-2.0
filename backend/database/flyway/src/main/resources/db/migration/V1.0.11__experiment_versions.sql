ALTER TABLE Experiment_Revision ADD COLUMN version INT;
ALTER TABLE Experiment_Revision ADD COLUMN snapshot JSONB;
ALTER TABLE Experiment_Revision ADD CONSTRAINT experiment_revision_experiment_id_version_uq UNIQUE (experiment_id, version);

CREATE TABLE Experiment_Edit_Session (
    id UUID PRIMARY KEY,
    experiment_id UUID NOT NULL,
    user_id UUID NOT NULL,
    started TIMESTAMPTZ NOT NULL,
    last_active TIMESTAMPTZ NOT NULL,
    finished TIMESTAMPTZ,
    CONSTRAINT experiment_edit_session_experiment_id_fk FOREIGN KEY (experiment_id) REFERENCES Experiment (id),
    CONSTRAINT experiment_edit_session_user_id_fk FOREIGN KEY (user_id) REFERENCES User_Account (id)
);

ALTER TABLE Experiment_Revision ADD COLUMN edit_session_id UUID;
ALTER TABLE Experiment_Revision ADD CONSTRAINT experiment_revision_edit_session_id_fk FOREIGN KEY (edit_session_id) REFERENCES Experiment_Edit_Session (id);

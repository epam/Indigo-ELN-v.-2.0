ALTER TABLE Experiment ADD COLUMN title VARCHAR(1024);
ALTER TABLE Experiment ADD COLUMN literature TEXT;
ALTER TABLE Experiment ADD COLUMN batch_creator_id UUID;
UPDATE Experiment SET batch_creator_id = created_by_id WHERE true;
ALTER TABLE Experiment ALTER COLUMN batch_creator_id SET NOT NULL;

CREATE TABLE Experiment_Linked_Experiment (
    parent_id UUID NOT NULL,
    experiment_id UUID NOT NULL,
    CONSTRAINT experiment_linked_experiment_pk PRIMARY KEY (parent_id, experiment_id),
    CONSTRAINT experiment_linked_experiment_parent_id_fk FOREIGN KEY (parent_id) REFERENCES Experiment (id) ON DELETE CASCADE,
    CONSTRAINT experiment_linked_experiment_experiment_id_fk FOREIGN KEY (experiment_id) REFERENCES Experiment (id) ON DELETE CASCADE
);

CREATE TABLE Experiment_Continued_From (
    parent_id UUID NOT NULL,
    experiment_id UUID NOT NULL,
    CONSTRAINT experiment_continued_from_pk PRIMARY KEY (parent_id, experiment_id),
    CONSTRAINT experiment_continued_from_parent_id_fk FOREIGN KEY (parent_id) REFERENCES Experiment (id) ON DELETE CASCADE,
    CONSTRAINT experiment_continued_from_experiment_id_fk FOREIGN KEY (experiment_id) REFERENCES Experiment (id) ON DELETE CASCADE
);

CREATE TABLE Experiment_Continued_To (
    parent_id UUID NOT NULL,
    experiment_id UUID NOT NULL,
    CONSTRAINT experiment_continued_to_pk PRIMARY KEY (parent_id, experiment_id),
    CONSTRAINT experiment_continued_to_parent_id_fk FOREIGN KEY (parent_id) REFERENCES Experiment (id) ON DELETE CASCADE,
    CONSTRAINT experiment_continued_to_experiment_id_fk FOREIGN KEY (experiment_id) REFERENCES Experiment (id) ON DELETE CASCADE
);

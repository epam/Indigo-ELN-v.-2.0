UPDATE Experiment
SET model = (model - 'lastUsedAnchor') || '{"revision": 1}'::jsonb
WHERE true;

ALTER TABLE Experiment_Dictionary_Item RENAME TO Experiment_Referenced_Dictionary_Item;

CREATE TABLE Experiment_Referenced_Compound (
    experiment_id UUID NOT NULL,
    compound_id UUID NOT NULL,
    CONSTRAINT experiment_referenced_compound_pk PRIMARY KEY (compound_id, experiment_id),
    CONSTRAINT experiment_referenced_compound_experiment_id_fk FOREIGN KEY (experiment_id) REFERENCES Experiment(id) ON DELETE CASCADE,
    CONSTRAINT experiment_referenced_compound_compound_id_fk FOREIGN KEY (compound_id) REFERENCES Compound(id)
);
INSERT INTO Experiment_Referenced_Compound (experiment_id, compound_id)
SELECT experiment_id, compound_id
FROM Compound_Experiment;

DROP TABLE Compound_Experiment;

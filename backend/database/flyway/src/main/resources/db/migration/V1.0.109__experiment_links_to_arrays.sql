ALTER TABLE Experiment
    ADD COLUMN linked_experiments UUID[] NOT NULL DEFAULT '{}',
    ADD COLUMN continued_from UUID[] NOT NULL DEFAULT '{}',
    ADD COLUMN continued_to UUID[] NOT NULL DEFAULT '{}';

UPDATE Experiment e SET linked_experiments = sub.ids
FROM (SELECT parent_id, array_agg(experiment_id) ids FROM Experiment_Linked_Experiment GROUP BY parent_id) sub
WHERE sub.parent_id = e.id;

UPDATE Experiment e SET continued_from = sub.ids
FROM (SELECT parent_id, array_agg(experiment_id) ids FROM Experiment_Continued_From GROUP BY parent_id) sub
WHERE sub.parent_id = e.id;

UPDATE Experiment e SET continued_to = sub.ids
FROM (SELECT parent_id, array_agg(experiment_id) ids FROM Experiment_Continued_To GROUP BY parent_id) sub
WHERE sub.parent_id = e.id;

DROP TABLE Experiment_Linked_Experiment;
DROP TABLE Experiment_Continued_From;
DROP TABLE Experiment_Continued_To;

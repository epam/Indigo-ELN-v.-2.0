ALTER TABLE Project_Revision ADD COLUMN undo_for INT;
ALTER TABLE Project_Revision ADD COLUMN redo_for INT;
ALTER TABLE Project_Revision DROP COLUMN reverse_mutation;

ALTER TABLE Notebook_Revision ADD COLUMN undo_for INT;
ALTER TABLE Notebook_Revision ADD COLUMN redo_for INT;
ALTER TABLE Notebook_Revision DROP COLUMN reverse_mutation;

ALTER TABLE Experiment_Revision ADD COLUMN undo_for INT;
ALTER TABLE Experiment_Revision ADD COLUMN redo_for INT;
ALTER TABLE Experiment_Revision DROP COLUMN reverse_mutation;

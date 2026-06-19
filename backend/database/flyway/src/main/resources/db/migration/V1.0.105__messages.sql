ALTER TABLE Experiment_Revision ADD COLUMN messages VARCHAR(1024)[];
ALTER TABLE Experiment ADD COLUMN version INT;

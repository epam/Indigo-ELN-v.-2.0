CREATE TABLE Experiment_Search_Batch (
    experiment_id UUID NOT NULL,
    batch_purity DOUBLE PRECISION,
    batch_yield DOUBLE PRECISION,
    CONSTRAINT experiment_search_batch_parent_id_fk FOREIGN KEY (experiment_id) REFERENCES Experiment (id) ON DELETE CASCADE
);

UPDATE Project SET search_vector = ''::tsvector WHERE search_vector IS NULL;
UPDATE Notebook SET search_vector = ''::tsvector WHERE search_vector IS NULL;
UPDATE Experiment SET search_vector = ''::tsvector WHERE search_vector IS NULL;
UPDATE Sample SET search_vector = ''::tsvector WHERE search_vector IS NULL;

ALTER TABLE Project ALTER COLUMN search_vector SET NOT NULL;
ALTER TABLE Notebook ALTER COLUMN search_vector SET NOT NULL;
ALTER TABLE Experiment ALTER COLUMN search_vector SET NOT NULL;
ALTER TABLE Sample ALTER COLUMN search_vector SET NOT NULL;

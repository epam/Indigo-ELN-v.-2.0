CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX ix_project_name ON Project USING GIN (name gin_trgm_ops);
CREATE INDEX ix_notebook_name ON Notebook USING GIN (name gin_trgm_ops);
CREATE INDEX ix_experiment_name ON Experiment USING GIN (name gin_trgm_ops);

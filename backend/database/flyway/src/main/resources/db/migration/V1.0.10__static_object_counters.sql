CREATE TABLE Total_Counts (
    id UUID PRIMARY KEY,
    projects INT NOT NULL,
    notebooks INT NOT NULL,
    experiments_by_status Experiment_Count[] NOT NULL
);
INSERT INTO Total_Counts VALUES (gen_random_uuid(), -1, -1, '{}');

ALTER TABLE Project ADD COLUMN notebook_count INT NOT NULL DEFAULT 0;
ALTER TABLE Project ADD COLUMN experiment_count Experiment_Count[] NOT NULL DEFAULT '{}';
ALTER TABLE Notebook ADD COLUMN experiment_count Experiment_Count[] NOT NULL DEFAULT '{}';

CREATE FUNCTION update_total_counters()
RETURNS VOID AS $$
BEGIN
    UPDATE Total_Counts SET
        projects = (SELECT COUNT(*) FROM Project),
        notebooks = (SELECT COUNT(*) FROM Notebook),
        experiments_by_status = (
            SELECT coalesce(array_agg(ROW(status, count_)::Experiment_Count), '{}')
            FROM (
                SELECT e.status, COUNT(*) count_
                FROM Experiment e
                GROUP BY e.status
            ) t
        )
    WHERE true;
END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION update_Project_counters(
    current_project_id UUID
) RETURNS VOID AS $$
BEGIN
    UPDATE Project SET
        notebook_count = (
            SELECT COUNT(*)
            FROM Notebook
            WHERE project_id = current_project_id
        ),
        experiment_count = (
            SELECT coalesce(array_agg(ROW(status, count_)::Experiment_Count), '{}')
            FROM (
                SELECT e.status, COUNT(*) count_
                FROM Experiment e
                WHERE e.project_id = current_project_id
                GROUP BY e.status
            ) t
        )
    WHERE id = current_project_id;
END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION update_Notebook_counters(
    current_notebook_id UUID
) RETURNS VOID AS $$
BEGIN
    UPDATE Notebook SET
        experiment_count = (
            SELECT coalesce(array_agg(ROW(status, count_)::Experiment_Count), '{}')
            FROM (
                SELECT e.status, COUNT(*) count_
                FROM Experiment e
                WHERE e.notebook_id = current_notebook_id
                GROUP BY e.status
            ) t
        )
    WHERE id = current_notebook_id;
END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION update_total_counters_trigger()
RETURNS TRIGGER AS $$
BEGIN
    PERFORM update_total_counters();
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION update_Project_counters_trigger()
RETURNS TRIGGER AS $$
BEGIN
    PERFORM
        update_Project_counters(coalesce(NEW.project_id, OLD.project_id)),
        update_total_counters();
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION update_Notebook_counters_trigger()
RETURNS TRIGGER AS $$
BEGIN
    PERFORM
        update_Notebook_counters(coalesce(NEW.notebook_id, OLD.notebook_id)),
        update_Project_counters(coalesce(NEW.project_id, OLD.project_id)),
        update_total_counters();
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trigger_update_total_counters
AFTER INSERT OR DELETE ON Project
FOR EACH ROW EXECUTE FUNCTION update_total_counters_trigger();

CREATE OR REPLACE TRIGGER trigger_update_Project_notebook_counter
AFTER INSERT OR DELETE ON Notebook
FOR EACH ROW EXECUTE FUNCTION update_Project_counters_trigger();

CREATE OR REPLACE TRIGGER trigger_update_Notebook_experiments_counter
AFTER INSERT OR DELETE ON Experiment
FOR EACH ROW EXECUTE FUNCTION update_Notebook_counters_trigger();

SELECT update_total_counters();
SELECT update_Project_counters(id) FROM Project;
SELECT update_Notebook_counters(id) FROM Notebook;

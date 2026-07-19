DROP VIEW IF EXISTS Project_Base_View;

DROP VIEW IF EXISTS Notebook_Base_View;

DROP VIEW IF EXISTS Experiment_Base_View;

CREATE MATERIALIZED VIEW Total_Counts_View
AS
SELECT
    (
        SELECT COUNT(*) FROM Project
    ) projects,
    (
        SELECT COUNT(*) FROM Notebook
    ) notebooks,
    (
        SELECT coalesce(array_agg(ROW(status, count_)::Experiment_Count), '{}')
        FROM (
            SELECT e.status, COUNT(*) count_
            FROM Experiment e
            GROUP BY e.status
        ) t
    ) experiments_by_status
;

CREATE FUNCTION update_total_counters_trigger()
    RETURNS TRIGGER AS $$
BEGIN
    REFRESH MATERIALIZED VIEW Total_Counts_View;
RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trigger_update_total_counters
AFTER INSERT OR DELETE ON Project
FOR EACH ROW EXECUTE FUNCTION update_total_counters_trigger();

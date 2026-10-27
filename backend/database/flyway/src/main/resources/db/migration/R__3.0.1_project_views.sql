DROP VIEW IF EXISTS Project_View_2;

CREATE OR REPLACE VIEW Project_Access_View AS
SELECT project_id, current_access_or_null
FROM (
    SELECT p.id project_id,
           COALESCE(
               CASE WHEN p.created_by_id = current_setting('eln.currentUserId')::UUID THEN 'AUTHOR'::Access_Level END,
               pa.level
           ) current_access_or_null
    FROM Project p
    LEFT JOIN Project_ACL pa ON pa.project_id = p.id AND pa.user_id = current_setting('eln.currentUserId')::UUID
) t
WHERE current_setting('eln.viewAllProjects')::BOOLEAN OR current_access_or_null IS NOT NULL;


CREATE OR REPLACE FUNCTION update_Project_counters(
    current_project_id UUID
) RETURNS VOID AS $$
BEGIN
    UPDATE Project SET
        notebook_count = (
            SELECT count(*)
            FROM notebook
            WHERE project_id = current_project_id
        ),
        experiment_count = (
            SELECT coalesce(array_agg(row(status, count_)::Experiment_Count), '{}')
            FROM (
                SELECT e.status, count(*) count_
                FROM experiment e
                WHERE e.project_id = current_project_id
                GROUP BY e.status
            ) t
        )
    WHERE id = current_project_id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_Project_counters_trigger()
    RETURNS TRIGGER AS $$
BEGIN
    PERFORM update_Project_counters(coalesce(NEW.project_id, OLD.project_id));
    REFRESH MATERIALIZED VIEW Total_Counts_View;
RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trigger_update_Project_counter
AFTER INSERT OR DELETE ON Notebook
FOR EACH ROW EXECUTE FUNCTION update_Project_counters_trigger();

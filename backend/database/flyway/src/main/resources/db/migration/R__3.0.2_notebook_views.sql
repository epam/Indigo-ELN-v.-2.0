DROP VIEW IF EXISTS Notebook_View_2;

CREATE OR REPLACE VIEW Notebook_Access_View AS
SELECT notebook_id, current_access_or_null
FROM (
    SELECT n.id notebook_id,
           COALESCE(
               CASE WHEN n.created_by_id = current_setting('eln.currentUserId')::UUID THEN 'AUTHOR'::Access_Level END,
               NULLIF(na.level, 'IMPLICIT_VIEW'),
               NULLIF(pa.level, 'IMPLICIT_VIEW'),
               na.level
           ) current_access_or_null
    FROM Notebook n
    LEFT JOIN Notebook_ACL na ON na.notebook_id = n.id AND na.user_id = current_setting('eln.currentUserId')::UUID
    LEFT JOIN Project_ACL pa ON pa.project_id = n.project_id AND pa.user_id = current_setting('eln.currentUserId')::UUID
) t
WHERE current_setting('eln.viewAllNotebooks')::BOOLEAN OR current_access_or_null IS NOT NULL;


CREATE OR REPLACE FUNCTION update_Notebook_counters(
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

CREATE OR REPLACE FUNCTION update_Notebook_counters_trigger()
    RETURNS TRIGGER AS $$
BEGIN
    PERFORM
        update_Notebook_counters(coalesce(NEW.notebook_id, OLD.notebook_id)),
        update_Project_counters(coalesce(NEW.project_id, OLD.project_id));
    REFRESH MATERIALIZED VIEW Total_Counts_View;
RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trigger_update_Notebook_counter
AFTER INSERT OR DELETE ON Experiment
FOR EACH ROW EXECUTE FUNCTION update_Notebook_counters_trigger();

-- Separate trigger: WHEN may not reference OLD on INSERT
CREATE OR REPLACE TRIGGER trigger_update_Notebook_counter_on_status
AFTER UPDATE OF status ON Experiment
FOR EACH ROW WHEN (OLD.status IS DISTINCT FROM NEW.status)
EXECUTE FUNCTION update_Notebook_counters_trigger();

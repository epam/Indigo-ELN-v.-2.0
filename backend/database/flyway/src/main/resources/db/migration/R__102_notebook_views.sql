DROP VIEW IF EXISTS Notebook_View_2;

-- Postgres tend to fall back to full table scan on OR conditions, so using UNION ALL for viewAll vs normal
CREATE OR REPLACE VIEW Notebook_Access_View AS
SELECT n.id notebook_id, na.level current_access_or_null, array_length(n.full_acl, 1) acl_count
FROM Notebook n
LEFT JOIN LATERAL unnest(n.full_acl) na ON na.user_id = current_setting('eln.currentUserId')::UUID
WHERE current_setting('eln.viewAllNotebooks')::BOOLEAN
UNION ALL
SELECT n.id notebook_id, na.level current_access_or_null, array_length(n.full_acl, 1) acl_count
FROM Notebook n
LEFT JOIN LATERAL unnest(n.full_acl) na ON na.user_id = current_setting('eln.currentUserId')::UUID
WHERE NOT current_setting('eln.viewAllNotebooks')::BOOLEAN
  AND acl_user_ids(n.full_acl) @> ARRAY[current_setting('eln.currentUserId')::UUID];


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

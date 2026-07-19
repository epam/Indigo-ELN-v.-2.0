DROP VIEW IF EXISTS Notebook_View_2;

CREATE OR REPLACE VIEW Notebook_Access_View AS
SELECT n.id notebook_id, na.level current_access_or_null, array_length(n.full_acl, 1) acl_count
FROM Notebook n
LEFT JOIN LATERAL unnest(n.full_acl) na ON na.user_id = current_setting('eln.currentUserId')::UUID
WHERE current_setting('eln.viewAllNotebooks')::BOOLEAN OR na.level IS NOT NULL;

CREATE OR REPLACE FUNCTION get_notebook_search_vector(
    IN current_notebook_id UUID
) RETURNS TSVECTOR AS $$
BEGIN
    RETURN (
        SELECT
            setweight(to_tsvector('english', coalesce(n.name, '')), 'A') ||
            setweight(to_tsvector('english', coalesce(n.description, '')), 'D') ||
            setweight(to_tsvector('english', coalesce(c.display_name, '')), 'C')
        FROM Notebook n
        JOIN User_Account c ON c.id = n.created_by_id
        WHERE n.id = current_notebook_id
    );
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_Notebook_search_vector()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE Notebook SET search_vector = get_notebook_search_vector(new.id) WHERE id = new.id;
    RETURN new;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trigger_update_Notebook_search_vector
AFTER INSERT OR UPDATE OF name, description ON Notebook
FOR EACH ROW EXECUTE FUNCTION update_Notebook_search_vector();

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

CREATE FUNCTION update_Notebook_counters_trigger()
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

DROP VIEW IF EXISTS Project_View_2;

-- Postgres tend to fall back to full table scan on OR conditions, so using UNION ALL for viewAll vs normal
CREATE OR REPLACE VIEW Project_Access_View AS
SELECT p.id project_id, pa.level current_access_or_null, array_length(p.full_acl, 1) acl_count
FROM Project p
LEFT JOIN LATERAL unnest(p.full_acl) pa ON pa.user_id = current_setting('eln.currentUserId')::UUID
WHERE current_setting('eln.viewAllProjects')::BOOLEAN
UNION ALL
SELECT p.id project_id, pa.level current_access_or_null, array_length(p.full_acl, 1) acl_count
FROM Project p
LEFT JOIN LATERAL unnest(p.full_acl) pa ON pa.user_id = current_setting('eln.currentUserId')::UUID
WHERE NOT current_setting('eln.viewAllProjects')::BOOLEAN
  AND acl_user_ids(p.full_acl) @> ARRAY[current_setting('eln.currentUserId')::UUID];

CREATE OR REPLACE FUNCTION get_project_search_vector(
    IN current_project_id UUID
) RETURNS TSVECTOR AS $$
BEGIN
    RETURN (
        SELECT
            setweight(to_tsvector('english', coalesce(p.name, '')), 'A') ||
            setweight(to_tsvector('english', coalesce(p.description, '')), 'D') ||
            setweight(to_tsvector('english', coalesce(p.literature, '')), 'D') ||
            setweight(to_tsvector('english', coalesce(c.display_name, '')), 'C')
        FROM Project p
        JOIN User_Account c ON c.id = p.created_by_id
        WHERE p.id = current_project_id
    );
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_Project_search_vector()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE Project SET search_vector = get_project_search_vector(new.id) WHERE id = new.id;
    RETURN new;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trigger_update_Project_search_vector
AFTER INSERT OR UPDATE OF name, description, literature ON Project
FOR EACH ROW EXECUTE FUNCTION update_Project_search_vector();

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

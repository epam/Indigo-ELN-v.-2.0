DROP VIEW IF EXISTS Experiment_View_2;

-- Postgres tend to fall back to full table scan on OR conditions, so using UNION ALL for viewAll vs normal
CREATE OR REPLACE VIEW Experiment_Access_View AS
SELECT e.id experiment_id, ea.level current_access_or_null, array_length(e.full_acl, 1) acl_count
FROM Experiment e
LEFT JOIN LATERAL unnest(e.full_acl) ea ON ea.user_id = current_setting('eln.currentUserId')::UUID
WHERE current_setting('eln.viewAllExperiments')::BOOLEAN
UNION ALL
SELECT e.id experiment_id, ea.level current_access_or_null, array_length(e.full_acl, 1) acl_count
FROM Experiment e
LEFT JOIN LATERAL unnest(e.full_acl) ea ON ea.user_id = current_setting('eln.currentUserId')::UUID
WHERE NOT current_setting('eln.viewAllExperiments')::BOOLEAN
  AND acl_user_ids(e.full_acl) @> ARRAY[current_setting('eln.currentUserId')::UUID];

CREATE OR REPLACE VIEW Experiment_Marked_View AS
SELECT m.experiment_id, TRUE marked_or_null
FROM Experiment_Mark m
WHERE user_id = current_setting('eln.currentUserId')::UUID;

CREATE OR REPLACE FUNCTION get_experiment_search_vector(
    IN current_experiment_id UUID
) RETURNS TSVECTOR AS $$
BEGIN
    RETURN (
        SELECT
            setweight(to_tsvector('english', coalesce(e.name, '')), 'A') ||
            setweight(to_tsvector('english', coalesce(e.title, '')), 'A') ||
            setweight(to_tsvector('english', coalesce(e.description, '')), 'D') ||
            setweight(to_tsvector('english', coalesce(e.literature, '')), 'D') ||
            setweight(to_tsvector('english', coalesce(c.display_name, '')), 'C')
        FROM Experiment e
        JOIN User_Account c ON c.id = e.created_by_id
        WHERE e.id = current_experiment_id
    );
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_Experiment_search_vector()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE Experiment SET search_vector = get_experiment_search_vector(new.id) WHERE id = new.id;
    RETURN new;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trigger_update_Experiment_search_vector
AFTER INSERT OR UPDATE OF name, description ON Experiment
FOR EACH ROW EXECUTE FUNCTION update_Experiment_search_vector();

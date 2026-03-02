CREATE OR REPLACE VIEW Experiment_View_2 AS
SELECT e.id,
    e.current_access,
    exists(SELECT 1 FROM Experiment_Mark WHERE experiment_id = e.id AND user_id = current_setting('eln.currentUserId')::UUID) marked,
    array_length(e.full_acl, 1) acl_count
FROM Experiment_Base_View e;

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

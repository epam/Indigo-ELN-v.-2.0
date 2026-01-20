CREATE OR REPLACE VIEW Project_View_2 AS
SELECT p.id,
    p.current_access,
    array_length(p.full_acl, 1) acl_count
FROM Project_Base_View p;

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

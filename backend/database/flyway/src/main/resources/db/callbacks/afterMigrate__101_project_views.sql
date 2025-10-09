CREATE OR REPLACE VIEW Project_ACL_View AS
SELECT a.project_id, a.user_id, u.display_name, a.level
FROM Project_ACL a
JOIN User_Account u ON u.id = a.user_id;

CREATE OR REPLACE VIEW Project_View AS
SELECT p.*,
    (
        SELECT COUNT(*)
        FROM Notebook_Base_View n
        WHERE n.project_id = p.id
    ) notebook_count,
    (
        SELECT coalesce(array_agg(ROW(status, count_)::Experiment_Count), '{}')
        FROM (SELECT e.status, COUNT(*) count_
              FROM Experiment_Base_View e
              WHERE e.project_id = p.id
              GROUP BY e.status
        ) t
    ) experiment_count,
    ARRAY(
        SELECT ROW(a.user_id, a.display_name, a.level, FALSE)::ACL_Entry
        FROM Project_ACL_View a
        WHERE a.project_id = p.id AND a.level != 'IMPLICIT_VIEW'
        ORDER BY a.level DESC, a.display_name
        LIMIT 3
    ) acl_short,
    (
        SELECT COUNT(*)
        FROM Project_ACL_View a
        WHERE a.project_id = p.id AND a.level != 'IMPLICIT_VIEW'
    ) acl_count
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

CREATE OR REPLACE FUNCTION insert_Project_View()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO Project (id, created_by_id, created_at, modified_by_id, modified_at, name, literature, description)
    VALUES (new.id, new.created_by_id, new.created_at, new.modified_by_id, new.modified_at, new.name, new.literature, new.description);

    UPDATE Project SET search_vector = get_project_search_vector(new.id) WHERE id = new.id;

    RETURN new;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER insert_Project_View
INSTEAD OF INSERT ON Project_View
FOR EACH ROW
EXECUTE FUNCTION insert_Project_View();

CREATE OR REPLACE FUNCTION update_Project_View()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE Project
    SET modified_by_id = new.modified_by_id,
        modified_at = new.modified_at,
        name = new.name,
        literature = new.literature,
        description = new.description
    WHERE id = new.id;

    UPDATE Project SET search_vector = get_project_search_vector(new.id) WHERE id = new.id;

    RETURN new;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER update_Project_View
INSTEAD OF UPDATE ON Project_View
FOR EACH ROW
EXECUTE FUNCTION update_Project_View();

CREATE OR REPLACE VIEW Notebook_View AS
SELECT n.*,
    (
        SELECT coalesce(array_agg(ROW(status, count_)::Experiment_Count), '{}')
        FROM (SELECT e.status, COUNT(*) count_
              FROM Experiment_Base_View e
              WHERE e.notebook_id = n.id
              GROUP BY e.status
        ) t
    ) experiment_count,
    array_length(n.full_acl, 1) acl_count
FROM Notebook_Base_View n;

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

CREATE OR REPLACE FUNCTION insert_Notebook_View()
    RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO Notebook (id, created_by_id, created_at, modified_by_id, modified_at, project_id, name, description, full_acl, acl_short)
    VALUES (new.id, new.created_by_id, new.created_at, new.modified_by_id, new.modified_at, new.project_id, new.name, new.description, new.full_acl, new.acl_short);

    UPDATE Notebook SET search_vector = get_notebook_search_vector(new.id) WHERE id = new.id;

    RETURN new;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER insert_Notebook_View
INSTEAD OF INSERT ON Notebook_View
FOR EACH ROW
EXECUTE FUNCTION insert_Notebook_View();

CREATE OR REPLACE FUNCTION update_Notebook_View()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE Notebook
    SET modified_by_id = new.modified_by_id,
        modified_at = new.modified_at,
        name = new.name,
        description = new.description,
        full_acl = new.full_acl,
        acl_short = new.acl_short
    WHERE id = new.id;

    UPDATE Notebook SET search_vector = get_notebook_search_vector(new.id) WHERE id = new.id;

    RETURN new;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER update_Notebook_View
    INSTEAD OF UPDATE ON Notebook_View
    FOR EACH ROW
EXECUTE FUNCTION update_Notebook_View();


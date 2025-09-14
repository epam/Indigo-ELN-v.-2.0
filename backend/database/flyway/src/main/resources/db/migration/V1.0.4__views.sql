/* Base views */

CREATE VIEW Project_Base_View AS
SELECT p.*, pa.level current_access
FROM Project p
LEFT JOIN Project_ACL pa ON pa.project_id = p.id AND pa.user_id = current_setting('eln.currentUserId')::UUID
WHERE current_setting('eln.viewAllProjects')::BOOLEAN OR pa.project_id IS NOT NULL;

CREATE VIEW Notebook_Base_View AS
SELECT n.*, na.level current_access
FROM Notebook n
LEFT JOIN Notebook_ACL na ON na.notebook_id = n.id AND na.user_id = current_setting('eln.currentUserId')::UUID
WHERE current_setting('eln.viewAllNotebooks')::BOOLEAN OR na.notebook_id IS NOT NULL;

CREATE VIEW Experiment_Base_View AS
SELECT e.*, ea.level current_access
FROM Experiment e
LEFT JOIN Experiment_ACL ea ON ea.experiment_id = e.id AND ea.user_id = current_setting('eln.currentUserId')::UUID
WHERE current_setting('eln.viewAllExperiments')::BOOLEAN OR ea.experiment_id IS NOT NULL;

CREATE VIEW Project_ACL_View AS
SELECT a.project_id, a.user_id, u.display_name, a.level
FROM Project_ACL a
JOIN User_Account u ON u.id = a.user_id;

CREATE VIEW Notebook_ACL_View AS
SELECT a.notebook_id, a.user_id, u.display_name, a.level, a.inherited
FROM Notebook_ACL a
JOIN User_Account u ON u.id = a.user_id;

CREATE VIEW Experiment_ACL_View AS
SELECT a.experiment_id, a.user_id, u.display_name, a.level, a.inherited
FROM Experiment_ACL a
JOIN User_Account u ON u.id = a.user_id;

/* Projects */

CREATE VIEW Project_View AS
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

CREATE FUNCTION get_project_search_vector(
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

CREATE FUNCTION insert_Project_View()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO Project (id, created_by_id, created_at, modified_by_id, modified_at, name, literature, description)
    VALUES (new.id, new.created_by_id, new.created_at, new.modified_by_id, new.modified_at, new.name, new.literature, new.description);

    UPDATE Project SET search_vector = get_project_search_vector(new.id) WHERE id = new.id;

    RETURN new;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER insert_Project_View
INSTEAD OF INSERT ON Project_View
FOR EACH ROW
EXECUTE FUNCTION insert_Project_View();

CREATE FUNCTION update_Project_View()
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

CREATE TRIGGER update_Project_View
INSTEAD OF UPDATE ON Project_View
FOR EACH ROW
EXECUTE FUNCTION update_Project_View();

/* Notebooks */

CREATE VIEW Notebook_View AS
SELECT n.*,
    (
        SELECT coalesce(array_agg(ROW(status, count_)::Experiment_Count), '{}')
        FROM (SELECT e.status, COUNT(*) count_
              FROM Experiment_Base_View e
              WHERE e.notebook_id = n.id
              GROUP BY e.status
        ) t
    ) experiment_count,
    ARRAY(
        SELECT ROW(a.user_id, a.display_name, a.level, a.inherited)::ACL_Entry
        FROM Notebook_ACL_View a
        WHERE a.notebook_id = n.id AND a.level != 'IMPLICIT_VIEW'
        ORDER BY a.inherited, a.level DESC, a.display_name
        LIMIT 3
    ) acl_short,
    (
        SELECT COUNT(*)
        FROM Notebook_ACL a
        WHERE a.notebook_id = n.id AND a.level != 'IMPLICIT_VIEW'
    ) acl_count
FROM Notebook_Base_View n;

CREATE FUNCTION get_notebook_search_vector(
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

CREATE FUNCTION insert_Notebook_View()
    RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO Notebook (id, created_by_id, created_at, modified_by_id, modified_at, project_id, name, description)
    VALUES (new.id, new.created_by_id, new.created_at, new.modified_by_id, new.modified_at, new.project_id, new.name, new.description);

    UPDATE Notebook SET search_vector = get_notebook_search_vector(new.id) WHERE id = new.id;

    RETURN new;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER insert_Notebook_View
INSTEAD OF INSERT ON Notebook_View
FOR EACH ROW
EXECUTE FUNCTION insert_Notebook_View();

CREATE FUNCTION update_Notebook_View()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE Notebook
    SET modified_by_id = new.modified_by_id,
        modified_at = new.modified_at,
        name = new.name,
        description = new.description
    WHERE id = new.id;

    UPDATE Notebook SET search_vector = get_notebook_search_vector(new.id) WHERE id = new.id;

    RETURN new;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_Notebook_View
    INSTEAD OF UPDATE ON Notebook_View
    FOR EACH ROW
EXECUTE FUNCTION update_Notebook_View();

/* Experiments */

CREATE VIEW Experiment_View AS
SELECT e.*,
    exists(SELECT 1 FROM Experiment_Mark WHERE experiment_id = e.id AND user_id = current_setting('eln.currentUserId')::UUID) marked,
    ARRAY(
        SELECT ROW(a.user_id, a.display_name, a.level, a.inherited)::ACL_Entry
        FROM Experiment_ACL_View a
        WHERE a.experiment_id = e.id AND a.level != 'IMPLICIT_VIEW'
        ORDER BY a.inherited, a.level DESC, a.display_name
        LIMIT 3
    ) acl_short,
    (
        SELECT COUNT(*)
        FROM Experiment_ACL_View a
        WHERE a.experiment_id = e.id AND a.level != 'IMPLICIT_VIEW'
    ) acl_count
FROM Experiment_Base_View e;

CREATE FUNCTION get_experiment_search_vector(
    IN current_experiment_id UUID
) RETURNS TSVECTOR AS $$
BEGIN
    RETURN (
        SELECT
            setweight(to_tsvector('english', coalesce(e.name, '')), 'A') ||
            setweight(to_tsvector('english', coalesce(e.description, '')), 'D') ||
            setweight(to_tsvector('english', coalesce(c.display_name, '')), 'C')
        FROM Experiment e
        JOIN User_Account c ON c.id = e.created_by_id
        WHERE e.id = current_experiment_id
    );
END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION insert_Experiment_View()
    RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO Experiment (id, created_by_id, created_at, modified_by_id, modified_at, project_id, notebook_id, template_id, name, status, description, therapeutic_area_id, project_code_id, model, picture)
    VALUES (new.id, new.created_by_id, new.created_at, new.modified_by_id, new.modified_at, new.project_id, new.notebook_id, new.template_id, new.name, new.status, new.description, new.therapeutic_area_id, new.project_code_id, new.model, new.picture);

    UPDATE Experiment SET search_vector = get_experiment_search_vector(new.id) WHERE id = new.id;

    RETURN new;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER insert_Experiment_View
INSTEAD OF INSERT ON Experiment_View
FOR EACH ROW
EXECUTE FUNCTION insert_Experiment_View();

CREATE FUNCTION update_Experiment_View()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE Experiment
    SET modified_by_id = new.modified_by_id,
        modified_at = new.modified_at,
        name = new.name,
        status = new.status,
        description = new.description,
        therapeutic_area_id = new.therapeutic_area_id,
        project_code_id = new.project_code_id,
        model = new.model,
        picture = new.picture
    WHERE id = new.id;

    UPDATE Experiment SET search_vector = get_experiment_search_vector(new.id) WHERE id = new.id;

    RETURN new;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_Experiment_View
    INSTEAD OF UPDATE ON Experiment_View
    FOR EACH ROW
EXECUTE FUNCTION update_Experiment_View();

/* Other */

CREATE VIEW Total_Counts_View AS
SELECT
    (
        SELECT COUNT(*)
        FROM Project_Base_View
    ) projects,
    (
        SELECT COUNT(*)
        FROM Notebook_Base_View
    ) notebooks,
    (
        SELECT coalesce(array_agg(ROW(status, count_)::Experiment_Count), '{}')
        FROM (SELECT e.status, COUNT(*) count_
              FROM Experiment_Base_View e
              GROUP BY e.status
        ) t
    ) experiments_by_status;

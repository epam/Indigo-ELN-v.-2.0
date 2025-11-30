CREATE OR REPLACE VIEW Experiment_View AS
SELECT e.*,
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
            setweight(to_tsvector('english', coalesce(e.description, '')), 'D') ||
            setweight(to_tsvector('english', coalesce(c.display_name, '')), 'C')
        FROM Experiment e
        JOIN User_Account c ON c.id = e.created_by_id
        WHERE e.id = current_experiment_id
    );
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION insert_Experiment_View()
    RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO Experiment (id, created_by_id, created_at, modified_by_id, modified_at, project_id, notebook_id, template_id, name, status, description, therapeutic_area_id, project_code_id, model, picture, report_for_signature_id, full_acl, acl_short)
    VALUES (new.id, new.created_by_id, new.created_at, new.modified_by_id, new.modified_at, new.project_id, new.notebook_id, new.template_id, new.name, new.status, new.description, new.therapeutic_area_id, new.project_code_id, new.model, new.picture, new.report_for_signature_id, new.full_acl, new.acl_short);

    UPDATE Experiment SET search_vector = get_experiment_search_vector(new.id) WHERE id = new.id;

    RETURN new;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER insert_Experiment_View
INSTEAD OF INSERT ON Experiment_View
FOR EACH ROW
EXECUTE FUNCTION insert_Experiment_View();

CREATE OR REPLACE FUNCTION update_Experiment_View()
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
        picture = new.picture,
        report_for_signature_id = new.report_for_signature_id,
        full_acl = new.full_acl,
        acl_short = new.acl_short
    WHERE id = new.id;

    UPDATE Experiment SET search_vector = get_experiment_search_vector(new.id) WHERE id = new.id;

    RETURN new;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER update_Experiment_View
    INSTEAD OF UPDATE ON Experiment_View
    FOR EACH ROW
EXECUTE FUNCTION update_Experiment_View();

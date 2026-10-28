DROP VIEW IF EXISTS Experiment_View_2;

CREATE OR REPLACE VIEW Experiment_Access_View AS
SELECT experiment_id, current_access_or_null
FROM (
    SELECT e.id experiment_id,
           COALESCE(
               CASE WHEN e.created_by_id = current_setting('eln.currentUserId')::UUID THEN 'AUTHOR'::Access_Level END,
               ea.level,
               na.level,
               pa.level
           ) current_access_or_null
    FROM Experiment e
    LEFT JOIN Experiment_ACL ea ON ea.experiment_id = e.id AND ea.user_id = current_setting('eln.currentUserId')::UUID
    LEFT JOIN Notebook_ACL na ON na.notebook_id = e.notebook_id AND na.user_id = current_setting('eln.currentUserId')::UUID
    LEFT JOIN Project_ACL pa ON pa.project_id = e.project_id AND pa.user_id = current_setting('eln.currentUserId')::UUID
) t
WHERE current_setting('eln.viewAllExperiments')::BOOLEAN OR current_access_or_null IS NOT NULL;

CREATE OR REPLACE VIEW Experiment_Marked_View AS
SELECT m.experiment_id, TRUE marked_or_null
FROM Experiment_Mark m
WHERE user_id = current_setting('eln.currentUserId')::UUID;


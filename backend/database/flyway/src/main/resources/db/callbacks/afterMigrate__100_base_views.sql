CREATE OR REPLACE VIEW Project_Base_View AS
SELECT p.*, pa.level current_access
FROM Project p
LEFT JOIN Project_ACL pa ON pa.project_id = p.id AND pa.user_id = current_setting('eln.currentUserId')::UUID
WHERE current_setting('eln.viewAllProjects')::BOOLEAN OR pa.project_id IS NOT NULL;

CREATE OR REPLACE VIEW Notebook_Base_View AS
SELECT n.*, na.level current_access
FROM Notebook n
LEFT JOIN Notebook_ACL na ON na.notebook_id = n.id AND na.user_id = current_setting('eln.currentUserId')::UUID
WHERE current_setting('eln.viewAllNotebooks')::BOOLEAN OR na.notebook_id IS NOT NULL;

CREATE OR REPLACE VIEW Experiment_Base_View AS
SELECT e.*, ea.level current_access
FROM Experiment e
LEFT JOIN Experiment_ACL ea ON ea.experiment_id = e.id AND ea.user_id = current_setting('eln.currentUserId')::UUID
WHERE current_setting('eln.viewAllExperiments')::BOOLEAN OR ea.experiment_id IS NOT NULL;

CREATE OR REPLACE VIEW Total_Counts_View AS
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

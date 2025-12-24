CREATE OR REPLACE VIEW Project_Base_View AS
SELECT p.*, pa.level current_access
FROM Project p
LEFT JOIN LATERAL unnest(p.full_acl) pa ON pa.user_id = current_setting('eln.currentUserId')::UUID
WHERE current_setting('eln.viewAllProjects')::BOOLEAN OR pa.level IS NOT NULL;

CREATE OR REPLACE VIEW Notebook_Base_View AS
SELECT n.*, na.level current_access
FROM Notebook n
LEFT JOIN LATERAL unnest(n.full_acl) na ON na.user_id = current_setting('eln.currentUserId')::UUID
WHERE current_setting('eln.viewAllNotebooks')::BOOLEAN OR na.level IS NOT NULL;

CREATE OR REPLACE VIEW Experiment_Base_View AS
SELECT e.*, ea.level current_access
FROM Experiment e
LEFT JOIN LATERAL unnest(e.full_acl) ea ON ea.user_id = current_setting('eln.currentUserId')::UUID
WHERE current_setting('eln.viewAllExperiments')::BOOLEAN OR ea.level IS NOT NULL;

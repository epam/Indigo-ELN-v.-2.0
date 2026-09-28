
CREATE OR REPLACE VIEW Sample_Is_Marked AS
SELECT s.id sample_id, true marked
FROM Sample s
JOIN Sample_Mark m ON s.id = m.sample_id AND m.user_id = current_setting('eln.currentUserId')::UUID;

CREATE FUNCTION mark_experiment(
    IN current_experiment_id UUID,
    IN current_user_id UUID,
    IN current_mark BOOLEAN
) RETURNS VOID AS $$
BEGIN
    IF current_mark THEN
        INSERT INTO Experiment_Mark (experiment_id, user_id)
        VALUES (current_experiment_id, current_user_id)
        ON CONFLICT ON CONSTRAINT experiment_mark_pk DO NOTHING;
    ELSE
        DELETE FROM Experiment_Mark
        WHERE experiment_id = current_experiment_id AND user_id = current_user_id;
    END IF;
END
$$ LANGUAGE plpgsql;

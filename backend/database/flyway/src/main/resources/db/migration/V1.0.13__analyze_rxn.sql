ALTER TABLE Experiment_Referenced_Compound
    DROP CONSTRAINT experiment_referenced_compound_pk;

ALTER TABLE Experiment_Referenced_Compound
    ADD CONSTRAINT experiment_referenced_compound_pk PRIMARY KEY (compound_id, experiment_id, reaction_role);

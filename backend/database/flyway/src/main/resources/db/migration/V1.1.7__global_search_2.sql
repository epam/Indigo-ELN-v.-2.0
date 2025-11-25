CREATE TYPE Reaction_Role AS ENUM (
    'REACTANT',
    'CATALYST',
    'SOLVENT',
    'OUTPUT'
);

ALTER TABLE Experiment_Referenced_Compound ADD COLUMN reaction_role Reaction_Role;
UPDATE Experiment_Referenced_Compound SET reaction_role='REACTANT';
ALTER TABLE Experiment_Referenced_Compound ALTER COLUMN reaction_role SET NOT NULL;

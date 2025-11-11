CREATE TABLE Experiment_Rxnfile (
    experiment_id UUID NOT NULL,
    ordinal INT NOT NULL,
    rxnfile TEXT NOT NULL,
    CONSTRAINT experiment_rxnfile_pk PRIMARY KEY (experiment_id, ordinal),
    CONSTRAINT experiment_rxnfile_experiment_id_fk FOREIGN KEY (experiment_id) REFERENCES Experiment(id) ON DELETE CASCADE
);

CREATE INDEX ix_experiment_rxnfile_rxnfile ON Experiment_Rxnfile USING bingo_idx (rxnfile bingo.reaction);

CREATE TABLE Experiment_Dictionary_Item (
    experiment_id UUID NOT NULL,
    dictionary_item_id UUID NOT NULL,
    CONSTRAINT experiment_dictionary_item_pk PRIMARY KEY (experiment_id, dictionary_item_id),
    CONSTRAINT experiment_dictionary_item_experiment_id_fk FOREIGN KEY (experiment_id) REFERENCES Experiment(id) ON DELETE CASCADE,
    CONSTRAINT experiment_dictionary_item_dictionary_item_id_fk FOREIGN KEY (dictionary_item_id) REFERENCES dictionary_item(id)
);

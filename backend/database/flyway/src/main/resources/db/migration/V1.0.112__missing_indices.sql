CREATE INDEX ix_notebook_project_id ON Notebook (project_id);
CREATE INDEX ix_experiment_notebook_id ON Experiment (notebook_id);
CREATE INDEX ix_experiment_project_id ON Experiment (project_id);
CREATE INDEX ix_sample_compound_id ON Sample (compound_id);
CREATE INDEX ix_experiment_referenced_compound_experiment_id ON Experiment_Referenced_Compound (experiment_id);
CREATE INDEX ix_experiment_mark_user_id ON Experiment_Mark (user_id);
CREATE INDEX ix_sample_mark_user_id ON Sample_Mark (user_id);
CREATE INDEX ix_sample_search_vector ON Sample USING GIN (search_vector);

CREATE INDEX ix_project_created_by_id ON Project (created_by_id);
CREATE INDEX ix_notebook_created_by_id ON Notebook (created_by_id);
CREATE INDEX ix_experiment_created_by_id ON Experiment (created_by_id);
CREATE INDEX ix_template_created_by_id ON Template (created_by_id);
CREATE INDEX ix_experiment_therapeutic_area_id ON Experiment (therapeutic_area_id);
CREATE INDEX ix_experiment_project_code_id ON Experiment (project_code_id);
CREATE INDEX ix_dictionary_item_dictionary_id ON Dictionary_Item (dictionary_id);

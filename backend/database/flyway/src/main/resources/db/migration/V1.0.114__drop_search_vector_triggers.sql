DROP TRIGGER IF EXISTS trigger_update_Project_search_vector ON Project;
DROP TRIGGER IF EXISTS trigger_update_Project_search_vector_on_keyword_insert ON Project_Keyword;
DROP TRIGGER IF EXISTS trigger_update_Project_search_vector_on_keyword_update ON Project_Keyword;
DROP TRIGGER IF EXISTS trigger_update_Project_search_vector_on_keyword_delete ON Project_Keyword;
DROP TRIGGER IF EXISTS trigger_update_Notebook_search_vector ON Notebook;
DROP TRIGGER IF EXISTS trigger_update_Experiment_search_vector ON Experiment;
DROP TRIGGER IF EXISTS update_Sample ON Sample;

DROP FUNCTION IF EXISTS update_Project_search_vector();
DROP FUNCTION IF EXISTS update_Project_search_vector_on_keyword_insert();
DROP FUNCTION IF EXISTS update_Project_search_vector_on_keyword_delete();
DROP FUNCTION IF EXISTS update_Notebook_search_vector();
DROP FUNCTION IF EXISTS update_Experiment_search_vector();
DROP FUNCTION IF EXISTS update_Sample_search_vector();
DROP FUNCTION IF EXISTS get_project_search_vector(UUID);
DROP FUNCTION IF EXISTS get_notebook_search_vector(UUID);
DROP FUNCTION IF EXISTS get_experiment_search_vector(UUID);
DROP FUNCTION IF EXISTS get_sample_search_vector(UUID);

CREATE FUNCTION acl_user_ids(ACL_Entry[]) RETURNS UUID[] AS $$
    SELECT array_agg((a).user_id) FROM unnest($1) a;
$$ LANGUAGE SQL IMMUTABLE PARALLEL SAFE;

CREATE INDEX ix_project_acl_gin ON Project USING GIN (acl_user_ids(full_acl));
CREATE INDEX ix_notebook_acl_gin ON Notebook USING GIN (acl_user_ids(full_acl));
CREATE INDEX ix_experiment_acl_gin ON Experiment USING GIN (acl_user_ids(full_acl));

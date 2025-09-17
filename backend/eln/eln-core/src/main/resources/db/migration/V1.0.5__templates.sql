ALTER TABLE Template
ADD COLUMN templateTabs JSONB NOT NULL;

ALTER TABLE Template
DROP COLUMN components;

ALTER TABLE Template
ADD CONSTRAINT template_name_unique UNIQUE (name);
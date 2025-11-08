UPDATE Experiment
SET model = (model - 'lastUsedAnchor') || '{"revision": 1}'::jsonb
WHERE true;

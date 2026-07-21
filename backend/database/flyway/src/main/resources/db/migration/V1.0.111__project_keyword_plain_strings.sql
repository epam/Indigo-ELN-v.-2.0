-- Replace dictionary-backed project keywords with plain-string keywords.
-- Project_Keyword used to reference Dictionary_Item(keyword_id); it now stores the keyword text directly
-- as an @ElementCollection element table (project_id, keyword, ordinal).

-- 1. Add the text column and backfill it from the referenced dictionary items (preserve existing keywords).
ALTER TABLE Project_Keyword ADD COLUMN keyword VARCHAR(1000);
UPDATE Project_Keyword pk SET keyword = di.name
FROM Dictionary_Item di
WHERE pk.keyword_id = di.id;
ALTER TABLE Project_Keyword ALTER COLUMN keyword SET NOT NULL;

-- 2. Drop the dictionary coupling. Keep the deferrable UNIQUE (project_id, ordinal) that lets Hibernate
--    reorder the @OrderColumn list within a transaction.
ALTER TABLE Project_Keyword DROP CONSTRAINT project_keyword_pk;             -- was (project_id, keyword_id)
ALTER TABLE Project_Keyword DROP CONSTRAINT project_keyword_keyword_id_fk;
ALTER TABLE Project_Keyword DROP COLUMN keyword_id;

-- 3. Locale-independent, case-insensitive prefix index backing the keyword suggestion query
--    (SELECT DISTINCT keyword ... WHERE LOWER(keyword) LIKE 'x%').
CREATE INDEX ix_project_keyword_suggest ON Project_Keyword (LOWER(keyword) varchar_pattern_ops);

-- 4. Retire the now-unused PROJECT_KEYWORD dictionary and its (user-created) items. The project_keyword
--    FK to Dictionary_Item is already gone, so these rows are no longer referenced.
DELETE FROM Dictionary_Item WHERE dictionary_id = '1cc9d41e-1e5e-4447-abd5-5067e9ba8210';
DELETE FROM Dictionary WHERE id = '1cc9d41e-1e5e-4447-abd5-5067e9ba8210';

UPDATE Document_Signature ds
SET ordinal = rn
FROM (
    SELECT id, row_number() OVER () rn
    FROM Document_Signature
) t
WHERE ds.id = t.id;

ALTER TABLE Document_Signature ALTER COLUMN ordinal SET NOT NULL;

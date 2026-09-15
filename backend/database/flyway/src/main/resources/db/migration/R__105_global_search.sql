CREATE AGGREGATE tsvector_agg(tsvector) (
    STYPE = pg_catalog.tsvector,
    SFUNC = pg_catalog.tsvector_concat,
    INITCOND = ''
);

CREATE OR REPLACE FUNCTION calculate_tsvector (
    input JSONB
) RETURNS tsvector AS $$
BEGIN
    IF input IS NULL OR jsonb_typeof(input) = 'null' THEN
        RETURN ''::tsvector;
    END IF;
    RETURN (
        (
            SELECT COALESCE(tsvector_agg(setweight(to_tsvector('english', t), 'A')), ''::tsvector)
            FROM jsonb_array_elements_text(input->'a') AS t
        )
        ||
        (
            SELECT COALESCE(tsvector_agg(setweight(to_tsvector('english', t), 'B')), ''::tsvector)
            FROM jsonb_array_elements_text(input->'b') AS t
        )
        ||
        (
            SELECT COALESCE(tsvector_agg(setweight(to_tsvector('english', t), 'C')), ''::tsvector)
            FROM jsonb_array_elements_text(input->'c') AS t
        )
        ||
        (
            SELECT COALESCE(tsvector_agg(setweight(to_tsvector('english', t), 'D')), ''::tsvector)
            FROM jsonb_array_elements_text(input->'d') AS t
        )
    );
END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION get_sample_search_vector(
    IN current_sample_id UUID
) RETURNS TSVECTOR AS $$
BEGIN
    RETURN (
        SELECT
            setweight(to_tsvector('english', coalesce(s.str_code, '')), 'A') ||
            setweight(to_tsvector('english', coalesce(s.nbk_batch_number, '')), 'A') ||
            setweight(to_tsvector('english', coalesce(c.cas_number, '')), 'A') ||
            setweight(to_tsvector('english', coalesce(chemical_name, '')), 'B')
        FROM Sample s
        JOIN Compound c ON c.id = s.compound_id
        WHERE s.id = current_sample_id
    );
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_Sample_search_vector()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE Sample SET search_vector = get_sample_search_vector(coalesce(old.id, new.id)) WHERE id = coalesce(old.id, new.id);
    RETURN new;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER update_Sample
    AFTER UPDATE ON Sample
    FOR EACH ROW
EXECUTE FUNCTION update_Sample_search_vector();

CREATE OR REPLACE TRIGGER update_Sample
    AFTER INSERT ON Sample
    FOR EACH ROW
EXECUTE FUNCTION update_Sample_search_vector();

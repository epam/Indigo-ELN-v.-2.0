CREATE EXTENSION IF NOT EXISTS pg_stat_statements;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE USER eln WITH PASSWORD 'eln';
CREATE DATABASE eln TEMPLATE postgres OWNER eln;

CREATE USER keycloak WITH PASSWORD 'keycloak';
CREATE DATABASE keycloak TEMPLATE postgres OWNER keycloak;

CREATE USER signature WITH PASSWORD 'signature';
CREATE DATABASE signature TEMPLATE postgres OWNER signature;

CREATE USER sampleregistration WITH PASSWORD 'sampleregistration';
CREATE DATABASE sampleregistration TEMPLATE postgres OWNER sampleregistration;

-- 00_bingo_install.sql runs as postgres; grant the eln and sampleregistration app user access
-- to the bingo schema so Flyway migrations can create bingo_idx indexes.
\c eln
GRANT USAGE ON SCHEMA bingo TO eln;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA bingo TO eln;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA bingo TO eln;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA bingo TO eln;
\c sampleregistration
GRANT USAGE ON SCHEMA bingo TO sampleregistration;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA bingo TO sampleregistration;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA bingo TO sampleregistration;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA bingo TO sampleregistration;

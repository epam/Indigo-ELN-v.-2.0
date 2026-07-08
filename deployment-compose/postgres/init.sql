CREATE USER eln WITH PASSWORD 'eln';
CREATE DATABASE eln OWNER eln;

CREATE USER keycloak WITH PASSWORD 'keycloak';
CREATE DATABASE keycloak OWNER keycloak;

CREATE USER signature WITH PASSWORD 'signature';
CREATE DATABASE signature OWNER signature;

-- pg_stat_statements requires superuser; install it here so the Flyway
-- `CREATE EXTENSION IF NOT EXISTS` (run as the eln app user) is a no-op.
-- Bingo (provides the bingo_idx access method used by V1.0.3__compounds.sql)
-- is installed by 00_bingo_install.sql against the default `postgres` DB;
-- re-run it against `eln` so the Flyway migration can create the index.
\c eln
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;
\i /docker-entrypoint-initdb.d/00_bingo_install.sql

-- 00_bingo_install.sql runs as postgres; grant the eln app user access
-- to the bingo schema so Flyway migrations can create bingo_idx indexes.
GRANT USAGE ON SCHEMA bingo TO eln;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA bingo TO eln;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA bingo TO eln;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA bingo TO eln;

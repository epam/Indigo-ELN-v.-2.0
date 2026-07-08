CREATE USER eln WITH PASSWORD 'eln';
CREATE DATABASE eln OWNER eln;

CREATE USER keycloak WITH PASSWORD 'keycloak';
CREATE DATABASE keycloak OWNER keycloak;

CREATE USER signature WITH PASSWORD 'signature';
CREATE DATABASE signature OWNER signature;

-- pg_stat_statements requires superuser; install it here so the Flyway
-- `CREATE EXTENSION IF NOT EXISTS` (run as the eln app user) is a no-op.
\c eln
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;

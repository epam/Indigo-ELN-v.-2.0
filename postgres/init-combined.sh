#!/bin/bash
# Creates the application databases and their owning roles.
#
# A shell script rather than plain SQL so the passwords can come from the environment: on AWS they
# are injected from a generated secret, while the literals below keep the local compose stack and the
# integration-test stacks working unchanged.
#
# Runs after 00_bingo_install.sql, which installs bingo into POSTGRES_DB. POSTGRES_DB defaults to
# POSTGRES_USER, so POSTGRES_USER must be `postgres` for `TEMPLATE postgres` below to carry the bingo
# schema into each new database.
set -euo pipefail

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
	CREATE USER eln WITH PASSWORD '${ELN_DB_PASSWORD:-eln}';
	CREATE DATABASE eln TEMPLATE postgres OWNER eln;

	CREATE USER keycloak WITH PASSWORD '${KEYCLOAK_DB_PASSWORD:-keycloak}';
	CREATE DATABASE keycloak TEMPLATE postgres OWNER keycloak;

	CREATE USER signature WITH PASSWORD '${SIGNATURE_DB_PASSWORD:-signature}';
	CREATE DATABASE signature TEMPLATE postgres OWNER signature;

	CREATE USER sampleregistration WITH PASSWORD '${SAMPLEREGISTRATION_DB_PASSWORD:-sampleregistration}';
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
EOSQL

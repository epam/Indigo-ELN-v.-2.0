## Indigo Signature Service Database Schema Installation

### Oracle

- Install

        sqlplus sys/admin@//localhost:1521/xe as sysdba @oracle/schema_install.sql

- Uninstall

        sqlplus sys/admin@//localhost:1521/xe as sysdba @oracle/schema_uninstall.sql

### PostgreSQL

- Install

        psql -h localhost -p 5432 -d postgres -U postgres -f postgresql/schema_install.sql

- Uninstall

        psql -h localhost -p 5432 -d postgres -U postgres -f postgresql/schema_uninstall.sql

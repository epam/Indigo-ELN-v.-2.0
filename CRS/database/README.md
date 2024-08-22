## CRS Database Schema Installation

Bingo 1.7.9 should be installed in database

### Oracle

- Notes while installing Bingo on Oracle

    - In case of ORA-28595 - use `libdir` with path inside Oracle folder
    - In case if `sys as sysdba` is necessary - use `sys` username and add `as sysdba` to lines with `sqlplus %dbaname%...` in `bingo-oracle-install.bat`

- Install

        sqlplus sys/admin@//localhost:1521/xe as sysdba @oracle/schema_install.sql

- Uninstall

        sqlplus sys/admin@//localhost:1521/xe as sysdba @oracle/schema_uninstall.sql

### PostgreSQL

- Install

        psql -h localhost -p 5432 -d postgres -U postgres -f postgresql/schema_install.sql

- Uninstall

        psql -h localhost -p 5432 -d postgres -U postgres -f postgresql/schema_uninstall.sql

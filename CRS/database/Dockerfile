FROM public.ecr.aws/docker/library/postgres:12

COPY postgresql/schema_install.sql /docker-entrypoint-initdb.d/

COPY bingo-lib/lib/libbingo-postgres.so /usr/local/lib/postgresql/
COPY bingo-lib /bingo-lib
WORKDIR /bingo-lib
RUN chmod +x ./bingo-pg-install.sh
RUN ./bingo-pg-install.sh -libdir /usr/local/lib/postgresql -y
RUN cp bingo_install.sql /docker-entrypoint-initdb.d/00_bingo_install.sql

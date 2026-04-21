#!/bin/bash

FROM public.ecr.aws/docker/library/bash:latest AS builder

RUN mkdir /build
WORKDIR /build
RUN wget https://lifescience.opensource.epam.com/downloads/bingo-1.36.0/bingo-postgres-16-linux-x86_64.zip -O bingo.zip

RUN unzip bingo.zip && tar xzf bingo-postgres*.tgz
RUN mv */ bingo-postgres
WORKDIR /build/bingo-postgres

RUN chmod +x ./bingo-pg-install.sh
RUN ./bingo-pg-install.sh -libdir /usr/local/lib/postgresql -y

FROM public.ecr.aws/docker/library/postgres:16.6
COPY --from=builder /build/bingo-postgres/lib/libbingo-postgres.so /usr/local/lib/postgresql/
COPY --from=builder /build/bingo-postgres/bingo_install.sql /docker-entrypoint-initdb.d/00_bingo_install.sql

RUN echo "shared_preload_libraries = 'pg_stat_statements'" >> /usr/share/postgresql/postgresql.conf.sample && \
    echo "pg_stat_statements.track = all" >> /usr/share/postgresql/postgresql.conf.sample && \
    echo "pg_stat_statements.max = 10000" >> /usr/share/postgresql/postgresql.conf.sample

RUN echo "CREATE EXTENSION IF NOT EXISTS pg_stat_statements;" > /docker-entrypoint-initdb.d/01_pg_stat.sql


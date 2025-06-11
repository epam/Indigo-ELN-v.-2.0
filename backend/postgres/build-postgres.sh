#!/bin/bash
set -e

docker build --platform linux/amd64 -t indigoeln-postgres-bingo .

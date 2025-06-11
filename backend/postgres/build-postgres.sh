#!/bin/bash
set -e

podman build --arch amd64 . -t indigoeln-postgres-bingo

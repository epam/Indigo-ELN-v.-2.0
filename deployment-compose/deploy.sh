#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "$SCRIPT_DIR/../backend" && pwd)"

echo "==> Building backend services (eln, signature, reports)"
(
    cd "$BACKEND_DIR"
    ./gradlew \
        :eln:eln-service:quarkusBuild \
        :signature:signature-service:quarkusBuild \
        :reports:reports-service:quarkusBuild
)

echo "==> Starting docker compose stack"
cd "$SCRIPT_DIR"
docker compose up --build "$@"

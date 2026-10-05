#!/bin/bash
# Brings the compose stack to the state the CDK stack describes. Run by user-data on first boot, by
# the systemd unit after a reboot, and by `deployment-aws/deploy-compose.sh` via SSM for an update.
#
# Everything it needs is addressed by fixed names, so user-data never has to carry a content hash —
# which matters because any user-data change replaces the instance.
set -euo pipefail

APP_DIR=/opt/indigoeln
CONFIG_BUCKET="${CONFIG_BUCKET:?CONFIG_BUCKET not set}"
CONFIG_PREFIX="${CONFIG_PREFIX:-compose}"
ENV_PARAM="${ENV_PARAM:?ENV_PARAM not set}"
DB_SECRET="${DB_SECRET:?DB_SECRET not set}"
AWS_REGION="${AWS_REGION:-$(curl -s -H "X-aws-ec2-metadata-token: $(curl -s -X PUT \
  http://169.254.169.254/latest/api/token -H 'X-aws-ec2-metadata-token-ttl-seconds: 60')" \
  http://169.254.169.254/latest/meta-data/placement/region)}"
export AWS_REGION

mkdir -p "$APP_DIR"
# --delete prunes compose files removed upstream, but this directory also holds two locally
# generated files: deploy.conf (written once by user-data, never regenerated) and .env, which
# must sit beside docker-compose.yml for compose to pick it up. Without these excludes the
# sync deletes its own configuration on the first run.
aws s3 sync --delete --exclude deploy.conf --exclude .env \
  "s3://${CONFIG_BUCKET}/${CONFIG_PREFIX}/" "$APP_DIR/"
# s3 sync does not carry file modes, so a re-downloaded copy of this script arrives non-executable.
chmod +x "$APP_DIR/deploy.sh"

# .env is assembled from two sources so that no secret is ever embedded in another resource's value:
# the non-secret settings live in a plain SSM parameter, the password in Secrets Manager.
umask 077
aws ssm get-parameter --name "$ENV_PARAM" --query Parameter.Value --output text > "$APP_DIR/.env"
DB_PASSWORD=$(aws secretsmanager get-secret-value --secret-id "$DB_SECRET" \
  --query SecretString --output text | jq -r .password)
printf 'DB_PASSWORD=%s\n' "$DB_PASSWORD" >> "$APP_DIR/.env"

REGISTRY=$(awk -F= '/^ELN_AWS_IMAGE=/{split($2,a,"/"); print a[1]}' "$APP_DIR/.env")
aws ecr get-login-password --region "$AWS_REGION" | docker login --username AWS --password-stdin "$REGISTRY"

cd "$APP_DIR"
# Fail loudly on a mangled .env rather than starting Postgres with an empty password.
docker compose config >/dev/null
docker compose up -d --pull always --remove-orphans --wait --wait-timeout 120
docker compose ps

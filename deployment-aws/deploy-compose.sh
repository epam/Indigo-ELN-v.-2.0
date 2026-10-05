#!/bin/bash
# Rolls the compose stack on the EC2 instance forward to whatever the CDK stack currently describes.
#
# Use after `cdk deploy indigoeln-dev` (which rewrites the SSM parameter holding the image tags), or
# on its own to re-pull and restart. No SSH and no inbound ports: the instance runs the work itself
# via SSM. Does not replace the instance.
set -o errexit
set -o pipefail

source ./_aws-prepare-env.sh

#set -o xtrace
PS4='+ ${BASH_SOURCE##*/}:${LINENO}:${FUNCNAME[0]:-main}: '

# Which environment to act on. MainStack tags every resource with stage=<envName>
# (see IndigoELNApp), so that tag is what keeps dev and prod apart.
ENV_NAME="${1:-}"
ENV_NAME="${ENV_NAME#indigoeln-}"   # accept either `dev` or `indigoeln-dev`
if [[ -z "$ENV_NAME" ]]; then
  echo "Usage: $0 <env>    e.g. $0 dev" >&2
  exit 1
fi

INSTANCE_ID=$(aws ec2 describe-instances \
  --filters "Name=tag:indigoeln:role,Values=compose-host" \
            "Name=tag:stage,Values=$ENV_NAME" \
            "Name=instance-state-name,Values=running" \
  --query 'Reservations[].Instances[].InstanceId' --output text)

if [[ -z "$INSTANCE_ID" || "$INSTANCE_ID" == "None" ]]; then
  echo "Error: no running compose-host instance for stage=$ENV_NAME" >&2
  exit 1
fi
# Refuse to guess if the filters were not selective enough, rather than sending to an arbitrary one.
if [[ $(wc -w <<< "$INSTANCE_ID") -ne 1 ]]; then
  echo "Error: stage=$ENV_NAME matched several compose-host instances: $INSTANCE_ID" >&2
  exit 1
fi
echo "Environment: $ENV_NAME"
echo "Instance:    $INSTANCE_ID"

COMMAND_ID=$(aws ssm send-command \
  --document-name AWS-RunShellScript \
  --targets "Key=instanceids,Values=$INSTANCE_ID" \
  --comment "indigoeln compose deploy ($ENV_NAME)" \
  --parameters 'commands=["set -a; . /opt/indigoeln/deploy.conf; set +a; bash /opt/indigoeln/deploy.sh"]' \
  --query 'Command.CommandId' --output text)
echo "Command: $COMMAND_ID"

# send-command returns before the document is registered against the instance.
sleep 5
until STATUS=$(aws ssm get-command-invocation --command-id "$COMMAND_ID" --instance-id "$INSTANCE_ID" \
                 --query Status --output text 2>/dev/null) \
      && [[ "$STATUS" != "Pending" && "$STATUS" != "InProgress" && "$STATUS" != "Delayed" ]]; do
  echo "  ... ${STATUS:-starting}"
  sleep 5
done

aws ssm get-command-invocation --command-id "$COMMAND_ID" --instance-id "$INSTANCE_ID" \
  --query 'StandardOutputContent' --output text

if [[ "$STATUS" != "Success" ]]; then
  echo "=== FAILED ($STATUS) ===" >&2
  aws ssm get-command-invocation --command-id "$COMMAND_ID" --instance-id "$INSTANCE_ID" \
    --query 'StandardErrorContent' --output text >&2
  exit 1
fi
echo "=== $STATUS ==="

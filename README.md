# Indigo-ELN-v.-2.0

Indigo - The Open-Source Chemistry Electronic Lab Notebook

# Sandbox

https://indigo-eln.test.lifescience.opensource.epam.com/
please contact indigoeln@epam.com to request access

# Installation

**NB**: Indigo ELN is currently being severely transformed. Deployment script, application code and documentation may contain errors and inconsistencies.

Please contact the Indigo ELN team in case of any questions.

**NB**: Right now, build script assumes the application is deployed into us-east-1 region (North Virginia) because it uses AWS Public Container Registry which is only available in this region.
However, you can copy images to your own ECR repository and adjust XXXServiceTag parameters to refer to that repository.


## Local Installation

The local docker-compose stack runs the full application — frontend, backend (`eln-service`), PostgreSQL, and Keycloak — and is the recommended setup for local development.

### Prerequisites

- Docker with Docker Compose v2 (`docker compose ...`)
- Java 21 (required for the Gradle backend build)
- ~4 GB of free RAM

### Running on macOS

Docker Desktop is not required — [Colima](https://github.com/abiosoft/colima) works well as a Docker runtime. 
On **Apple Silicon**, next services (`postgres`, `eln-service`) bundle x86_64-only native libraries, so the VM must run **natively as `aarch64` with Rosetta** for amd64 translation.

```bash
brew install colima docker docker-compose
colima start --arch aarch64 --vz-rosetta --cpu 4 --memory 16 --disk 100
```

### Setup

```bash
git clone https://github.com/epam/Indigo-ELN-v.-2.0.git
cd Indigo-ELN-v.-2.0/deployment-compose
./deploy.sh
```
### Access

| URL | Purpose |
|---|---|
| `http://localhost` | Frontend (use this — port 80 via nginx) |
| `http://localhost:10020` | `eln-service` REST API |
| `http://localhost:8088` | Keycloak admin console (admin / admin) |

> **Important:** open the app at `http://localhost` (port 80), not `http://localhost:8080`. Port 8080 is the Angular dev server, whose `indigo-frontend/src/proxy.conf.js` forwards `/api` calls to the EPAM-hosted dev backend; local Keycloak tokens will not validate there and you will see `401 Unauthorized` on every API call.

### Users and realm

The `indigo-eln` Keycloak realm — including users, roles, and clients — is provisioned declaratively from `deployment-compose/keycloak-config-cli/realm-config.json` on first start. Edit that file to add or change users.

Two test users are seeded out of the box:

| Username | Password |
|---|---|
| `admin` | `admin` |

Use to log in at `http://localhost` once the stack is up.

### Stopping

```bash
./stop.sh
```

### Manual build (without `deploy.sh`)

```bash
cd backend && ./gradlew :eln:eln-service:quarkusBuild
cd ../deployment-compose && docker compose up --build
```


## Installation on AWS
- Check our Indigo ELN repository:
```bash
git clone https://github.com/epam/Indigo-ELN-v.-2.0.git
cd Indigo-ELN-v.-2.0
```

### Deploy a CloudFormation script
`indigo_eln_script.yaml`

You can do it via AWS Console or AWS CLI (`aws cloudformation create-stack`) or a helper tool like Rain (https://github.com/aws-cloudformation/rain)

Be sure to review the script adjust parameters before deploy.

Some required parameters:
- KeyPairName: Name of an existing Amazon EC2 key pair to enable SSH or RDP access to the instances
- VPCId: ID of your existing AWS VPC
- HostedZoneId: Existing Route53 Hosted zone ID
- IndigoELNDomainName: Domain name where Indigo ELN will be hosted
- IndigoELNAPIDomainName: Domain name where Indigo ELN API will be hosted
- IndigoELNContentBucketName: S3 bucket where frontend code will be served from (will be created)
- IndigoELNBuildLogsBucketName: S3 bucket where build logs are stored (will be created)
- MongoDBPassword: password for the created instance of MongoDB
- DefaultAdminPassword: password for main application admin user, BCrypt encoded

Also, update image tags for the Docker images used. Typically, you need the latest time tag with given prefix. Latest tags at the time of writing are already included in the CloudFormation script. Alternatively, you can find it here https://gallery.ecr.aws/m5k0g6n7/indigo_eln. 

### Validate installation and troubleshooting

- Make sure CloudFormation stack is deployed and all resources created without errors.

- Connect to EC2 instance using specified key pair, make sure all docker containers are running:
```bash
docker ps
```
There should be 8 containers running: mongo-db, postgres-signature, postgres-crs, bingodb, server, signature, crs, indigo

If there are fewer of them, you can find IDs of stopped containers and inspect its logs:
```bash
docker ps --all
docker logs <container_id>
```

- Now you can log into the application using the domain name you specified in the CloudFormation script (default credentials is admin/admin, if you didn't change the password parameter)

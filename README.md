# Indigo-ELN-v.-2.0
Indigo - The Open-Source Chemistry Electronic Lab Notebook

# Installation

**NB**: Indigo ELN is currently being severely transformed. Deployment script, application code and documentation may contain errors and inconsistencies.

Right now the most recent code, including transition to CloudFormation, is stored in work-in-progress branches:
- `indigo-eln` for Cloud Automation repository: https://git.epam.com/epm-lsop/cloud-automation.git
- `upgrade-java` for Indigo ELN repository: https://github.com/epam/Indigo-ELN-v.-2.0.git

This guide will use mention which branches to use.

Please contact the Indigo ELN team in case of any questions.

**NB**: Right now, build script assumes the application is deployed into us-east-1 region (North Virginia) because it uses AWS Public Container Registry which is only available in this region.
However, you can copy images to your own ECR repository and adjust XXXServiceTag parameters to refer to that repository.


## Local Installation
- You must have recent version of Docker installed
- Check our Indigo ELN repository and switch to branch:
```bash
git clone https://github.com/epam/Indigo-ELN-v.-2.0.git
cd Indigo-ELN-v.-2.0
git switch upgrade-java
```
- From the root of the repository, run:
```bash
docker compose -f docker-compose-local.yml up
```
It will build all containers and start the application. You can access the application UI at `http://localhost:9000`


## Installation on AWS

**NB**: Only accessible within EPAM

### Clone Cloud Automation repository and switch to branch
```bash
git clone https://git.epam.com/epm-lsop/cloud-automation.git
cd cloud-automation
git switch indigo-eln
```

### Deploy a CloudFormation script
`scripts/indigo_eln_script.yaml`

You can do it via AWS Console or AWS CLI (`aws cloudformation create-stack`) or a helper tool like Rain (https://github.com/aws-cloudformation/rain)

Be sure to adjust the script parameters, at least these:
- KeyPairName: Name of an existing Amazon EC2 key pair to enable SSH or RDP access to the instances
- VPCId: ID of your existing AWS VPC
- HostedZoneId: Existing Route53 Hosted zone ID
- IndigoELNDomainName: Domain name where Indigo ELN will be hosted
- IndigoELNAPIDomainName: Domain name where Indigo ELN API will be hosted
- IndigoELNContentBucketName: S3 bucket where frontend code will be served from (will be created)
- IndigoELNBuildLogsBucketName: S3 bucket where build logs are stored (will be created)
- MongoDBPassword: password for the created instance of MongoDB
- DefaultAdminPassword: password for main application admin user, BCrypt encoded

Also, update image tags for the Docker images used. Typically, you need the latest time tag with given prefix. Latest tags at the time of writing are listed below. Alternatively, you can find it here https://gallery.ecr.aws/m5k0g6n7/indigo_eln. 
- IndigoServerTag: public.ecr.aws/m5k0g6n7/indigo_eln:server-20241206-150630
- IndigoBingoServiceTag: public.ecr.aws/m5k0g6n7/indigo_eln:bingodb-20241114-210249
- IndigoSignatureServiceTag: public.ecr.aws/m5k0g6n7/indigo_eln:signature-20241114-220535
- IndigoCrsServiceTag: public.ecr.aws/m5k0g6n7/indigo_eln:crs-20241128-204151
- IndigoPostgresServiceTag: public.ecr.aws/m5k0g6n7/indigo_eln:postgres-20241114-220535
- IndigoPostgresCrsServiceTag: public.ecr.aws/m5k0g6n7/indigo_eln:postgres-crs-20241128-204151

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

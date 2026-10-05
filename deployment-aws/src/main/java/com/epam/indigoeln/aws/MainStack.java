package com.epam.indigoeln.aws;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ecr.IRepository;
import software.amazon.awscdk.services.ecr.Repository;
import software.constructs.Construct;

public class MainStack extends Stack {

    public MainStack(Construct scope, String id, StackProps stackProps, String envName, StageParameters props) {
        super(scope, id, stackProps);

        // The `combined` target: the base image only installs bingo and the extensions, so the
        // CREATE DATABASE statements for eln/keycloak/signature/sampleregistration live here.
        IRepository postgresRepo = Repository.fromRepositoryName(this, "ecr-postgres-combined", "indigoeln/postgres-combined");
        // The `*-aws` images: long-running HTTP servers, run by docker-compose on the EC2 instance.
        // The `*-lambda` images are still built by CI but nothing deploys them any more.
        IRepository elnAwsRepo = Repository.fromRepositoryName(this, "ecr-eln-aws", "indigoeln/eln-aws");
        IRepository reportsAwsRepo = Repository.fromRepositoryName(this, "ecr-reports-aws", "indigoeln/reports-aws");
        IRepository signatureAwsRepo = Repository.fromRepositoryName(this, "ecr-signature-aws", "indigoeln/signature-aws");
        IRepository sampleRegistrationAwsRepo = Repository.fromRepositoryName(this, "ecr-sampleregistration-aws", "indigoeln/sampleregistration-aws");

        InfraStack infraStack = new InfraStack(this, new InfraStack.Props(
                envName,
                props.getRegion(),
                props.getVpc(),
                props.getDataVolumeAz(),
                props.getEc2KeyPair(),
                props.getHostedZone(),
                props.getHostedZoneName(),
                props.getSecurityGroups(),
                props.getStorageBucketName(),
                props.getApiGatewaySecret(),
                props.getLambdaSubnets(),
                props.isCreateS3Gateway()
        ));

        CognitoStack cognitoStack = new CognitoStack(this, new CognitoStack.Props(
                props.getDomainName(),
                envName
        ));

        new ComposeStack(this, new ComposeStack.Props(
                envName,
                props.getRegion(),
                "/indigoeln/" + envName + "/compose",
                infraStack.getInstance(),
                infraStack.getEc2Role(),
                infraStack.getStorageBucket(),
                cognitoStack.getUserPool(),
                infraStack.getApiSecret(),
                postgresRepo,
                props.getPostgresImageTag(),
                elnAwsRepo,
                props.getElnAwsImageTag(),
                reportsAwsRepo,
                props.getReportsAwsImageTag(),
                signatureAwsRepo,
                props.getSignatureAwsImageTag(),
                sampleRegistrationAwsRepo,
                props.getSampleRegistrationAwsImageTag()
        ));

        new CloudFrontStack(this, new CloudFrontStack.Props(
                envName,
                infraStack.getHostedZone(),
                infraStack.getInstanceIp(),
                props.getDomainName(),
                infraStack.getApiSecret(),
                cognitoStack.getUserPool(),
                cognitoStack.getUserPoolClient()
        ));
    }
}

package com.epam.indigoeln.aws;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ecr.IRepository;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.rds.Credentials;
import software.constructs.Construct;

public class MainStack extends Stack {

    public MainStack(Construct scope, String id, StackProps stackProps, String envName, StageParameters props) {
        super(scope, id, stackProps);

        IRepository elnLambdaRepo = Repository.fromRepositoryName(this, "ecr-indigo-eln",  "indigoeln/indigo-eln-lambda");
        IRepository reportsLambdaRepo = Repository.fromRepositoryName(this, "ecr-indigo-eln-reports", "indigoeln/indigo-eln-reports-lambda");
        IRepository signatureLambdaRepo = Repository.fromRepositoryName(this, "ecr-indigo-eln-signature", "indigoeln/indigo-eln-signature-lambda");
        IRepository postgresRepo = Repository.fromRepositoryName(this, "ecr-indigo-eln-postgres", "indigoeln/indigo-eln-postgres");

        InfraStack infraStack = new InfraStack(this, new InfraStack.Props(
                props.getVpc(),
                props.getEc2KeyPair(),
                props.getHostedZone(),
                props.getHostedZoneName(),
                props.getSecurityGroups(),
                parameters.getStorageBucketName()
        ));

        PostgresStack postgresStack = new PostgresStack(this, new PostgresStack.Props(
                infraStack.getPrivateDnsNamespace(),
                props.getPostgresMasterUsername(),
                infraStack.getEcsCluster(),
                infraStack.getEc2SecurityGroup(),
                infraStack.getAdditionalSecurityGroups(),
                postgresRepo,
                props.getPostgresImageTag()
        ));

        CognitoStack cognitoStack = new CognitoStack(this, new CognitoStack.Props(
                props.getDomainName(),
                envName
        ));

        ELNLambdaStack elnLambdaStack = new ELNLambdaStack(this, new ELNLambdaStack.Props(
                infraStack.getVpc(),
                infraStack.getEc2SecurityGroup(),
                Credentials.fromSecret(postgresStack.getDbSecret()),
                infraStack.getLambdaSecurityGroup(),
                cognitoStack.getUserPool(),
                cognitoStack.getUserPoolClient(),
                elnLambdaRepo,
                reportsLambdaRepo,
                signatureLambdaRepo,
                props.getLambdaSubnets(),
                props.getElnLambdaImageTag(),
                props.getReportsLambdaImageTag(),
                props.getSignatureLambdaImageTag(),
                props.getApiGatewaySecret(),
                infraStack.getStorageBucket()
        ));

        CloudFrontStack cloudFrontStack = new CloudFrontStack(this, new CloudFrontStack.Props(
                infraStack.getHostedZone(),
                elnLambdaStack.getHttpApi(),
                props.getDomainName(),
                elnLambdaStack.getApiGatewaySecret()
        ));
    }
}

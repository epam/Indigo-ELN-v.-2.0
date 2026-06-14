package com.epam.indigoeln.aws;

import lombok.Value;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ecr.IRepository;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.rds.Credentials;
import software.constructs.Construct;

public class MainStack extends Stack {

    public MainStack(Construct scope, String id, Props props) {
        super(scope, id, props);

        StageParameters parameters = StageParameters.load(props.getEnvName());

        IRepository elnLambdaRepo = Repository.fromRepositoryName(this, "ecr-indigo-eln",  "indigoeln/indigo-eln-lambda");
        IRepository reportsLambdaRepo = Repository.fromRepositoryName(this, "ecr-indigo-eln-reports", "indigoeln/indigo-eln-reports-lambda");
        IRepository signatureLambdaRepo = Repository.fromRepositoryName(this, "ecr-indigo-eln-signature", "indigoeln/indigo-eln-signature-lambda");
        IRepository postgresRepo = Repository.fromRepositoryName(this, "ecr-indigo-eln-postgres", "indigoeln/indigo-eln-postgres");

        InfraStack infraStack = new InfraStack(this, "infra-stack", new InfraStack.Props(
                parameters.getVpc(),
                parameters.getEc2KeyPair(),
                parameters.getHostedZone(),
                parameters.getHostedZoneName(),
                parameters.getSecurityGroups()
        ));

        PostgresStack postgresStack = new PostgresStack(this, "postgres-stack", new PostgresStack.Props(
                infraStack.getPrivateDnsNamespace(),
                parameters.getPostgresMasterUsername(),
                infraStack.getEcsCluster(),
                infraStack.getEc2SecurityGroup(),
                infraStack.getAdditionalSecurityGroups(),
                postgresRepo,
                parameters.getPostgresImageTag()
        ));
        postgresStack.addDependency(infraStack);

        CognitoStack cognitoStack = new CognitoStack(this, "cognito-stack", new CognitoStack.Props(
                parameters.getDomainName(),
                props.getEnvName()
        ));

        ELNLambdaStack elnLambdaStack = new ELNLambdaStack(this, "eln-lambda-stack", new ELNLambdaStack.Props(
                infraStack.getVpc(),
                infraStack.getEc2SecurityGroup(),
                Credentials.fromSecret(postgresStack.getDbSecret()),
                infraStack.getLambdaSecurityGroup(),
                cognitoStack.getUserPool(),
                cognitoStack.getUserPoolClient(),
                elnLambdaRepo,
                reportsLambdaRepo,
                signatureLambdaRepo,
                parameters.getLambdaSubnets(),
                parameters.getElnLambdaImageTag(),
                parameters.getReportsLambdaImageTag(),
                parameters.getSignatureLambdaImageTag(),
                parameters.getApiGatewaySecret()
        ));
        elnLambdaStack.addDependency(infraStack);
        elnLambdaStack.addDependency(postgresStack);

        CloudFrontStack cloudFrontStack = new CloudFrontStack(this, "cloud-formation-stack", new CloudFrontStack.Props(
                infraStack.getHostedZone(),
                elnLambdaStack.getHttpApi(),
                parameters.getDomainName(),
                elnLambdaStack.getApiGatewaySecret()
        ));
        cloudFrontStack.addDependency(infraStack);
        cloudFrontStack.addDependency(elnLambdaStack);
    }

    @Value
    public static class Props implements StackProps {

        String envName;
    }
}

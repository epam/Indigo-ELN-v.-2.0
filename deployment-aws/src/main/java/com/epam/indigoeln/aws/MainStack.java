package com.epam.indigoeln.aws;

import lombok.Value;
import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.rds.Credentials;
import software.constructs.Construct;

public class MainStack extends Stack {

    public MainStack(@Nullable Construct scope, @Nullable String id, @Nullable Props props) {
        super(scope, id, props);

        StageParameters parameters = StageParameters.load(props.getEnvName());

        InfraStack infraStack = new InfraStack(this, "infra-stack", new InfraStack.Props(
                parameters.getVpc(),
                parameters.getEc2KeyPair(),
                parameters.getHostedZone(),
                parameters.getHostedZoneName(),
                parameters.getSecurityGroups()
        ));

        BuildStack buildStack = new BuildStack(this, "build-stack", new BuildStack.Props());

        PostgresStack postgresStack = new PostgresStack(this, "postgres-stack", new PostgresStack.Props(
                infraStack.getPrivateDnsNamespace(),
                parameters.getPostgresMasterUsername(),
                infraStack.getEcsCluster(),
                infraStack.getEc2SecurityGroup(),
                buildStack.getPostgresRepo(),
                parameters.getPostgresImageTag()
        ));
        postgresStack.addDependency(buildStack);
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
                buildStack.getElnLambdaRepo(),
                buildStack.getReportsLambdaRepo(),
                parameters.getLambdaSubnets(),
                parameters.getElnLambdaImageTag(),
                parameters.getReportsLambdaImageTag(),
                parameters.getApiGatewaySecret()
        ));
        elnLambdaStack.addDependency(buildStack);
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

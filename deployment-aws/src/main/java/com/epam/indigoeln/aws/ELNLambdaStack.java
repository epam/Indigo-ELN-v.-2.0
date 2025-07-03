package com.epam.indigoeln.aws;

import lombok.Getter;
import lombok.Value;
import software.amazon.awscdk.*;
import software.amazon.awscdk.aws_apigatewayv2_authorizers.HttpUserPoolAuthorizer;
import software.amazon.awscdk.aws_apigatewayv2_integrations.HttpLambdaIntegration;
import software.amazon.awscdk.services.apigateway.EndpointConfiguration;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.apigatewayv2.*;
import software.amazon.awscdk.services.cognito.IUserPool;
import software.amazon.awscdk.services.cognito.IUserPoolClient;
import software.amazon.awscdk.services.ec2.ISecurityGroup;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.InterfaceVpcEndpoint;
import software.amazon.awscdk.services.ec2.VpcEndpoint;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.eventsources.SqsEventSource;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.rds.Credentials;
import software.amazon.awscdk.services.sqs.DeadLetterQueue;
import software.amazon.awscdk.services.sqs.Queue;
import software.constructs.Construct;

import java.io.File;
import java.util.List;
import java.util.Map;

import static com.epam.indigoeln.aws.Utils.mapOf;

public class ELNLambdaStack extends NestedStack {

    @Getter
    private final Function elnFunction;
    @Getter
    private final HttpApi httpApi;
    @Getter
    private final IHttpRouteAuthorizer httpAuthorizer;

    public ELNLambdaStack(final Construct scope, final String id, final Props props) {
        super(scope, id, props);

        httpApi = HttpApi.Builder.create(this, "http-api")
                .build();
        httpAuthorizer = HttpUserPoolAuthorizer.Builder.create("http-authorizer", props.getUserPool())
                .userPoolClients(List.of(props.getUserPoolClient()))
                .build();

        Map<String, String> elnFunctionEnvironment = mapOf(
                "QUARKUS_DATASOURCE_JDBC_URL", String.format("jdbc:postgresql://%s/%s", "172.31.52.120", props.getDbCredentials().getUsername()) // TODO
                , "QUARKUS_DATASOURCE_USERNAME", props.getDbCredentials().getUsername()
                , "QUARKUS_DATASOURCE_PASSWORD", props.getDbCredentials().getPassword().unsafeUnwrap() // TODO retrieve credentials in lambda code
                , "ELN_COGNITO_USER_POOL_ID", props.getUserPool().getUserPoolId()
//                , "QUARKUS_LOG_LEVEL", "DEBUG"
        );
        File elnBuild = new File("../backend/eln/eln-lambda/build");
        elnFunction = Utils.createQuarkusFunction(
                this,
                props,
                "eln-function",
//                new File(elnBuild, "function.zip"),
                props.getElnRepository(),
                props.getElnImageTag(),
                props.getLambdaSecurityGroup(),
                elnFunctionEnvironment
        );

        httpApi.addRoutes(AddRoutesOptions.builder()
                .path("/api/eln/{proxy+}")
                .integration(HttpLambdaIntegration.Builder.create("eln-api-integration", elnFunction).build())
                .authorizer(httpAuthorizer)
                .build()
        );
        httpApi.addRoutes(AddRoutesOptions.builder()
                .path("/openapi/eln")
                .integration(HttpLambdaIntegration.Builder.create("eln-api-integration", elnFunction).build())
                .build()
        );
        httpApi.addRoutes(AddRoutesOptions.builder()
                .path("/swagger/eln/{proxy+}")
                .integration(HttpLambdaIntegration.Builder.create("eln-api-integration", elnFunction).build())
                .build()
        );
/*
        httpAPI.addRoutes(AddRoutesOptions.builder()
                .path("/api/example/{proxy+}")
                .integration(HttpLambdaIntegration.Builder.create("example-api-integration", exampleFunction).build())
                .authorizer(authorizer)
                .build()
        );
*/
/*
        httpAPI.addRoutes(AddRoutesOptions.builder()
                .path("/api/signature/{proxy+}")
                .integration(HttpLambdaIntegration.Builder.create("signature-api-integration", signatureFunction).build())
                .authorizer(authorizer)
                .build()
        );
*/
//        httpAPI.addRoutes(AddRoutesOptions.builder()
//                .path("/api/print_all")
//                .integration(HttpLambdaIntegration.Builder.create("print-api-integration", printFunction).build())
//                .build()
//        );
//        httpAPI.addRoutes(AddRoutesOptions.builder()
//                .path("/api/print_all_secure")
//                .integration(HttpLambdaIntegration.Builder.create("print-api-integration-secure", printFunction).build())
//                .authorizer(authorizer)
//                .build()
//        );
    }

    @Value
    public static class Props implements NestedStackProps {

        IVpc vpc;
        ISecurityGroup ec2SecurityGroup;
        Credentials dbCredentials;
        ISecurityGroup lambdaSecurityGroup;
        IUserPool userPool;
        IUserPoolClient userPoolClient;
        Repository elnRepository;
        String elnImageTag;
    }
}

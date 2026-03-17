package com.epam.indigoeln.aws;

import com.epam.indigoeln.aws.util.Utils;
import lombok.Getter;
import lombok.Value;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.NestedStack;
import software.amazon.awscdk.NestedStackProps;
import software.amazon.awscdk.aws_apigatewayv2_authorizers.HttpUserPoolAuthorizer;
import software.amazon.awscdk.aws_apigatewayv2_integrations.HttpLambdaIntegration;
import software.amazon.awscdk.services.apigatewayv2.AddRoutesOptions;
import software.amazon.awscdk.services.apigatewayv2.HttpApi;
import software.amazon.awscdk.services.apigatewayv2.IHttpRouteAuthorizer;
import software.amazon.awscdk.services.apigatewayv2.IpAddressType;
import software.amazon.awscdk.services.cognito.IUserPool;
import software.amazon.awscdk.services.cognito.IUserPoolClient;
import software.amazon.awscdk.services.ec2.ISecurityGroup;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.rds.Credentials;
import software.amazon.awscdk.services.ssm.IStringParameter;
import software.amazon.awscdk.services.ssm.StringParameter;
import software.constructs.Construct;

import java.io.File;
import java.util.List;
import java.util.Map;

import static com.epam.indigoeln.aws.util.Utils.mapOf;

public class ELNLambdaStack extends NestedStack {

    @Getter
    private final Function elnFunction;
    @Getter
    private final Function reportsFunction;
    @Getter
    private final HttpApi httpApi;
    @Getter
    private final IHttpRouteAuthorizer httpAuthorizer;
    @Getter
    private final IStringParameter apiGatewaySecret;
    @Getter
    private final IStringParameter internalApiGatewaySecret;

    public ELNLambdaStack(final Construct scope, final String id, final Props props) {
        super(scope, id, props);

        apiGatewaySecret = StringParameter.Builder.create(this, "api-gateway-secret")
                .parameterName("api-gateway-secret")
                .stringValue(props.getApiGatewaySecret())
                .build();
        internalApiGatewaySecret = StringParameter.Builder.create(this, "internal-api-gateway-secret")
                .parameterName("internal-api-gateway-secret")
                .stringValue(props.getInternalApiGatewaySecret())
                .build();

        httpApi = HttpApi.Builder.create(this, "http-api")
                .ipAddressType(IpAddressType.DUAL_STACK)
                .build();
        httpAuthorizer = HttpUserPoolAuthorizer.Builder.create("http-authorizer", props.getUserPool())
                .userPoolClients(List.of(props.getUserPoolClient()))
                .build();

        Map<String, String> elnFunctionEnvironment = mapOf(
                "QUARKUS_DATASOURCE_JDBC_URL", String.format("jdbc:postgresql://%s/%s", props.getDbHostname(), props.getDbCredentials().getUsername())
                , "QUARKUS_DATASOURCE_USERNAME", props.getDbCredentials().getUsername()
                , "QUARKUS_DATASOURCE_PASSWORD", props.getDbCredentials().getPassword().unsafeUnwrap() // TODO retrieve credentials in lambda code
                , "ELN_COGNITO_USER_POOL_ID", props.getUserPool().getUserPoolId()
                , "ELN_API_SECRET", apiGatewaySecret.getStringValue()
                , "ELN_INTERNAL_API_SECRET", internalApiGatewaySecret.getStringValue()
                , "QUARKUS_REST_CLIENT_REPORTS_API_URL", httpApi.getApiEndpoint()
                , "QUARKUS_REST_CLIENT_LOGGING_SCOPE", "request-response"
                , "QUARKUS_REST_CLIENT_LOGGING_BODY_LIMIT", "9999"
                , "QUARKUS_REST_CLIENT_EXTENSIONS_API_SCOPE", "all"
                , "QUARKUS_LOG_LEVEL", "INFO"
                , "QUARKUS_LOG_CATEGORY__COM_EPAM__LEVEL", "DEBUG"
        );
        elnFunction = Utils.createNativeFunction(
                this,
                props,
                "eln-function",
                props.getElnRepository(),
                props.getElnImageTag(),
                props.getLambdaSecurityGroup(),
                elnFunctionEnvironment
        );
        props.getUserPool().grant(elnFunction.getRole(),
                "cognito-idp:AdminCreateUser",
                "cognito-idp:AdminSetUserPassword",
                "cognito-idp:AdminUpdateUserAttributes"
        );

        Map<String, String> reportsFunctionEnvironment = mapOf(
                "ELN_API_SECRET", internalApiGatewaySecret.getStringValue()
//                , "QUARKUS_LOG_LEVEL", "DEBUG"
        );
        reportsFunction = Utils.createSnapStartFunction(
                this,
                props,
                "reports-function",
                new File("../backend/reports/reports-lambda/build/function.zip"),
//                props.getReportsRepository(),
//                props.getReportsImageTag(),
                props.getLambdaSecurityGroup(),
                reportsFunctionEnvironment
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
        httpApi.addRoutes(AddRoutesOptions.builder()
                .path("/internalapi/reports/{proxy+}")
                .integration(HttpLambdaIntegration.Builder.create("reports-api-integration", reportsFunction)
                        .timeout(Duration.seconds(29))
                        .build())
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
        String dbHostname;
        Credentials dbCredentials;
        ISecurityGroup lambdaSecurityGroup;
        IUserPool userPool;
        IUserPoolClient userPoolClient;
        Repository elnRepository;
        Repository reportsRepository;
        List<String> lambdaSubnets;
        String elnImageTag;
        String reportsImageTag;
        String apiGatewaySecret;
        String internalApiGatewaySecret;
    }
}

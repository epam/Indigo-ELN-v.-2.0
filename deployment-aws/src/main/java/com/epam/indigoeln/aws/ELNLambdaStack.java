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

import java.util.List;
import java.util.Map;

import static com.epam.indigoeln.aws.util.Utils.entry;
import static com.epam.indigoeln.aws.util.Utils.mapOf;

public class ELNLambdaStack extends NestedStack {

    @Getter
    private final Function reportsFunction;
    @Getter
    private final HttpApi httpApi;
    @Getter
    private final IHttpRouteAuthorizer httpAuthorizer;
    @Getter
    private final IStringParameter apiGatewaySecret;

    public ELNLambdaStack(final Construct scope, final String id, final Props props) {
        super(scope, id, props);

        apiGatewaySecret = StringParameter.Builder.create(this, "api-gateway-secret")
                .parameterName("api-gateway-secret")
                .stringValue(props.getApiGatewaySecret())
                .build();

        httpApi = HttpApi.Builder.create(this, "http-api")
                .ipAddressType(IpAddressType.DUAL_STACK)
                .build();
        httpAuthorizer = HttpUserPoolAuthorizer.Builder.create("http-authorizer", props.getUserPool())
                .userPoolClients(List.of(props.getUserPoolClient()))
                .build();

        Map<String, String> elnFunctionEnvironment = mapOf(
                entry("QUARKUS_DATASOURCE_JDBC_URL", String.format("jdbc:postgresql://pgbouncer.indigoeln.local:6432/%s", props.getDbCredentials().getUsername())),
                entry("QUARKUS_DATASOURCE_USERNAME", props.getDbCredentials().getUsername()),
                entry("QUARKUS_DATASOURCE_PASSWORD", props.getDbCredentials().getPassword().unsafeUnwrap()), // TODO retrieve credentials in lambda code
                entry("ELN_COGNITO_USER_POOL_ID", props.getUserPool().getUserPoolId()),
                entry("ELN_API_SECRET", apiGatewaySecret.getStringValue()),
                entry("QUARKUS_REST_CLIENT_REPORTS_API_URL", httpApi.getApiEndpoint()),
                entry("QUARKUS_REST_CLIENT_SIGNATURE_API_URL", httpApi.getApiEndpoint()),
                entry("QUARKUS_REST_CLIENT_LOGGING_SCOPE", "request-response"),
                entry("QUARKUS_REST_CLIENT_LOGGING_BODY_LIMIT", "9999"),
                entry("QUARKUS_REST_CLIENT_EXTENSIONS_API_SCOPE", "all"),
                entry("QUARKUS_LOG_LEVEL", "INFO"),
                entry("QUARKUS_LOG_CATEGORY__COM_EPAM__LEVEL", "DEBUG")
        );
        Function elnFunction = Utils.createDockerFunction(
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
                entry("JAVA_TOOL_OPTIONS", "-XX:+TieredCompilation -XX:TieredStopAtLevel=1"),
                entry("ELN_API_SECRET", apiGatewaySecret.getStringValue()),
                entry("QUARKUS_LOG_LEVEL", "INFO"),
                entry("QUARKUS_LOG_CATEGORY__COM_EPAM__LEVEL", "DEBUG")
        );
        reportsFunction = Utils.createDockerFunction(
                this,
                props,
                "reports-function",
                props.getReportsRepository(),
                props.getReportsImageTag(),
                props.getLambdaSecurityGroup(),
                reportsFunctionEnvironment
        );

        Map<String, String> signatureFunctionEnvironment = mapOf(
                entry("QUARKUS_DATASOURCE_JDBC_URL", String.format("jdbc:postgresql://pgbouncer.indigoeln.local:6433/%s", "signature")),
                entry("QUARKUS_DATASOURCE_USERNAME", props.getDbCredentials().getUsername()),
                entry("QUARKUS_DATASOURCE_PASSWORD", props.getDbCredentials().getPassword().unsafeUnwrap()), // TODO retrieve credentials in lambda code
                entry("ELN_COGNITO_USER_POOL_ID", props.getUserPool().getUserPoolId()),
                entry("ELN_API_SECRET", apiGatewaySecret.getStringValue()),
                entry("ELN_SIGNATURE_KEYSTORE_PASSWORD", "1234"),
                entry("QUARKUS_REST_CLIENT_ELN_INTERNAL_API_URL", httpApi.getApiEndpoint()),
                entry("QUARKUS_REST_CLIENT_LOGGING_SCOPE", "request-response"),
                entry("QUARKUS_REST_CLIENT_LOGGING_BODY_LIMIT", "9999"),
                entry("QUARKUS_REST_CLIENT_EXTENSIONS_API_SCOPE", "all"),
                entry("QUARKUS_LOG_LEVEL", "INFO"),
                entry("QUARKUS_LOG_CATEGORY__COM_EPAM__LEVEL", "DEBUG")
        );
        Function signatureFunction = Utils.createDockerFunction(
                this,
                props,
                "signature-function",
                props.getSignatureRepository(),
                props.getSignatureImageTag(),
                props.getLambdaSecurityGroup(),
                signatureFunctionEnvironment
        );

        httpApi.addRoutes(AddRoutesOptions.builder()
                .path("/api/eln/{proxy+}")
                .integration(HttpLambdaIntegration.Builder.create("eln-api-integration", elnFunction).build())
                .authorizer(httpAuthorizer)
                .build()
        );
        httpApi.addRoutes(AddRoutesOptions.builder()
                .path("/internalapi/eln/{proxy+}")
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
        httpApi.addRoutes(AddRoutesOptions.builder()
                .path("/api/signature/{proxy+}")
                .integration(HttpLambdaIntegration.Builder.create("signature-api-integration", signatureFunction)
                        .timeout(Duration.seconds(29))
                        .build())
                .authorizer(httpAuthorizer)
                .build()
        );
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
        Repository reportsRepository;
        Repository signatureRepository;
        List<String> lambdaSubnets;
        String elnImageTag;
        String reportsImageTag;
        String signatureImageTag;
        String apiGatewaySecret;
    }
}

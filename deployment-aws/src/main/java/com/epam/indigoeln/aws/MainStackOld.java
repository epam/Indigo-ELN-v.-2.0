package com.epam.indigoeln.aws;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.ISecurityGroup;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.iam.IManagedPolicy;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

import java.io.File;
import java.util.List;

public class MainStackOld extends Stack {
    public MainStackOld(final Construct scope, final String id) {
        this(scope, id, null);
    }

    IVpc vpc;
    IManagedPolicy permissionsBoundary;
    SecurityGroup lambdaSecurityGroup;
    SecurityGroup dbSecurityGroup;
    ISecurityGroup group1;
    ISecurityGroup group2;

    public MainStackOld(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        permissionsBoundary = ManagedPolicy.fromManagedPolicyArn(this, "role-boundary", "arn:aws:iam::657609648574:policy/eo_role_boundary");

//        dbSecurityGroup.addIngressRule(dbSecurityGroup, Port.tcp(5432), "from-db-proxy");


//
//        DatabaseCluster auroraCluster = DatabaseCluster.Builder.create(this, "db-cluster")
//                .engine(DatabaseClusterEngine.auroraPostgres(AuroraPostgresClusterEngineProps.builder().version(AuroraPostgresEngineVersion.VER_16_6).build()))
//                .credentials(Credentials.fromSecret(dbSecret))
//                .vpc(vpc)
//                .vpcSubnets(SubnetSelection.builder().subnetType(SubnetType.PRIVATE_ISOLATED).build())
//                .securityGroups(List.of(dbSecurityGroup, group1, group2)) // group1/2 required to be able to connect from EC2 via SSH
//                .serverlessV2MinCapacity(0)
//                .serverlessV2MaxCapacity(1)
//                .writer(ClusterInstance.serverlessV2("db-writer",
//                        ServerlessV2ClusterInstanceProps.builder()
//                                .publiclyAccessible(false)
//                                .build())
//                )
//                .parameters(Map.of("rds.force_ssl", "1"))
//                .storageEncrypted(true)
//                .build();
//        DatabaseProxy auroraProxy = DatabaseProxy.Builder.create(this, "db-proxy")
//                .vpc(vpc)
//                .vpcSubnets(SubnetSelection.builder().subnetType(SubnetType.PRIVATE_ISOLATED).build())
//                .secrets(List.of(dbSecret))
//                .securityGroups(List.of(dbSecurityGroup, group1, group2))
//                .proxyTarget(ProxyTarget.fromCluster(auroraCluster))
//                .build();
//        String endpoint = auroraProxy.getEndpoint(); //auroraCluster.getClusterEndpoint().getSocketAddress();

//        Map<String, String> exampleFunctionEnvironment = Map.of(
//                "QUARKUS_DATASOURCE_JDBC_URL", String.format("jdbc:postgresql://%s/postgres", endpoint),
//                "QUARKUS_DATASOURCE_USERNAME", Credentials.fromSecret(dbSecret).getUsername(),
//                "QUARKUS_DATASOURCE_PASSWORD", Credentials.fromSecret(dbSecret).getPassword().unsafeUnwrap() // TODO retrieve credentials in lambda code
//                , "QUARKUS_LOG_LEVEL", "DEBUG"
//        );
//        File exampleBuild = new File("../example/example-lambda/build");
//        Function exampleFunction = createQuarkusFunction(
//                "example-function",
//                List.of(new File(exampleBuild, "function-base.zip"), new File(exampleBuild, "function-project.zip")),
//                new File(exampleBuild, "function-module.zip"),
//                List.of(),
//                new File(exampleBuild, "function-native.zip"),
//                new File(exampleBuild, "function.zip"),
//                exampleFunctionEnvironment
//        );
//

//        Map<String, String> signatureFunctionEnvironment = Map.of(
//                "QUARKUS_DATASOURCE_JDBC_URL", String.format("jdbc:postgresql://%s/postgres", endpoint),
//                "QUARKUS_DATASOURCE_USERNAME", Credentials.fromSecret(dbSecret).getUsername(),
//                "QUARKUS_DATASOURCE_PASSWORD", Credentials.fromSecret(dbSecret).getPassword().unsafeUnwrap() // TODO retrieve credentials in lambda code
//                , "QUARKUS_LOG_LEVEL", "DEBUG"
//        );
        File signatureBuild = new File("../signature/signature-lambda/build");
/*
        Function signatureFunction = createQuarkusFunction(
                "signature-function",
//                List.of(new File(signatureBuild, "function-base.zip"), new File(signatureBuild, "function-project.zip")),
//                new File(signatureBuild, "function-module.zip"),
                List.of(),
                new File(signatureBuild, "function.zip"),
                signatureFunctionEnvironment
        );
*/
        Function printFunction = Function.Builder.create(this, "print-function")
                .vpc(vpc)
                .allowPublicSubnet(true)
                .securityGroups(List.of(lambdaSecurityGroup))
                .runtime(Runtime.PYTHON_3_13)
                .code(Code.fromAsset("print_all"))
                .handler("print_all.lambda_handler")
                .role(Role.Builder.create(this, "print-function-role")
                        .assumedBy(ServicePrincipal.fromStaticServicePrincipleName("lambda.amazonaws.com"))
                        .permissionsBoundary(permissionsBoundary)
                        .managedPolicies(List.of(
                                ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSLambdaBasicExecutionRole"),
                                ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSLambdaVPCAccessExecutionRole")
                        ))
                        .build()
                )
                .build();
    }
}

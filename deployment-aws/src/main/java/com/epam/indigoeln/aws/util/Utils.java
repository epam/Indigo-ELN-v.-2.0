package com.epam.indigoeln.aws.util;

import com.epam.indigoeln.aws.ELNLambdaStack;
import org.jspecify.annotations.Nullable;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.ec2.ISecurityGroup;
import software.amazon.awscdk.services.ec2.SubnetFilter;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.s3.assets.AssetOptions;
import software.constructs.Construct;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;

public class Utils {

//    public static final Map<String, String> FUNCTION_IMAGE_URIS = new ConcurrentHashMap<>();
//
    public static String calculateHashCode(File location) {
        try {
            List<File> files = location.isDirectory()
                    ? Files.walk(location.toPath(), FileVisitOption.FOLLOW_LINKS).map(Path::toFile).toList()
                    : List.of(location);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (File file : files) {
                baos.write(file.getAbsolutePath().getBytes(StandardCharsets.UTF_8));
                if (file.isFile()) {
                    try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                        baos.write(is.readAllBytes());
                    }
                }
            }
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(baos.toByteArray());
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Function createDockerFunction(Construct parent, ELNLambdaStack.Props props, String id, Repository repository, String imageTag, ISecurityGroup securityGroup, Map<String, String> environment) {
        return doCreateFunction(parent, props, id, null, repository, imageTag, securityGroup, environment);
    }

    public static Function createSnapStartFunction(Construct parent, ELNLambdaStack.Props props, String id, File functionCode, ISecurityGroup securityGroup, Map<String, String> environment) {
        environment = new LinkedHashMap<>(environment);
        environment.putIfAbsent("JAVA_TOOL_OPTIONS", "-XX:+TieredCompilation -XX:TieredStopAtLevel=1");
        return doCreateFunction(parent, props, id, functionCode, null, null, securityGroup, environment);
    }

    public static Function createSnapStartFunction(Construct parent, ELNLambdaStack.Props props, String id, Repository repository, String imageTag, ISecurityGroup securityGroup, Map<String, String> environment) {
        environment = new LinkedHashMap<>(environment);
        environment.putIfAbsent("JAVA_TOOL_OPTIONS", "-XX:+TieredCompilation -XX:TieredStopAtLevel=1");
        return doCreateFunction(parent, props, id, null, repository, imageTag, securityGroup, environment);
    }

    public static Function doCreateFunction(Construct parent, ELNLambdaStack.Props props, String id, @Nullable File functionCode, @Nullable Repository repository, @Nullable String imageTag, ISecurityGroup securityGroup, Map<String, String> environment) {
        LogGroup logGroup = LogGroup.Builder.create(parent, id + "-log-group")
                .logGroupName("/aws/lambda/" + id)
                .removalPolicy(RemovalPolicy.DESTROY)
                .retention(RetentionDays.ONE_MONTH)
                .build();
        Function.Builder builder = Function.Builder.create(parent, id)
                .vpc(props.getVpc())
                .vpcSubnets(SubnetSelection.builder()
                        .subnetFilters(List.of(SubnetFilter.byIds(props.getLambdaSubnets())))
                        .build()
                )
                .ipv6AllowedForDualStack(true)
                .securityGroups(List.of(securityGroup))
                .role(Role.Builder.create(parent, id + "-role")
                                .assumedBy(ServicePrincipal.fromStaticServicePrincipleName("lambda.amazonaws.com"))
                                .managedPolicies(List.of(
                                        ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSLambdaBasicExecutionRole"),
                                        ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSLambdaVPCAccessExecutionRole")
                                ))
                                .build()
                )
                .environment(environment)
                .memorySize(1024)
                .timeout(Duration.seconds(120))
                .currentVersionOptions(VersionOptions.builder().removalPolicy(RemovalPolicy.DESTROY).build())
                .tracing(Tracing.ACTIVE)
                .logGroup(logGroup);
        if (functionCode != null) {
            builder
                .runtime(Runtime.JAVA_21)
                .handler("io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest")
//                .code(Code.fromAsset(functionCode.getPath(), AssetOptions.builder().assetHash(Utils.calculateHashCode(functionCode)).build()))
                .code(Code.fromAsset(functionCode.getPath(), AssetOptions.builder().assetHash("2").build())) // !!! to avoid redeploy on every change
                .snapStart(SnapStartConf.ON_PUBLISHED_VERSIONS);
        } else if (repository != null && imageTag != null) {
            builder
                .runtime(Runtime.FROM_IMAGE)
                .handler(Handler.FROM_IMAGE)
                .code(Code.fromEcrImage(repository, EcrImageCodeProps.builder().tagOrDigest(imageTag).build()));
        } else {
            throw new IllegalArgumentException("either functionCode or repository/imageTag must be provided");
        }
        return builder.build();
    }

    // Map.of(...) may mix the order of elements, forcing CloudFormation to do unnecessary updates; so stick to LinkedHashMap

    public static <K, V> Map<K, V> mapOf() {
        return new LinkedHashMap<>();
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1) {
        Map<K, V> map = mapOf();
        map.put(k1, v1);
        return map;
    }

    @SafeVarargs
    public static <K, V> Map<K, V> mapOf(Map.Entry<K, V>... entries) {
        Map<K, V> map = mapOf();
        for (Map.Entry<K, V> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    public static <K, V> Map.Entry<K, V> entry(K k, V v) {
        return new AbstractMap.SimpleImmutableEntry<>(k, v);
    }
}

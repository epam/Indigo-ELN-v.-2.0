package com.epam.indigoeln.aws;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
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

    public static Function createQuarkusFunction(Construct parent, ELNLambdaStack.Props props, String id, /*File functionCode, */Repository repository, String imageTag, ISecurityGroup securityGroup, Map<String, String> environment) {
//        environment = new LinkedHashMap<>(environment);
//        environment.putIfAbsent("JAVA_TOOL_OPTIONS", "-XX:+TieredCompilation -XX:TieredStopAtLevel=1");
        LogGroup logGroup = LogGroup.Builder.create(parent, id + "-log-group")
                .logGroupName("/aws/lambda/" + id)
                .removalPolicy(RemovalPolicy.DESTROY)
                .retention(RetentionDays.ONE_MONTH)
                .build();
        return Function.Builder.create(parent, id)
                .vpc(props.getVpc())
                .vpcSubnets(SubnetSelection.builder()
                        .subnetFilters(List.of(SubnetFilter.byIds(props.getLambdaSubnets())))
                        .build()
                )
                .ipv6AllowedForDualStack(true)
                .securityGroups(List.of(securityGroup))
                .runtime(Runtime.FROM_IMAGE)
                .handler(Handler.FROM_IMAGE)
                .code(Code.fromEcrImage(repository, EcrImageCodeProps.builder().tagOrDigest(imageTag).build()))
//                .runtime(Runtime.PROVIDED_AL2023)
//                .handler("ignored")
//                .code(Code.fromAsset(functionCode.getPath(), AssetOptions.builder().assetHash(Utils.calculateHashCode(functionCode)).build()))
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
                .logGroup(logGroup)
                .build();
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

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
        Map<K, V> map = mapOf(k1, v1);
        map.put(k2, v2);
        return map;
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K, V> map = mapOf(k1, v1, k2, v2);
        map.put(k3, v3);
        return map;
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        Map<K, V> map = mapOf(k1, v1, k2, v2, k3, v3);
        map.put(k4, v4);
        return map;
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        Map<K, V> map = mapOf(k1, v1, k2, v2, k3, v3, k4, v4);
        map.put(k5, v5);
        return map;
    }
}

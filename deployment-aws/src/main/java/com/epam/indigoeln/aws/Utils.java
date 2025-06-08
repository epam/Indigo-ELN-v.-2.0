package com.epam.indigoeln.aws;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.ec2.ISecurityGroup;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.*;
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

    public static Function createQuarkusFunction(Construct parent, ELNLambdaStack.Props props, String id, File functionCode, ISecurityGroup securityGroup, Map<String, String> environment) {
        environment = new HashMap<>(environment);
//        environment.putIfAbsent("JAVA_TOOL_OPTIONS", "-XX:+TieredCompilation -XX:TieredStopAtLevel=1");
        return Function.Builder.create(parent, id)
                .vpc(props.getVpc())
                .allowPublicSubnet(true)
                .securityGroups(List.of(securityGroup))
                .runtime(Runtime.PROVIDED_AL2023)
                .code(Code.fromAsset(functionCode.getPath(), AssetOptions.builder().assetHash(Utils.calculateHashCode(functionCode)).build()))
                .handler("ignored") // not used in Provided runtime
                .role(Role.Builder.create(parent, id + "-role")
                                .assumedBy(ServicePrincipal.fromStaticServicePrincipleName("lambda.amazonaws.com"))
                                .managedPolicies(List.of(
                                        ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSLambdaBasicExecutionRole"),
                                        ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSLambdaVPCAccessExecutionRole")
                                ))
                                .build()
                )
                .environment(environment)
                .memorySize(1024) // !!! 128
                .timeout(Duration.seconds(120)) // !!! 15 seconds
                .currentVersionOptions(VersionOptions.builder().removalPolicy(RemovalPolicy.DESTROY).build())
                .tracing(Tracing.ACTIVE)
                .build();
    }

    // Map.of(...) may mix the order of elements, forcing CloudFormation to do unnecessary updates; so stick to LinkedHashMap
    public static <K, V> Map<K, V> mapOf() {
        return Map.of();
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1) {
        Map<K, V> map = new LinkedHashMap<>();
        map.put(k1, v1);
        return map;
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
        Map<K, V> map = new LinkedHashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K, V> map = new LinkedHashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return map;
    }
}

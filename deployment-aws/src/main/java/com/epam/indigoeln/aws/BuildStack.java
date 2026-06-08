package com.epam.indigoeln.aws;

import com.epam.indigoeln.aws.util.Utils;
import lombok.Value;
import one.util.streamex.EntryStream;
import org.jspecify.annotations.Nullable;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.codebuild.*;
import software.amazon.awscdk.services.ecr.CfnPublicRepository;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecr.TagMutability;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.s3.Bucket;
import software.constructs.Construct;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.epam.indigoeln.aws.util.Utils.entry;
import static com.epam.indigoeln.aws.util.Utils.mapOf;

public class BuildStack extends Stack {

    public BuildStack(final Construct scope, final String id, Props props) {
        super(scope, id);

        Repository elnLambdaRepo = createECRRepo("ecr-indigo-eln", "indigoeln/indigo-eln-lambda");
        Repository reportsLambdaRepo = createECRRepo("ecr-indigo-eln-reports", "indigoeln/indigo-eln-reports-lambda");
        Repository signatureLambdaRepo = createECRRepo("ecr-indigo-eln-signature", "indigoeln/indigo-eln-signature-lambda");
        Repository postgresRepo = createECRRepo("ecr-indigo-eln-postgres", "indigoeln/indigo-eln-postgres");

        Bucket buildLogsBucket = Bucket.Builder.create(this, "build-logs-bucket")
                .build();

        PolicyStatement ecrPublicPermissions = PolicyStatement.Builder.create()
                .effect(Effect.ALLOW)
                .actions(Arrays.asList(
                        "ecr-public:GetAuthorizationToken",
                        "ecr-public:BatchCheckLayerAvailability",
                        "ecr-public:GetRepositoryPolicy",
                        "ecr-public:DescribeRepositories",
                        "ecr-public:DescribeImages",
                        "ecr-public:InitiateLayerUpload",
                        "ecr-public:UploadLayerPart",
                        "ecr-public:CompleteLayerUpload",
                        "ecr-public:PutImage",
                        "sts:GetServiceBearerToken"
                ))
                .resources(List.of("*")) // ECR Public GetAuthorizationToken requires resource "*"
                .build();

        Project postgresBuild = createBuild("eln-postgres-build"
                , "indigo-eln-postgres-build"
                , "deployment-aws/codebuild/eln-build-postgres.yaml"
                , buildLogsBucket
                , ecrPublicPermissions
                , mapOf(
                        entry("REGISTRY_URI", postgresRepo.getRegistryUri()),
                        entry("REPO_URI", postgresRepo.getRepositoryUri()),
                        entry("REPO_URI_PUBLIC", "public.ecr.aws/m5k0g6n7/indigoeln/indigo-eln-postgres")
                )
        );
        postgresRepo.grantPullPush(postgresBuild);

        Project elnBuild = createBuild("eln-build"
                , "indigo-eln-build"
                , "deployment-aws/codebuild/eln-build.yaml"
                , buildLogsBucket
                , ecrPublicPermissions
                , mapOf(
                        entry("BUILD_ELN_LAMBDA", "true"),
                        entry("BUILD_REPORTS_LAMBDA", "false"),
                        entry("BUILD_SIGNATURE_LAMBDA", "false"),
                        entry("ELN_REGISTRY_URI", elnLambdaRepo.getRegistryUri()),
                        entry("ELN_REPO_URI", elnLambdaRepo.getRepositoryUri()),
                        entry("REPORTS_REGISTRY_URI", reportsLambdaRepo.getRegistryUri()),
                        entry("REPORTS_REPO_URI", reportsLambdaRepo.getRepositoryUri()),
                        entry("SIGNATURE_REGISTRY_URI", signatureLambdaRepo.getRegistryUri()),
                        entry("SIGNATURE_REPO_URI", signatureLambdaRepo.getRepositoryUri()),
                        entry("S3_LOGS", buildLogsBucket.getBucketName())
                )
        );
        elnLambdaRepo.grantPullPush(elnBuild);
        reportsLambdaRepo.grantPullPush(elnBuild);
        signatureLambdaRepo.grantPullPush(elnBuild);

        Utils.applyPermissionBoundary(this, props.getPermissionBoundary());
    }

    private Project createBuild(String id, String projectName, String buildSpecFile, Bucket buildLogsBucket, PolicyStatement policy, Map<String, String> environment) {
        Project project = Project.Builder.create(this, id)
                .projectName(projectName)
                .source(Source.gitHub(GitHubSourceProps.builder()
                        .owner("epam")
                        .repo("Indigo-ELN-v.-2.0")
                        .branchOrRef("3.0")
                        .webhook(false)
                        .build()))
                .environment(BuildEnvironment.builder()
                        .buildImage(LinuxBuildImage.STANDARD_7_0)
                        .computeType(ComputeType.LARGE)
                        .privileged(true) // The 'privileged' flag is required for the CodeBuild project to build Docker images.
                        .build())
                .environmentVariables(EntryStream.of(environment)
                                .mapValues(v -> BuildEnvironmentVariable.builder().value(v).build())
                                .toCustomMap(LinkedHashMap::new)
                )
                // The BuildSpec defines the commands to run during the build.
                .buildSpec(BuildSpec.fromSourceFilename(buildSpecFile))
                .timeout(Duration.minutes(30))
                .logging(LoggingOptions.builder()
                        .cloudWatch(CloudWatchLoggingOptions.builder().enabled(false).build())
                        .s3(S3LoggingOptions.builder().enabled(true).bucket(buildLogsBucket).prefix("backend").build())
                        .build())
                .build();
        project.addToRolePolicy(policy);
        return project;
    }

    private Repository createECRRepo(String id, String repositoryName) {
        Repository repo = Repository.Builder.create(this, id)
                .repositoryName(repositoryName)
                .imageTagMutability(TagMutability.MUTABLE)
                .build();
        repo.applyRemovalPolicy(RemovalPolicy.RETAIN);
        repo.addToResourcePolicy(PolicyStatement.Builder.create()
                .sid("LambdaECRImageRetrievalPolicy")
                .effect(Effect.ALLOW)
                .principals(List.of(new ServicePrincipal("lambda.amazonaws.com")))
                .actions(List.of(
                        "ecr:BatchGetImage",
                        "ecr:GetDownloadUrlForLayer"
                ))
                .conditions(Map.of("StringLike", Map.of(
                        "aws:sourceArn", "arn:aws:lambda:" + getRegion() + ":" + getAccount() + ":function:*"
                )))
                .build());
        CfnPublicRepository publicRepo = CfnPublicRepository.Builder.create(this, id + "-public")
                .repositoryName(repositoryName)
                .build();
        publicRepo.applyRemovalPolicy(RemovalPolicy.RETAIN);
        return repo;
    }

    @Value
    public static class Props implements StackProps {

        @Nullable
        String permissionBoundary;
    }
}

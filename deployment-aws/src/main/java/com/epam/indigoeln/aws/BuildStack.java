package com.epam.indigoeln.aws;

import lombok.Getter;
import lombok.Value;
import one.util.streamex.EntryStream;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.NestedStack;
import software.amazon.awscdk.NestedStackProps;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.codebuild.*;
import software.amazon.awscdk.services.ecr.CfnPublicRepository;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecr.TagMutability;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.s3.Bucket;
import software.constructs.Construct;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BuildStack extends NestedStack {

    @Getter
    Repository elnLambdaRepo;
    @Getter
    Repository postgresRepo;

    public BuildStack(final Construct scope, final String id, final Props props) {
        super(scope, id, props);

        elnLambdaRepo = createECRRepo("ecr-indigo-eln", "indigoeln/indigo-eln-lambda");
        postgresRepo = createECRRepo("ecr-indigo-eln-postgres", "indigoeln/indigo-eln-postgres");

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
                , postgresRepo
                , "deployment-aws/codebuild/eln-build-postgres.yaml"
                , buildLogsBucket
                , ecrPublicPermissions
                , Utils.mapOf(
                        "REGISTRY_URI", postgresRepo.getRegistryUri(),
                        "REPO_URI", postgresRepo.getRepositoryUri(),
                        "REPO_URI_PUBLIC", "public.ecr.aws/m5k0g6n7/indigoeln/indigo-eln-postgres"
                )
        );
        postgresRepo.grantPullPush(postgresBuild);

        Project elnBuild = createBuild("eln-build"
                , "indigo-eln-build"
                , elnLambdaRepo
                , "deployment-aws/codebuild/eln-build.yaml"
                , buildLogsBucket
                , ecrPublicPermissions
                , Utils.mapOf(
                        "REGISTRY_URI", elnLambdaRepo.getRegistryUri(),
                        "ELN_REPO_URI", elnLambdaRepo.getRepositoryUri()
                )
        );
        elnLambdaRepo.grantPullPush(elnBuild);
    }

    private Project createBuild(String id, String projectName, Repository repository, String buildSpecFile, Bucket buildLogsBucket, PolicyStatement policy, Map<String, Object> environment) {
        Project project = Project.Builder.create(this, id)
                .projectName(projectName)
                .source(Source.gitHub(GitHubSourceProps.builder()
                        .owner("epam")
                        .repo("Indigo-ELN-v.-2.0")
                        .branchOrRef("backend-v3")
                        .webhook(false)
                        .build()))
                .environment(BuildEnvironment.builder()
                        .buildImage(LinuxBuildImage.STANDARD_7_0)
                        .computeType(ComputeType.LARGE)
                        .privileged(true) // The 'privileged' flag is required for the CodeBuild project to build Docker images.
                        .build())
                .environmentVariables(EntryStream.of(environment)
                                .mapValues(v -> BuildEnvironmentVariable.builder().value(v.toString()).build())
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
        CfnPublicRepository privateRepo = CfnPublicRepository.Builder.create(this, id + "-public")
                .repositoryName(repositoryName)
                .build();
        repo.applyRemovalPolicy(RemovalPolicy.RETAIN);
        return repo;
    }

    @Value
    public static class Props implements NestedStackProps {
    }
}

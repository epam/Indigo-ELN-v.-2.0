package com.epam.indigoeln.aws;

import com.google.common.base.MoreObjects;
import lombok.Getter;
import lombok.Value;
import org.jspecify.annotations.Nullable;
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
import java.util.List;

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

        createBuild("eln-postgres-build"
                , "indigo-eln-postgres-build"
                , postgresRepo
                , "public.ecr.aws/m5k0g6n7/indigoeln/indigo-eln-postgres"
                , "deployment-aws/codebuild/eln-build-postgres.yaml"
                , buildLogsBucket
                , ecrPublicPermissions
        );

        createBuild("eln-build"
                , "indigo-eln-build"
                , elnLambdaRepo
                , null
                , "deployment-aws/codebuild/eln-build.yaml"
                , buildLogsBucket
                , ecrPublicPermissions
        );
    }

    private Project createBuild(String id, String projectName, Repository repository, @Nullable String publicRepo, String buildSpecFile, Bucket buildLogsBucket, PolicyStatement policy) {
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
                .environmentVariables(Utils.mapOf(
                        "REGISTRY_URI", BuildEnvironmentVariable.builder().value(repository.getRegistryUri()).build(),
                        "REPO_URI", BuildEnvironmentVariable.builder().value(repository.getRepositoryUri()).build(),
                        "REPO_URI_PUBLIC", BuildEnvironmentVariable.builder().value(MoreObjects.firstNonNull(publicRepo, "")).build()
                ))
                // The BuildSpec defines the commands to run during the build.
                .buildSpec(BuildSpec.fromSourceFilename(buildSpecFile))
                .timeout(Duration.minutes(30))
                .logging(LoggingOptions.builder()
                        .cloudWatch(CloudWatchLoggingOptions.builder().enabled(false).build())
                        .s3(S3LoggingOptions.builder().enabled(true).bucket(buildLogsBucket).prefix("backend").build())
                        .build())
                .build();
        project.addToRolePolicy(policy);
        repository.grantPullPush(project);
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

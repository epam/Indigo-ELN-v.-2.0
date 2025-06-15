package com.epam.indigoeln.aws;

import lombok.Getter;
import lombok.Value;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.NestedStack;
import software.amazon.awscdk.NestedStackProps;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.codebuild.*;
import software.amazon.awscdk.services.ecr.CfnPublicRepository;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.s3.Bucket;
import software.constructs.Construct;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BuildStack extends NestedStack {

    @Getter
    String elnLambdaRepoName;
    @Getter
    String postgresRepoName;

    public BuildStack(final Construct scope, final String id, final Props props) {
        super(scope, id, props);

        elnLambdaRepoName = createECRRepo("ecr-indigo-eln", "indigoeln/indigo-eln-lambda");
        postgresRepoName = createECRRepo("ecr-indigo-eln-postgres", "indigoeln/indigo-eln-postgres");

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
                , "public.ecr.aws/m5k0g6n7/indigoeln/indigo-eln-postgres"
                , "deployment-aws/codebuild/eln-build-postgres.yaml"
                , buildLogsBucket
                , ecrPublicPermissions
        );

        createBuild("eln-build"
                , "indigo-eln-build"
                , "public.ecr.aws/m5k0g6n7/indigoeln/indigo-eln-lambda"
                , "deployment-aws/codebuild/eln-build.yaml"
                , buildLogsBucket
                , ecrPublicPermissions
        );
    }

    private Project createBuild(String id, String projectName, String repositoryURL, String buildSpecFile, Bucket buildLogsBucket, PolicyStatement policy) {
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
                // Pass the ECR repository URI as an environment variable to the build.
                .environmentVariables(Map.of(
                        // TODO read created repo URL
                        "REPO_URI", BuildEnvironmentVariable.builder().value(repositoryURL).build()
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
        return project;
    }

    private String createECRRepo(String id, String repositoryName) {
        CfnPublicRepository repo = CfnPublicRepository.Builder.create(this, id)
                .repositoryName(repositoryName)
                .build();

        // It's a CDK feature applied to the construct's "node".
        repo.applyRemovalPolicy(RemovalPolicy.RETAIN);

//        return CfnOutput.Builder.create(this, id + "-arn-output")
//                .value(repo.getAttrArn())
//                .exportName(id + "-arn")
//                .build();
        return repositoryName;
    }

    @Value
    public static class Props implements NestedStackProps {
    }
}

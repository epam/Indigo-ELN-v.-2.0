package com.epam.indigoeln.aws;

import lombok.Value;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.NestedStack;
import software.amazon.awscdk.NestedStackProps;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.codebuild.*;
import software.amazon.awscdk.services.ecr.CfnPublicRepository;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.constructs.Construct;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BuildStack extends NestedStack {

    public BuildStack(final Construct scope, final String id, final Props props) {
        super(scope, id, props);

        final String repoName = "my-l1-public-app";
        CfnPublicRepository elnLambdaRepo = CfnPublicRepository.Builder.create(this, "ecr-indigo-eln")
                .repositoryName("indigoeln/indigo-eln-lambda")
                .build();

        // It's a CDK feature applied to the construct's "node".
        elnLambdaRepo.applyRemovalPolicy(RemovalPolicy.RETAIN);

        // This is the biggest difference. L1 constructs do not have convenient getters like .getRepositoryUri().
        // You must access the raw CloudFormation "Fn::GetAtt" return values.
        // The CfnPublicRepository resource only returns the ARN.
        CfnOutput elnLambdaRepoARN = CfnOutput.Builder.create(this, "RepositoryArnOutput")
                .value(elnLambdaRepo.getAttrArn())
                .description("The ARN of the L1 public ECR repository")
                .exportName("ecr-indigo-eln-lambda")
                .build();

        PipelineProject codeBuildProject = PipelineProject.Builder.create(this, "MyCodeBuildProject")
                .projectName("PublicEcrImageBuilder")
                .environment(BuildEnvironment.builder()
                        .buildImage(LinuxBuildImage.STANDARD_7_0)
                        .privileged(true) // The 'privileged' flag is required for the CodeBuild project to build Docker images.
                        .build())
                // Pass the ECR repository URI as an environment variable to the build.
                .environmentVariables(Map.of(
                        "REPO_URI", BuildEnvironmentVariable.builder().value(elnLambdaRepoARN).build()
                ))
                // The BuildSpec defines the commands to run during the build.
                .buildSpec(BuildSpec.fromObject(Map.of(
                        "version", "0.2",
                        "phases", Map.of(
                                "pre_build", Map.of(
                                        "commands", Arrays.asList(
                                                // The login region for ECR Public is ALWAYS us-east-1.
                                                "echo Logging in to Amazon ECR Public...",
                                                "aws ecr-public get-login-password --region us-east-1 | docker login --username AWS --password-stdin " + "xxx", // !!! repo url
                                                // The image tag can be dynamic, e.g., using the commit hash.
                                                "IMAGE_TAG=$(echo $CODEBUILD_RESOLVED_SOURCE_VERSION | cut -c 1-7)"
                                        )
                                ),
                                "build", Map.of(
                                        "commands", Arrays.asList(
                                                "echo Build started on `date`",
                                                "echo Building the Docker image...",
                                                "cd backend",
                                                "chmod +x gradlew",
                                                "./gradlew build :eln:eln-lambda:quarkusIntTest",
                                                // Assumes a Dockerfile is in the root of your source repository.
                                                // !!! add Dockerfile or enable JIB
                                                "docker build -t $REPO_URI:latest .",
                                                "docker tag $REPO_URI:latest $REPO_URI:$IMAGE_TAG"
                                        )
                                ),
                                "post_build", Map.of(
                                        "commands", Arrays.asList(
                                                "echo Build completed on `date`",
                                                "echo Pushing the Docker images to ECR Public...",
                                                "docker push $REPO_URI:latest",
                                                "docker push $REPO_URI:$IMAGE_TAG"
                                        )
                                )
                        )
                )))
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
                        "ecr-public:PutImage"
                ))
                .resources(List.of("*")) // ECR Public GetAuthorizationToken requires resource "*"
                .build();

        codeBuildProject.addToRolePolicy(ecrPublicPermissions);
    }

    @Value
    public static class Props implements NestedStackProps {
    }
}

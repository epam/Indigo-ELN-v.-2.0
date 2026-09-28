package com.epam.indigoeln.aws;

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
import software.amazon.awscdk.services.secretsmanager.Secret;
import software.constructs.Construct;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.epam.indigoeln.aws.util.Utils.entry;
import static com.epam.indigoeln.aws.util.Utils.mapOf;

public class BuildStack extends Stack {

    public BuildStack(final Construct scope, final String id, StackProps stackProps, GlobalParameters globalParameters) {
        super(scope, id, stackProps);

        Repository elnLambdaRepo = createECRRepo("ecr-eln-lambda", "indigoeln/eln-lambda");
        Repository reportsLambdaRepo = createECRRepo("ecr-reports-lambda", "indigoeln/reports-lambda");
        Repository signatureLambdaRepo = createECRRepo("ecr-signature-lambda", "indigoeln/signature-lambda");
        Repository sampleRegistrationLambdaRepo = createECRRepo("ecr-sampleregistration-lambda", "indigoeln/sampleregistration-lambda");
        Repository postgresRepo = createECRRepo("ecr-postgres-base", "indigoeln/postgres-base");
        // The `combined` target of the same Dockerfile: bingo plus every application database in
        // one instance, for the compose stack and the integration tests.
        Repository postgresCombinedRepo = createECRRepo("ecr-postgres-combined", "indigoeln/postgres-combined");
        // The `*-aws` modules: the same services as the lambdas, packaged as long-running HTTP
        // servers for the compose stack on EC2. The lambda images keep being built alongside them.
        Repository elnAwsRepo = createECRRepo("ecr-eln-aws", "indigoeln/eln-aws");
        Repository reportsAwsRepo = createECRRepo("ecr-reports-aws", "indigoeln/reports-aws");
        Repository signatureAwsRepo = createECRRepo("ecr-signature-aws", "indigoeln/signature-aws");
        Repository sampleRegistrationAwsRepo = createECRRepo("ecr-sampleregistration-aws", "indigoeln/sampleregistration-aws");

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

        Secret sonarTokenSecret = Secret.Builder.create(this, "sonar-token-secret")
                .description("SonarQube analysis token used by CodeBuild projects")
                .build();
        String sonarHostUrl = "https://" + globalParameters.getSonarDomainName();
        Map<String, BuildEnvironmentVariable> sonarSecretEnvironment = mapOf(
                entry("SONAR_TOKEN", BuildEnvironmentVariable.builder()
                        .type(BuildEnvironmentVariableType.SECRETS_MANAGER)
                        .value(sonarTokenSecret.getSecretArn())
                        .build())
        );

        Project postgresBuild = createBuild("eln-postgres-build"
                , "indigo-eln-postgres-build"
                , "deployment-aws/codebuild/eln-build-postgres.yaml"
                , buildLogsBucket
                , ecrPublicPermissions
                , mapOf(
                        entry("REGISTRY_URI", postgresRepo.getRegistryUri()), // same registry for both repos
                        entry("REPO_URI", postgresRepo.getRepositoryUri()),
                        entry("REPO_URI_PUBLIC", "public.ecr.aws/m5k0g6n7/indigoeln/postgres-base"),
                        entry("COMBINED_REPO_URI", postgresCombinedRepo.getRepositoryUri()),
                        entry("COMBINED_REPO_URI_PUBLIC", "public.ecr.aws/m5k0g6n7/indigoeln/postgres-combined")
                )
                , mapOf()
        );
        postgresRepo.grantPullPush(postgresBuild);
        postgresCombinedRepo.grantPullPush(postgresBuild);

        Project elnBuild = createBuild("eln-build"
                , "indigo-eln-build"
                , "deployment-aws/codebuild/eln-build.yaml"
                , buildLogsBucket
                , ecrPublicPermissions
                , mapOf(
                        entry("BUILD_ELN_LAMBDA", "false"),
                        entry("BUILD_REPORTS_LAMBDA", "false"),
                        entry("BUILD_SIGNATURE_LAMBDA", "false"),
                        // All private repos share the account registry, so one login in the buildspec covers every push.
                        entry("REGISTRY_URI", elnLambdaRepo.getRegistryUri()),
                        entry("ELN_REPO_URI", elnLambdaRepo.getRepositoryUri()),
                        entry("REPORTS_REPO_URI", reportsLambdaRepo.getRepositoryUri()),
                        entry("SIGNATURE_REPO_URI", signatureLambdaRepo.getRepositoryUri()),
                        entry("BUILD_SAMPLEREGISTRATION_LAMBDA", "false"),
                        entry("SAMPLEREGISTRATION_REPO_URI", sampleRegistrationLambdaRepo.getRepositoryUri()),
                        entry("BUILD_ELN_AWS", "true"),
                        entry("BUILD_REPORTS_AWS", "false"),
                        entry("BUILD_SIGNATURE_AWS", "false"),
                        entry("BUILD_SAMPLEREGISTRATION_AWS", "false"),
                        // One registry for every private repo in the account, so the lambda logins above cover these too.
                        entry("ELN_AWS_REPO_URI", elnAwsRepo.getRepositoryUri()),
                        entry("REPORTS_AWS_REPO_URI", reportsAwsRepo.getRepositoryUri()),
                        entry("SIGNATURE_AWS_REPO_URI", signatureAwsRepo.getRepositoryUri()),
                        entry("SAMPLEREGISTRATION_AWS_REPO_URI", sampleRegistrationAwsRepo.getRepositoryUri()),
                        entry("S3_LOGS", buildLogsBucket.getBucketName()),
                        entry("SONAR_HOST_URL", sonarHostUrl)
                )
                , sonarSecretEnvironment
        );
        elnLambdaRepo.grantPullPush(elnBuild);
        reportsLambdaRepo.grantPullPush(elnBuild);
        signatureLambdaRepo.grantPullPush(elnBuild);
        sampleRegistrationLambdaRepo.grantPullPush(elnBuild);
        elnAwsRepo.grantPullPush(elnBuild);
        reportsAwsRepo.grantPullPush(elnBuild);
        signatureAwsRepo.grantPullPush(elnBuild);
        sampleRegistrationAwsRepo.grantPullPush(elnBuild);
        sonarTokenSecret.grantRead(elnBuild);

        Project frontendBuild = createBuild("frontend-build"
                , "indigo-eln-frontend-build"
                , "deployment-aws/codebuild/frontend-build.yaml"
                , buildLogsBucket
                , null
                , mapOf(
                        entry("SONAR_HOST_URL", sonarHostUrl)
                )
                , sonarSecretEnvironment
        );
        sonarTokenSecret.grantRead(frontendBuild);
    }

    private Project createBuild(String id, String projectName, String buildSpecFile, Bucket buildLogsBucket, @Nullable PolicyStatement policy, Map<String, String> environment, Map<String, BuildEnvironmentVariable> secretEnvironment) {
        Map<String, BuildEnvironmentVariable> environmentVariables = EntryStream.of(environment)
                .mapValues(v -> BuildEnvironmentVariable.builder().value(v).build())
                .toCustomMap(LinkedHashMap::new);
        environmentVariables.putAll(secretEnvironment);

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
                .environmentVariables(environmentVariables)
                // The BuildSpec defines the commands to run during the build.
                .buildSpec(BuildSpec.fromSourceFilename(buildSpecFile))
                // The eln project runs the whole repo's unit tests, then up to eight Quarkus builds
                // (four lambda, four aws) with a docker build and two pushes each.
                .timeout(Duration.minutes(60))
                .logging(LoggingOptions.builder()
                        .cloudWatch(CloudWatchLoggingOptions.builder().enabled(false).build())
                        .s3(S3LoggingOptions.builder().enabled(true).bucket(buildLogsBucket).prefix("backend").build())
                        .build())
                .build();
        if (policy != null) {
            project.addToRolePolicy(policy);
        }
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
}

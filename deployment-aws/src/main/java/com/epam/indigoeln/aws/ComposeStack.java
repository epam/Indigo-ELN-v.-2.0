package com.epam.indigoeln.aws;

import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.ec2.Instance;
import software.amazon.awscdk.services.ecr.IRepository;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.s3.IBucket;
import software.amazon.awscdk.services.s3.deployment.BucketDeployment;
import software.amazon.awscdk.services.s3.deployment.Source;
import software.amazon.awscdk.services.secretsmanager.Secret;
import software.amazon.awscdk.services.secretsmanager.SecretStringGenerator;
import software.amazon.awscdk.services.ssm.StringParameter;
import software.amazon.awscdk.services.cognito.IUserPool;
import software.constructs.Construct;

import java.util.List;

/**
 * Everything the instance needs in order to bring the compose stack up, published under fixed names
 * so that user-data never has to carry a content hash.
 *
 * <p>Split in two on purpose: the non-secret settings go into a plain SSM parameter, and the password
 * stays in Secrets Manager. That avoids nesting one secret inside another resource's value, and an
 * SSM SecureString is not an option because CloudFormation cannot create one.
 *
 * <p>One generated password, shared by the {@code postgres} superuser and the {@code eln},
 * {@code signature} and {@code sampleregistration} roles - CDK generates at most one password per
 * secret. The roles stay distinct, each owning its own database, but the shared password means a
 * compromised service container can also connect as the superuser. Acceptable for a single-instance
 * dev environment.
 */
public class ComposeStack {

    public ComposeStack(Construct scope, Props props) {
        Secret dbSecret = Secret.Builder.create(scope, "db-secret")
                .secretName(InfraStack.dbSecretName(props.envName()))
                .generateSecretString(SecretStringGenerator.builder()
                        .secretStringTemplate("{}")
                        .generateStringKey("password")
                        // Keeps the value safe to write into a .env file without quoting.
                        .excludePunctuation(true)
                        .passwordLength(30)
                        .build())
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

        LogGroup logGroup = LogGroup.Builder.create(scope, "compose-log-group")
                .logGroupName(props.logGroupName())
                .retention(RetentionDays.ONE_MONTH)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
        // The awslogs driver in the compose file writes here. `awslogs-create-group: true` needs
        // CreateLogGroup as well, since the driver checks rather than assuming the group exists.
        logGroup.grantWrite(props.ec2Role());
        props.ec2Role().addToPrincipalPolicy(PolicyStatement.Builder.create()
                .actions(List.of("logs:CreateLogGroup", "logs:DescribeLogStreams"))
                .resources(List.of(logGroup.getLogGroupArn()))
                .build());

        String env = String.join("\n",
                "AWS_REGION=" + props.region(),
                "AWS_LOG_GROUP=" + props.logGroupName(),
                "POSTGRES_IMAGE=" + image(props.postgresRepo(), props.postgresImageTag()),
                "ELN_AWS_IMAGE=" + image(props.elnAwsRepo(), props.elnAwsImageTag()),
                "REPORTS_AWS_IMAGE=" + image(props.reportsAwsRepo(), props.reportsAwsImageTag()),
                "SIGNATURE_AWS_IMAGE=" + image(props.signatureAwsRepo(), props.signatureAwsImageTag()),
                "SAMPLEREGISTRATION_AWS_IMAGE=" + image(props.sampleRegistrationAwsRepo(), props.sampleRegistrationAwsImageTag()),
                "ELN_COGNITO_USER_POOL_ID=" + props.userPool().getUserPoolId(),
                "ELN_STORAGE_S3_BUCKET=" + props.storageBucket().getBucketName(),
                // The name, not the value: deploy.sh resolves it, as it does the database password.
                "ELN_API_SECRET_ID=" + InfraStack.apiSecretName(props.envName()),
                ""); // deploy.sh appends DB_PASSWORD and ELN_API_SECRET

        StringParameter envParameter = StringParameter.Builder.create(scope, "compose-env")
                .parameterName(InfraStack.composeEnvParameterName(props.envName()))
                .stringValue(env)
                .build();
        envParameter.grantRead(props.ec2Role());
        dbSecret.grantRead(props.ec2Role());

        // CognitoExternalUserService creates and updates ELN users in the pool. On Lambda this was
        // granted to the function role; the containers get it from the instance profile via IMDS.
        props.userPool().grant(props.ec2Role(),
                "cognito-idp:AdminCreateUser",
                "cognito-idp:AdminSetUserPassword",
                "cognito-idp:AdminUpdateUserAttributes"
        );

        // A fixed key under the existing bucket rather than a CDK Asset: asset keys embed a content
        // hash, and anything hash-derived that user-data references would replace the instance on
        // every compose edit. It also avoids needing kms:Decrypt on the bootstrap CMK.
        BucketDeployment deployment = BucketDeployment.Builder.create(scope, "compose-deployment")
                .sources(List.of(Source.asset("compose")))
                .destinationBucket(props.storageBucket())
                .destinationKeyPrefix("compose")
                // The bucket also holds application data; prune would delete everything outside this prefix.
                .prune(false)
                .memoryLimit(256)
                .build();

        // User-data syncs s3://<bucket>/compose/ and deploy.sh reads the SSM parameter and the
        // secret, all on first boot. None of that is visible to CloudFormation, which would otherwise
        // be free to create the instance in parallel with them - the instance would boot, find an
        // empty prefix, and never start the stack. Order it explicitly.
        props.instance().getNode().addDependency(deployment, envParameter, dbSecret);

        // Tagged so deploy-compose.sh can find the instance without hardcoding an id. The env tag is
        // what keeps it unambiguous once dev and prod both exist - role alone would match every one.
        software.amazon.awscdk.Tags.of(props.instance()).add("indigoeln:role", "compose-host");
        software.amazon.awscdk.Tags.of(props.instance()).add("indigoeln:env", props.envName());
    }

    private static String image(IRepository repo, String tag) {
        return repo.getRepositoryUri() + ":" + tag;
    }

    public record Props(
            String envName,
            String region,
            String logGroupName,
            Instance instance,
            Role ec2Role,
            IBucket storageBucket,
            IUserPool userPool,
            IRepository postgresRepo,
            String postgresImageTag,
            IRepository elnAwsRepo,
            String elnAwsImageTag,
            IRepository reportsAwsRepo,
            String reportsAwsImageTag,
            IRepository signatureAwsRepo,
            String signatureAwsImageTag,
            IRepository sampleRegistrationAwsRepo,
            String sampleRegistrationAwsImageTag
    ) {}
}

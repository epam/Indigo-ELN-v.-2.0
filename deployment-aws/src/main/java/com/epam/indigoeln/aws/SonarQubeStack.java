package com.epam.indigoeln.aws;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.certificatemanager.Certificate;
import software.amazon.awscdk.services.certificatemanager.CertificateValidation;
import software.amazon.awscdk.services.cloudfront.*;
import software.amazon.awscdk.services.cloudfront.origins.HttpOrigin;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ec2.Volume;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.HealthCheck;
import software.amazon.awscdk.services.iam.IManagedPolicy;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.rds.Credentials;
import software.amazon.awscdk.services.rds.DatabaseSecret;
import software.amazon.awscdk.services.route53.*;
import software.amazon.awscdk.services.route53.targets.CloudFrontTarget;
import software.amazon.awscdk.services.wafv2.CfnWebACL;
import software.amazon.awsconstructs.services.wafwebaclcloudfront.WafwebaclToCloudFront;
import software.amazon.awsconstructs.services.wafwebaclcloudfront.WafwebaclToCloudFrontProps;
import software.constructs.Construct;

import java.util.ArrayList;
import java.util.List;

import static com.epam.indigoeln.aws.util.Utils.entry;
import static com.epam.indigoeln.aws.util.Utils.mapOf;

public class SonarQubeStack extends Stack {

    public SonarQubeStack(final Construct scope, final String id, final StackProps stackProps, final Props props) {
        super(scope, id, stackProps);

        IVpc vpc = Vpc.fromLookup(this, "vpc", VpcLookupOptions.builder().vpcId(props.vpcId()).build());
        IHostedZone hostedZone = HostedZone.fromHostedZoneAttributes(this, "hosted-zone", HostedZoneAttributes.builder()
                .hostedZoneId(props.hostedZone())
                .zoneName(props.hostedZoneName())
                .build()
        );

        IKeyPair ec2KeyPair = KeyPair.fromKeyPairName(this, "ec2-key-pair", props.ec2KeyPair());

        List<ISecurityGroup> additionalSecurityGroups = new ArrayList<>();
        for (String groupId : props.securityGroups()) {
            additionalSecurityGroups.add(SecurityGroup.fromSecurityGroupId(this, "security-group-" + (additionalSecurityGroups.size() + 1), groupId));
        }

        SecurityGroup ec2SecurityGroup = SecurityGroup.Builder.create(this, "ec2-security-group")
                .vpc(vpc)
                .allowAllOutbound(true)
                .build();
        // Restrict port 9000 to CloudFront's own IP ranges (AWS-managed prefix list) instead of 0.0.0.0/0 -
        // direct http://<instance-public-dns>:9000 access from elsewhere on the internet is rejected at the
        // security-group layer, forcing all traffic through CloudFront/HTTPS/WAF.
        IPrefixList cloudFrontPrefixList = PrefixList.fromLookup(this, "cloudfront-prefix-list", PrefixListLookupOptions.builder()
                .prefixListName("com.amazonaws.global.cloudfront.origin-facing")
                .build());
        ec2SecurityGroup.addIngressRule(Peer.prefixList(cloudFrontPrefixList.getPrefixListId()), Port.tcp(9000), "sonarqube-http-from-cloudfront");

        IManagedPolicy ecsInstancePolicy = ManagedPolicy.fromAwsManagedPolicyName("service-role/AmazonEC2ContainerServiceforEC2Role");
        Role ec2Role = Role.Builder.create(this, "ec2-role")
                .assumedBy(new ServicePrincipal("ec2.amazonaws.com"))
                .managedPolicies(List.of(ecsInstancePolicy))
                .build();

        String dataVolumeAz = vpc.getAvailabilityZones().get(0);
        Volume dataVolume = Volume.Builder.create(this, "postgres-data-volume")
                .availabilityZone(dataVolumeAz)
                .size(Size.gibibytes(20))
                .volumeType(EbsDeviceVolumeType.GP3)
                .removalPolicy(RemovalPolicy.RETAIN)
                .build();

        Cluster ecsCluster = Cluster.Builder.create(this, "ecs-cluster")
                .vpc(vpc)
                .clusterName("indigoeln-sonarqube")
                .build();

        UserData userData = UserData.forLinux();
        userData.addCommands(
                "echo ECS_CLUSTER=" + ecsCluster.getClusterName() + " >> /etc/ecs/ecs.config",
                "DEVICE=/dev/xvdb",
                "MOUNT_POINT=/data/postgres",
                // volume is attached declaratively via CfnVolumeAttachment; wait for it to show up
                "while [ ! -b $DEVICE ]; do sleep 1; done",
                "if ! blkid $DEVICE > /dev/null 2>&1; then mkfs.ext4 -F $DEVICE; fi",
                "mkdir -p $MOUNT_POINT",
                "mount $DEVICE $MOUNT_POINT || true",
                "grep -q \"$DEVICE\" /etc/fstab || echo \"$DEVICE $MOUNT_POINT ext4 defaults 0 2\" >> /etc/fstab",
                "chown -R 999:999 $MOUNT_POINT", // UID 999 = postgres user in the official postgres image
                // required by SonarQube's bundled Elasticsearch bootstrap checks
                "echo 'vm.max_map_count=262144' > /etc/sysctl.d/99-sonarqube.conf",
                "echo 'fs.file-max=131072' >> /etc/sysctl.d/99-sonarqube.conf",
                "sysctl -p /etc/sysctl.d/99-sonarqube.conf"
        );

        Instance instance = Instance.Builder.create(this, "ec2-instance")
                .vpc(vpc)
                .instanceType(InstanceType.of(InstanceClass.T3A, InstanceSize.LARGE))
                .machineImage(EcsOptimizedImage.amazonLinux2023(AmiHardwareType.STANDARD))
                .vpcSubnets(SubnetSelection.builder()
                        .subnetType(SubnetType.PUBLIC)
                        // must match the EBS volume's AZ - a standalone EBS can only attach to instances in the same AZ
                        .availabilityZones(List.of(dataVolumeAz))
                        .build())
                .securityGroup(ec2SecurityGroup)
                .role(ec2Role)
                .keyPair(ec2KeyPair)
                .userData(userData)
                .associatePublicIpAddress(true)
                .build();
        for (ISecurityGroup sg : additionalSecurityGroups) {
            instance.addSecurityGroup(sg);
        }

        CfnVolumeAttachment.Builder.create(this, "postgres-data-attachment")
                .volumeId(dataVolume.getVolumeId())
                .instanceId(instance.getInstanceId())
                .device("/dev/xvdb")
                .build();

        DatabaseSecret dbSecret = DatabaseSecret.Builder.create(this, "sonar-db-secret")
                .username(props.sonarPostgresMasterUsername())
                .build();

        TaskDefinition task = TaskDefinition.Builder.create(this, "ecs-task-sonarqube")
                .compatibility(Compatibility.EC2)
                // HOST networking: sonarqube <-> postgres talk over 127.0.0.1 (shared netns), and sonarqube's
                // port 9000 is directly the EC2 instance's port 9000, which CloudFront's origin needs
                .networkMode(NetworkMode.HOST)
                .cpu("2048")
                .memoryMiB("7168")
                .build();

        task.addVolume(software.amazon.awscdk.services.ecs.Volume.builder()
                .name("postgres-data")
                .host(Host.builder().sourcePath("/data/postgres").build())
                .build());

        ContainerDefinition postgresContainer = task.addContainer("postgres-container", ContainerDefinitionOptions.builder()
                .image(ContainerImage.fromRegistry("postgres:16-alpine"))
                .environment(mapOf(
                        entry("POSTGRES_USER", Credentials.fromSecret(dbSecret).getUsername()),
                        entry("POSTGRES_PASSWORD", Credentials.fromSecret(dbSecret).getPassword().unsafeUnwrap()),
                        entry("POSTGRES_DB", "sonar"),
                        entry("PGDATA", "/var/lib/postgresql/data/pgdata")
                ))
                .portMappings(List.of(PortMapping.builder().containerPort(5432).build()))
                .healthCheck(HealthCheck.builder()
                        .command(List.of("CMD-SHELL", "pg_isready -U " + props.sonarPostgresMasterUsername() + " || exit 1"))
                        .interval(Duration.seconds(10))
                        .timeout(Duration.seconds(5))
                        .retries(5)
                        .startPeriod(Duration.seconds(10))
                        .build())
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder().streamPrefix("postgres").build()))
                .build());
        postgresContainer.addMountPoints(MountPoint.builder()
                .sourceVolume("postgres-data")
                .containerPath("/var/lib/postgresql/data")
                .readOnly(false)
                .build());

        ContainerDefinition sonarqubeContainer = task.addContainer("sonarqube-container", ContainerDefinitionOptions.builder()
                .image(ContainerImage.fromRegistry(props.sonarImage()))
                .environment(mapOf(
                        entry("SONAR_JDBC_URL", "jdbc:postgresql://127.0.0.1:5432/sonar"),
                        entry("SONAR_JDBC_USERNAME", Credentials.fromSecret(dbSecret).getUsername()),
                        entry("SONAR_JDBC_PASSWORD", Credentials.fromSecret(dbSecret).getPassword().unsafeUnwrap())
                ))
                .portMappings(List.of(PortMapping.builder().containerPort(9000).build()))
                .ulimits(List.of(
                        Ulimit.builder().name(UlimitName.NOFILE).softLimit(131072).hardLimit(131072).build(),
                        Ulimit.builder().name(UlimitName.NPROC).softLimit(8192).hardLimit(8192).build()
                ))
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder().streamPrefix("sonarqube").build()))
                .build());
        sonarqubeContainer.addContainerDependencies(ContainerDependency.builder()
                .container(postgresContainer)
                .condition(ContainerDependencyCondition.HEALTHY)
                .build());

        // Ec2Service (L2) refuses to synth unless Cluster.addCapacity()/addAsgCapacityProvider() was called,
        // which only exists to provision an ASG - exactly what this stack avoids to keep a stable, synth-time
        // known instance for CloudFront's origin. The instance joins the cluster itself via UserData
        // (ECS_CLUSTER in /etc/ecs/ecs.config), so drop to the L1 CfnService to skip that ASG-only check.
        CfnService.Builder.create(this, "sonarqube-service")
                .cluster(ecsCluster.getClusterName())
                .taskDefinition(task.getTaskDefinitionArn())
                .desiredCount(1)
                .deploymentConfiguration(CfnService.DeploymentConfigurationProperty.builder()
                        .minimumHealthyPercent(0)
                        .maximumPercent(100)
                        .build())
                .build();

        Certificate certificate = Certificate.Builder.create(this, "sonarqube-certificate")
                .domainName(props.sonarDomainName())
                .validation(CertificateValidation.fromDns(hostedZone))
                .build();

        Distribution distribution = Distribution.Builder.create(this, "sonarqube-cloudfront")
                .defaultBehavior(BehaviorOptions.builder()
                        .origin(HttpOrigin.Builder.create(instance.getInstancePublicDnsName())
                                .protocolPolicy(OriginProtocolPolicy.HTTP_ONLY)
                                .httpPort(9000)
                                .build())
                        .allowedMethods(AllowedMethods.ALLOW_ALL)
                        .cachePolicy(CachePolicy.CACHING_DISABLED)
                        .originRequestPolicy(OriginRequestPolicy.ALL_VIEWER_EXCEPT_HOST_HEADER)
                        .viewerProtocolPolicy(ViewerProtocolPolicy.HTTPS_ONLY)
                        .build())
                .domainNames(List.of(props.sonarDomainName()))
                .certificate(certificate)
                .priceClass(PriceClass.PRICE_CLASS_200)
                .build();

        // Scanner report uploads to /api/ce/submit run well past WAF's body-inspection limit (max 64 KB) -
        // most body-inspecting rules in these managed groups block on oversized bodies by default (not just
        // the SizeRestrictions_BODY label), so skip these groups entirely for that endpoint rather than
        // trying to override every rule that inspects the body.
        CfnWebACL.StatementProperty notReportUpload = excludePathStatement("/api/ce/submit");

        CfnWebACL.RuleProperty ipReputationsRuleSet = createWAFRuleSet("AWS", "AWSManagedRulesAmazonIpReputationList", 0, List.of(), null);
        CfnWebACL.RuleProperty commonRuleSet = createWAFRuleSet("AWS", "AWSManagedRulesCommonRuleSet", 1, List.of(
                // SonarQube scanner report uploads can exceed the default inspected-body size; count instead of block
                CfnWebACL.RuleActionOverrideProperty.builder()
                        .name("SizeRestrictions_BODY")
                        .actionToUse(CfnWebACL.RuleActionProperty.builder().count(CfnWebACL.CountActionProperty.builder().build()).build())
                        .build()
        ), notReportUpload);
        CfnWebACL.RuleProperty knownBadInputRuleSet = createWAFRuleSet("AWS", "AWSManagedRulesKnownBadInputsRuleSet", 2, List.of(), notReportUpload);

        CfnWebACL wafWebACL = CfnWebACL.Builder.create(this, "sonarqube-wafwebacl")
                .scope("CLOUDFRONT")
                .rules(List.of(ipReputationsRuleSet, commonRuleSet, knownBadInputRuleSet))
                .defaultAction(CfnWebACL.DefaultActionProperty.builder().allow(CfnWebACL.AllowActionProperty.builder().build()).build())
                .visibilityConfig(CfnWebACL.VisibilityConfigProperty.builder().cloudWatchMetricsEnabled(true).metricName("SonarQubeWebACLMetric").sampledRequestsEnabled(true).build())
                .build();
        new WafwebaclToCloudFront(this, "sonarqube-wafwebacl-cloudfront", WafwebaclToCloudFrontProps.builder()
                .existingWebaclObj(wafWebACL)
                .existingCloudFrontWebDistribution(distribution)
                .build()
        );

        ARecord.Builder.create(this, "sonarqube-domain-record")
                .zone(hostedZone)
                .recordName(props.sonarDomainName() + ".")
                .target(RecordTarget.fromAlias(new CloudFrontTarget(distribution)))
                .build();
    }

    private CfnWebACL.RuleProperty createWAFRuleSet(String vendor, String name, int priority, List<CfnWebACL.RuleActionOverrideProperty> overrides, CfnWebACL.StatementProperty scopeDownStatement) {
        CfnWebACL.ManagedRuleGroupStatementProperty.Builder managedRuleGroupStatement = CfnWebACL.ManagedRuleGroupStatementProperty.builder()
                .name(name)
                .vendorName(vendor)
                .ruleActionOverrides(overrides);
        if (scopeDownStatement != null) {
            managedRuleGroupStatement.scopeDownStatement(scopeDownStatement);
        }
        return CfnWebACL.RuleProperty.builder()
                .name("rule-" + name)
                .priority(priority)
                .statement(CfnWebACL.StatementProperty.builder()
                        .managedRuleGroupStatement(managedRuleGroupStatement.build())
                        .build())
                .visibilityConfig(CfnWebACL.VisibilityConfigProperty.builder()
                        .sampledRequestsEnabled(true)
                        .cloudWatchMetricsEnabled(true)
                        .metricName("metric-sonarqube-" + name)
                        .build())
                .overrideAction(CfnWebACL.OverrideActionProperty.builder()
                        .none(mapOf())
                        .build())
                .build();
    }

    // Managed rule group only applies when this is true - i.e. skips the whole group for requests to pathPrefix
    private CfnWebACL.StatementProperty excludePathStatement(String pathPrefix) {
        return CfnWebACL.StatementProperty.builder()
                .notStatement(CfnWebACL.NotStatementProperty.builder()
                        .statement(CfnWebACL.StatementProperty.builder()
                                .byteMatchStatement(CfnWebACL.ByteMatchStatementProperty.builder()
                                        .searchString(pathPrefix)
                                        .fieldToMatch(CfnWebACL.FieldToMatchProperty.builder().uriPath(mapOf()).build())
                                        .textTransformations(List.of(
                                                CfnWebACL.TextTransformationProperty.builder().priority(0).type("NONE").build()
                                        ))
                                        .positionalConstraint("STARTS_WITH")
                                        .build())
                                .build())
                        .build())
                .build();
    }

    public record Props(
            String sonarDomainName,
            String sonarPostgresMasterUsername,
            String sonarImage,
            String vpcId,
            String hostedZone,
            String hostedZoneName,
            String ec2KeyPair,
            List<String> securityGroups
    ) {}
}

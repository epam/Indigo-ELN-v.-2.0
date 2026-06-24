package com.epam.indigoeln.aws;

import lombok.Getter;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Size;
import software.amazon.awscdk.services.autoscaling.AutoScalingGroup;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ecs.AmiHardwareType;
import software.amazon.awscdk.services.ecs.AsgCapacityProvider;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.EcsOptimizedImage;
import software.amazon.awscdk.services.iam.IManagedPolicy;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.route53.HostedZone;
import software.amazon.awscdk.services.route53.HostedZoneAttributes;
import software.amazon.awscdk.services.route53.IHostedZone;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.IBucket;
import software.amazon.awscdk.services.servicediscovery.PrivateDnsNamespace;
import software.constructs.Construct;

import java.util.ArrayList;
import java.util.List;

public class InfraStack {

    @Getter
    private final IVpc vpc;
    @Getter
    private final PrivateDnsNamespace privateDnsNamespace;
    @Getter
    private final List<ISecurityGroup> additionalSecurityGroups;
    @Getter
    private final IHostedZone hostedZone;
    @Getter
    private final IKeyPair ec2KeyPair;
    @Getter
    private final Cluster ecsCluster;
    @Getter
    private final SecurityGroup ec2SecurityGroup;
    @Getter
    private final SecurityGroup lambdaSecurityGroup;
    @Getter
    private final IBucket storageBucket;

    public InfraStack(final Construct scope, final Props props) {
        vpc = Vpc.fromLookup(scope, "vpc", VpcLookupOptions.builder().vpcId(props.vpcId()).build());
        additionalSecurityGroups = new ArrayList<>();
        for (String groupId : props.securityGroups()) {
            ISecurityGroup group = SecurityGroup.fromSecurityGroupId(scope, "security-group-" + (additionalSecurityGroups.size() + 1), groupId);
            additionalSecurityGroups.add(group);
        }

        ec2KeyPair = KeyPair.fromKeyPairName(scope, "ec2-key-pair", props.ec2KeyPair());

        hostedZone = HostedZone.fromHostedZoneAttributes(scope, "hosted-zone", HostedZoneAttributes.builder()
                .hostedZoneId(props.hostedZone())
                .zoneName(props.hostedZoneName())
                .build()
        );

        privateDnsNamespace = PrivateDnsNamespace.Builder.create(scope, "vpc-dns-namespace")
                .name("indigoeln.local")
                .vpc(vpc)
                .build();

        ec2SecurityGroup = SecurityGroup.Builder.create(scope, "ec2-security-group")
                .vpc(vpc)
                .allowAllOutbound(true)
                .build();

        IManagedPolicy policy = ManagedPolicy.fromAwsManagedPolicyName("service-role/AmazonEC2ContainerServiceforEC2Role");
        Role ec2Role = Role.Builder.create(scope, "ec2-role")
                .assumedBy(new ServicePrincipal("ec2.amazonaws.com"))
                .managedPolicies(List.of(policy))
                .build();

        String dataVolumeAz = vpc.getAvailabilityZones().get(0);
        Volume dataVolume = Volume.Builder.create(scope, "postgres-data-volume")
                .availabilityZone(dataVolumeAz)
                .size(Size.gibibytes(10))
                .volumeType(EbsDeviceVolumeType.GP3)
                .removalPolicy(RemovalPolicy.RETAIN)
                .build();
        dataVolume.grantAttachVolume(ec2Role);

        UserData userData = UserData.forLinux();
        // attach and format EBS storage if needed
        userData.addCommands(
                "VOLUME_ID=" + dataVolume.getVolumeId(),
                "DEVICE=/dev/xvdb",
                "MOUNT_POINT=/data/postgres",
                "echo VOLUME_ID=$VOLUME_ID, DEVICE=$DEVICE, MOUNT_POINT=$MOUNT_POINT",
                "TOKEN=$(curl -s -X PUT http://169.254.169.254/latest/api/token -H 'X-aws-ec2-metadata-token-ttl-seconds: 21600')",
                "INSTANCE_ID=$(curl -s -H \"X-aws-ec2-metadata-token: $TOKEN\" http://169.254.169.254/latest/meta-data/instance-id)",
                "REGION=$(curl -s -H \"X-aws-ec2-metadata-token: $TOKEN\" http://169.254.169.254/latest/meta-data/placement/region)",
                // Attach only when the device isn't present (new instance, not a restart)
                "if [ ! -b $DEVICE ]; then",
                "  aws ec2 attach-volume --volume-id $VOLUME_ID --instance-id $INSTANCE_ID --device $DEVICE --region $REGION",
                "  while [ ! -b $DEVICE ]; do sleep 1; done",
                "fi",
                // Format only on very first use
                "if ! blkid $DEVICE > /dev/null 2>&1; then mkfs.ext4 -F $DEVICE; fi",
                "mkdir -p $MOUNT_POINT",
                "mount $DEVICE $MOUNT_POINT || true",
                "grep -q \"$DEVICE\" /etc/fstab || echo \"$DEVICE $MOUNT_POINT ext4 defaults 0 2\" >> /etc/fstab",
                "chown -R 999:999 $MOUNT_POINT"  // UID 999 = postgres user in the official postgres image
        );

        LaunchTemplate launchTemplate = LaunchTemplate.Builder.create(scope, "ec2-launch-template")
                .instanceType(InstanceType.of(InstanceClass.T3A, InstanceSize.MICRO))
                .machineImage(EcsOptimizedImage.amazonLinux2023(AmiHardwareType.STANDARD))
                .userData(userData)
                .securityGroup(ec2SecurityGroup)
                .role(ec2Role)
                .keyPair(ec2KeyPair)
                .build();
        for (ISecurityGroup sg : additionalSecurityGroups) {
            launchTemplate.addSecurityGroup(sg);
        }

        AutoScalingGroup autoScalingGroup = AutoScalingGroup.Builder.create(scope, "ec2-auto-scaling-group")
                .vpc(vpc)
                .launchTemplate(launchTemplate)
                .minCapacity(1)
                .maxCapacity(1)
                .desiredCapacity(1)
                // Must match the EBS volume's AZ — a standalone EBS can only attach to instances in the same AZ
                .vpcSubnets(SubnetSelection.builder()
                        .subnetType(SubnetType.PUBLIC)
                        .availabilityZones(List.of(dataVolumeAz))
                        .build())
                .build();

        ecsCluster = Cluster.Builder.create(scope, "ecs-cluster")
                .vpc(vpc)
                .build();

        ecsCluster.addAsgCapacityProvider(AsgCapacityProvider.Builder.create(scope, "ec2-capacity-provider")
                .autoScalingGroup(autoScalingGroup)
                .build());

        lambdaSecurityGroup = SecurityGroup.Builder.create(scope, "lambda-security-group")
                .vpc(vpc)
                .allowAllOutbound(true)
                .allowAllIpv6Outbound(true)
                .build();
        ec2SecurityGroup.addIngressRule(lambdaSecurityGroup, Port.tcp(6432), "from-lambda");
        ec2SecurityGroup.addIngressRule(lambdaSecurityGroup, Port.tcp(6433), "from-lambda");

        storageBucket = Bucket.Builder.create(scope, "storage-bucket")
                .bucketName(props.storageBucketName())
                .removalPolicy(RemovalPolicy.RETAIN)
                .build();

        GatewayVpcEndpoint.Builder.create(scope, "s3-gateway-endpoint")
                .vpc(vpc)
                .service(GatewayVpcEndpointAwsService.S3)
                .subnets(List.of(SubnetSelection.builder()
                        .subnetFilters(List.of(SubnetFilter.byIds(props.lambdaSubnets())))
                        .build()))
                .build();
    }

    public record Props(
            String vpcId,
            String ec2KeyPair,
            String hostedZone,
            String hostedZoneName,
            List<String> securityGroups,
            String storageBucketName,
            List<String> lambdaSubnets
    ) {}
}

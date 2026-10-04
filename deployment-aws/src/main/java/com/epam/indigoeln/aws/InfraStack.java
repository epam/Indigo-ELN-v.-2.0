package com.epam.indigoeln.aws;

import lombok.Getter;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Size;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.route53.HostedZone;
import software.amazon.awscdk.services.route53.HostedZoneAttributes;
import software.amazon.awscdk.services.route53.IHostedZone;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.IBucket;
import software.amazon.awscdk.services.ssm.IStringParameter;
import software.amazon.awscdk.services.ssm.StringParameter;
import software.constructs.Construct;

import java.util.ArrayList;
import java.util.List;

public class InfraStack {

    // Pinned deliberately. Docker Compose v2 is not on the Amazon Linux 2023 AMI and
    // docker-compose-plugin is not in its default repositories, so the binary is fetched in
    // user-data - and user-data must never change on its own, because a change replaces the instance.
    private static final String DOCKER_COMPOSE_VERSION = "v2.29.7";
    private static final String DATA_DEVICE = "/dev/xvdb";
    private static final String DATA_MOUNT = "/data/postgres";
    private static final String APP_DIR = "/opt/indigoeln";

    @Getter
    private final IVpc vpc;
    @Getter
    private final List<ISecurityGroup> additionalSecurityGroups;
    @Getter
    private final IHostedZone hostedZone;
    @Getter
    private final SecurityGroup ec2SecurityGroup;
    @Getter
    private final IBucket storageBucket;
    @Getter
    private final Instance instance;
    @Getter
    private final Role ec2Role;
    @Getter
    private final IStringParameter apiSecret;

    public InfraStack(final Construct scope, final Props props) {
        vpc = Vpc.fromLookup(scope, "vpc", VpcLookupOptions.builder().vpcId(props.vpcId()).build());
        additionalSecurityGroups = new ArrayList<>();
        for (String groupId : props.securityGroups()) {
            ISecurityGroup group = SecurityGroup.fromSecurityGroupId(scope, "security-group-" + (additionalSecurityGroups.size() + 1), groupId);
            additionalSecurityGroups.add(group);
        }

        IKeyPair ec2KeyPair = KeyPair.fromKeyPairName(scope, "ec2-key-pair", props.ec2KeyPair());

        hostedZone = HostedZone.fromHostedZoneAttributes(scope, "hosted-zone", HostedZoneAttributes.builder()
                .hostedZoneId(props.hostedZone())
                .zoneName(props.hostedZoneName())
                .build()
        );

        // Shared between CloudFront (which sends it as an origin custom header) and every service's
        // APISecretFilter. It is what proves a request arrived through the CDN rather than directly.
        apiSecret = StringParameter.Builder.create(scope, "api-gateway-secret")
                .parameterName("api-gateway-secret-" + props.envName)
                .stringValue(props.apiSecret())
                .build();

        ec2SecurityGroup = SecurityGroup.Builder.create(scope, "ec2-security-group")
                .vpc(vpc)
                .allowAllOutbound(true)
                .build();
        // Port 80 only, and only from CloudFront's own ranges, so the instance cannot be reached
        // directly from the internet even though it sits in a public subnet with a public IP.
        IPrefixList cloudFrontPrefixList = PrefixList.fromLookup(scope, "cloudfront-prefix-list", PrefixListLookupOptions.builder()
                .prefixListName("com.amazonaws.global.cloudfront.origin-facing")
                .build());
        ec2SecurityGroup.addIngressRule(Peer.prefixList(cloudFrontPrefixList.getPrefixListId()), Port.tcp(80), "http-from-cloudfront");

        ec2Role = Role.Builder.create(scope, "ec2-role")
                .assumedBy(new ServicePrincipal("ec2.amazonaws.com"))
                .managedPolicies(List.of(
                        // Lets `aws ssm send-command` reach the instance: the whole deploy path, with
                        // no inbound ports and no SSH key.
                        ManagedPolicy.fromAwsManagedPolicyName("AmazonSSMManagedInstanceCore"),
                        // `docker compose pull` from the private registry.
                        ManagedPolicy.fromAwsManagedPolicyName("AmazonEC2ContainerRegistryReadOnly")
                ))
                .build();

        storageBucket = Bucket.Builder.create(scope, "storage-bucket")
                .bucketName(props.storageBucketName())
                .removalPolicy(RemovalPolicy.RETAIN)
                .build();
        // The containers pick up this role from IMDS. S3FileStorage lists, puts *and* gets - the
        // lambda only ever had grantPut, which would have made incident-report reads 403.
        storageBucket.grantReadWrite(ec2Role);

        if (props.createS3Gateway) {
            GatewayVpcEndpoint.Builder.create(scope, "s3-gateway-endpoint")
                    .vpc(vpc)
                    .service(GatewayVpcEndpointAwsService.S3)
                    .subnets(List.of(SubnetSelection.builder()
                            .subnetFilters(List.of(SubnetFilter.byIds(props.lambdaSubnets())))
                            .build()))
                    .build();
        }

        // Hardcoded rather than vpc.getAvailabilityZones().get(0): cdk.context.json caches an empty
        // availabilityZones list for this VPC, so a context refresh could silently move the volume,
        // and AvailabilityZone is a replacement property on a resource holding all the data.
        Volume dataVolume = Volume.Builder.create(scope, "postgres-data-volume")
                .availabilityZone(props.dataVolumeAz())
                .size(Size.gibibytes(20))
                .volumeType(EbsDeviceVolumeType.GP3)
                .removalPolicy(RemovalPolicy.RETAIN)
                .build();

        instance = Instance.Builder.create(scope, "ec2-instance")
                .vpc(vpc)
                // MEDIUM (4 GiB) fits the container limits in compose/docker-compose.yml, which sum to
                // ~3.4 GiB and leave ~600 MiB for the OS and docker, plus the swapfile user-data adds.
                // Raising the limits there means raising this too.
                .instanceType(InstanceType.of(InstanceClass.T3A, InstanceSize.MEDIUM))
                // Plain Amazon Linux 2023, x86_64: the Indigo native libraries in the eln image are
                // x86_64-only, and nothing here needs the ECS-optimized variant now that ECS is gone.
                //
                // cachedInContext is the important part. Without it the AMI is an SSM parameter
                // resolved at *deploy* time, so an unrelated `cdk deploy` after AWS publishes a new
                // AMI would replace the instance - and a replacement deadlocks on the EBS attachment.
                // With it, the id is resolved once, written to cdk.context.json as a literal, and only
                // changes when refreshed on purpose:
                //   cdk context --reset ami:account=...:region=...
                .machineImage(MachineImage.latestAmazonLinux2023(AmazonLinux2023ImageSsmParameterProps.builder()
                        .cpuType(AmazonLinuxCpuType.X86_64)
                        .kernel(AmazonLinux2023Kernel.DEFAULT)
                        .cachedInContext(true)
                        .build()))
                .vpcSubnets(SubnetSelection.builder()
                        .subnetType(SubnetType.PUBLIC)
                        // Must match the volume's AZ - a standalone EBS only attaches within one AZ.
                        .availabilityZones(List.of(props.dataVolumeAz()))
                        .build())
                .blockDevices(List.of(BlockDevice.builder()
                        .deviceName("/dev/xvda")
                        .volume(BlockDeviceVolume.ebs(50, EbsDeviceOptions.builder()
                                .volumeType(EbsDeviceVolumeType.GP3)
                                .deleteOnTermination(true)
                                .build()))
                        .build()))
                .securityGroup(ec2SecurityGroup)
                .role(ec2Role)
                .keyPair(ec2KeyPair)
                .userData(userData(props))
                .associatePublicIpAddress(true)
                .build();
        for (ISecurityGroup sg : additionalSecurityGroups) {
            instance.addSecurityGroup(sg);
        }

        // Declarative rather than `aws ec2 attach-volume` in user-data, so CloudFormation knows about
        // the attachment and orders the detach on replacement.
        CfnVolumeAttachment.Builder.create(scope, "postgres-data-attachment")
                .volumeId(dataVolume.getVolumeId())
                .instanceId(instance.getInstanceId())
                .device(DATA_DEVICE)
                .build();
    }

    /**
     * Must stay byte-for-byte stable across deploys: any change to user data replaces the instance,
     * and a replacement collides with the EBS attachment. Everything variable - the compose file, the
     * image tags, the secrets - is fetched at run time by deploy.sh, addressed by fixed names.
     */
    private UserData userData(Props props) {
        UserData userData = UserData.forLinux();
        userData.addCommands(
                "set -euxo pipefail",

                "dnf install -y docker jq",
                "mkdir -p /usr/libexec/docker/cli-plugins",
                "curl -fsSL https://github.com/docker/compose/releases/download/" + DOCKER_COMPOSE_VERSION
                        + "/docker-compose-linux-x86_64 -o /usr/libexec/docker/cli-plugins/docker-compose",
                "chmod +x /usr/libexec/docker/cli-plugins/docker-compose",
                "systemctl enable --now docker",
                "docker compose version",

                // A 2 GiB swapfile as a cushion: the container limits in compose/docker-compose.yml
                // sum to ~3.4 GiB of this instance's 4 GiB, so a spike (report generation, structure
                // rendering) has little headroom and would otherwise be resolved by the kernel OOM
                // killer picking whichever process is largest - usually Postgres. Swapping degrades
                // instead. The fstab entry is what brings it back after a reboot; user-data itself
                // runs only on first boot.
                //
                // dd rather than fallocate: swapon rejects a file with holes on ext4.
                "dd if=/dev/zero of=/swapfile bs=1M count=2048 status=none",
                "chmod 600 /swapfile",
                "mkswap /swapfile",
                "swapon /swapfile",
                "echo '/swapfile none swap sw 0 0' >> /etc/fstab",
                // Favour reclaiming page cache over swapping out live JVM heap; swap is the cushion,
                // not the plan.
                "echo 'vm.swappiness=10' > /etc/sysctl.d/99-indigoeln.conf",
                "sysctl -q -w vm.swappiness=10",

                // Format only on very first use; the volume survives instance replacement.
                "DEVICE=" + DATA_DEVICE,
                "MOUNT_POINT=" + DATA_MOUNT,
                "while [ ! -b $DEVICE ]; do sleep 1; done",
                "if ! blkid $DEVICE > /dev/null 2>&1; then mkfs.ext4 -F $DEVICE; fi",
                "mkdir -p $MOUNT_POINT",
                // No `|| true`: a silently failed mount would let Postgres initialize an empty PGDATA
                // on the root volume and come up healthy with no data.
                "mount $DEVICE $MOUNT_POINT",
                "grep -q \"$DEVICE\" /etc/fstab || echo \"$DEVICE $MOUNT_POINT ext4 defaults,nofail,x-systemd.device-timeout=30 0 2\" >> /etc/fstab",
                "chown -R 999:999 $MOUNT_POINT", // UID 999 = postgres in the official image

                "mkdir -p " + APP_DIR,
                "cat > " + APP_DIR + "/deploy.conf <<'EOF'",
                "CONFIG_BUCKET=" + props.storageBucketName(),
                "CONFIG_PREFIX=compose",
                "ENV_PARAM=" + composeEnvParameterName(props.envName()),
                "DB_SECRET=" + dbSecretName(props.envName()),
                "AWS_REGION=" + props.region(),
                "EOF",

                // Bootstrap: fetch deploy.sh (and the compose files) before the unit that runs it.
                "aws s3 sync s3://" + props.storageBucketName() + "/compose/ " + APP_DIR + "/ --region " + props.region(),
                "install -m 0644 " + APP_DIR + "/indigoeln.service /etc/systemd/system/indigoeln.service",
                "chmod +x " + APP_DIR + "/deploy.sh",
                "systemctl daemon-reload",
                "systemctl enable --now indigoeln.service"
        );
        return userData;
    }

    /** Deterministic so that user-data can reference it without depending on the construct. */
    public static String composeEnvParameterName(String envName) {
        return "/indigoeln/" + envName + "/compose-env";
    }

    /** Deterministic so that user-data can reference it without depending on the construct. */
    public static String dbSecretName(String envName) {
        return "indigoeln/" + envName + "/db";
    }

    public record Props(
            String envName,
            String region,
            String vpcId,
            String dataVolumeAz,
            String ec2KeyPair,
            String hostedZone,
            String hostedZoneName,
            List<String> securityGroups,
            String storageBucketName,
            String apiSecret,
            List<String> lambdaSubnets,
            boolean createS3Gateway
    ) {}
}

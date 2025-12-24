package com.epam.indigoeln.aws;

import lombok.Getter;
import lombok.Value;
import software.amazon.awscdk.NestedStack;
import software.amazon.awscdk.NestedStackProps;
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
import software.constructs.Construct;

import java.util.ArrayList;
import java.util.List;

public class InfraStack extends NestedStack {

    @Getter
    private final IVpc vpc;
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
//    @Getter
//    private final CfnOutput ec2PrivateIP;

    public InfraStack(final Construct scope, final String id, final Props props) {
        super(scope, id, props);

        vpc = Vpc.fromLookup(this, "vpc", VpcLookupOptions.builder().vpcId(props.getVpcId()).build());
        additionalSecurityGroups = new ArrayList<>();
        for (String groupId : props.getSecurityGroups()) {
            ISecurityGroup group = SecurityGroup.fromSecurityGroupId(this, "security-group-" + (additionalSecurityGroups.size() + 1), groupId);
            additionalSecurityGroups.add(group);
        }

        ec2KeyPair = KeyPair.fromKeyPairName(this, "ec2-key-pair", props.getEc2KeyPair());

        hostedZone = HostedZone.fromHostedZoneAttributes(this, "hosted-zone", HostedZoneAttributes.builder()
                .hostedZoneId(props.getHostedZone())
                .zoneName(props.getHostedZoneName())
                .build()
        );

        ec2SecurityGroup = SecurityGroup.Builder.create(this, "ec2-security-group")
                .vpc(vpc)
                .allowAllOutbound(true)
                .build();

        IManagedPolicy policy = ManagedPolicy.fromAwsManagedPolicyName("service-role/AmazonEC2ContainerServiceforEC2Role");
        Role ec2Role = Role.Builder.create(this, "ec2-role")
                .assumedBy(new ServicePrincipal("ec2.amazonaws.com"))
                .managedPolicies(List.of(policy))
                .build();

        LaunchTemplate launchTemplate = LaunchTemplate.Builder.create(this, "ec2-launch-template")
                .instanceType(InstanceType.of(InstanceClass.T3, InstanceSize.MICRO))
                .machineImage(EcsOptimizedImage.amazonLinux2023(AmiHardwareType.STANDARD))
                .userData(UserData.forLinux())
                .securityGroup(ec2SecurityGroup)
                .role(ec2Role)
                .keyPair(ec2KeyPair)
                .build();
        for (ISecurityGroup sg : additionalSecurityGroups) {
            launchTemplate.addSecurityGroup(sg);
        }

        AutoScalingGroup autoScalingGroup = AutoScalingGroup.Builder.create(this, "ec2-auto-scaling-group")
                .vpc(vpc)
                .launchTemplate(launchTemplate)
                .minCapacity(1)
                .maxCapacity(1)
                .desiredCapacity(1)
                .vpcSubnets(SubnetSelection.builder().subnetType(SubnetType.PUBLIC).build())
                .build();

//        new CfnOutput(this, "ec2-public-ip", CfnOutputProps.builder()
//                .value(autoScalingGroup.getInstances().get(0).getPublicIp())
//                .description("The public IP address of the EC2 instance")
//                .build());
//
//        new CfnOutput(this, "ec2-private-ip", CfnOutputProps.builder()
//                .value(autoScalingGroup.getInstances().get(0).getPrivateIp())
//                .description("The private IP address of the EC2 instance")
//                .build());

        ecsCluster = Cluster.Builder.create(this, "ecs-cluster")
                .vpc(vpc)
                .build();

        ecsCluster.addAsgCapacityProvider(AsgCapacityProvider.Builder.create(this, "ec2-capacity-provider")
                .autoScalingGroup(autoScalingGroup)
                .build());

        lambdaSecurityGroup = SecurityGroup.Builder.create(this, "lambda-security-group")
                .vpc(vpc)
                .allowAllOutbound(true)
                .build();
        ec2SecurityGroup.addIngressRule(lambdaSecurityGroup, Port.tcp(5432), "from-lambda");
    }

    @Value
    public static class Props implements NestedStackProps {

        String vpcId;
        String ec2KeyPair;
        String hostedZone;
        String hostedZoneName;
        List<String> securityGroups;
    }
}

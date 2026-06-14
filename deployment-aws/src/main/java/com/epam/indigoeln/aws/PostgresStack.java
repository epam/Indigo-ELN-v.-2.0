package com.epam.indigoeln.aws;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Value;
import software.amazon.awscdk.NestedStack;
import software.amazon.awscdk.NestedStackProps;
import software.amazon.awscdk.services.ec2.ISecurityGroup;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ecr.IRepository;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.rds.Credentials;
import software.amazon.awscdk.services.rds.DatabaseSecret;
import software.amazon.awscdk.services.servicediscovery.DnsRecordType;
import software.amazon.awscdk.services.servicediscovery.PrivateDnsNamespace;
import software.constructs.Construct;

import java.util.List;

import static com.epam.indigoeln.aws.util.Utils.entry;
import static com.epam.indigoeln.aws.util.Utils.mapOf;

public class PostgresStack extends NestedStack {

    @Getter
    private final DatabaseSecret dbSecret;

    public PostgresStack(final Construct scope, final String id, final Props props) {
        super(scope, id, props);

        dbSecret = DatabaseSecret.Builder.create(this, "db-secret")
                .username(props.getPostgresMasterUsername())
                .build();

        final TaskDefinition postgresTask = TaskDefinition.Builder.create(this, "ecs-task-postgres")
                .compatibility(Compatibility.EC2)
                .networkMode(NetworkMode.AWS_VPC)
                .cpu("256")
                .memoryMiB("512")
                .build();

        postgresTask.addVolume(Volume.builder()
                .name("postgres-data")
                .host(Host.builder()
                        .sourcePath("/data/postgres")
                        .build())
                .build());

        ContainerDefinition postgresContainer = postgresTask.addContainer("ecs-task-postgres-container", ContainerDefinitionOptions.builder()
                .image(ContainerImage.fromEcrRepository(props.getPostgresRepo(), props.getPostgresImageTag()))
                .environment(mapOf(
                        entry("POSTGRES_USER", Credentials.fromSecret(dbSecret).getUsername()),
                        entry("POSTGRES_PASSWORD", Credentials.fromSecret(dbSecret).getPassword().unsafeUnwrap()),
                        entry("PGDATA", "/var/lib/postgresql/data/pgdata")
                ))
                .portMappings(List.of(PortMapping.builder().containerPort(5432).build()))
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder().streamPrefix("postgres").build()))
                .build());
        postgresContainer.addMountPoints(MountPoint.builder()
                .sourceVolume("postgres-data")
                .containerPath("/var/lib/postgresql/data")
                .readOnly(false)
                .build());

        postgresTask.addContainer("ecs-task-pgbouncer-container", ContainerDefinitionOptions.builder()
                .image(ContainerImage.fromRegistry("edoburu/pgbouncer:latest"))
                .environment(mapOf(
                        entry("DB_HOST", "127.0.0.1"), // explicit IPv4 to avoid accidentally resolving localhost to ::1
                        entry("DB_PORT", "5432"),
                        entry("DB_USER", Credentials.fromSecret(dbSecret).getUsername()),
                        entry("DB_PASSWORD", Credentials.fromSecret(dbSecret).getPassword().unsafeUnwrap()),
                        entry("DB_NAME", Credentials.fromSecret(dbSecret).getUsername()),
                        entry("POOL_MODE", "transaction"),
                        entry("MAX_CLIENT_CONN", "40"),
                        entry("DEFAULT_POOL_SIZE", "10"),
                        entry("AUTH_TYPE", "scram-sha-256"),
                        entry("LISTEN_PORT", "6432")
                ))
                .portMappings(List.of(PortMapping.builder().containerPort(6432).build()))
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder().streamPrefix("pgbouncer").build()))
                .build());

        postgresTask.addContainer("ecs-task-pgbouncer-container-2", ContainerDefinitionOptions.builder()
                .image(ContainerImage.fromRegistry("edoburu/pgbouncer:latest"))
                .environment(mapOf(
                        entry("DB_HOST", "127.0.0.1"), // explicit IPv4 to avoid accidentally resolving localhost to ::1
                        entry("DB_PORT", "5432"),
                        entry("DB_USER", Credentials.fromSecret(dbSecret).getUsername()),
                        entry("DB_PASSWORD", Credentials.fromSecret(dbSecret).getPassword().unsafeUnwrap()),
                        entry("DB_NAME", "signature"),
                        entry("POOL_MODE", "transaction"),
                        entry("MAX_CLIENT_CONN", "40"),
                        entry("DEFAULT_POOL_SIZE", "10"),
                        entry("AUTH_TYPE", "scram-sha-256"),
                        entry("LISTEN_PORT", "6433")
                ))
                .portMappings(List.of(PortMapping.builder().containerPort(6433).build()))
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder().streamPrefix("pgbouncer2").build()))
                .build());

        Ec2Service.Builder.create(this, "ecs-postgres-service")
                .cluster(props.getEcsCluster())
                .taskDefinition(postgresTask)
                .minHealthyPercent(0)
                .desiredCount(1)
                // awsvpc: place the task ENI in the same public subnet as the EC2 host
                .vpcSubnets(SubnetSelection.builder().subnetType(SubnetType.PUBLIC).build())
                .securityGroups(ImmutableList.<ISecurityGroup>builder().add(props.ec2SecurityGroup).addAll(props.additionalSecurityGroups).build())
                .cloudMapOptions(CloudMapOptions.builder()
                        .cloudMapNamespace(props.getPrivateDnsNamespace())
                        .name("pgbouncer")
                        // awsvpc gives each task its own IP → A record works, Lambda resolves pgbouncer.indigoeln.local directly
                        .dnsRecordType(DnsRecordType.A)
                        .build())
                .build();
    }

    @Value
    public static class Props implements NestedStackProps {

        PrivateDnsNamespace privateDnsNamespace;
        String postgresMasterUsername;
        ICluster ecsCluster;
        ISecurityGroup ec2SecurityGroup;
        List<ISecurityGroup> additionalSecurityGroups;
        IRepository postgresRepo;
        String postgresImageTag;
    }
}

package com.epam.indigoeln.aws;

import lombok.Getter;
import lombok.Value;
import software.amazon.awscdk.NestedStack;
import software.amazon.awscdk.NestedStackProps;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.rds.Credentials;
import software.amazon.awscdk.services.rds.DatabaseSecret;
import software.constructs.Construct;

import java.util.List;

import static com.epam.indigoeln.aws.Utils.mapOf;

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
                .cpu("256")
                .memoryMiB("512")
                .build();

        postgresTask.addContainer("ecs-task-postgres-container", ContainerDefinitionOptions.builder()
                .image(ContainerImage.fromRegistry("public.ecr.aws/docker/library/postgres:17.4"))
                .environment(mapOf(
                        "POSTGRES_USER", Credentials.fromSecret(dbSecret).getUsername(),
                        "POSTGRES_PASSWORD", Credentials.fromSecret(dbSecret).getPassword().unsafeUnwrap()
                ))
                .portMappings(List.of(PortMapping.builder().containerPort(5432).hostPort(5432).build()))
                .build());

        // TODO use EBS for persistent storage

        Ec2Service.Builder.create(this, "ecs-postgres-service")
                .cluster(props.getEcsCluster())
                .taskDefinition(postgresTask)
                .minHealthyPercent(0)
                .desiredCount(1)
                .build();
    }

    @Value
    public static class Props implements NestedStackProps {

        String postgresMasterUsername;
        ICluster ecsCluster;
    }
}

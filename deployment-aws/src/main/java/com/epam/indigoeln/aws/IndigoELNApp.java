package com.epam.indigoeln.aws;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.PermissionsBoundary;
import software.amazon.awscdk.StackProps;

import java.util.List;
import java.util.Map;

public class IndigoELNApp {

    public static void main(final String[] args) {
        GlobalParameters globalParameters = GlobalParameters.load();

        App app = new App();

        PermissionsBoundary permissionsBoundary = globalParameters.getPermissionBoundary() != null
                ? PermissionsBoundary.fromArn(globalParameters.getPermissionBoundary())
                : null;

        BuildStack buildStack = new BuildStack(app, "indigoeln-build-stack", StackProps.builder()
                .env(Environment.builder()
                        .account(globalParameters.getAccount())
                        .region(globalParameters.getRegion())
                        .build()
                )
                .permissionsBoundary(permissionsBoundary)
                .tags(Map.of("project", "IndigoELN", "stage", "common"))
                .build()
                , globalParameters
        );

        SonarQubeStack.Props sonarQubeProps = new SonarQubeStack.Props(
                globalParameters.getSonarDomainName(),
                globalParameters.getSonarPostgresMasterUsername(),
                globalParameters.getSonarImage(),
                globalParameters.getVpc(),
                globalParameters.getHostedZone(),
                globalParameters.getHostedZoneName(),
                globalParameters.getEc2KeyPair(),
                globalParameters.getSecurityGroups()
        );
        SonarQubeStack sonarQubeStack = new SonarQubeStack(app, "indigoeln-sonarqube", StackProps.builder()
                .env(Environment.builder()
                        .account(globalParameters.getAccount())
                        .region(globalParameters.getRegion())
                        .build()
                )
                .permissionsBoundary(permissionsBoundary)
                .tags(Map.of("project", "IndigoELN", "stage", "common", "component", "sonarqube"))
                .build()
                , sonarQubeProps);

        for (String envName : List.of("dev")) {
            StageParameters stageParameters = StageParameters.load(envName);

            MainStack mainStack = new MainStack(app, "indigoeln-" + envName, StackProps.builder()
                    .env(Environment.builder()
                            .account(stageParameters.getAccount())
                            .region(stageParameters.getRegion())
                            .build()
                    )
                    .permissionsBoundary(permissionsBoundary)
                    .tags(Map.of("project", "IndigoELN", "stage", envName))
                    .build()
                    , envName
                    , stageParameters);
        }

        app.synth();
    }
}

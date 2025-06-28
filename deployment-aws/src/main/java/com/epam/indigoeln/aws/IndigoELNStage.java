package com.epam.indigoeln.aws;

import software.amazon.awscdk.Aspects;
import software.amazon.awscdk.Stage;
import software.amazon.awscdk.StageProps;
import software.amazon.awscdk.Tags;
import software.amazon.awscdk.services.iam.CfnRole;
import software.constructs.Construct;

public class IndigoELNStage extends Stage {

    public IndigoELNStage(Construct scope, String id, software.amazon.awscdk.Environment env) {
        super(scope, id, StageProps.builder().env(env).build());

        new MainStack(this, "main-stack", new MainStack.Props(
                id
        ));

        Tags.of(this).add("project", "IndigoELN");
        Tags.of(this).add("stage", id);

        String permissionBoundary = StageParameters.load(id).getPermissionBoundary();

        Aspects.of(this).add(node -> {
            if (node instanceof CfnRole role && role.getPermissionsBoundary() == null && permissionBoundary != null) {
                role.setPermissionsBoundary(permissionBoundary);
            }
        });
    }
}

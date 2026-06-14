package com.epam.indigoeln.aws;

import com.epam.indigoeln.aws.util.Utils;
import software.amazon.awscdk.Stage;
import software.amazon.awscdk.StageProps;
import software.amazon.awscdk.Tags;
import software.constructs.Construct;

public class IndigoELNStage extends Stage {

    public IndigoELNStage(Construct scope, String id, software.amazon.awscdk.Environment env, GlobalParameters params) {
        super(scope, id, StageProps.builder().env(env).build());

        new MainStack(this, "main-stack", new MainStack.Props(
                id
        ));

        Tags.of(this).add("project", "IndigoELN");
        Tags.of(this).add("stage", id);

        Utils.applyPermissionBoundary(this, params.getPermissionBoundary());
    }
}

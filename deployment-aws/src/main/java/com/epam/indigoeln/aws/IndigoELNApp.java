package com.epam.indigoeln.aws;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;

import java.util.List;

public class IndigoELNApp {

    public static void main(final String[] args) {
        GlobalParameters globalParameters = GlobalParameters.load();

        App app = new App();

        new BuildStack(app, "indigoeln-build-stack", new BuildStack.Props(
                globalParameters.getPermissionBoundary()
        ));

        for (String env : List.of("dev")) {
            StageParameters stageParameters = StageParameters.load(env);
            new IndigoELNStage(
                    app,
                    env,
                    Environment.builder()
                        .account(stageParameters.getAccount())
                        .region(stageParameters.getRegion())
                        .build(),
                    globalParameters
            );
        }

        app.synth();
    }
}

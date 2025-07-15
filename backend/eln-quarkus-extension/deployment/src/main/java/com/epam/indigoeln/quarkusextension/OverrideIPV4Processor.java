package com.epam.indigoeln.quarkusextension;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.SystemPropertyBuildItem;
import io.quarkus.deployment.pkg.steps.NativeBuild;
import jakarta.annotation.Priority;

public class OverrideIPV4Processor {

    private static final String FEATURE = "override-ipv4";

    @BuildStep
    public FeatureBuildItem produceFeatureBuildItem() {
        return new FeatureBuildItem(FEATURE);
    }

    @Priority(Integer.MIN_VALUE)
    @BuildStep(onlyIf = NativeBuild.class)
    void overrideIpv4Only(BuildProducer<SystemPropertyBuildItem> systemProperty) {
        systemProperty.produce(new SystemPropertyBuildItem("java.net.preferIPv4Stack", "false"));
        systemProperty.produce(new SystemPropertyBuildItem("java.net.preferIPv4Stack1", "false"));
    }

    @Priority(Integer.MAX_VALUE)
    @BuildStep(onlyIf = NativeBuild.class)
    void override2Ipv4Only(BuildProducer<SystemPropertyBuildItem> systemProperty) {
        systemProperty.produce(new SystemPropertyBuildItem("java.net.preferIPv4Stack", "false"));
        systemProperty.produce(new SystemPropertyBuildItem("java.net.preferIPv4Stack1", "false"));
    }
}

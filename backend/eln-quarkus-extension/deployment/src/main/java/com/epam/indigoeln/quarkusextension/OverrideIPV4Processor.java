package com.epam.indigoeln.quarkusextension;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.SystemPropertyBuildItem;
import io.quarkus.deployment.pkg.steps.NativeBuild;
import io.quarkus.deployment.pkg.steps.NativeOrNativeSourcesBuild;
import jakarta.annotation.Priority;

// The point of this extension is to override the default IPv4-only set by io.quarkus:quarkus-amazon-lambda-common.
// Which likely enforces IPv4-only because previosly AWS Lambda did not support IPv6.
// However, it works fine now, and we need IPv6 to be able to use Egress-only internet gateway instead of NAT gateway.
public class OverrideIPV4Processor {

    private static final String FEATURE = "override-ipv4";

    @BuildStep
    public FeatureBuildItem produceFeatureBuildItem() {
        return new FeatureBuildItem(FEATURE);
    }

    @Priority(Integer.MIN_VALUE)
    @BuildStep(onlyIf = NativeOrNativeSourcesBuild.class)
    void overrideIpv4Only(BuildProducer<SystemPropertyBuildItem> systemProperty) {
        systemProperty.produce(new SystemPropertyBuildItem("java.net.preferIPv4Stack", "false"));
        systemProperty.produce(new SystemPropertyBuildItem("java.net.preferIPv6Addresses", "true"));
    }

    @Priority(Integer.MAX_VALUE)
    @BuildStep(onlyIf = NativeOrNativeSourcesBuild.class)
    void override2Ipv4Only(BuildProducer<SystemPropertyBuildItem> systemProperty) {
        systemProperty.produce(new SystemPropertyBuildItem("java.net.preferIPv4Stack", "false"));
        systemProperty.produce(new SystemPropertyBuildItem("java.net.preferIPv6Addresses", "true"));
    }
}

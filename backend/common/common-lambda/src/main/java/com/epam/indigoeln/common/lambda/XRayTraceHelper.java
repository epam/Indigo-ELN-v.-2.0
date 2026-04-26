package com.epam.indigoeln.common.lambda;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Subsegment;
import com.epam.indigoeln.common.config.TraceHelper;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;

@ApplicationScoped
public class XRayTraceHelper implements TraceHelper {

    @Override
    public boolean isActive() {
        return AWSXRay.getCurrentSegmentOptional().isPresent();
    }

    @Override
    public Object begin(String name) {
        return AWSXRay.beginSubsegment(name);
    }

    @Override
    public void end(Object span) {
        AWSXRay.endSubsegment();
    }

    @Override
    public void attachException(Object span, Throwable exception) {
        ((Subsegment) span).addException(exception);
        ((Subsegment) span).setFault(true);
    }

    @Override
    public void attachMetadata(String namespace, Map<String, Object> metadata) {
        AWSXRay.getCurrentSubsegmentOptional()
                .ifPresentOrElse(
                        sub -> sub.putMetadata(namespace, metadata),
                        ()  -> AWSXRay.getCurrentSegmentOptional()
                                      .ifPresent(seg -> seg.putMetadata(namespace, metadata))
                );
    }
}

package com.epam.indigoeln.common.config;

import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;

@DefaultBean
@ApplicationScoped
public class NoOpTraceHelper implements TraceHelper {

    // singleton no-op span — allocation-free
    private static final Object NO_OP_SPAN = new Object();

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public Object begin(String name) {
        return NO_OP_SPAN;
    }

    @Override
    public void end(Object span) {
        // do nothing
    }

    @Override
    public void attachException(Object span, Throwable exception) {
        // do nothing
    }

    @Override
    public void attachMetadata(String namespace, Map<String, Object> metadata) {
        // do nothing
    }
}

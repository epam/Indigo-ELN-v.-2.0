package com.epam.indigoeln.common.config;

import java.util.Map;

public interface TraceHelper {

    boolean isActive();

    Object begin(String name);

    void end(Object span);

    void attachException(Object span, Throwable exception);

    void attachMetadata(String namespace, Map<String, Object> metadata);
}

package com.epam.indigoeln.eln.quarkus.cachecontrol;

import io.quarkus.runtime.annotations.Recorder;

import java.util.Map;
import java.util.function.Supplier;

@Recorder
public class CachedEndpointsRecorder {

    /**
     * Called from the {@code @BuildStep} at {@code STATIC_INIT}.
     * Returns a {@link Supplier} that Quarkus Arc uses to instantiate the synthetic
     * {@link CachedEndpoints} CDI bean — the map is captured in the closure and
     * baked into the native image, so no reflection is needed at runtime.
     */
    public Supplier<CachedEndpoints> create(Map<String, Integer> maxAgeByMethod) {
        return () -> {
            CachedEndpoints endpoints = new CachedEndpoints();
            endpoints.init(maxAgeByMethod);
            return endpoints;
        };
    }
}


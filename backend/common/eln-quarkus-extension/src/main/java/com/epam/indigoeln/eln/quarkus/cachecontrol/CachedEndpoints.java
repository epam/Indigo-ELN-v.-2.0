package com.epam.indigoeln.eln.quarkus.cachecontrol;

import java.util.Map;

/**
 * Holds the build-time-computed map of resource method keys → Cache-Control max-age seconds.
 * Key format: "fully.qualified.ResourceClass#methodName"
 * Populated as a synthetic CDI bean by {@code CacheControlProcessor} during Quarkus augmentation.
 */
public class CachedEndpoints {

    private Map<String, Integer> maxAgeByMethod = Map.of();

    public void init(Map<String, Integer> maxAgeByMethod) {
        this.maxAgeByMethod = Map.copyOf(maxAgeByMethod);
    }

    public Integer getMaxAge(String key) {
        return maxAgeByMethod.get(key);
    }
}


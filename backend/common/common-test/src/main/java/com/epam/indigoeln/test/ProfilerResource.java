package com.epam.indigoeln.test;

import lombok.extern.slf4j.Slf4j;
import one.profiler.AsyncProfiler;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ProfilerResource implements BeforeAllCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(ProfilerResource.class);

    private static final boolean enableProfiler = "true".equalsIgnoreCase(System.getenv("ENABLE_PROFILER"));

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (enableProfiler) {
            context.getRoot().getStore(NAMESPACE).getOrComputeIfAbsent(
                    "profiler-resource",
                    key -> {
                        try {
                            return new ResourceImpl();
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to start profiler: " + e.getMessage(), e);
                        }
                    }
            );
        }
    }
}

@Slf4j
class ResourceImpl implements ExtensionContext.Store.CloseableResource {

    ResourceImpl() throws Exception {
        AsyncProfiler.getInstance().execute("start,jfr,event=cpu,file=tests.jfr");
    }

    @Override
    public void close() {
        AsyncProfiler.getInstance().stop();
    }
}

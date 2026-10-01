package com.epam.indigoeln.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.TestAbortedException;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@Slf4j
public abstract class SharedTestEnvironment implements AutoCloseable {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(SharedTestEnvironment.class);
    private static final String INSTANCE = "instance";

    protected final AtomicBoolean started = new AtomicBoolean();
    protected final AtomicBoolean stopped = new AtomicBoolean();
    protected final AtomicBoolean failed = new AtomicBoolean();

    public static void install(ExtensionContext context, Supplier<? extends SharedTestEnvironment> constructor) {
        BaseTest.setIntegrationTest(true);
        SharedTestEnvironment instance = (SharedTestEnvironment) context.getRoot().getStore(NAMESPACE).computeIfAbsent(INSTANCE, _ -> constructor.get());
        if (instance.failed.get()) {
            throw new TestAbortedException("Shared test environment not started");
        }
        if (instance.started.compareAndSet(false, true)) {
            try {
                log.info("Starting test environment");
                instance.start();
                log.info("Test environment started");
            } catch (Exception e) {
                log.error("Failed to start test environment", e);
                instance.failed.set(true);
                throw new RuntimeException("Failed to start test environment", e);
            }
        }
    }

    protected abstract void start() throws Exception;

    protected abstract void stop() throws Exception;

    @Override
    public void close() throws Exception {
        if (stopped.compareAndSet(false, true)) {
            log.info("Stopping test environment");
            stop();
            log.info("Test environment stopped");
        }
    }
}

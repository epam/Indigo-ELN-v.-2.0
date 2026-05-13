package com.epam.indigoeln.test;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.security.TestSecurity;
import lombok.Setter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.support.AnnotationSupport;

import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Timeout(value = 30, unit = TimeUnit.SECONDS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(ProfilerResource.class)
public abstract class BaseTest {

    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_DISPLAY_NAME = "Administrator";

    @Setter
    protected static boolean integrationTest = false;

    protected final AtomicReference<String> username = new AtomicReference<>();

    @TestHTTPResource("/")
    URI serverBaseURL;

    protected URI getServerURL() {
        if (integrationTest) {
            return URI.create("http://%s:%s".formatted(System.getProperty("quarkus.http.test-host"), System.getProperty("quarkus.http.test-port")));
        }
        return serverBaseURL;
    }

    @BeforeEach
    void setupBase(TestInfo testInfo) {
        username.set(null);
        TestSecurity testSecurity = AnnotationSupport.findAnnotation(testInfo.getTestMethod().get(), TestSecurity.class)
                .or(() -> AnnotationSupport.findAnnotation(testInfo.getTestClass().get(), TestSecurity.class))
                .orElse(null);
        if (testSecurity != null) {
            username.set(testSecurity.user());
        }
    }

    protected <T> T buildClient(Class<T> klass) {
        AtomicReference<String> authorization = new AtomicReference<>();
        return FeignUtil.buildFeignClient(getServerURL(), klass, username, authorization);
    }

    protected void withUser(String username, Runnable runnable) {
        withUser(username, () -> {
            runnable.run();
            return null;
        });
    }

    @SneakyThrows
    protected <T> T withUser(String username, Callable<T> callable) {
        String oldUsername = this.username.get();
        try {
            this.username.set(username);
            return callable.call();
        } finally {
            this.username.set(oldUsername);
        }
    }
}

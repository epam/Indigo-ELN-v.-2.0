package com.epam.indigoeln.test;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.*;
import org.junit.platform.commons.support.AnnotationSupport;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

//@Timeout(30)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class BaseTest {

    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_DISPLAY_NAME = "Administrator";

    protected final boolean integrationTest = AnnotationSupport.isAnnotated(getClass(), QuarkusIntegrationTest.class);

    protected final AtomicReference<String> username = new AtomicReference<>();

    @TestHTTPResource("/")
    URI serverBaseURL;

    protected URI getServerURL() {
        if (integrationTest) {
             return URI.create("http://localhost:28080");
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
        String oldUsername = this.username.get();
        try {
            this.username.set(username);
            runnable.run();
        } finally {
            this.username.set(oldUsername);
        }
    }
}

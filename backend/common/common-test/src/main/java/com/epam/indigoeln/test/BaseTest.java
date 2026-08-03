package com.epam.indigoeln.test;

import com.google.common.base.Suppliers;
import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.security.TestSecurity;
import lombok.Setter;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.*;
import org.junit.platform.commons.support.AnnotationSupport;

import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Timeout(value = 30, unit = TimeUnit.SECONDS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class BaseTest {

    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_DISPLAY_NAME = "Administrator";

    protected final static Supplier<AgroalDataSource> databasePool = Suppliers.memoize(BaseTest::createDatabasePool);

    @Setter
    protected static boolean integrationTest = false;

    protected final AtomicReference<@Nullable String> username = new AtomicReference<>();

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
        AtomicReference<@Nullable String> authorization = new AtomicReference<>();
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

    @SneakyThrows
    private static AgroalDataSource createDatabasePool() {
        String jdbcUrl = integrationTest ? System.getProperty("eln.test.datasource.jdbc-url") : ConfigProvider.getConfig().getValue("quarkus.datasource.jdbc.url", String.class);
        String username = integrationTest ? System.getProperty("eln.test.datasource.username") : ConfigProvider.getConfig().getValue("quarkus.datasource.username", String.class);
        String password = integrationTest ? System.getProperty("eln.test.datasource.password") : ConfigProvider.getConfig().getValue("quarkus.datasource.password", String.class);
        return AgroalDataSource.from(new AgroalDataSourceConfigurationSupplier()
                .connectionPoolConfiguration(cp -> cp
                        .minSize(0)
                        .maxSize(2)
                        .connectionFactoryConfiguration(cf -> cf
                                .jdbcUrl(jdbcUrl)
                                .principal(new NamePrincipal(username))
                                .credential(new SimplePassword(password)))));
    }
}

package com.epam.indigoeln.eln;

import com.epam.indigoeln.eln.client.*;
import com.epam.indigoeln.eln.controller.MiscResource;
import com.epam.indigoeln.eln.util.FeignUtil;
import com.epam.indigoeln.eln.util.TestHelper;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.*;
import org.junit.platform.commons.support.AnnotationSupport;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class BaseTest {

    @TestHTTPResource
    @TestHTTPEndpoint(MiscResource.class)
    URI serverURL;

    protected final boolean integrationTest = AnnotationSupport.isAnnotated(getClass(), QuarkusIntegrationTest.class);

    protected ProjectsClient projectsClient;
    protected NotebooksClient notebooksClient;
    protected ExperimentsClient experimentsClient;
    protected TemplatesClient templatesClient;
    protected MiscClient miscClient;
    protected UsersClient usersClient;

    protected TestHelper testHelper;

    private final AtomicReference<String> username = new AtomicReference<>();

    @BeforeAll
    void setupAllBase() throws Exception {
        System.out.println("BaseTest.setupAllBase: " + serverURL);
        if (integrationTest) {
            serverURL = URI.create("http://localhost:8081");
        }
        URI baseURL = serverURL.resolve("/");

        AtomicReference<String> authorization = new AtomicReference<>();
        projectsClient = FeignUtil.buildFeignClient(baseURL, ProjectsClient.class, username, authorization);
        notebooksClient = FeignUtil.buildFeignClient(baseURL, NotebooksClient.class, username, authorization);
        experimentsClient = FeignUtil.buildFeignClient(baseURL, ExperimentsClient.class, username, authorization);
        templatesClient = FeignUtil.buildFeignClient(baseURL, TemplatesClient.class, username, authorization);
        miscClient = FeignUtil.buildFeignClient(baseURL, MiscClient.class, username, authorization);
        usersClient = FeignUtil.buildFeignClient(baseURL, UsersClient.class, username, authorization);

        miscClient.migrate(); // TODO remove, not needed?
        testHelper = new TestHelper(usersClient, miscClient);
        testHelper.createTestUsers();
    }

    @BeforeEach
    void setupBase(TestInfo testInfo) {
        username.set(null);
        TestSecurity testSecurity = AnnotationSupport.findAnnotation(testInfo.getTestMethod().get(), TestSecurity.class)
                .or(() -> AnnotationSupport.findAnnotation(testInfo.getTestClass().get(), TestSecurity.class))
                .orElse(null);
        if (testSecurity != null && integrationTest) {
            username.set(testSecurity.user());
        }
    }
}

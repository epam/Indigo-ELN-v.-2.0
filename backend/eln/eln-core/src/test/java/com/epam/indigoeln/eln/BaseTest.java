package com.epam.indigoeln.eln;

import com.epam.indigoeln.eln.client.*;
import com.epam.indigoeln.eln.controller.MiscResource;
import com.epam.indigoeln.eln.model.TemplateComponent;
import com.epam.indigoeln.eln.model.TemplateDetailsDTO;
import com.epam.indigoeln.eln.model.TemplateRequest;
import com.epam.indigoeln.eln.util.FeignUtil;
import com.epam.indigoeln.eln.util.TestHelper;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import lombok.Getter;
import org.junit.jupiter.api.*;
import org.junit.platform.commons.support.AnnotationSupport;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class BaseTest {

    @TestHTTPResource
    @TestHTTPEndpoint(MiscResource.class)
    URI serverURL;

    @Inject
    CognitoIdentityProviderClient cognito;

    protected final boolean integrationTest = AnnotationSupport.isAnnotated(getClass(), QuarkusIntegrationTest.class);

    protected ProjectsClient projectsClient;
    protected NotebooksClient notebooksClient;
    protected ExperimentsClient experimentsClient;
    protected TemplatesClient templatesClient;
    protected MiscClient miscClient;
    protected UsersClient usersClient;

    protected TestHelper testHelper;

    private final AtomicReference<String> username = new AtomicReference<>();

    private int lastUsedNotebookNumber = 0;

    @Getter
    private UUID emptyTemplateID;

    @BeforeAll
    void setupAllBase() throws Exception {
        System.out.println("BaseTest.setupAllBase: " + serverURL);
        if (integrationTest) {
            serverURL = URI.create("http://localhost:8082"); // !!! 8081
        }
        URI baseURL = serverURL.resolve("/");

        AtomicReference<String> authorization = new AtomicReference<>();
        projectsClient = FeignUtil.buildFeignClient(baseURL, ProjectsClient.class, username, authorization);
        notebooksClient = FeignUtil.buildFeignClient(baseURL, NotebooksClient.class, username, authorization);
        experimentsClient = FeignUtil.buildFeignClient(baseURL, ExperimentsClient.class, username, authorization);
        templatesClient = FeignUtil.buildFeignClient(baseURL, TemplatesClient.class, username, authorization);
        miscClient = FeignUtil.buildFeignClient(baseURL, MiscClient.class, username, authorization);
        usersClient = FeignUtil.buildFeignClient(baseURL, UsersClient.class, username, authorization);
        miscClient.migrate();
        testHelper = new TestHelper(usersClient, miscClient);
        testHelper.cleanupDatabase();
        testHelper.createTestUsers();
        emptyTemplateID = templatesClient.createTemplate(new TemplateRequest("Empty template", List.of(new TemplateComponent.Attachments()))).getId();
    }

    @BeforeEach
    void setupBase(TestInfo testInfo) {
        username.set(null);
        TestSecurity testSecurity = AnnotationSupport.findAnnotation(testInfo.getTestMethod().get(), TestSecurity.class)
                .or(() -> AnnotationSupport.findAnnotation(testInfo.getTestClass().get(), TestSecurity.class))
                .orElse(null);
        System.out.println("!!! testSecurity: " + testSecurity);
        if (testSecurity != null) {
            username.set(testSecurity.user());
        }
    }

    protected String nextNotebookName() {
        return "%08d".formatted(++lastUsedNotebookNumber);
    }
}

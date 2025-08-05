package com.epam.indigoeln.eln;

import com.epam.indigoeln.eln.client.*;
import com.epam.indigoeln.eln.controller.MiscResource;
import com.epam.indigoeln.eln.model.TemplateComponent;
import com.epam.indigoeln.eln.model.TemplateRequest;
import com.epam.indigoeln.eln.util.FeignUtil;
import com.epam.indigoeln.eln.util.TestHelper;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.security.TestSecurity;
import lombok.Getter;
import org.junit.jupiter.api.*;
import org.junit.platform.commons.support.AnnotationSupport;

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

    protected final boolean integrationTest = AnnotationSupport.isAnnotated(getClass(), QuarkusIntegrationTest.class);

    protected ProjectClient projectClient;
    protected NotebookClient notebookClient;
    protected ExperimentClient experimentClient;
    protected TemplateClient templateClient;
    protected SignatureTemplateClient signatureTemplateClient;
    protected CompoundClient compoundClient;
    protected MiscClient miscClient;
    protected TestSupportClient testSupportClient;
    protected UserClient userClient;
    protected DictionaryClient dictionaryClient;
    protected RoleClient roleClient;
    protected GlobalSearchClient globalSearchClient;

    protected TestHelper testHelper;

    private final AtomicReference<String> username = new AtomicReference<>();

    private int lastUsedNotebookNumber = 0;

    @Getter
    private UUID emptyTemplateID;

    @BeforeAll
    void setupAllBase() throws Exception {
        System.out.println("BaseTest.setupAllBase: " + serverURL);
        if (integrationTest) {
            serverURL = URI.create("http://localhost:8081");
        }
        URI baseURL = serverURL.resolve("/");

        AtomicReference<String> authorization = new AtomicReference<>();
        projectClient = FeignUtil.buildFeignClient(baseURL, ProjectClient.class, username, authorization);
        notebookClient = FeignUtil.buildFeignClient(baseURL, NotebookClient.class, username, authorization);
        experimentClient = FeignUtil.buildFeignClient(baseURL, ExperimentClient.class, username, authorization);
        templateClient = FeignUtil.buildFeignClient(baseURL, TemplateClient.class, username, authorization);
        signatureTemplateClient = FeignUtil.buildFeignClient(baseURL, SignatureTemplateClient.class, username, authorization);
        compoundClient = FeignUtil.buildFeignClient(baseURL, CompoundClient.class, username, authorization);
        miscClient = FeignUtil.buildFeignClient(baseURL, MiscClient.class, username, authorization);
        userClient = FeignUtil.buildFeignClient(baseURL, UserClient.class, username, authorization);
        dictionaryClient = FeignUtil.buildFeignClient(baseURL, DictionaryClient.class, username, authorization);
        roleClient = FeignUtil.buildFeignClient(baseURL, RoleClient.class, username, authorization);
        testSupportClient = FeignUtil.buildFeignClient(baseURL, TestSupportClient.class, username, authorization);
        globalSearchClient = FeignUtil.buildFeignClient(baseURL, GlobalSearchClient.class, username, authorization);
        miscClient.migrate();
        testHelper = new TestHelper(userClient, testSupportClient, username);
        testHelper.cleanupDatabase();
        testHelper.createTestUsers();
        emptyTemplateID = templateClient.createTemplate(new TemplateRequest("Empty template", List.of(new TemplateComponent.Attachments()))).getId();
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

    protected String nextNotebookName() {
        return "%08d".formatted(++lastUsedNotebookNumber);
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

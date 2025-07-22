package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.client.*;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.util.FeignUtil;
import com.epam.indigoeln.eln.util.TestHelper;
import io.quarkus.test.junit.QuarkusTest;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static com.epam.indigoeln.eln.util.TestHelper.*;


@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InsertTestDataTest {

    private final Random random = new Random();

    ProjectClient projectClient;
    NotebookClient notebookClient;
    ExperimentClient experimentClient;
    MiscClient miscClient;
    UserClient userClient;
    TemplateClient templateClient;
    DictionaryClient dictionaryClient;

    @BeforeEach
    void setup() {
        URI baseURI = URI.create("https://indigo-eln-dev.test.lifescience.opensource.epam.com/");
        AtomicReference<String> testUsername = new AtomicReference<>();
        AtomicReference<String> authorization = new AtomicReference<>(
                "Bearer eyJraWQiOiJUbTFZSmg5UUJjZkQrVnBKVlc1WEQ3UEV5NEh1d2gxeUlvVlZwcmYxb0UwPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiI3NDA4ODQ0OC1hMDgxLTcwZWUtMjNiNS05MTQ0N2U1MGUwNDciLCJpc3MiOiJodHRwczpcL1wvY29nbml0by1pZHAudXMtZWFzdC0xLmFtYXpvbmF3cy5jb21cL3VzLWVhc3QtMV82RGlyZ3RRMXAiLCJjbGllbnRfaWQiOiJhNGtraDAwb2IyM2w3dXBtODhoaDNtbWo1Iiwib3JpZ2luX2p0aSI6ImQzMWQ0ZGJkLWUxNzAtNDQxNi1hMTAwLWVhYWZlZGZiM2MzMiIsImV2ZW50X2lkIjoiYWFjYTFiOTMtZTA1Ny00ZTE3LWJkNzEtZTVlNDdhNGJlYjFkIiwidG9rZW5fdXNlIjoiYWNjZXNzIiwic2NvcGUiOiJhd3MuY29nbml0by5zaWduaW4udXNlci5hZG1pbiIsImF1dGhfdGltZSI6MTc1MjAwODkwMCwiZXhwIjoxNzUyMDEyNTAwLCJpYXQiOjE3NTIwMDg5MDAsImp0aSI6ImM3OGIxMWQ1LThlZjktNGVkOC04MWY3LWZmZDczMDg5NzMzNiIsInVzZXJuYW1lIjoiYWxpY2VAZWxuLmNvbSJ9.Qxmt3QqxspaqF8TXUccsF8TBAtHP_s-QN2gGji8aCetMqv6-TOy6ECmk8buwSsqmSLYEqd4ueYxiJzrzeLZDssQPgFWSJFvBcX4dUIc5zsNzba49MBCaK0Hbyk4YnTcXOVbCa-5ITuI_5HTMP6iBmsewVNjH93vfxcxFzdPna8msXgPQfdGTLyhYpsENfX7e2tlHwAfSYQdRYREarQsb4tnJlCT9TUfnQLk3xeDsloOjwAU2a9sxpDbaGZeYBl17ErJ4NHJmNZZ1hLpzrwyJ3ZCvaBvWSoDs_VJ33LhlF7ZZwn6bIS6Y-EL8Hsshjp7UqXV4krfJDhoYQhRAn0_0fw"
        );
        projectClient = FeignUtil.buildFeignClient(baseURI, ProjectClient.class, testUsername, authorization);
        notebookClient = FeignUtil.buildFeignClient(baseURI, NotebookClient.class, testUsername, authorization);
        experimentClient = FeignUtil.buildFeignClient(baseURI, ExperimentClient.class, testUsername, authorization);
        miscClient = FeignUtil.buildFeignClient(baseURI, MiscClient.class, testUsername, authorization);
        templateClient = FeignUtil.buildFeignClient(baseURI, TemplateClient.class, testUsername, authorization);
        dictionaryClient = FeignUtil.buildFeignClient(baseURI, DictionaryClient.class, testUsername, authorization);
        userClient = FeignUtil.buildFeignClient(baseURI, UserClient.class, testUsername, authorization);
    }

//    @Test
    @Order(1)
    void flyway() {
        Object result = miscClient.migrate();
        System.out.println(result);
    }

//    @Test
    @Order(2)
    void insertUsers() {
        userClient.createUser(new UserRequest("alice@eln.com", "Alice", "Smith", "password", List.of(ROLE_CONTENT_EDITOR, ROLE_TEMPLATE_EDITOR)));
        userClient.createUser(new UserRequest("bob@eln.com", "Bob", "Johnson", "password", List.of(ROLE_ADMINISTRATOR)));
        userClient.createUser(new UserRequest("charlie@eln.com", "Charlie", "Williams", "password", List.of(ROLE_CONTENT_EDITOR)));
    }

//    @Test
    @Order(2)
    void insertTestData(@TempDir Path tempDir) {
        List<DictionaryItemRef> therapeuticAreas = dictionaryClient.getDictionary(Dictionary.THERAPEUTIC_AREA);
        List<DictionaryItemRef> projectCodes = dictionaryClient.getDictionary(Dictionary.PROJECT_CODE);
        int lastUsedNotebookNumber = 0;
        UUID templateID = templateClient.createTemplate(new TemplateRequest("Empty template", List.of(new TemplateComponent.Attachments()))).getId();
        for (int projectNo = 1; projectNo <= random.nextInt(4, 16); projectNo++) {
            System.out.println("project " + projectNo);
            List<String> keywords = IntStream.range(0, random.nextInt(4)).mapToObj(i -> "keyword" + i).toList();
            ProjectDetailsDTO project = projectClient.createProject(new ProjectRequest("Test Project " + projectNo, keywords, "literature", "description"));
            for (int attachmentNo = 1; attachmentNo <= random.nextInt(0, 2); attachmentNo++) {
                System.out.println("\tattachment " + attachmentNo);
                projectClient.createProjectAttachment(project.getId(), "attachment" + attachmentNo + ".txt", tempDir, "content".getBytes());
            }
            for (int notebookNo = 1; notebookNo <= random.nextInt(1, 4); notebookNo++) {
                System.out.println("\tnotebook " + notebookNo);
                NotebookDetailsDTO notebook = notebookClient.createNotebook(project.getId(), new NotebookRequest("%08d".formatted(++lastUsedNotebookNumber), "description"));
                for (int attachmentNo = 1; attachmentNo <= random.nextInt(0, 4); attachmentNo++) {
                    System.out.println("\tattachment " + attachmentNo);
                    notebookClient.createNotebookAttachment(notebook.getId(), "attachment" + attachmentNo + ".txt", tempDir, "content".getBytes());
                }
                for (int experimentNo = 1; experimentNo <= random.nextInt(1, 12); experimentNo++) {
                    System.out.println("\t\texperiment " + experimentNo);
                    ExperimentDetailsDTO experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(templateID
                            , "image"
                            , randomOrNone(therapeuticAreas)
                            , randomOrNone(projectCodes)
                    ));
                    for (int attachmentNo = 1; attachmentNo <= random.nextInt(0, 4); attachmentNo++) {
                        System.out.println("\tattachment " + attachmentNo);
                        experimentClient.createExperimentAttachment(experiment.getId(), "attachment" + attachmentNo + ".txt", tempDir, "content".getBytes());
                    }
                }
            }
        }
    }

    private <T> @Nullable T randomOrNone(List<T> list) {
        int no = random.nextInt(-1, list.size());
        return no == -1 ? null : list.get(no);
    }
}

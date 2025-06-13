package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.eln.client.*;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.util.FeignUtil;
import io.quarkus.test.junit.QuarkusTest;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;


@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnabledIfEnvironmentVariable(named = "INSERT_TEST_DATA", matches = ".+")
class InsertTestDataTest {

    private final Random random = new Random();

    ProjectsClient projectsClient;
    NotebooksClient notebooksClient;
    ExperimentsClient experimentsClient;
    MiscClient miscClient;
    UsersClient usersClient;

    @BeforeEach
    void setup() {
        URI baseURI = URI.create("https://indigo-eln-dev.test.lifescience.opensource.epam.com/");
        AtomicReference<String> testUsername = new AtomicReference<>();
        AtomicReference<String> authorization = new AtomicReference<>(
                "Bearer eyJraWQiOiJUbTFZSmg5UUJjZkQrVnBKVlc1WEQ3UEV5NEh1d2gxeUlvVlZwcmYxb0UwPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiIwNDY4ZTQyOC1hMGYxLTcwN2YtYWE1Yy0zMjgzZGY3NWY2ZjgiLCJpc3MiOiJodHRwczpcL1wvY29nbml0by1pZHAudXMtZWFzdC0xLmFtYXpvbmF3cy5jb21cL3VzLWVhc3QtMV82RGlyZ3RRMXAiLCJ2ZXJzaW9uIjoyLCJjbGllbnRfaWQiOiJhNGtraDAwb2IyM2w3dXBtODhoaDNtbWo1Iiwib3JpZ2luX2p0aSI6IjI1ZDQwNDMzLTcwNDktNGQ1Yy04MTk2LWE2YzZlNjE5ODhlNiIsImV2ZW50X2lkIjoiZTIxN2M5MjgtZWYxMi00MjAyLTlhMTQtN2Q2MjA3NjhlYzc1IiwidG9rZW5fdXNlIjoiYWNjZXNzIiwic2NvcGUiOiJhd3MuY29nbml0by5zaWduaW4udXNlci5hZG1pbiBwaG9uZSBvcGVuaWQgcHJvZmlsZSBlbWFpbCIsImF1dGhfdGltZSI6MTc0NjY0OTQzMiwiZXhwIjoxNzQ2NjUzMDMyLCJpYXQiOjE3NDY2NDk0MzIsImp0aSI6IjllNmUzYjM0LTU4MDMtNDNhOC05ZWYxLTY1MGM3NDc2YTYxNiIsInVzZXJuYW1lIjoiYWxpY2UifQ.W9WQ1fCrrPcP-m9U7bNkltql1Gan6dlYtOYJbsvHIHQejVPNVZlny7Z2TEAOYN-fYBXzNa8D7gWbA0RaB1DspOtddnsIMCl6FydfGgHzmQ1deFZCjhzLIbmwaFLrsJQMD3-LqO6XYnakOR_Aw1_Y7ctg2Q5Wrcj7qKuo53VN-cktSzFrBbokyaKv4zSrnCl1zV41EZMQLOJaaMmLQpXG5pKsh_RJqFoA-VnmJcsxvHXU4WCxacrx3DshdybzMDAxr14PfO5IvhNCBcZfQIxvb97TGW32RI71WhTo_aySPPq4tB_ZIR6sitb0q7045iK1yHFZ-QcmNzgpm6ua3ploDg"
        );
        projectsClient = FeignUtil.buildFeignClient(baseURI, ProjectsClient.class, testUsername, authorization);
        notebooksClient = FeignUtil.buildFeignClient(baseURI, NotebooksClient.class, testUsername, authorization);
        experimentsClient = FeignUtil.buildFeignClient(baseURI, ExperimentsClient.class, testUsername, authorization);
        miscClient = FeignUtil.buildFeignClient(baseURI, MiscClient.class, testUsername, authorization);
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
        usersClient.createUser(new UserRequest("alice", "Alice", "Smith", new ApplicationRole[]{ApplicationRole.CONTENT_EDITOR, ApplicationRole.TEMPLATE_EDITOR}));
        usersClient.getOrCreateUser(new UserRequest("bob", "Bob", "Johnson", new ApplicationRole[]{ApplicationRole.ADMINISTRATOR}));
        usersClient.getOrCreateUser(new UserRequest("charlie", "Charlie", "Williams", new ApplicationRole[]{ApplicationRole.CONTENT_EDITOR}));
    }

//    @Test
    @Order(2)
    void insertTestData(@TempDir Path tempDir) {
        List<DictionaryRef> therapeuticAreas = miscClient.getDictionary(Dictionary.THERAPEUTIC_AREA);
        List<DictionaryRef> projectCodes = miscClient.getDictionary(Dictionary.PROJECT_CODE);
        for (int projectNo = 1; projectNo <= random.nextInt(4, 16); projectNo++) {
            System.out.println("project " + projectNo);
            List<String> keywords = IntStream.range(0, random.nextInt(4)).mapToObj(i -> "keyword" + i).toList();
            ProjectDetailsDTO project = projectsClient.createProject(new ProjectRequest("Test Project " + projectNo, keywords, "literature", "description"));
            for (int attachmentNo = 1; attachmentNo <= random.nextInt(0, 2); attachmentNo++) {
                System.out.println("\tattachment " + attachmentNo);
                projectsClient.createProjectAttachment(project.getId(), "attachment" + attachmentNo + ".txt", tempDir, "content".getBytes());
            }
            for (int notebookNo = 1; notebookNo <= random.nextInt(1, 4); notebookNo++) {
                System.out.println("\tnotebook " + notebookNo);
                NotebookDetailsDTO notebook = notebooksClient.createNotebook(project.getId(), new NotebookRequest("Test Notebook " + notebookNo, "description"));
                for (int attachmentNo = 1; attachmentNo <= random.nextInt(0, 4); attachmentNo++) {
                    System.out.println("\tattachment " + attachmentNo);
                    notebooksClient.createNotebookAttachment(notebook.getId(), "attachment" + attachmentNo + ".txt", tempDir, "content".getBytes());
                }
                for (int experimentNo = 1; experimentNo <= random.nextInt(1, 12); experimentNo++) {
                    System.out.println("\t\texperiment " + experimentNo);
                    ExperimentDetailsDTO experiment = experimentsClient.createExperiment(notebook.getId(), new ExperimentRequest("Test Experiment " + experimentNo
                            , "image"
                            , randomOrNone(therapeuticAreas)
                            , randomOrNone(projectCodes)
                    ));
                    for (int attachmentNo = 1; attachmentNo <= random.nextInt(0, 4); attachmentNo++) {
                        System.out.println("\tattachment " + attachmentNo);
                        experimentsClient.createExperimentAttachment(experiment.getId(), "attachment" + attachmentNo + ".txt", tempDir, "content".getBytes());
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
